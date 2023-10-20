package com.uae.pass.validator;

import com.liferay.petra.string.StringPool;
import com.liferay.portal.configuration.metatype.bnd.util.ConfigurableUtil;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.model.*;
import com.liferay.portal.kernel.service.*;
import com.liferay.portal.kernel.servlet.BaseFilter;
import com.liferay.portal.kernel.util.ContentTypes;
import com.liferay.portal.kernel.util.ParamUtil;
import com.liferay.portal.kernel.util.PortalUtil;
import com.liferay.portal.kernel.util.Validator;
import com.uae.pass.api.UAEPassAPIClient;
import com.uae.pass.api.UAEPassAPIException;
import com.uae.pass.config.UAEPassConfiguration;
import com.uae.pass.constants.UaePassModulePortletKeys;
import com.uae.pass.model.UAEPassTokenResponse;
import com.uae.pass.model.UAEPassUserInfoResponse;
import com.uae.pass.util.UAEPassLiferayUtil;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Modified;
import org.osgi.service.component.annotations.Reference;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.List;
import java.util.Map;

@Component(
        configurationPid = UaePassModulePortletKeys.CONFIGURATION_PID,
        immediate = true,
        property = {
                "before-filter=Auto Login Filter",
                "dispatcher=REQUEST",
                "servlet-context-name=",
                "servlet-filter-name=External UAE Pass Filter",
                "url-pattern=" +UaePassModulePortletKeys.AUTHENTICATE_URL_PATTERN
        },
        service = Filter.class
)

public class UAEPassExternalServletFilter extends BaseFilter {
    public  static UAEPassAPIClient uaePassAPIClient;

    @Activate
    @Modified
    public synchronized void activate(Map<String, String> properties) {
        this.configuration = ConfigurableUtil.createConfigurable(UAEPassConfiguration.class, properties);
    }

    public volatile UAEPassConfiguration configuration;

    @Override
    protected Log getLog() {
        return _log;
    }

    public static final Log _log = LogFactoryUtil.getLog(
            UAEPassExternalServletFilter.class);

    @Override
    protected void processFilter(
            HttpServletRequest httpServletRequest,
            HttpServletResponse httpServletResponse, FilterChain filterChain)
            throws Exception {

        String code = ParamUtil.get(httpServletRequest, "code", "");
        String state = ParamUtil.get(httpServletRequest, "state", "");

        if(_log.isDebugEnabled()) {
            _log.debug("Auto Login Code: " + code);
            _log.debug("Auto Login State: " + state);
        }

        if(Validator.isNotNull(code) && Validator.isNotNull(state)) {

            User user = getRemoteUser(httpServletRequest);
            String redirectURL = "";

            //User is not logged in, dont need to check if validated, but redirect for autologin attempt
            if(Validator.isNull(user)) {
                redirectURL = PortalUtil.getPortalURL(httpServletRequest) + "?code=" + code + "&state=" + state;
                httpServletResponse.sendRedirect(redirectURL);
                return;
            }

            //Check if user needs to verify their UAE Pass session
            boolean verifyUAEPass = false;
            HttpSession session = httpServletRequest.getSession();
            Object verifyUAEPassObj = session.getAttribute(UaePassModulePortletKeys.VERIFY_UAE_PASS);
            if(Validator.isNotNull(verifyUAEPassObj)) {
                verifyUAEPass = (boolean) verifyUAEPassObj;
                session.removeAttribute(UaePassModulePortletKeys.VERIFY_UAE_PASS);
            }

            //Check if user needs to verifyUAEPass
            if(verifyUAEPass) {
                boolean isVerified = true;
                long companyId = PortalUtil.getCompanyId(httpServletRequest);

                String mediaType = ContentTypes.APPLICATION_X_WWW_FORM_URLENCODED;
                String client_secret = configuration.clientSecret();
                String client_id = configuration.clientId();
                String redirect_uri = PortalUtil.getPortalURL(httpServletRequest) + UaePassModulePortletKeys.AUTHENTICATE_URL_PATTERN;
                String grant_type = "authorization_code";
                String baseURL =  configuration.baseURL();

                getUAEPassAPIClient(baseURL);

                try {
                    UAEPassTokenResponse tokenResponse = uaePassAPIClient.getToken(mediaType, grant_type, code, redirect_uri, client_id, client_secret);

                    //Get token from token response, call getUserInfo
                    String token = tokenResponse.getAccessToken();
                    String bearerToken = "Bearer " + token;

                    UAEPassUserInfoResponse userInfo = uaePassAPIClient.getUserInfo(bearerToken);
                    String emirateId = userInfo.getIdn();
                    String email = userInfo.getEmail();
                    String fistName = userInfo.getFirstnameEN();
                    String lastName = userInfo.getLastnameEN();
                    String mobileNumber = userInfo.getMobile();
                    //Fetch the user. Null if not found
                    User UAEPassUser = _userLocalservice.fetchUserByEmailAddress(companyId, email);

                    // check if other user exist is database with email from uae pass
                    if(Validator.isNotNull(UAEPassUser) && UAEPassUser.getUserId()!=user.getUserId()){
                        // other user exists with email address provided by UAE
                        isVerified = false;
                    }else{
                        // If user is signed in update email, first name, last name, mobile number
                        if(!user.getEmailAddress().equalsIgnoreCase(email)){
                            user.setEmailAddress(email);
                            user.setEmailAddressVerified(true);
                        }
                        user.setFirstName(fistName);
                        user.setLastName(lastName);
                        _userLocalservice.updateUser(user);
                        user.getExpandoBridge().setAttribute(UaePassModulePortletKeys.CUSTOM_UAE_PASS_VERIFIED,true,false);
                        user.getExpandoBridge().setAttribute(UaePassModulePortletKeys.CUSTOM_FIELD_EMIRATE_ID, emirateId, false);
                        _log.info("Updating users information "+mobileNumber);
                        updateMobilePhone(user, StringPool.BLANK,mobileNumber,true);
                        isVerified = true;
                    }



                    //Get where the user needs to be redirected back in the portal
                    Object redirectValue = session.getAttribute(UaePassModulePortletKeys.VERIFY_REDIRECT_URL);
                    if(Validator.isNotNull(redirectValue)) {
                        redirectURL = (String) redirectValue;
                        session.removeAttribute(UaePassModulePortletKeys.VERIFY_REDIRECT_URL);
                    }

                    redirectURL = redirectURL.concat("?verified=" + isVerified);


                    httpServletResponse.sendRedirect(redirectURL);
                    return;

                } catch (UAEPassAPIException e) {
                    _log.error("UAEPassAPIException while logging in user with UAE pass", e);
                } catch (IOException e) {
                    _log.error("IOException while logging in user with UAE pass", e);
                }
            }
        }

    }


    public static UAEPassAPIClient getUAEPassAPIClient(String baseURL) {
        if (uaePassAPIClient == null) {
            uaePassAPIClient = new UAEPassAPIClient(baseURL);
        }

        return uaePassAPIClient;
    }

    public static User getRemoteUser(HttpServletRequest httpServletRequest) {
        Object remoteUserIdObj = httpServletRequest.getAttribute("USER_ID");
        long userId = 0;
        if(Validator.isNotNull(remoteUserIdObj)) {
            userId = (long) remoteUserIdObj;
        }

        if(Validator.isNull(userId)) {
            return null;
        }

        User user = null;
        try {
            user = UserLocalServiceUtil.getUser(userId);
        } catch (PortalException e) {
            return null;
        }

        if(user.isDefaultUser()) {
            return null;
        }

        return user;
    }

    public boolean updateMobilePhone(User user, String requestCurrentPhone, String requestNewPhone, boolean skipCheck) throws PortalException {

        if(Validator.isNotNull(requestNewPhone)) {
            List<Phone> phones = user.getPhones();

            //If no phone is set and no requestCurrentPhone value is provided, will be setting it for the first time
            if(Validator.isNull(phones) || phones.isEmpty()) {
                boolean primary = true;
                ServiceContext serviceContext = new ServiceContext();
                long groupId = UAEPassLiferayUtil.getGroupIdFromUser(user);
                long companyId = user.getCompanyId();
                serviceContext.setScopeGroupId(groupId);
                serviceContext.setUserId(user.getUserId());
                serviceContext.setCompanyId(companyId);
                ListType listType = ListTypeLocalServiceUtil.getListType("personal",
                        ListTypeConstants.CONTACT_PHONE);
                PhoneLocalServiceUtil.addPhone(user.getUserId(), Contact.class.getName(), user.getContactId(), requestNewPhone, StringPool.BLANK, listType.getListTypeId(), primary, serviceContext);
            } else {
                //check for primary phone and update it
                for(Phone phone : phones) {
                    if(phone.isPrimary()) {
                        String currentPhone = phone.getNumber();
                        //validate against passed current phone
                        if(skipCheck || currentPhone.equalsIgnoreCase(requestCurrentPhone)) {
                            phone.setNumber(requestNewPhone);
                            PhoneLocalServiceUtil.updatePhone(phone);
                        } else {
                            return true;
                        }
                    }
                }
            }
        } else {
            return true;
        }

        return false;
    }

    @Reference
    public UserLocalService _userLocalservice;
}

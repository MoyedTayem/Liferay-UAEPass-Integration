package com.uae.pass.autologin;

import com.liferay.petra.string.StringPool;
import com.liferay.portal.configuration.metatype.bnd.util.ConfigurableUtil;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.model.Group;
import com.liferay.portal.kernel.model.User;
import com.liferay.portal.kernel.security.auto.login.AutoLogin;
import com.liferay.portal.kernel.security.auto.login.AutoLoginException;
import com.liferay.portal.kernel.service.*;
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
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Modified;
import org.osgi.service.component.annotations.Reference;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.Serializable;
import java.util.Date;
import java.util.Locale;
import java.util.Map;


@Component(configurationPid = UaePassModulePortletKeys.CONFIGURATION_PID,
        immediate = true)
public class UAEPassAutoLogin  implements AutoLogin {
    public static UAEPassAPIClient uaePassAPIClient;

    @Activate
    @Modified
    public synchronized void activate(Map<String, String> properties) {
        this.configuration = ConfigurableUtil.createConfigurable(UAEPassConfiguration.class, properties);
    }

    public volatile UAEPassConfiguration configuration;

    public static final Log _log = LogFactoryUtil.getLog(
            UAEPassAutoLogin.class);

    @Override
    public String[] login(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws AutoLoginException {

        String code = ParamUtil.get(httpServletRequest, "code", "");

        String state = ParamUtil.get(httpServletRequest, "state", "");

        String userIdRequest = ParamUtil.get(httpServletRequest, "userId", "");

        if(_log.isDebugEnabled()) {
            _log.debug("Auto Login Code: " + code);
            _log.debug("Auto Login State: " + code);
        }

        if(Validator.isNotNull(code) && Validator.isNotNull(state)) {

            try {

                String mediaType = ContentTypes.APPLICATION_X_WWW_FORM_URLENCODED;
                String client_secret = configuration.clientSecret();
                String client_id = configuration.clientId();
                String redirect_uri = PortalUtil.getPortalURL(httpServletRequest) + UaePassModulePortletKeys.AUTHENTICATE_URL_PATTERN;
                String grant_type = "authorization_code";
                String baseURL = configuration.baseURL();

                getUAEPassAPIClient(baseURL);

                UAEPassTokenResponse tokenResponse = uaePassAPIClient.getToken(mediaType, grant_type, code, redirect_uri, client_id, client_secret);

                //Get token from token response, call getUserInfo
                String token = tokenResponse.getAccessToken();
                String bearerToken = "Bearer " + token;

                UAEPassUserInfoResponse userInfo = uaePassAPIClient.getUserInfo(bearerToken);
                String emiratesId =userInfo.getIdn();
                String email = userInfo.getEmail();
                String firstName = userInfo.getFirstnameEN();
                String lastName = userInfo.getLastnameEN();

                User user = fetchUser(email, firstName, lastName, httpServletRequest);

                String userId = String.valueOf(user.getUserId());
                String password = user.getPassword();
                String passwordEncrypted = Boolean.FALSE.toString();
                if(user.getPasswordEncrypted()) {
                    passwordEncrypted = Boolean.TRUE.toString();
                }

//                boolean isProfileComplete = (boolean) user.getExpandoBridge().getAttribute("isProfileSetupCompleted", false);
                  String userRedirect = redirect_uri;
//                if(isProfileComplete) {
//                    userRedirect = configuration.profileCompleteRedirect();
//                } else {
//                    userRedirect = configuration.setupProfileRedirect();
//                }


//                Serializable isProfileSetupCompleted = user.getExpandoBridge().getAttribute("isProfileSetupCompleted", false);
//                boolean isProfileCompleted = false;
//                if(Validator.isNotNull(isProfileSetupCompleted))
//                    isProfileCompleted = (boolean) isProfileSetupCompleted;


//                boolean isUaePassValidated = false;
//                if(Validator.isNotNull(emiratesId) && (userInfo.getUserType().equalsIgnoreCase("SOP2") || userInfo.getUserType().equalsIgnoreCase("SOP3"))){
//                    user.getExpandoBridge().setAttribute(UaePassModulePortletKeys.CUSTOM_FIELD_EMIRATE_ID, emiratesId,false);
//                    user.getExpandoBridge().setAttribute(UaePassModulePortletKeys.CUSTOM_FIELD_UAE_RESIDENT, true,false);
//                    user.getExpandoBridge().setAttribute(UaePassModulePortletKeys.CUSTOM_FIELD_IS_UAE_PASS_VALIDATED,true,false);
//                    isUaePassValidated = true;
//                }


               // user.getExpandoBridge().setAttribute(UaePassModulePortletKeys.CUSTOM_FIELD_SIGN_WITH_EMAIL, Boolean.FALSE.toString(),false);

                // default site assignment
                Group group=groupLocalService.fetchGroup(user.getCompanyId(),configuration.defaultSiteAssignment());
                _userLocalservice.addGroupUser(group.getGroupId(),user);

                httpServletRequest.setAttribute("AUTO_LOGIN_REDIRECT_AND_CONTINUE", userRedirect);

                String[] credentials = new String[] {userId, password, passwordEncrypted};

                return credentials;

            } catch (UAEPassAPIException e) {
                _log.error("UAEPassAPIException while logging in user with UAE pass", e);
            } catch (IOException e) {
                _log.error("IOException while logging in user with UAE pass", e);
            } catch (PortalException e) {
                _log.error("PortalException while logging in user with UAE pass", e);
            }
        }else if(Validator.isNotNull(userIdRequest)){
            try {


                User user = _userLocalservice.getUser(Long.parseLong(userIdRequest));
                String userId = String.valueOf(user.getUserId());
                String password = user.getPassword();
                String passwordEncrypted = Boolean.FALSE.toString();
                if (user.getPasswordEncrypted()) {
                    passwordEncrypted = Boolean.TRUE.toString();
                }


                String userRedirect = (String) httpServletRequest.getAttribute("CURRENT_URL");


                httpServletRequest.setAttribute("AUTO_LOGIN_REDIRECT_AND_CONTINUE", userRedirect);

                String[] credentials = new String[]{userId, password, passwordEncrypted};

                return credentials;

            } catch (PortalException e) {
                _log.error("PortalException while logging in user with UAE pass", e);
            }

        }


        return null;
    }

    public static UAEPassAPIClient getUAEPassAPIClient(String baseURL) {
        if (uaePassAPIClient == null) {
            uaePassAPIClient = new UAEPassAPIClient(baseURL);
        }

        return uaePassAPIClient;
    }

    public User fetchUser(String email, String firstName, String lastName,
                           HttpServletRequest httpServletRequest) throws PortalException {

        long companyId = PortalUtil.getCompanyId(httpServletRequest);
        User user = _userLocalservice.fetchUserByEmailAddress(companyId, email);

        if (Validator.isNull(user)) {

            ServiceContext serviceContext = new ServiceContext();
            long groupId = PortalUtil.getScopeGroupId(httpServletRequest);
            serviceContext.setScopeGroupId(groupId);
            serviceContext.setCompanyId(companyId);
            serviceContext.setPathMain("/c");
            serviceContext.setPortalURL(PortalUtil.getPortalURL(httpServletRequest));

            String middleName = StringPool.BLANK;
            String jobTitle = StringPool.BLANK;

            // Add User to liferay table
            int birthdayDay = 01;
            int birthdayMonth = 04;
            int birthdayYear = 1970;
            boolean sendEmail = false;
            long prefixId = 0;
            long suffixId = 0;
            boolean isMale = true;
            boolean autoScreenName = true;
            boolean autoPassword = true;
            String pssw0rd1 = StringPool.BLANK;
            String pssw0rd2 = StringPool.BLANK;;
            String screenName = StringPool.BLANK;
            long[] groupIds = new long[] {groupId};
            long[] organizationIds = new long[] {};
            long[] roleIds = new long[] {};
            long[] userGroupIds = new long[] {};
            int type = 0;

            long creatorUserId = 0;
            user = _userLocalservice.addUser(creatorUserId,
                    companyId, autoPassword, pssw0rd1, pssw0rd2,
                    autoScreenName, screenName, email, Locale.US, firstName, middleName,
                    lastName, prefixId, suffixId, isMale, birthdayMonth, birthdayDay, birthdayYear, jobTitle,type,
                    groupIds, organizationIds, roleIds, userGroupIds, sendEmail, serviceContext);

            Date date = new Date();
            user.setLastLoginDate(date);
            user.setModifiedDate(date);
            user.setNew(false);
            user.setPasswordModified(true);
            user.setModifiedDate(date);
            user.setPasswordReset(false);
            user.setEmailAddressVerified(false);
            user = UserLocalServiceUtil.updateUser(user);

        }

        return user;
    }

    @Reference
    public UserLocalService _userLocalservice;
    @Reference
    public GroupLocalService groupLocalService;
}

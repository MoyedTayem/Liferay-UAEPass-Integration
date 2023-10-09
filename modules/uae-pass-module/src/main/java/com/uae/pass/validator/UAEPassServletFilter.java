package com.uae.pass.validator;

import com.liferay.portal.configuration.metatype.bnd.util.ConfigurableUtil;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.model.User;
import com.liferay.portal.kernel.service.UserLocalServiceUtil;
import com.liferay.portal.kernel.servlet.BaseFilter;
import com.liferay.portal.kernel.util.ParamUtil;
import com.liferay.portal.kernel.util.PortalUtil;
import com.liferay.portal.kernel.util.Validator;
import com.uae.pass.config.UAEPassConfiguration;
import com.uae.pass.constants.UaePassModulePortletKeys;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Modified;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.Map;

@Component(
        configurationPid = UaePassModulePortletKeys.CONFIGURATION_PID,
        immediate = true,
        property = {
                "before-filter=Auto Login Filter",
                "dispatcher=REQUEST",
                "servlet-context-name=",
                "servlet-filter-name=UAE Pass Servlet Filter",
                "url-pattern=" + UaePassModulePortletKeys.VERIFY_URL_PATTERN
        },
        service = Filter.class
)
public class UAEPassServletFilter extends BaseFilter {
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

    @Override
    protected void processFilter(
            HttpServletRequest httpServletRequest,
            HttpServletResponse httpServletResponse, FilterChain filterChain)
            throws Exception {

        User user = getRemoteUser(httpServletRequest);

        if(Validator.isNull(user)) {
            super.processFilter(httpServletRequest, httpServletResponse, filterChain);
            return;
        }

        //Set the redirect URL and UAE Pass verificaiton session values
        String sessionRedirectURL = ParamUtil.getString(httpServletRequest, "redirectURL");
        HttpSession session = httpServletRequest.getSession();
        session.setAttribute(UaePassModulePortletKeys.VERIFY_REDIRECT_URL, sessionRedirectURL);
        session.setAttribute(UaePassModulePortletKeys.VERIFY_UAE_PASS, true);

        //Redirect user to the UAE pass login
        String baseURL = configuration.uaePassLoginURL();
        String state = configuration.state();
        String scope = configuration.scope();
        String acrValues = configuration.acr_values();
        String clientId = configuration.clientId();
        String responseType = configuration.response_type();
        String redirectURL = PortalUtil.getPortalURL(httpServletRequest) + UaePassModulePortletKeys.AUTHENTICATE_URL_PATTERN;
        String UAEPassredirectURL = String.format("%s?response_type=%s&client_id=%s&scope=%s&state=%s&redirect_uri=%s&acr_values=%s", baseURL, responseType, clientId, scope, state, redirectURL, acrValues);

        httpServletResponse.sendRedirect(UAEPassredirectURL);

        httpServletResponse.addHeader(
                "UAE-Pass-Servlet-Filter", httpServletRequest.getRequestURI());
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

    public static final Log _log = LogFactoryUtil.getLog(
            UAEPassServletFilter.class);
}

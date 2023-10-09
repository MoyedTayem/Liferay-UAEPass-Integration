package com.uae.pass.config;
import com.liferay.portal.configuration.metatype.annotations.ExtendedObjectClassDefinition;

import aQute.bnd.annotation.metatype.Meta;
import com.uae.pass.constants.UaePassModulePortletKeys;

@ExtendedObjectClassDefinition(
        scope = ExtendedObjectClassDefinition.Scope.SYSTEM,
        category = "uaepass-integrations"
)
@Meta.OCD(
        id = UaePassModulePortletKeys.CONFIGURATION_PID,
        name = "UAE Pass configuration")
public interface UAEPassConfiguration {
    @Meta.AD(deflt = "https://stg-id.uaepass.ae", description = "The base URL of UAE Pass APIs", required = true)
    String baseURL();

    @Meta.AD(deflt = "https://stg-id.uaepass.ae/idshub/authorize", description = "The URL for logging in the user in UAE Pass", required = true)
    String uaePassLoginURL();

    @Meta.AD(description = "The state parameter for the UAE Pass URL", required = true)
    String state();

    @Meta.AD(description = "The scope parameter for the UAE Pass URL", required = true)
    String scope();

    @Meta.AD(description = "The acr values parameter for the UAE Pass URL", required = true)
    String acr_values();

    @Meta.AD(description = "The response type parameter for the UAE Pass URL", required = true)
    String response_type();

    @Meta.AD(description = "The Client ID of UAE Pass", required = true)
    String clientId();

    @Meta.AD(description = "The Client Secret of UAE Pass", required = true)
    String clientSecret();

    @Meta.AD(description = "URL redirect if profile is not setup", required = true)
    String setupProfileRedirect();

    @Meta.AD(description = "Default URL in Liferay user redirects to after login", required = true)
    String profileCompleteRedirect();

    @Meta.AD(description = "Default site user should be assigned", required = true)
    String defaultSiteAssignment();

}

package com.uae.pass.util;


import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.model.User;
import com.liferay.portal.kernel.service.GroupLocalServiceUtil;

public class UAEPassLiferayUtil {

    public static Log _log = LogFactoryUtil.getLog(UAEPassLiferayUtil.class);
    public static long getGroupIdFromUser(User user) {
        long[] userGroupIds = user.getGroupIds();
        long groupId = 0;
        for(long userGroupId : userGroupIds ) {
            try {
                if(!GroupLocalServiceUtil.getGroup(userGroupId).isUserPersonalSite()) {
                    groupId = userGroupId;
                    break;
                }
            } catch (PortalException e) {
                _log.error("PortalException", e);
            }
        }
        return groupId;
    }
}

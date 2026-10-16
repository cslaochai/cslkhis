package com.his.system.api;

import com.his.api.entity.CurrentUser;
import com.his.api.service.AuthContextApi;
import com.his.system.utils.UserUtils;
import org.springframework.stereotype.Service;

/**
 * 当前登录用户上下文端口实现：把院内 {@code CurrentUser} 收敛成 his-api 契约。
 */
@Service
public class AuthContextApiImpl implements AuthContextApi {

    @Override
    public CurrentUser getCurrentUser() {
        com.his.system.entity.CurrentUser src = UserUtils.getCurrentUser();
        if (src == null) {
            return null;
        }
        CurrentUser target = new CurrentUser();
        target.setUserId(src.getUserId());
        target.setUsername(src.getUsername());
        target.setRealName(src.getRealName());
        target.setPatientId(src.getPatientId());
        target.setUserType(src.getUserType());
        return target;
    }
}

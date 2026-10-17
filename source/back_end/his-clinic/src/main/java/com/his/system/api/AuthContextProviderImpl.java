package com.his.system.api;

import com.his.common.entity.CurrentUser;
import com.his.provider.service.AuthContextProvider;
import com.his.system.utils.UserUtils;
import org.springframework.stereotype.Service;

/**
 * 当前登录用户上下文端口实现。
 */
@Service
public class AuthContextProviderImpl implements AuthContextProvider {

    @Override
    public CurrentUser getCurrentUser() {
        return UserUtils.getCurrentUser();
    }
}

package com.his.system.api;

import com.his.api.service.JwtApi;
import com.his.system.utils.JwtUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * JWT 签发端口实现（委托 his-system 的 {@code JwtUtils}）。
 */
@Service
@RequiredArgsConstructor
public class JwtApiImpl implements JwtApi {

    private final JwtUtils jwtUtils;

    @Override
    public String generateToken(Long userId, String username, String currentRole, Long deptId, String deptName) {
        return jwtUtils.generateToken(userId, username, currentRole, deptId, deptName);
    }
}

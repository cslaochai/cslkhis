package com.his.miniapp.service.impl;

import com.his.common.exception.BusinessException;
import com.his.miniapp.dto.WxLoginDTO;
import com.his.miniapp.mapper.MiniappSysUserMapper;
import com.his.miniapp.service.MiniAuthService;
import com.his.miniapp.service.WxLoginChannelService;
import com.his.miniapp.vo.MiniUserRowVO;
import com.his.miniapp.vo.MiniWxLoginVO;
import com.his.system.utils.JwtUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 患者端认证服务实现：微信登录口子。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MiniAuthServiceImpl implements MiniAuthService {

    private final JwtUtils jwtUtils;

    private final WxLoginChannelService wxLoginChannelService;

    private final MiniappSysUserMapper miniappSysUserMapper;

    @Override
    public MiniWxLoginVO wxLogin(WxLoginDTO dto) {
        String openid = wxLoginChannelService.code2Session(dto.getCode());
        MiniUserRowVO user = miniappSysUserMapper.selectByOpenid(openid);
        MiniWxLoginVO vo = new MiniWxLoginVO();
        vo.setBound(false);
        if (user == null) {
            return vo;
        }
        if (!Integer.valueOf(3).equals(user.getUserType())) {
            throw new BusinessException("该微信已绑定院内员工账号，患者端不支持该方式登录");
        }
        if (!Integer.valueOf(1).equals(user.getStatus())) {
            throw new BusinessException("账号已停用，请联系医院");
        }
        Long userId = user.getId();
        String username = user.getUserName();
        String realName = user.getRealName() == null ? username : user.getRealName();
        Long patientId = user.getPatientId();

        String token = jwtUtils.generateToken(userId, username, "PATIENT", null, null);
        vo.setBound(true);
        vo.setToken(token);
        vo.setUserId(userId);
        vo.setUsername(username);
        vo.setRealName(realName);
        vo.setPatientId(patientId);
        vo.setUserType(3);
        return vo;
    }
}

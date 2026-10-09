package com.his.miniapp.controller;

import com.his.common.base.Result;
import com.his.miniapp.dto.WxLoginDTO;
import com.his.miniapp.service.MiniAuthService;
import com.his.miniapp.service.WxLoginChannelService;
import com.his.miniapp.vo.MiniWxLoginVO;
import com.his.patient.service.PatientGuardianService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 患者端认证（微信登录口子）。
 */
@Tag(name = "患者端-认证")
@RestController
@RequestMapping("/miniapp/auth")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('PATIENT')")
public class MiniAuthController {

    private final MiniAuthService miniAuthService;

    private final WxLoginChannelService wxLoginChannelService;

    private final PatientGuardianService patientGuardianService;

    @Operation(summary = "微信一键登录（患者端口子，白名单）")
    @PreAuthorize("permitAll()")
    @PostMapping("/wxLogin")
    public Result<MiniWxLoginVO> wxLogin(@RequestBody @Valid WxLoginDTO dto) {
        return Result.success(miniAuthService.wxLogin(dto));
    }

    @Operation(summary = "绑定当前账号微信openid")
    @PostMapping("/bindOpenid")
    public Result<Void> bindOpenid(@RequestBody @Valid WxLoginDTO dto) {
        String openid = wxLoginChannelService.code2Session(dto.getCode());
        patientGuardianService.bindOpenid(openid);
        return Result.success("绑定成功", null);
    }
}

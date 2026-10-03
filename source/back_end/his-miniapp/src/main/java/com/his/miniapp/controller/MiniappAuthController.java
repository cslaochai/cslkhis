package com.his.miniapp.controller;

import com.his.common.base.Result;
import com.his.miniapp.dto.WxLoginDTO;
import com.his.miniapp.vo.WxLoginVO;
import com.his.miniapp.service.MiniappPayService;
import com.his.patient.service.PatientGuardianService;
import com.his.security.UserUtils;
import com.his.common.exception.BusinessException;
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
public class MiniappAuthController {

    private final MiniappPayService miniappPayService;
    private final com.his.miniapp.service.WxLoginChannelService wxLoginChannelService;
    private final PatientGuardianService patientGuardianService;

    /**
     * wx.login code 换 openid 登录。已绑定患者账号 → 直接发 token；
     * 未绑定 → bound=false，前端引导账密/短信注册登录后调 bindOpenid。
     */
    @Operation(summary = "微信一键登录（患者端口子，白名单）")
    @PreAuthorize("permitAll()")
    @PostMapping("/wxLogin")
    public Result<WxLoginVO> wxLogin(@RequestBody @Valid WxLoginDTO dto) {
        return Result.success(miniappPayService.wxLogin(dto));
    }

    /**
     * 登录态下绑定微信 openid（wx.login code → 换 openid → 绑定当前患者账号，
     * 订阅消息发送依赖此绑定）。唯一性冲突直接拒绝，不做抢占。
     */
    @Operation(summary = "绑定当前账号微信openid")
    @PostMapping("/bindOpenid")
    public Result<Void> bindOpenid(@RequestBody @Valid WxLoginDTO dto) {
        String openid = wxLoginChannelService.code2Session(dto.getCode());
        patientGuardianService.bindOpenid(openid);
        return Result.success("绑定成功", null);
    }
}

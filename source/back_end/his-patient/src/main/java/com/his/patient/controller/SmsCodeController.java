package com.his.patient.controller;

import com.his.common.base.Result;
import com.his.patient.dto.SmsCodeSendDTO;
import com.his.patient.vo.SmsSendVO;
import com.his.system.service.SmsCodeService;
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
 * 小程序端短信验证码控制器。
 *
 * <p>匿名可访问（在 {@code SecurityConfig} 白名单内），因此场景码固定为 REGISTER，
 * 频次由 {@link SmsCodeService} 的 Redis 限流兜住。
 */
@Tag(name = "短信验证码")
@RestController
@RequestMapping("/patient/sms")
@RequiredArgsConstructor
@PreAuthorize("permitAll()")
public class SmsCodeController {

    private static final String SCENE_REGISTER = "register";

    private final SmsCodeService smsCodeService;

    @Operation(summary = "发送注册验证码")
    @PostMapping("/sendCode")
    public Result<SmsSendVO> sendCode(@RequestBody @Valid SmsCodeSendDTO dto) {
        SmsCodeService.SendResult send = smsCodeService.send(dto.getPhone(), SCENE_REGISTER);
        if (!send.success()) {
            return Result.error(send.message());
        }
        SmsSendVO vo = new SmsSendVO();
        vo.setMockCode(send.code());
        return Result.success("验证码已发送", vo);
    }
}

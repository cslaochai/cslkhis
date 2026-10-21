package com.his.patient.controller;

import com.his.common.base.Result;
import com.his.patient.dto.SmsCodeSendDTO;
import com.his.patient.vo.SmsSendVO;
import com.his.spi.service.SmsCodeService;
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
 * 小程序端短信验证码控制器：注册场景验证码下发的对外接口入口。
 */
@Tag(name = "短信验证码")
@RestController
@RequestMapping("/patient/sms")
@RequiredArgsConstructor
@PreAuthorize("permitAll()")
public class SmsCodeController {

    private static final String SCENE_REGISTER = "register";

    private final SmsCodeService smsCodeService;

    /**
     * 向指定手机号下发注册场景的短信验证码，未登录即可调用。
     * 用于患者端小程序注册页的「获取验证码」；发送未成功时按业务错误返回。
     *
     * @return 发送结果，联调模式下回带验证码便于免真机验证，正式通道下恒为空
     */
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

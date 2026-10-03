package com.his.patient.controller;

import com.his.common.base.Result;
import com.his.patient.dto.GuardianUpsertDTO;
import com.his.patient.dto.GuardianBindDTO;
import com.his.patient.dto.GuardianOpenidDTO;
import com.his.patient.dto.GuardianOpDTO;
import com.his.patient.dto.GuardianSendAddCodeDTO;
import com.his.patient.dto.GuardianSendBindCodeDTO;
import com.his.patient.service.PatientGuardianService;
import com.his.patient.vo.GuardianPatientVO;
import com.his.patient.vo.SmsSendVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;

/**
 * 就诊人绑定控制器（患者端小程序）。
 *
 * <p>身份收口在 service（requirePatientUser）：仅 user_type=3 的患者账号可操作，
 * 且所有写动作只作用于**当前登录账号自己**的绑定 —— 入参里不存在 userId，
 * 前端无法替别人绑人。
 */
@Tag(name = "就诊人绑定（小程序）")
@RestController
@RequestMapping("/patient/guardian")
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
public class PatientGuardianController {

    private final PatientGuardianService guardianService;

    @Operation(summary = "我的就诊人列表")
    @GetMapping("/myPatients")
    public Result<List<GuardianPatientVO>> myPatients() {
        return Result.success(guardianService.myPatients());
    }

    @PreAuthorize("hasAuthority('patient:edit')")
    @Operation(summary = "绑定场景发送短信验证码（发往建档预留手机号）")
    @PostMapping("/sendBindCode")
    public Result<SmsSendVO> sendBindCode(@Valid @RequestBody GuardianSendBindCodeDTO dto) {
        return Result.success("验证码已发送", guardianService.sendBindCode(dto));
    }

    @PreAuthorize("hasAuthority('patient:edit')")
    @Operation(summary = "绑定已有就诊人（姓名+身份证+建档预留手机号短信码校验）")
    @PostMapping("/bindPatient")
    public Result<GuardianPatientVO> bindPatient(@Valid @RequestBody GuardianBindDTO dto) {
        return Result.success("绑定成功", guardianService.bindPatient(dto));
    }

    @PreAuthorize("hasAuthority('patient:add')")
    @Operation(summary = "新增建档场景发送短信验证码")
    @PostMapping("/sendAddCode")
    public Result<SmsSendVO> sendAddCode(@Valid @RequestBody GuardianSendAddCodeDTO dto) {
        return Result.success("验证码已发送", guardianService.sendAddCode(dto));
    }

    @PreAuthorize("hasAuthority('patient:add')")
    @Operation(summary = "新增就诊人（建档并自动绑定，需短信验证）")
    @PostMapping("/addPatient")
    public Result<GuardianPatientVO> addPatient(@Valid @RequestBody GuardianUpsertDTO dto) {
        return Result.success("新增成功", guardianService.addPatient(dto));
    }

    @PreAuthorize("hasAuthority('patient:edit')")
    @Operation(summary = "解绑就诊人")
    @PostMapping("/unbindPatient")
    public Result<Void> unbindPatient(@Valid @RequestBody GuardianOpDTO dto) {
        guardianService.unbindPatient(dto.getPatientId());
        return Result.success();
    }

    @PreAuthorize("hasAuthority('patient:edit')")
    @Operation(summary = "设为默认就诊人")
    @PostMapping("/setDefault")
    public Result<Void> setDefault(@Valid @RequestBody GuardianOpDTO dto) {
        guardianService.setDefault(dto.getPatientId());
        return Result.success();
    }

    @PreAuthorize("hasAuthority('patient:edit')")
    @Operation(summary = "绑定当前账号微信openid（订阅消息发送用）")
    @PostMapping("/bindOpenid")
    public Result<Void> bindOpenid(@Valid @RequestBody GuardianOpenidDTO dto) {
        guardianService.bindOpenid(dto.getOpenid());
        return Result.success();
    }

    @PreAuthorize("hasAuthority('patient:edit')")
    @Operation(summary = "订阅消息通道自测（给当前账号发一条测试消息）")
    @PostMapping("/testNotify")
    public Result<String> testNotify() {
        return Result.success(guardianService.testNotify());
    }
}

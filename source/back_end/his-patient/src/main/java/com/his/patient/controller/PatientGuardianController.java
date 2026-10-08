package com.his.patient.controller;

import com.his.common.base.Result;
import com.his.patient.dto.*;
import com.his.patient.service.PatientGuardianService;
import com.his.patient.vo.GuardianPatientVO;
import com.his.patient.vo.SmsSendVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 就诊人绑定控制器（患者端小程序）。
 */
@Tag(name = "就诊人绑定（小程序）")
@RestController
@RequestMapping("/patient/guardian")
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
public class PatientGuardianController {

    private final PatientGuardianService patientGuardianService;

    @Operation(summary = "我的就诊人列表")
    @GetMapping("/myPatients")
    public Result<List<GuardianPatientVO>> myPatients() {
        return Result.success(patientGuardianService.myPatients());
    }

    @PreAuthorize("hasAuthority('patient:edit')")
    @Operation(summary = "绑定场景发送短信验证码（发往建档预留手机号）")
    @PostMapping("/sendBindCode")
    public Result<SmsSendVO> sendBindCode(@Valid @RequestBody GuardianSendBindCodeDTO dto) {
        return Result.success("验证码已发送", patientGuardianService.sendBindCode(dto));
    }

    @PreAuthorize("hasAuthority('patient:edit')")
    @Operation(summary = "绑定已有就诊人（姓名+身份证+建档预留手机号短信码校验）")
    @PostMapping("/bindPatient")
    public Result<GuardianPatientVO> bindPatient(@Valid @RequestBody GuardianBindDTO dto) {
        return Result.success("绑定成功", patientGuardianService.bindPatient(dto));
    }

    @PreAuthorize("hasAuthority('patient:add')")
    @Operation(summary = "新增建档场景发送短信验证码")
    @PostMapping("/sendAddCode")
    public Result<SmsSendVO> sendAddCode(@Valid @RequestBody GuardianSendAddCodeDTO dto) {
        return Result.success("验证码已发送", patientGuardianService.sendAddCode(dto));
    }

    @PreAuthorize("hasAuthority('patient:add')")
    @Operation(summary = "新增就诊人（建档并自动绑定，需短信验证）")
    @PostMapping("/addPatient")
    public Result<GuardianPatientVO> addPatient(@Valid @RequestBody GuardianUpsertDTO dto) {
        return Result.success("新增成功", patientGuardianService.addPatient(dto));
    }

    @PreAuthorize("hasAuthority('patient:edit')")
    @Operation(summary = "解绑就诊人")
    @PostMapping("/unbindPatient")
    public Result<Void> unbindPatient(@Valid @RequestBody GuardianOpDTO dto) {
        patientGuardianService.unbindPatient(dto.getPatientId());
        return Result.success();
    }

    @PreAuthorize("hasAuthority('patient:edit')")
    @Operation(summary = "设为默认就诊人")
    @PostMapping("/setDefault")
    public Result<Void> setDefault(@Valid @RequestBody GuardianOpDTO dto) {
        patientGuardianService.setDefault(dto.getPatientId());
        return Result.success();
    }

    @PreAuthorize("hasAuthority('patient:edit')")
    @Operation(summary = "绑定当前账号微信openid（订阅消息发送用）")
    @PostMapping("/bindOpenid")
    public Result<Void> bindOpenid(@Valid @RequestBody GuardianOpenidDTO dto) {
        patientGuardianService.bindOpenid(dto.getOpenid());
        return Result.success();
    }

    @PreAuthorize("hasAuthority('patient:edit')")
    @Operation(summary = "订阅消息通道自测（给当前账号发一条测试消息）")
    @PostMapping("/testNotify")
    public Result<String> testNotify() {
        return Result.success(patientGuardianService.testNotify());
    }
}

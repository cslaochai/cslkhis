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

    /**
     * 当前账号已绑定的就诊人清单。
     * 用于小程序「就诊人管理」页展示，启动时同步本地就诊人也以它为准。
     */
    @Operation(summary = "我的就诊人列表")
    @GetMapping("/myPatients")
    public Result<List<GuardianPatientVO>> myPatients() {
        return Result.success(patientGuardianService.getMyPatients());
    }

    /**
     * 绑定场景发送短信验证码，只发往该档案建档时预留的手机号。
     * 用于小程序「就诊人管理」页绑定已建档就诊人弹层的发码按钮。
     */
    @PreAuthorize("hasAuthority('patient:edit')")
    @Operation(summary = "绑定场景发送短信验证码（发往建档预留手机号）")
    @PostMapping("/sendBindCode")
    public Result<SmsSendVO> sendBindCode(@Valid @RequestBody GuardianSendBindCodeDTO dto) {
        return Result.success("验证码已发送", patientGuardianService.sendBindCode(dto));
    }

    /**
     * 把院内已有档案绑定到当前账号：姓名 + 身份证 + 预留手机号短信码三重校验通过才建关联。
     * 用于小程序「就诊人管理」页绑定已建档就诊人弹层的提交。
     */
    @PreAuthorize("hasAuthority('patient:edit')")
    @Operation(summary = "绑定已有就诊人（姓名+身份证+建档预留手机号短信码校验）")
    @PostMapping("/bindPatient")
    public Result<GuardianPatientVO> bindPatient(@Valid @RequestBody GuardianBindDTO dto) {
        return Result.success("绑定成功", patientGuardianService.bindPatient(dto));
    }

    /**
     * 新增建档场景发送短信验证码，发往待建档的新手机号。
     * 用于小程序「就诊人管理」页添加新就诊人弹层的发码按钮。
     */
    @PreAuthorize("hasAuthority('patient:add')")
    @Operation(summary = "新增建档场景发送短信验证码")
    @PostMapping("/sendAddCode")
    public Result<SmsSendVO> sendAddCode(@Valid @RequestBody GuardianSendAddCodeDTO dto) {
        return Result.success("验证码已发送", patientGuardianService.sendAddCode(dto));
    }

    /**
     * 新建患者档案并自动绑定当前账号，短信码校验通过才落档。
     * 用于小程序「就诊人管理」页添加新就诊人弹层的提交。
     */
    @PreAuthorize("hasAuthority('patient:add')")
    @Operation(summary = "新增就诊人（建档并自动绑定，需短信验证）")
    @PostMapping("/addPatient")
    public Result<GuardianPatientVO> addPatient(@Valid @RequestBody GuardianUpsertDTO dto) {
        return Result.success("新增成功", patientGuardianService.addPatient(dto));
    }

    /**
     * 解除当前账号与某名就诊人的绑定关系，患者档案本身保留。
     * 用于小程序「就诊人管理」页行内的解绑操作。
     */
    @PreAuthorize("hasAuthority('patient:edit')")
    @Operation(summary = "解绑就诊人")
    @PostMapping("/unbindPatient")
    public Result<Void> unbindPatient(@Valid @RequestBody GuardianOpDTO dto) {
        patientGuardianService.unbindPatient(dto.getPatientId());
        return Result.success();
    }

    /**
     * 把某名已绑定就诊人设为当前账号的默认就诊人。
     * 用于小程序「就诊人管理」页行内的设为默认，后续挂号等业务只对当前选中的就诊人生效。
     */
    @PreAuthorize("hasAuthority('patient:edit')")
    @Operation(summary = "设为默认就诊人")
    @PostMapping("/setDefault")
    public Result<Void> setDefault(@Valid @RequestBody GuardianOpDTO dto) {
        patientGuardianService.setDefault(dto.getPatientId());
        return Result.success();
    }

    /**
     * 把微信 openid 绑到当前账号，作为订阅消息推送的收件人定位依据。
     * 用于小程序「我的」页打开时的静默绑定。
     */
    @PreAuthorize("hasAuthority('patient:edit')")
    @Operation(summary = "绑定当前账号微信openid（订阅消息发送用）")
    @PostMapping("/bindOpenid")
    public Result<Void> bindOpenid(@Valid @RequestBody GuardianOpenidDTO dto) {
        patientGuardianService.bindOpenid(dto.getOpenid());
        return Result.success();
    }
}

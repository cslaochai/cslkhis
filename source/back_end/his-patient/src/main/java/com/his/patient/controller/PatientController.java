package com.his.patient.controller;

import com.his.common.base.PageResult;
import com.his.common.base.Result;
import com.his.patient.dto.PatientQueryPageDTO;
import com.his.patient.dto.PatientRegisterDTO;
import com.his.patient.dto.PatientUpsertDTO;
import com.his.patient.service.BizPatientService;
import com.his.patient.vo.PatientDetailVO;
import com.his.patient.vo.PatientRegisterVO;
import com.his.patient.vo.PatientVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * 患者主档控制器
 */
@Tag(name = "患者管理")
@RestController
@PreAuthorize("hasAnyAuthority('opd:appointments:list', 'opd:doctorWorkstation:list', 'patient:list', 'opd:triage:list', 'opd:emergency:list', 'patient:empi:list', 'patient:profile:list', 'ipd:inpatient:list')")
@RequestMapping("/patient")
@RequiredArgsConstructor
public class PatientController {

    private final BizPatientService bizPatientService;

    @Operation(summary = "分页查询患者列表")
    @PostMapping("/listPage")
    public Result<PageResult<PatientVO>> listPage(@Valid @RequestBody PatientQueryPageDTO queryDTO) {
        return Result.success(bizPatientService.queryPatientPage(queryDTO));
    }

    @Operation(summary = "根据ID查询患者")
    @GetMapping("/getById")
    public Result<PatientVO> getById(@RequestParam Long patientId) {
        return Result.success(bizPatientService.getPatientVOById(patientId));
    }

    @Operation(summary = "获取患者完整信息")
    @GetMapping("/getDetailById")
    public Result<PatientDetailVO> getDetailById(@RequestParam Long patientId) {
        return Result.success(bizPatientService.getPatientDetail(patientId));
    }

    @Operation(summary = "根据患者号查询")
    @GetMapping("/getByNo")
    public Result<PatientVO> getByPatientNo(@RequestParam String patientNo) {
        return Result.success(bizPatientService.getPatientVOByNo(patientNo));
    }

    @Operation(summary = "新增或修改患者")
    @PostMapping("/patientUpsert")
    public Result<PatientVO> patientUpsert(@Valid @RequestBody PatientUpsertDTO patientUpsertDTO) {
        return Result.success(bizPatientService.upsertPatient(patientUpsertDTO));
    }

    @Operation(summary = "删除患者")
    @DeleteMapping("/deleteById")
    public Result<Void> remove(@RequestParam Long patientId) {
        bizPatientService.removePatient(patientId);
        return Result.success();
    }

    @Operation(summary = "患者自助注册（小程序端建档并开通账号）")
    @PreAuthorize("permitAll()")
    @PostMapping("/register")
    public Result<PatientRegisterVO> register(@Valid @RequestBody PatientRegisterDTO dto) {
        return Result.success("注册成功", bizPatientService.register(dto));
    }
}

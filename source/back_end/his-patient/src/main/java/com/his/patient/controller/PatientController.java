package com.his.patient.controller;

import com.his.common.base.PageResult;
import com.his.common.base.Result;
import com.his.patient.controller.PatientAllergyController;
import com.his.patient.controller.PatientContactController;
import com.his.patient.controller.PatientFamilyHistoryController;
import com.his.patient.controller.PatientPastDiseaseController;
import com.his.patient.controller.PatientSurgeryHistoryController;
import com.his.patient.dto.PatientQueryPageDTO;
import com.his.patient.dto.PatientRegisterDTO;
import com.his.patient.dto.PatientUpsertDTO;
import com.his.patient.service.PatientService;
import com.his.patient.vo.PatientDetailVO;
import com.his.patient.vo.PatientRegisterVO;
import com.his.patient.vo.PatientVO;
import com.his.patient.controller.PatientTagRelationController;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * 患者主档控制器
 *
 * <p>仅负责患者基本信息（建档、查询、修改、删除）与患者完整信息聚合。
 * 患者标签关联、过敏史、既往疾病史、手术外伤史、家族史、联系人已按业务域拆分为独立控制器：
 * {@link PatientTagRelationController}、{@link PatientAllergyController}、
 * {@link PatientPastDiseaseController}、{@link PatientSurgeryHistoryController}、
 * {@link PatientFamilyHistoryController}、{@link PatientContactController}。
 */
@Tag(name = "患者管理")
@RestController
@PreAuthorize("hasAnyAuthority('opd:appointments:list', 'opd:doctorWorkstation:list', 'patient:list', 'opd:triage:list', 'opd:emergency:list', 'patient:empi:list', 'patient:profile:list', 'ipd:inpatient:list')")
@RequestMapping("/patient")
@RequiredArgsConstructor
public class PatientController {

    private final PatientService patientService;

    @Operation(summary = "分页查询患者列表")
    @PostMapping("/listPage")
    public Result<PageResult<PatientVO>> listPage(@RequestBody PatientQueryPageDTO queryDTO) {
        return Result.success(patientService.queryPatientPage(queryDTO));
    }

    @Operation(summary = "根据ID查询患者")
    @GetMapping("/getById")
    public Result<PatientVO> getById(@RequestParam Long patientId) {
        return Result.success(patientService.getPatientVOById(patientId));
    }

    @Operation(summary = "获取患者完整信息")
    @GetMapping("/getDetailById")
    public Result<PatientDetailVO> getDetailById(@RequestParam Long patientId) {
        return Result.success(patientService.getPatientDetail(patientId));
    }

    @Operation(summary = "根据患者号查询")
    @GetMapping("/getByNo")
    public Result<PatientVO> getByPatientNo(@RequestParam String patientNo) {
        return Result.success(patientService.getPatientVOByNo(patientNo));
    }

    @Operation(summary = "新增或修改患者")
    @PostMapping("/patientUpsert")
    public Result<PatientVO> patientUpsert(@Valid @RequestBody PatientUpsertDTO patientUpsertDTO) {
        return Result.success(patientService.upsertPatient(patientUpsertDTO));
    }

    @Operation(summary = "删除患者")
    @DeleteMapping("/deleteById")
    public Result<Void> remove(@RequestParam Long patientId) {
        patientService.removePatient(patientId);
        return Result.success();
    }

    @Operation(summary = "患者自助注册（小程序端建档并开通账号）")
    @PreAuthorize("permitAll()")
    @PostMapping("/register")
    public Result<PatientRegisterVO> register(@RequestBody PatientRegisterDTO dto) {
        return Result.success("注册成功", patientService.register(dto));
    }
}

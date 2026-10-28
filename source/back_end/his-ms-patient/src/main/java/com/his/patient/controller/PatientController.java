package com.his.patient.controller;

import com.his.common.base.PageResult;
import com.his.common.base.Result;
import com.his.patient.dto.PatientPageQueryDTO;
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
 * 患者主档控制器：建档、检索与身份核对的接口入口，主档是就诊身份的事实来源。
 */
@Tag(name = "患者管理")
@RestController
@PreAuthorize("hasAnyAuthority('opd:appointments:list', 'opd:doctorWorkstation:list', 'patient:list', 'opd:triage:list', 'opd:emergency:list', 'opd:emergencyDoctor:list', 'patient:empi:list', 'patient:profile:list', 'ipd:inpatient:list')")
@RequestMapping("/patient")
@RequiredArgsConstructor
public class PatientController {

    private final BizPatientService bizPatientService;

    /**
     * 按姓名、患者号、手机号或身份证号分页检索患者主档，并带出标签与今日就诊信息。
     * 用于患者管理页的查询卡，以及各业务页选患者组件的候选列表。
     */
    @Operation(summary = "分页查询患者列表")
    @PostMapping("/listPage")
    public Result<PageResult<PatientVO>> listPage(@Valid @RequestBody PatientPageQueryDTO queryDTO) {
        return Result.success(bizPatientService.queryPatientPage(queryDTO));
    }

    /**
     * 按患者 id 取主档单条，证件号与联系方式为明文。
     * 用于患者修改弹框与各业务页建档表单的回显，禁止当展示接口用。
     */
    @Operation(summary = "根据ID查询患者")
    @GetMapping("/getById")
    public Result<PatientVO> getById(@RequestParam Long patientId) {
        return Result.success(bizPatientService.getPatientVOById(patientId));
    }

    /**
     * 按患者 id 取主档连同六组健康档案（过敏史、既往疾病史、手术外伤史、家族史、用药史、联系人）与标签。
     * 用于患者详情弹框与住院医嘱工作区的患者概览；临床各组按岗位可见性裁剪。
     */
    @Operation(summary = "获取患者完整信息")
    @GetMapping("/getDetailById")
    public Result<PatientDetailVO> getDetailById(@RequestParam Long patientId) {
        return Result.success(bizPatientService.getPatientDetail(patientId));
    }

    /**
     * 按患者号取主档单条。
     * 供只持有患者号（扫码或手工录入窗口的就诊卡号）时核对患者身份的取数场景。
     */
    @Operation(summary = "根据患者号查询")
    @GetMapping("/getByNo")
    public Result<PatientVO> getByPatientNo(@RequestParam String patientNo) {
        return Result.success(bizPatientService.getPatientVOByNo(patientNo));
    }

    /**
     * 建档或修改患者主档，返回落库后的主档。
     * 用于患者管理页的新增/修改弹框保存，以及预约登记页的现场建档。
     */
    @Operation(summary = "新增或修改患者")
    @PostMapping("/patientUpsert")
    public Result<PatientVO> patientUpsert(@Valid @RequestBody PatientUpsertDTO patientUpsertDTO) {
        return Result.success(bizPatientService.upsertPatient(patientUpsertDTO));
    }

    /**
     * 按患者 id 删除患者主档。
     * 用于患者管理页行操作「删除」的二次确认之后。
     */
    @Operation(summary = "删除患者")
    @DeleteMapping("/deleteById")
    public Result<Void> remove(@RequestParam Long patientId) {
        bizPatientService.removePatient(patientId);
        return Result.success();
    }

    /**
     * 患者自助注册：建档并同步开通小程序登录账号。
     * 用于患者小程序注册页提交，登录前即可访问。
     */
    @Operation(summary = "患者自助注册（小程序端建档并开通账号）")
    @PreAuthorize("permitAll()")
    @PostMapping("/register")
    public Result<PatientRegisterVO> register(@Valid @RequestBody PatientRegisterDTO dto) {
        return Result.success("注册成功", bizPatientService.register(dto));
    }
}

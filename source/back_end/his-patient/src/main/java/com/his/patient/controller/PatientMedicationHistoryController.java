package com.his.patient.controller;

import com.his.common.base.Result;
import com.his.patient.dto.PatientHistoryQueryDTO;
import com.his.patient.dto.PatientMedicationHistoryUpsertDTO;
import com.his.patient.service.PatientHealthProfileService;
import com.his.patient.vo.PatientMedicationHistoryVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 患者既往用药史控制器
 *
 * <p><b>这个控制器是补出来的。</b>「用药史」是健康档案六组之一，
 * 但此前只有 CDR 时间轴的裸 SQL（{@code CdrMapper.PROFILE_SQL} 的 'medication' 分支）
 * 在既往用药史上做过查询 —— 没有实体、没有 Mapper、
 * 没有 Controller。也就是说这一组**能看不能维护**：页面上永远显示「暂无」，
 * 医生填了用药史也没地方落库。六组里唯独它没有入口，闭环无从谈起。
 */
@Tag(name = "患者既往用药史")
@RestController
@RequestMapping("/patient/medication")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('patient:profile:list')")
public class PatientMedicationHistoryController {

    private final PatientHealthProfileService patientHealthProfileService;

    @Operation(summary = "查询患者的既往用药史列表")
    @PostMapping("/list")
    public Result<List<PatientMedicationHistoryVO>> list(@Valid @RequestBody PatientHistoryQueryDTO queryDTO) {
        return Result.success(patientHealthProfileService.getProfile(queryDTO.getPatientId()).getMedications());
    }

    @PreAuthorize("hasAuthority('patient:profile:add')")
    @Operation(summary = "新增或修改既往用药史")
    @PostMapping("/medicationUpsert")
    public Result<PatientMedicationHistoryVO> medicationUpsert(@RequestBody @Valid PatientMedicationHistoryUpsertDTO dto) {
        return Result.success(patientHealthProfileService.saveMedication(dto));
    }

    @PreAuthorize("hasAuthority('patient:profile:delete')")
    @Operation(summary = "根据ID删除既往用药史")
    @DeleteMapping("/deleteById")
    public Result<Void> deleteById(@RequestParam Long id) {
        patientHealthProfileService.deleteMedication(id);
        return Result.success();
    }
}

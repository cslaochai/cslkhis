package com.his.patient.controller;

import com.his.common.base.Result;
import com.his.patient.dto.PatientHistoryQueryDTO;
import com.his.patient.dto.PatientSurgeryHistoryUpsertDTO;
import com.his.patient.service.PatientHealthProfileService;
import com.his.patient.vo.PatientSurgeryHistoryVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;

/**
 * 患者手术外伤史控制器
 *
 * <p>这一组没有对应的主档文本字段（患者基本信息里没有「手术史」列），
 * 所以写完之后不需要回算投影 —— 它是六组里唯一「明细即全部」的一组。
 */
@Tag(name = "患者手术外伤史")
@RestController
@RequestMapping("/patient/surgeryHistory")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('patient:profile:list')")
public class PatientSurgeryHistoryController {

    private final PatientHealthProfileService healthProfileService;

    @Operation(summary = "查询患者的手术外伤史列表")
    @PostMapping("/list")
    public Result<List<PatientSurgeryHistoryVO>> list(@Valid @RequestBody PatientHistoryQueryDTO queryDTO) {
        return Result.success(healthProfileService.getProfile(queryDTO.getPatientId()).getSurgeryHistories());
    }

    @PreAuthorize("hasAuthority('patient:profile:add')")
    @Operation(summary = "新增或修改手术外伤史")
    @PostMapping("/surgeryHistoryUpsert")
    public Result<PatientSurgeryHistoryVO> surgeryHistoryUpsert(@RequestBody @Valid PatientSurgeryHistoryUpsertDTO dto) {
        return Result.success(healthProfileService.saveSurgeryHistory(dto));
    }

    @PreAuthorize("hasAuthority('patient:profile:delete')")
    @Operation(summary = "根据ID删除手术外伤史")
    @DeleteMapping("/deleteById")
    public Result<Void> deleteById(@RequestParam Long id) {
        healthProfileService.deleteSurgeryHistory(id);
        return Result.success();
    }
}

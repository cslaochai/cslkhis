package com.his.patient.controller;

import com.his.common.base.Result;
import com.his.patient.dto.PatientAllergyUpsertDTO;
import com.his.patient.dto.PatientHistoryQueryDTO;
import com.his.patient.service.PatientHealthProfileService;
import com.his.patient.vo.PatientAllergyVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 患者过敏史控制器
 *
 * <p>写操作全部走 {@link PatientHealthProfileService}：校验、操作人、主档文本投影回算
 * 都在服务层收口。控制器此前直接调 Mapper —— 于是「过敏反应表现」这一列（NOT NULL）
 * 传空值会直接 500，录入人只看到一句 SQL 报错。
 */
@Tag(name = "患者过敏史")
@RestController
@RequestMapping("/patient/allergy")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('patient:profile:list')")
public class PatientAllergyController {

    private final PatientHealthProfileService patientHealthProfileService;

    @Operation(summary = "查询患者的过敏史列表")
    @PostMapping("/list")
    public Result<List<PatientAllergyVO>> list(@Valid @RequestBody PatientHistoryQueryDTO queryDTO) {
        return Result.success(patientHealthProfileService.getProfile(queryDTO.getPatientId()).getAllergies());
    }

    @PreAuthorize("hasAuthority('patient:profile:add')")
    @Operation(summary = "新增或修改过敏史")
    @PostMapping("/allergyUpsert")
    public Result<PatientAllergyVO> allergyUpsert(@RequestBody @Valid PatientAllergyUpsertDTO allergyUpsertDTO) {
        return Result.success(patientHealthProfileService.saveAllergy(allergyUpsertDTO));
    }

    @PreAuthorize("hasAuthority('patient:profile:delete')")
    @Operation(summary = "根据ID删除过敏史")
    @DeleteMapping("/deleteById")
    public Result<Void> deleteById(@RequestParam Long id) {
        patientHealthProfileService.deleteAllergy(id);
        return Result.success();
    }
}

package com.his.patient.controller;

import com.his.common.base.Result;
import com.his.patient.dto.PatientHistoryQueryDTO;
import com.his.patient.dto.PatientPastDiseaseUpsertDTO;
import com.his.patient.service.PatientHealthProfileService;
import com.his.patient.vo.PatientPastDiseaseVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 患者既往疾病史控制器
 *
 * <p>补齐了两处此前缺的：① 查询路径统一成 {@code POST /list}（同一组档案原先是
 * 过敏史走 POST、既往史走 GET，前端 API 层索性一行都没写 —— 这一组于是长期没有入口）；
 * ② 新增 {@code DELETE /deleteById}，此前只能加不能删。
 */
@Tag(name = "患者既往疾病史")
@RestController
@RequestMapping("/patient/pastDisease")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('patient:profile:list')")
public class PatientPastDiseaseController {

    private final PatientHealthProfileService patientHealthProfileService;

    @Operation(summary = "查询患者的既往疾病史列表")
    @PostMapping("/list")
    public Result<List<PatientPastDiseaseVO>> list(@Valid @RequestBody PatientHistoryQueryDTO queryDTO) {
        return Result.success(patientHealthProfileService.getProfile(queryDTO.getPatientId()).getPastDiseases());
    }

    @PreAuthorize("hasAuthority('patient:profile:add')")
    @Operation(summary = "新增或修改既往疾病史")
    @PostMapping("/pastDiseaseUpsert")
    public Result<PatientPastDiseaseVO> pastDiseaseUpsert(@RequestBody @Valid PatientPastDiseaseUpsertDTO dto) {
        return Result.success(patientHealthProfileService.savePastDisease(dto));
    }

    @PreAuthorize("hasAuthority('patient:profile:delete')")
    @Operation(summary = "根据ID删除既往疾病史")
    @DeleteMapping("/deleteById")
    public Result<Void> deleteById(@RequestParam Long id) {
        patientHealthProfileService.deletePastDisease(id);
        return Result.success();
    }
}

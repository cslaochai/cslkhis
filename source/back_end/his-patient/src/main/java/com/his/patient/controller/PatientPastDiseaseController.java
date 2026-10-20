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
 * 患者既往疾病史控制器：健康档案中既往疾病史明细的查询与维护接口入口。
 */
@Tag(name = "患者既往疾病史")
@RestController
@RequestMapping("/patient/pastDisease")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('patient:profile:list')")
public class PatientPastDiseaseController {

    private final PatientHealthProfileService patientHealthProfileService;

    /**
     * 取一名患者的既往疾病史明细。
     * 档案页整组取数统一走健康档案聚合接口，本接口供只需要既往病史这一组的场景按患者号单独回读。
     */
    @Operation(summary = "查询患者的既往疾病史列表")
    @PostMapping("/list")
    public Result<List<PatientPastDiseaseVO>> list(@Valid @RequestBody PatientHistoryQueryDTO queryDTO) {
        return Result.success(patientHealthProfileService.getProfile(queryDTO.getPatientId()).getPastDiseases());
    }

    /**
     * 新增或修改一条既往疾病史，标识为空走新增、否则覆盖写。
     * 用于「健康档案」页编辑弹框保存既往病史明细。
     */
    @PreAuthorize("hasAuthority('patient:profile:add')")
    @Operation(summary = "新增或修改既往疾病史")
    @PostMapping("/pastDiseaseUpsert")
    public Result<PatientPastDiseaseVO> pastDiseaseUpsert(@RequestBody @Valid PatientPastDiseaseUpsertDTO dto) {
        return Result.success(patientHealthProfileService.savePastDisease(dto));
    }

    /**
     * 删除一条既往疾病史明细。
     * 用于「健康档案」页既往病史表格的行删除。
     */
    @PreAuthorize("hasAuthority('patient:profile:delete')")
    @Operation(summary = "根据ID删除既往疾病史")
    @DeleteMapping("/deleteById")
    public Result<Void> deleteById(@RequestParam Long id) {
        patientHealthProfileService.deletePastDisease(id);
        return Result.success();
    }
}

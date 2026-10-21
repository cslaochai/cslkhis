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
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 患者手术外伤史控制器：健康档案中手术外伤史明细的查询与维护接口入口。
 */
@Tag(name = "患者手术外伤史")
@RestController
@RequestMapping("/patient/surgeryHistory")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('patient:profile:list')")
public class PatientSurgeryHistoryController {

    private final PatientHealthProfileService patientHealthProfileService;

    /**
     * 取一名患者的手术外伤史明细。
     * 档案页整组取数统一走健康档案聚合接口，本接口供只需要手术外伤史这一组的场景按患者号单独回读。
     */
    @Operation(summary = "查询患者的手术外伤史列表")
    @PostMapping("/list")
    public Result<List<PatientSurgeryHistoryVO>> list(@Valid @RequestBody PatientHistoryQueryDTO queryDTO) {
        return Result.success(patientHealthProfileService.getProfile(queryDTO.getPatientId()).getSurgeryHistories());
    }

    /**
     * 新增或修改一条手术外伤史，标识为空走新增、否则覆盖写。
     * 用于「健康档案」页编辑弹框保存手术外伤史明细。
     */
    @PreAuthorize("hasAuthority('patient:profile:add')")
    @Operation(summary = "新增或修改手术外伤史")
    @PostMapping("/surgeryHistoryUpsert")
    public Result<PatientSurgeryHistoryVO> surgeryHistoryUpsert(@RequestBody @Valid PatientSurgeryHistoryUpsertDTO dto) {
        return Result.success(patientHealthProfileService.saveSurgeryHistory(dto));
    }

    /**
     * 删除一条手术外伤史明细。
     * 用于「健康档案」页手术外伤史表格的行删除。
     */
    @PreAuthorize("hasAuthority('patient:profile:delete')")
    @Operation(summary = "根据ID删除手术外伤史")
    @DeleteMapping("/deleteById")
    public Result<Void> deleteById(@RequestParam Long id) {
        patientHealthProfileService.deleteSurgeryHistory(id);
        return Result.success();
    }
}

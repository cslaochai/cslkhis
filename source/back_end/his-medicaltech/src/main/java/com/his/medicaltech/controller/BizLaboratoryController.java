package com.his.medicaltech.controller;

import com.his.common.base.PageResult;
import com.his.common.base.Result;
import com.his.medicaltech.dto.LabResultSaveDTO;
import com.his.medicaltech.dto.LaboratoryAuditDTO;
import com.his.medicaltech.dto.LaboratoryReceiveDTO;
import com.his.medicaltech.dto.LaboratoryRecordQueryDTO;
import com.his.medicaltech.service.MedicalTechService;
import com.his.medicaltech.vo.BizLaboratoryRecordVO;
import com.his.medicaltech.vo.LaboratoryDetailVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 医技管理 - 检验控制器
 */
@Tag(name = "医技管理")
@RestController
@RequestMapping("/medicaltech/laboratory")
// G5：类级兜底（方法级已有注解的保持不变）。
@PreAuthorize("hasAnyAuthority('opd:doctorWorkstation:list', 'medtech:laboratoryWorkstation:list')")
@RequiredArgsConstructor
public class BizLaboratoryController {

    private final MedicalTechService medicalTechService;

    @Operation(summary = "分页查询检验记录列表")
    @PostMapping("/listPage")
    public Result<PageResult<BizLaboratoryRecordVO>> laboratoryListPage(@Valid @RequestBody LaboratoryRecordQueryDTO queryDTO) {
        return Result.success(medicalTechService.selectLaboratoryRecordPageVO(
                queryDTO.getPatientId(), queryDTO.getLaboratoryDeptId(),
                queryDTO.getPageNum(), queryDTO.getPageSize()));
    }

    @Operation(summary = "查询检验记录列表（不分页）")
    @PostMapping("/list")
    public Result<List<BizLaboratoryRecordVO>> laboratoryList(@Valid @RequestBody LaboratoryRecordQueryDTO queryDTO) {
        return Result.success(medicalTechService.selectLaboratoryRecordListVO(
                queryDTO.getPatientId(), queryDTO.getLaboratoryDeptId()));
    }

    // 同 BizInspectionController#getInspectionDetail：医生站与检验技师站共用这个入口，
    // 临床侧走 emr:records:list，技师侧走 medtech:laboratoryWorkstation:list。
    @Operation(summary = "根据ID获取检验记录详情（含结果明细与报告）")
    @GetMapping("/getDetailById")
    @PreAuthorize("hasAnyAuthority('opd:doctorWorkstation:list', 'medtech:laboratoryWorkstation:list')")
    public Result<LaboratoryDetailVO> getLaboratoryDetail(@RequestParam Long recordId) {
        return Result.success(medicalTechService.getLaboratoryDetail(recordId));
    }

    @Operation(summary = "接收标本")
    @PostMapping("/receive")
    public Result<Void> receiveSpecimen(@Valid @RequestBody LaboratoryReceiveDTO receiveDTO) {
        boolean success = medicalTechService.receiveSpecimen(receiveDTO.getRecordId(), receiveDTO.getReceiveBy());
        return success ? Result.success("接收成功", null) : Result.error("接收失败");
    }

    @Operation(summary = "录入检验结果")
    @PostMapping("/inputResult")
    public Result<Void> inputLabResult(@Valid @RequestBody LabResultSaveDTO labResultSaveDTO) {
        boolean success = medicalTechService.saveResult(labResultSaveDTO);
        return success ? Result.success("录入成功", null) : Result.error("录入失败");
    }


    @Operation(summary = "审核检验报告")
    @PostMapping("/audit")
    public Result<Void> auditLaboratory(@Valid @RequestBody LaboratoryAuditDTO auditDTO) {
        boolean success = medicalTechService.auditLaboratory(auditDTO.getRecordId(), auditDTO.getAuditBy());
        return success ? Result.success("审核成功", null) : Result.error("审核失败");
    }
}

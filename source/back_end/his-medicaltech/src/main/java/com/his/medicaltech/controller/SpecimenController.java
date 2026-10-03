package com.his.medicaltech.controller;

import com.his.common.base.PageResult;
import com.his.common.base.Result;
import com.his.medicaltech.dto.SpecimenBarcodeDTO;
import com.his.medicaltech.dto.SpecimenQueryDTO;
import com.his.medicaltech.dto.SpecimenRejectDTO;
import com.his.medicaltech.dto.SpecimenSampleDTO;
import com.his.medicaltech.service.MedicalTechService;
import com.his.medicaltech.vo.BizLaboratoryRecordVO;
import com.his.medicaltech.vo.SpecimenStatsVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import org.springframework.security.access.prepost.PreAuthorize;

/**
 * 医技管理 - 标本控制器
 */
@Tag(name = "医技管理")
@RestController
@RequestMapping("/medicaltech/specimen")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('medtech:specimen:list')")
public class SpecimenController {

    private final MedicalTechService medicalTechService;

    @Operation(summary = "分页查询标本列表")
    @PostMapping("/list")
    public Result<PageResult<BizLaboratoryRecordVO>> specimenList(@RequestBody SpecimenQueryDTO queryDTO) {
        return Result.success(medicalTechService.selectSpecimenPageVO(
                queryDTO.getPatientId(), queryDTO.getRecordStatus(), queryDTO.getKeyword(),
                queryDTO.getPageNum(), queryDTO.getPageSize()));
    }

    @Operation(summary = "标本统计")
    @GetMapping("/stats")
    public Result<SpecimenStatsVO> specimenStats() {
        return Result.success(medicalTechService.getSpecimenStats());
    }

    @PreAuthorize("hasAuthority('medtech:specimen:edit')")
    @Operation(summary = "分配标本条码")
    @PostMapping("/barcode")
    public Result<Void> assignBarcode(@RequestBody SpecimenBarcodeDTO barcodeDTO) {
        boolean success = medicalTechService.assignBarcode(barcodeDTO.getRecordId(), barcodeDTO.getSpecimenNo());
        return success ? Result.success("分配成功", null) : Result.error("分配失败");
    }

    @PreAuthorize("hasAuthority('medtech:specimen:edit')")
    @Operation(summary = "标本采集确认")
    @PostMapping("/sample")
    public Result<Void> sampleSpecimen(@RequestBody SpecimenSampleDTO sampleDTO) {
        boolean success = medicalTechService.sampleSpecimen(sampleDTO.getRecordId(), sampleDTO.getSampleBy());
        return success ? Result.success("采集确认成功", null) : Result.error("操作失败");
    }

    @PreAuthorize("hasAuthority('medtech:specimen:edit')")
    @Operation(summary = "标本退回")
    @PostMapping("/reject")
    public Result<Void> rejectSpecimen(@RequestBody SpecimenRejectDTO rejectDTO) {
        boolean success = medicalTechService.rejectSpecimen(rejectDTO.getRecordId(), rejectDTO.getReason());
        return success ? Result.success("退回成功", null) : Result.error("操作失败");
    }
}

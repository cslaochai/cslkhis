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
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * 标本管理控制器：检验标本跟踪列表与赋码、采集确认、退回操作的接口入口。
 */
@Tag(name = "医技管理")
@RestController
@RequestMapping("/medicaltech/specimen")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('medtech:specimen:list')")
public class SpecimenController {

    private final MedicalTechService medicalTechService;

    /**
     * 标本跟踪分页：按患者、流程状态与关键字筛选。
     * 用于标本管理页查询卡。
     */
    @Operation(summary = "分页查询标本列表")
    @PostMapping("/list")
    public Result<PageResult<BizLaboratoryRecordVO>> specimenList(@Valid @RequestBody SpecimenQueryDTO queryDTO) {
        return Result.success(medicalTechService.selectSpecimenPageVO(
                queryDTO.getPatientId(), queryDTO.getRecordStatus(), queryDTO.getKeyword(),
                queryDTO.getPageNum(), queryDTO.getPageSize()));
    }

    /**
     * 统计今日送检与待采集、已采集、检验中、异常各环节的标本量。
     * 标本管理页与列表并行拉取，渲染顶部环节计数卡。
     */
    @Operation(summary = "标本统计")
    @GetMapping("/stats")
    public Result<SpecimenStatsVO> specimenStats() {
        return Result.success(medicalTechService.getSpecimenStats());
    }

    @PreAuthorize("hasAuthority('medtech:specimen:edit')")
    /**
     * 给标本记录绑定条码号，标本自此可按码追溯。
     * 用于标本管理页对待编码标本执行赋码。
     */
    @Operation(summary = "分配标本条码")
    @PostMapping("/barcode")
    public Result<Void> assignBarcode(@Valid @RequestBody SpecimenBarcodeDTO barcodeDTO) {
        boolean success = medicalTechService.assignBarcode(barcodeDTO.getRecordId(), barcodeDTO.getSpecimenNo());
        return success ? Result.success("分配成功", null) : Result.error("分配失败");
    }

    @PreAuthorize("hasAuthority('medtech:specimen:edit')")
    /**
     * 确认标本采集完成并登记采集人，流程推进到送检环节。
     * 用于标本管理页对已编码标本做采集确认。
     */
    @Operation(summary = "标本采集确认")
    @PostMapping("/sample")
    public Result<Void> sampleSpecimen(@Valid @RequestBody SpecimenSampleDTO sampleDTO) {
        boolean success = medicalTechService.sampleSpecimen(sampleDTO.getRecordId(), sampleDTO.getSampleBy());
        return success ? Result.success("采集确认成功", null) : Result.error("操作失败");
    }

    @PreAuthorize("hasAuthority('medtech:specimen:edit')")
    /**
     * 将不合格标本退回并记录退回原因。
     * 用于标本管理页对上送不合格标本执行退回。
     */
    @Operation(summary = "标本退回")
    @PostMapping("/reject")
    public Result<Void> rejectSpecimen(@Valid @RequestBody SpecimenRejectDTO rejectDTO) {
        boolean success = medicalTechService.rejectSpecimen(rejectDTO.getRecordId(), rejectDTO.getReason());
        return success ? Result.success("退回成功", null) : Result.error("操作失败");
    }
}

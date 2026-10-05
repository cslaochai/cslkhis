package com.his.emr.controller;

import com.his.common.base.PageResult;
import com.his.common.base.Result;
import com.his.emr.dto.MedicalRecordQueryDTO;
import com.his.emr.dto.MedicalRecordQueryPageDTO;
import com.his.emr.service.EmrService;
import com.his.emr.vo.BizMedicalRecordVO;
import com.his.emr.vo.EmrRecordDetailVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 电子病历管理控制器
 */
@Tag(name = "电子病历管理")
@RestController
@RequestMapping("/emr")
@RequiredArgsConstructor
@PreAuthorize("hasAnyAuthority('opd:doctorWorkstation:list', 'emr:medicalRecord:list', 'emr:medicalReview:list', 'opd:todayVisits:list')")
public class EmrController {

    private final EmrService emrService;

    @Operation(summary = "查询病历列表")
    @GetMapping("/getByPatientId")
    public Result<List<BizMedicalRecordVO>> getByPatientId(@Valid MedicalRecordQueryDTO queryDTO) {
        return Result.success(emrService.getByPatientId(queryDTO));
    }

    @Operation(summary = "查询当前病历")
    @GetMapping("/getByRegistId")
    public Result<BizMedicalRecordVO> getByRegistId(@RequestParam Long registId) {
        return Result.success(emrService.getByRegistId(registId));
    }

    @Operation(summary = "分页查询病历列表")
    @GetMapping("/listPage")
    public Result<PageResult<BizMedicalRecordVO>> listPage(@Valid MedicalRecordQueryPageDTO queryDTO) {
        return Result.success(emrService.listPage(queryDTO));
    }

    @Operation(summary = "根据ID获取病历详情（含处方、检查申请、检验申请）")
    @GetMapping("/getRecordDetailById")
    public Result<EmrRecordDetailVO> getRecordDetailById(@RequestParam Long recordId) {
        return Result.success(emrService.getRecordDetail(recordId));
    }

    // 患者端「我的病历列表 / 病历详情」已迁至 his-miniapp 的 /miniapp/emr/*（患者端点集中收口）

    @PreAuthorize("hasAuthority('emr:medicalRecord:edit')")
    @Operation(summary = "审核病历")
    @PostMapping("/recordReview")
    public Result<Void> recordReview(@RequestParam Long recordId,
                                     @RequestParam boolean approved,
                                     @RequestParam(required = false) String remark,
                                     @RequestParam(required = false) String reviewerName) {
        boolean success = emrService.reviewRecord(recordId, approved, remark, reviewerName);
        return success ? Result.success("审核完成", null) : Result.error("审核失败");
    }
}

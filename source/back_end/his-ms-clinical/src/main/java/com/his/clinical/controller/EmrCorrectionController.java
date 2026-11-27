package com.his.clinical.controller;

import com.his.clinical.dto.EmrCorrectionApplyDTO;
import com.his.clinical.dto.EmrCorrectionOpinionDTO;
import com.his.clinical.dto.EmrCorrectionPageQueryDTO;
import com.his.clinical.service.EmrCorrectionService;
import com.his.clinical.vo.EmrCorrectionVO;
import com.his.common.base.PageResult;
import com.his.common.base.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * 病历更正申请控制器：已提交/归档病历的更正审批与回写留痕。
 */
@Tag(name = "病历更正申请")
@RestController
@RequestMapping("/emr/correction")
@RequiredArgsConstructor
@PreAuthorize("hasAnyAuthority('emr:correction:list', 'emr:correction:apply', 'emr:correction:deptApprove', 'emr:correction:medicalApprove')")
public class EmrCorrectionController {

    private final EmrCorrectionService emrCorrectionService;

    /**
     * 发起更正申请：仅已提交/归档病历可发起，同一病历在途单唯一。
     */
    @PreAuthorize("hasAuthority('emr:correction:apply')")
    @Operation(summary = "发起病历更正申请")
    @PostMapping("/apply")
    public Result<EmrCorrectionVO> apply(@Valid @RequestBody EmrCorrectionApplyDTO dto) {
        return Result.success(emrCorrectionService.apply(dto));
    }

    /**
     * 科室初审：通过转医务审批，驳回终态。
     */
    @PreAuthorize("hasAuthority('emr:correction:deptApprove')")
    @Operation(summary = "科室审核更正申请")
    @PostMapping("/deptApprove")
    public Result<Void> deptApprove(@Valid @RequestBody EmrCorrectionOpinionDTO dto) {
        emrCorrectionService.deptApprove(dto);
        return Result.success("审核完成", null);
    }

    /**
     * 医务终审：通过置已批准待修改，驳回终态。
     */
    @PreAuthorize("hasAuthority('emr:correction:medicalApprove')")
    @Operation(summary = "医务审批更正申请")
    @PostMapping("/medicalApprove")
    public Result<Void> medicalApprove(@Valid @RequestBody EmrCorrectionOpinionDTO dto) {
        emrCorrectionService.medicalApprove(dto);
        return Result.success("审批完成", null);
    }

    /**
     * 申请人撤回在途申请（待科室审核 / 待医务审批）。
     */
    @PreAuthorize("hasAuthority('emr:correction:apply')")
    @Operation(summary = "撤回更正申请")
    @PostMapping("/revoke")
    public Result<Void> revoke(@RequestParam Long id) {
        emrCorrectionService.revoke(id);
        return Result.success("已撤回", null);
    }

    /**
     * 更正申请台账分页（按数据权限收口科室）。
     */
    @PreAuthorize("hasAuthority('emr:correction:list')")
    @Operation(summary = "更正申请台账分页")
    @PostMapping("/listPage")
    public Result<PageResult<EmrCorrectionVO>> listPage(@Valid @RequestBody EmrCorrectionPageQueryDTO queryDTO) {
        return Result.success(emrCorrectionService.page(queryDTO));
    }

    /**
     * 更正申请单详情（两级审批意见与回写留痕）。
     */
    @PreAuthorize("hasAuthority('emr:correction:list')")
    @Operation(summary = "更正申请单详情")
    @GetMapping("/getById")
    public Result<EmrCorrectionVO> getById(@RequestParam Long id) {
        return Result.success(emrCorrectionService.getDetail(id));
    }
}

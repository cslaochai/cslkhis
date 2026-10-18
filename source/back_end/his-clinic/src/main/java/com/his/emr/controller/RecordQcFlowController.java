package com.his.emr.controller;

import com.his.common.base.PageResult;
import com.his.common.base.Result;
import com.his.emr.dto.*;
import com.his.emr.service.RecordQcFlowService;
import com.his.emr.vo.RecordQcFlowActionVO;
import com.his.emr.vo.RecordQcFlowVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 病历三级质控流转控制器
 */
@Tag(name = "病历三级质控流转")
@RestController
@RequestMapping("/recordQcFlow")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('qc:recordQc:list')")
public class RecordQcFlowController {

    private final RecordQcFlowService recordQcFlowService;

    /**
     * 发起三级质控流转：同一份病历同时只允许一条在途流转，发起后进科级待审。
     * 用于病案质控工作台「三级质控流转」页签的发起流转弹窗。
     */
    @PreAuthorize("hasAuthority('qc:recordQc:edit')")
    @Operation(summary = "发起三级质控流转（同一病历同时只允许一条在途）")
    @PostMapping("/start")
    public Result<RecordQcFlowVO> start(@Valid @RequestBody RecordQcFlowStartDTO dto) {
        return Result.success("已发起，进入科级待审", recordQcFlowService.start(dto));
    }

    /**
     * 当前待审级审核通过：科级与病案室两级都走本接口签通过，审核意见可空。
     * 用于三级质控流转详情抽屉里当前级的通过操作；医务处那一环走终审，不在此口。
     */
    @PreAuthorize("hasAuthority('qc:recordQc:edit')")
    @Operation(summary = "当前级审核通过（科级/病案室；医务处请走 finalApprove）")
    @PostMapping("/approve")
    public Result<Void> approve(@Valid @RequestBody RecordQcFlowOpinionDTO dto) {
        recordQcFlowService.approve(dto);
        return Result.success("审核通过", null);
    }

    /**
     * 当前待审级退回整改：缺陷明细与整改要求必填，病历退回科室修改。
     * 用于三级质控流转详情抽屉里审核不通过时的退回操作。
     */
    @PreAuthorize("hasAuthority('qc:recordQc:edit')")
    @Operation(summary = "当前级退回整改（缺陷明细 + 整改要求必填）")
    @PostMapping("/return")
    public Result<Void> returnForRework(@Valid @RequestBody RecordQcFlowReturnDTO dto) {
        recordQcFlowService.returnForRework(dto);
        return Result.success("已退回整改", null);
    }

    /**
     * 科室整改提交：改完病历后回签，流转单回到退回发生的那一级重新待审。
     * 用于三级质控流转详情抽屉里科室侧的整改回传动作。
     */
    @PreAuthorize("hasAuthority('qc:recordQc:add')")
    @Operation(summary = "科室整改提交（回到退回发生级待审）")
    @PostMapping("/resubmit")
    public Result<Void> resubmit(@Valid @RequestBody RecordQcFlowOpinionDTO dto) {
        recordQcFlowService.resubmit(dto);
        return Result.success("整改已提交", null);
    }

    /**
     * 医务处终审：必须给出病历终级评级，通过后流转单进入终态。
     * 用于三级质控流转详情抽屉里医务处待审级的终审操作。
     */
    @PreAuthorize("hasAuthority('qc:recordQc:edit')")
    @Operation(summary = "医务处终审（定级必填，终态）")
    @PostMapping("/finalApprove")
    public Result<Void> finalApprove(@Valid @RequestBody RecordQcFlowFinalDTO dto) {
        recordQcFlowService.finalApprove(dto);
        return Result.success("终审通过", null);
    }

    /**
     * 流转单分页：按流转状态与单号/患者/科室关键词收口。
     * 用于「三级质控流转」页签的查询卡与流转台账。
     */
    @Operation(summary = "流转单分页")
    @PostMapping("/listPage")
    public Result<PageResult<RecordQcFlowVO>> listPage(@Valid @RequestBody RecordQcFlowPageQueryDTO queryDTO) {
        return Result.success(recordQcFlowService.page(queryDTO));
    }

    /**
     * 流转单详情：当前所处审核级、流转状态与各级已签意见。
     * 用于流转台账行点开的流转详情抽屉，分级操作按钮按这里的状态显隐。
     */
    @Operation(summary = "流转单详情")
    @GetMapping("/getDetailById")
    public Result<RecordQcFlowVO> getDetailById(@RequestParam Long id) {
        return Result.success(recordQcFlowService.getDetailById(id));
    }

    /**
     * 流转时间线：按动作发生先后列出流转单上每一步操作、操作人与意见。
     * 用于流转详情抽屉的过程留痕展示。
     */
    @Operation(summary = "流转时间线（动作升序）")
    @GetMapping("/listActions")
    public Result<List<RecordQcFlowActionVO>> listActions(@RequestParam Long flowId) {
        return Result.success(recordQcFlowService.listActions(flowId));
    }
}

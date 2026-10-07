package com.his.patient.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.his.common.base.Result;
import com.his.patient.dto.*;
import com.his.patient.enums.ConsultCategoryEnum;
import com.his.patient.service.InpatientConsultationService;
import com.his.patient.vo.ConsultationVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * 住院会诊（P4.1：申请 → 应答 → 会诊记录 → 完成 → 回写病历）
 *
 * <p>约定：查询一律 GET，写操作一律 POST，路径驼峰。
 *
 * <p>按钮可用性（canAccept / canFinish / canCancel / canEdit）由后端在列表与详情里给出，
 * 前端不自己判状态 —— 状态机只有一处实现，才不会出现"界面允许点、后端拒绝"的错位。
 */
@Tag(name = "住院会诊")
@RestController
@RequestMapping("/patient/inpatient/consultation")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('ipd:consultation:list')")
public class InpatientConsultationController {

    private final InpatientConsultationService inpatientConsultationService;

    @Operation(summary = "会诊分页（unfinishedOnly=1 只看未完成；toDeptId 用会诊科室工作台过滤）")
    @GetMapping("/listPage")
    public Result<IPage<ConsultationVO>> listPage(@Valid ConsultationQueryPageDTO query) {
        return Result.success(inpatientConsultationService.listPage(query));
    }

    @Operation(summary = "会诊详情")
    @GetMapping("/getDetailById")
    public Result<ConsultationVO> getDetailById(@RequestParam Long consultationId) {
        return Result.success(inpatientConsultationService.getDetailById(consultationId));
    }

    @PreAuthorize("hasAuthority('ipd:consultation:add')")
    @Operation(summary = "申请/修改会诊（返回会诊号；同一住院+同一会诊科室不允许并存两条未完成会诊）")
    @PostMapping("/save")
    public Result<String> save(@RequestBody @Valid ConsultationUpsertDTO dto) {
        // 本接口只发起普通科间会诊；营养会诊走 /patient/inpatient/nutrition/consultApply
        dto.setConsultCategory(ConsultCategoryEnum.NORMAL.getCode());
        return Result.success("会诊申请已提交", inpatientConsultationService.save(dto));
    }

    @PreAuthorize("hasAuthority('ipd:consultation:edit')")
    @Operation(summary = "会诊科室应答（接诊人 = 当前登录用户，不允许替别人接诊）")
    @PostMapping("/accept")
    public Result<Void> accept(@RequestBody @Valid ConsultationAcceptDTO dto) {
        inpatientConsultationService.accept(dto);
        return Result.success("已接诊", null);
    }

    @PreAuthorize("hasAuthority('ipd:consultation:edit')")
    @Operation(summary = "完成会诊（必须带结论；完成即回写住院病历，返回回写的病历ID）")
    @PostMapping("/finish")
    public Result<String> finish(@RequestBody @Valid ConsultationFinishDTO dto) {
        return Result.success("会诊已完成并回写病历", inpatientConsultationService.finish(dto));
    }

    @PreAuthorize("hasAuthority('ipd:consultation:delete')")
    @Operation(summary = "取消会诊申请（仅「待应答」；已接诊的必须走完成）")
    @PostMapping("/cancel")
    public Result<Void> cancel(@RequestBody @Valid ConsultationCancelDTO dto) {
        inpatientConsultationService.cancel(dto);
        return Result.success("会诊申请已取消", null);
    }

    @Operation(summary = "未完成会诊数（待应答 + 已应答；工作台角标用）")
    @GetMapping("/countUnfinished")
    public Result<Long> countUnfinished(@RequestParam(required = false) Long toDeptId,
                                        @RequestParam(required = false) Long admissionId) {
        return Result.success(inpatientConsultationService.countUnfinished(toDeptId, admissionId));
    }
}

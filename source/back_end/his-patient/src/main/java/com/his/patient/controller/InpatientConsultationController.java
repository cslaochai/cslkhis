package com.his.patient.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.his.common.base.Result;
import com.his.patient.dto.*;
import com.his.common.enums.ConsultCategoryEnum;
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
 */
@Tag(name = "住院会诊")
@RestController
@RequestMapping("/patient/inpatient/consultation")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('ipd:consultation:list')")
public class InpatientConsultationController {

    private final InpatientConsultationService inpatientConsultationService;

    /**
     * 会诊单据分页：按患者、申请科室、受邀科室、状态与是否急会诊过滤，可只看未完成。
     * 用于住院会诊工作台查询卡，受邀科室侧按会诊科室过滤本科待办。
     */
    @Operation(summary = "会诊分页（unfinishedOnly=1 只看未完成；toDeptId 用会诊科室工作台过滤）")
    @GetMapping("/listPage")
    public Result<IPage<ConsultationVO>> listPage(@Valid ConsultationPageQueryDTO query) {
        return Result.success(inpatientConsultationService.listPage(query));
    }

    /**
     * 单条会诊详情：含应答人、结论、回写的病历号与各动作可用性。
     * 用于会诊工作台点开某条会诊的详情弹框。
     */
    @Operation(summary = "会诊详情")
    @GetMapping("/getDetailById")
    public Result<ConsultationVO> getDetailById(@RequestParam Long consultationId) {
        return Result.success(inpatientConsultationService.getDetailById(consultationId));
    }

    /**
     * 发起或修改科间会诊申请，返回会诊号。
     * 用于住院会诊工作台「申请会诊」弹框；营养会诊另走营养科入口，不经本接口。
     */
    @PreAuthorize("hasAuthority('ipd:consultation:add')")
    @Operation(summary = "申请/修改会诊（返回会诊号；同一住院+同一会诊科室不允许并存两条未完成会诊）")
    @PostMapping("/save")
    public Result<String> save(@RequestBody @Valid ConsultationUpsertDTO dto) {
        // 本接口只发起普通科间会诊；营养会诊走 /patient/inpatient/nutrition/consultApply
        dto.setConsultCategory(ConsultCategoryEnum.NORMAL.getCode());
        return Result.success("会诊申请已提交", inpatientConsultationService.save(dto));
    }

    /**
     * 受邀科室医师应答会诊，接诊人只记当前登录用户，不许替别人接诊。
     * 用于会诊科室工作台的「接诊」动作。
     */
    @PreAuthorize("hasAuthority('ipd:consultation:edit')")
    @Operation(summary = "会诊科室应答（接诊人 = 当前登录用户，不允许替别人接诊）")
    @PostMapping("/accept")
    public Result<Void> accept(@RequestBody @Valid ConsultationAcceptDTO dto) {
        inpatientConsultationService.accept(dto);
        return Result.success("已接诊", null);
    }

    /**
     * 完成会诊并落结论，完成即回写住院会诊记录并返回该病历ID。
     * 用于受邀医师在接诊后填写结论并提交收口。
     */
    @PreAuthorize("hasAuthority('ipd:consultation:edit')")
    @Operation(summary = "完成会诊（必须带结论；完成即回写住院病历，返回回写的病历ID）")
    @PostMapping("/finish")
    public Result<String> finish(@RequestBody @Valid ConsultationFinishDTO dto) {
        return Result.success("会诊已完成并回写病历", inpatientConsultationService.finish(dto));
    }

    /**
     * 撤销尚未应答的会诊申请，已接诊的只能走完成收口。
     * 用于申请科室取消误发或已无必要的会诊。
     */
    @PreAuthorize("hasAuthority('ipd:consultation:delete')")
    @Operation(summary = "取消会诊申请（仅「待应答」；已接诊的必须走完成）")
    @PostMapping("/cancel")
    public Result<Void> cancel(@RequestBody @Valid ConsultationCancelDTO dto) {
        inpatientConsultationService.cancel(dto);
        return Result.success("会诊申请已取消", null);
    }

    /**
     * 未完成会诊条数（待应答 + 已应答）。
     * 用于会诊工作台「未完成会诊」统计卡与待办角标，可按受邀科室或单名住院患者取数。
     */
    @Operation(summary = "未完成会诊数（待应答 + 已应答；工作台角标用）")
    @GetMapping("/countUnfinished")
    public Result<Long> countUnfinished(@RequestParam(required = false) Long toDeptId,
                                        @RequestParam(required = false) Long admissionId) {
        return Result.success(inpatientConsultationService.countUnfinished(toDeptId, admissionId));
    }
}

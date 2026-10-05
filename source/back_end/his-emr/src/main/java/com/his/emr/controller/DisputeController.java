package com.his.emr.controller;

import com.his.common.base.PageResult;
import com.his.common.base.Result;
import com.his.emr.dto.*;
import com.his.emr.service.DisputeService;
import com.his.emr.vo.DisputeCaseVO;
import com.his.emr.vo.DisputeStatVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * 医疗纠纷 / 投诉登记（登记 → 受理 → 调查处理 → 结案 / 撤销，病历封存联动）。
 *
 * <p>按项目规范 @PreAuthorize 全部标到方法（类级注解会静默覆盖未标注方法）；
 * 按钮可用性由后端 VO 的 can* 字段给，前端不按 status 码值 switch。
 */
@Tag(name = "医疗纠纷与投诉登记")
@RestController
@RequestMapping("/emr/dispute")
@RequiredArgsConstructor
public class DisputeController {

    private final DisputeService disputeService;

    @PreAuthorize("hasAuthority('qc:dispute:list')")
    @Operation(summary = "纠纷/投诉分页")
    @PostMapping("/listPage")
    public Result<PageResult<DisputeCaseVO>> listPage(@Valid @RequestBody DisputeQueryPageDTO dto) {
        return Result.success(disputeService.listPage(dto));
    }

    @PreAuthorize("hasAuthority('qc:dispute:list')")
    @Operation(summary = "纠纷/投诉详情（含处理跟踪台账）")
    @GetMapping("/getDetailById")
    public Result<DisputeCaseVO> getDetailById(@RequestParam Long id) {
        return Result.success(disputeService.getDetailById(id));
    }

    @PreAuthorize("hasAuthority('qc:dispute:add')")
    @Operation(summary = "登记 / 修改（仅待受理可改）")
    @PostMapping("/caseUpsert")
    public Result<DisputeCaseVO> caseUpsert(@Valid @RequestBody DisputeCaseUpsertDTO dto) {
        return Result.success(dto.getId() == null ? "登记成功" : "修改成功", disputeService.caseUpsert(dto));
    }

    @PreAuthorize("hasAuthority('qc:dispute:edit')")
    @Operation(summary = "受理（待受理→调查中；需封存者联动封存已归档病历）")
    @PostMapping("/accept")
    public Result<DisputeCaseVO> accept(@Valid @RequestBody DisputeActionDTO dto) {
        return Result.success("已受理", disputeService.accept(dto));
    }

    @PreAuthorize("hasAuthority('qc:dispute:edit')")
    @Operation(summary = "登记处理跟踪（追加流水；可推进到调查中/处理中，不得直接结案）")
    @PostMapping("/follow")
    public Result<DisputeCaseVO> follow(@Valid @RequestBody DisputeFollowDTO dto) {
        return Result.success("处理跟踪已登记", disputeService.follow(dto));
    }

    @PreAuthorize("hasAuthority('qc:dispute:edit')")
    @Operation(summary = "补封存病历（受理时暂无已归档病历的单据，归档后回来补封）")
    @PostMapping("/sealNow")
    public Result<DisputeCaseVO> sealNow(@Valid @RequestBody DisputeActionDTO dto) {
        return Result.success("病历已封存", disputeService.sealNow(dto));
    }

    @PreAuthorize("hasAuthority('qc:dispute:edit')")
    @Operation(summary = "结案（途径+责任+赔偿+结论 四项必填，终态）")
    @PostMapping("/close")
    public Result<DisputeCaseVO> close(@Valid @RequestBody DisputeCloseDTO dto) {
        return Result.success("已结案", disputeService.close(dto));
    }

    @PreAuthorize("hasAuthority('qc:dispute:edit')")
    @Operation(summary = "撤销（非终态→已撤销，原因必填，终态）")
    @PostMapping("/revoke")
    public Result<DisputeCaseVO> revoke(@Valid @RequestBody DisputeActionDTO dto) {
        return Result.success("已撤销", disputeService.revoke(dto));
    }

    @PreAuthorize("hasAuthority('qc:dispute:add')")
    @Operation(summary = "删除（软删；仅待受理且无跟踪流水）")
    @DeleteMapping("/deleteById")
    public Result<Boolean> deleteById(@RequestParam Long id) {
        boolean ok = disputeService.deleteById(id);
        return Result.success(ok ? "已删除" : "删除失败", null);
    }

    @PreAuthorize("hasAuthority('qc:dispute:list')")
    @Operation(summary = "统计（状态分布/类型分布/科室TOP/赔偿合计/平均结案天数）")
    @GetMapping("/stat")
    public Result<DisputeStatVO> stat(@RequestParam(required = false) String dateFrom,
                                      @RequestParam(required = false) String dateTo) {
        return Result.success(disputeService.stat(dateFrom, dateTo));
    }
}

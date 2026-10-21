package com.his.inpatient.controller;

import com.his.common.base.PageResult;
import com.his.common.base.Result;
import com.his.inpatient.dto.ReferralDTO;
import com.his.inpatient.service.ReferralService;
import com.his.inpatient.vo.ReferralVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * 双向转诊控制器：上转/下转转诊单的登记、确认、完成与取消闭环的接口入口。
 */
@Tag(name = "双向转诊")
@RestController
@RequestMapping("/patient/referral")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('inpatient:referral:list')")
public class ReferralController {

    private final ReferralService referralService;

    /**
     * 登记一张双向转诊单，落单即进入待确认。
     * 用于「双向转诊」页查询卡右上角的转诊登记弹框提交。
     */
    @PreAuthorize("hasAuthority('inpatient:referral:add')")
    @Operation(summary = "转诊登记")
    @PostMapping("/create")
    public Result<ReferralVO> create(@Valid @RequestBody ReferralDTO.Create dto) {
        return Result.success("转诊单已登记", referralService.create(dto));
    }

    /**
     * 转诊单分页，可按转诊方向、状态与转入医院筛。
     * 用于「双向转诊」页查询卡与列表表格。
     */
    @Operation(summary = "分页查询转诊单")
    @PostMapping("/listPage")
    public Result<PageResult<ReferralVO>> listPage(@Valid @RequestBody ReferralDTO.PageQuery dto) {
        var page = referralService.listPage(dto);
        return Result.success(PageResult.of(page.getTotal(), page.getCurrent(), page.getSize(), page.getPages(),
                page.getRecords()));
    }

    /**
     * 单张转诊单详情，含各环节备注与状态流转结果。
     * 供确认接收、完成回看原单时按转诊单取全字段。
     */
    @Operation(summary = "转诊单详情")
    @GetMapping("/getDetailById")
    public Result<ReferralVO> getDetailById(@RequestParam Long referralId) {
        return Result.success(referralService.getDetailById(referralId));
    }

    /**
     * 确认接收转诊（待确认→已确认），可同时指定院内接收科室。
     * 用于「双向转诊」页待确认行的确认弹框。
     */
    @PreAuthorize("hasAuthority('inpatient:referral:edit')")
    @Operation(summary = "确认转诊（0→1）")
    @PostMapping("/audit")
    public Result<ReferralVO> audit(@Valid @RequestBody ReferralDTO.Audit dto) {
        return Result.success("转诊已确认", referralService.audit(dto));
    }

    /**
     * 完成转诊（已确认→已完成），记录转诊结局备注。
     * 用于「双向转诊」页已确认行的完成弹框。
     */
    @PreAuthorize("hasAuthority('inpatient:referral:edit')")
    @Operation(summary = "完成转诊（1→2）")
    @PostMapping("/finish")
    public Result<ReferralVO> finish(@Valid @RequestBody ReferralDTO.Finish dto) {
        return Result.success("转诊已完成", referralService.finish(dto));
    }

    /**
     * 转诊待确认超时催总值班的手工补跑（定时任务 {@code ReferralDutyEscalateTrigger} 每 30 分钟一轮）。
     * 挂 {@code inpatient:referral:edit}：只有管转诊闭环的人该主动"喊总值班"。
     */
    @PreAuthorize("hasAuthority('inpatient:referral:edit')")
    @Operation(summary = "转诊待确认超时催总值班（手工补跑，返回本轮发出的待办条数）")
    @PostMapping("/escalatePendingToDuty")
    public Result<String> escalatePendingToDuty() {
        return Result.success("已向当日总值班催办", String.valueOf(referralService.escalatePendingToDuty()));
    }

    /**
     * 取消转诊（非终态→已取消），取消原因必填。
     * 用于「双向转诊」页操作列的取消动作。
     */
    @PreAuthorize("hasAuthority('inpatient:referral:delete')")
    @Operation(summary = "取消转诊（0/1→3）")
    @PostMapping("/cancel")
    public Result<ReferralVO> cancel(@Valid @RequestBody ReferralDTO.Cancel dto) {
        return Result.success("转诊已取消", referralService.cancel(dto));
    }
}

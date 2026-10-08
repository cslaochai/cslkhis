package com.his.patient.controller;

import com.his.common.base.PageResult;
import com.his.common.base.Result;
import com.his.patient.dto.ReferralDTO;
import com.his.patient.service.ReferralService;
import com.his.patient.vo.ReferralVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * 双向转诊控制器。
 */
@Tag(name = "双向转诊")
@RestController
@RequestMapping("/patient/referral")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('inpatient:referral:list')")
public class ReferralController {

    private final ReferralService referralService;

    @PreAuthorize("hasAuthority('inpatient:referral:add')")
    @Operation(summary = "转诊登记")
    @PostMapping("/create")
    public Result<ReferralVO> create(@Valid @RequestBody ReferralDTO.Create dto) {
        return Result.success("转诊单已登记", referralService.create(dto));
    }

    @Operation(summary = "分页查询转诊单")
    @PostMapping("/listPage")
    public Result<PageResult<ReferralVO>> listPage(@Valid @RequestBody ReferralDTO.QueryPage dto) {
        var page = referralService.listPage(dto == null ? new ReferralDTO.QueryPage() : dto);
        return Result.success(PageResult.of(page.getTotal(), page.getCurrent(), page.getSize(), page.getPages(),
                page.getRecords()));
    }

    @Operation(summary = "转诊单详情")
    @GetMapping("/getDetailById")
    public Result<ReferralVO> getDetailById(@RequestParam Long referralId) {
        return Result.success(referralService.getDetailById(referralId));
    }

    @PreAuthorize("hasAuthority('inpatient:referral:edit')")
    @Operation(summary = "确认转诊（0→1）")
    @PostMapping("/audit")
    public Result<ReferralVO> audit(@Valid @RequestBody ReferralDTO.Audit dto) {
        return Result.success("转诊已确认", referralService.audit(dto));
    }

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

    @PreAuthorize("hasAuthority('inpatient:referral:delete')")
    @Operation(summary = "取消转诊（0/1→3）")
    @PostMapping("/cancel")
    public Result<ReferralVO> cancel(@Valid @RequestBody ReferralDTO.Cancel dto) {
        return Result.success("转诊已取消", referralService.cancel(dto));
    }
}

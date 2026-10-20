package com.his.drgdip.settle.controller;

import com.his.common.base.PageResult;
import com.his.common.base.Result;
import com.his.drgdip.settle.dto.MedpaySettleDTO;
import com.his.drgdip.settle.entity.BizCaseAppeal;
import com.his.drgdip.settle.service.MedpaySettleService;
import com.his.drgdip.settle.vo.MedpaySettleVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 按病种付费结算控制器（P4 结算层：出院结算账单按 DRG/DIP 计算医保应付与盈亏 + 特例单议）。
 */
@Tag(name = "按病种付费结算")
@RestController
@RequestMapping("/report/medpay")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('medpay:settle:list')")
public class MedpaySettleController {

    private final MedpaySettleService medpaySettleService;

    @Operation(summary = "按结算账单计算医保应付与盈亏（按 medpay.mode 路由 DRG/DIP）")
    @PostMapping("/settle")
    public Result<MedpaySettleVO.SettleRow> settle(@RequestParam Long billId) {
        return Result.success("结算计算完成", medpaySettleService.settleByBill(billId));
    }

    @Operation(summary = "批量结算（billIds 为空取最近 50 张出院结算账单）")
    @PostMapping("/settleBatch")
    public Result<MedpaySettleVO.BatchVO> settleBatch(@Valid @RequestBody MedpaySettleDTO.SettleBatch dto) {
        return Result.success(medpaySettleService.settleBatch(dto));
    }

    @Operation(summary = "结算结果分页")
    @PostMapping("/result/listPage")
    public Result<PageResult<MedpaySettleVO.SettleRow>> resultPage(@Valid @RequestBody MedpaySettleDTO.SettleQuery dto) {
        var page = medpaySettleService.resultPage(dto);
        return Result.success(PageResult.of(page.getTotal(), page.getCurrent(), page.getSize(), page.getPages(),
                page.getRecords()));
    }

    @Operation(summary = "当前医保付费参数（主方式 / 版本 / 费率 / 点值）")
    @GetMapping("/mode")
    public Result<MedpaySettleVO.ModeVO> mode() {
        return Result.success(medpaySettleService.mode());
    }

    @Operation(summary = "创建特例单议草稿")
    @PostMapping("/appeal/create")
    public Result<BizCaseAppeal> createAppeal(@Valid @RequestBody MedpaySettleDTO.AppealCreate dto) {
        return Result.success("特例单议草稿已创建", medpaySettleService.createAppeal(dto));
    }

    @Operation(summary = "提交特例单议（草稿→已提交）")
    @PostMapping("/appeal/submit")
    public Result<Void> submitAppeal(@RequestParam Long id) {
        medpaySettleService.submitAppeal(id);
        return Result.success("特例单议已提交", null);
    }

    @Operation(summary = "审核特例单议（通过/驳回）")
    @PostMapping("/appeal/review")
    public Result<Void> reviewAppeal(@Valid @RequestBody MedpaySettleDTO.AppealReview dto) {
        medpaySettleService.reviewAppeal(dto);
        return Result.success("特例单议审核完成", null);
    }

    @Operation(summary = "撤回特例单议草稿")
    @PostMapping("/appeal/withdraw")
    public Result<Void> withdrawAppeal(@RequestParam Long id) {
        medpaySettleService.withdrawAppeal(id);
        return Result.success("特例单议已撤回", null);
    }

    @Operation(summary = "特例单议分页")
    @PostMapping("/appeal/listPage")
    public Result<PageResult<MedpaySettleVO.AppealRow>> appealPage(@Valid @RequestBody MedpaySettleDTO.AppealQuery dto) {
        var page = medpaySettleService.appealPage(dto);
        return Result.success(PageResult.of(page.getTotal(), page.getCurrent(), page.getSize(), page.getPages(),
                page.getRecords()));
    }
}

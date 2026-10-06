package com.his.charge.controller;


import com.his.charge.dto.*;
import com.his.charge.service.PayChannelBillService;
import com.his.charge.vo.PayChannelBillVO;
import com.his.charge.vo.PayChannelCandidateVO;
import com.his.charge.vo.PayChannelSummaryVO;
import com.his.common.base.PageResult;
import com.his.common.base.Result;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/**
 * 支付渠道对账（M7 留口子，四层口径）。
 *
 * <p>形态：渠道侧"对方账"来自 {@code PayChannelService}（当前控制台打印桩），
 * 台账/勾对/长短款是真实落库的真实流程；接真渠道只换网关实现，本控制器不动。
 */
@RestController
@RequestMapping("/charge/payChannelBill")
public class PayChannelBillController {

    private final PayChannelBillService service;

    public PayChannelBillController(PayChannelBillService service) {
        this.service = service;
    }

    /**
     * 渠道流水分页
     */
    @PreAuthorize("hasAuthority('finance:payChannel:list')")
    @GetMapping("/listPage")
    public Result<PageResult<PayChannelBillVO>> listPage(@Valid PayChannelQueryPageDTO query) {
        return Result.success(service.selectPage(query));
    }

    /**
     * 拉取渠道账单（M7 口子：控制台打印桩模拟商户平台拉取）
     */
    @PreAuthorize("hasAuthority('finance:payChannel:import')")
    @PostMapping("/importBill")
    public Result<Integer> importBill(@Valid @RequestBody PayChannelImportDTO dto) {
        return Result.success(service.importBill(dto));
    }

    /**
     * 手工登记渠道侧流水
     */
    @PreAuthorize("hasAuthority('finance:payChannel:import')")
    @PostMapping("/manualRegister")
    public Result<Boolean> manualRegister(@Valid @RequestBody PayChannelManualDTO dto) {
        return Result.success(service.manualRegister(dto));
    }

    /**
     * 勾对候选：当日该渠道可勾对的本地支付流水（金额相等的排前面）
     */
    @PreAuthorize("hasAuthority('finance:payChannel:list')")
    @GetMapping("/matchCandidates")
    public Result<List<PayChannelCandidateVO>> matchCandidates(@RequestParam Long channelBillId) {
        return Result.success(service.matchCandidates(channelBillId));
    }

    /**
     * 人工勾对
     */
    @PreAuthorize("hasAuthority('finance:payChannel:match')")
    @PostMapping("/match")
    public Result<Boolean> match(@Valid @RequestBody PayChannelMatchDTO dto) {
        return Result.success(service.match(dto));
    }

    /**
     * 长款/短款处理
     */
    @PreAuthorize("hasAuthority('finance:payChannel:diff')")
    @PostMapping("/handleDiff")
    public Result<Boolean> handleDiff(@Valid @RequestBody PayChannelDiffDTO dto) {
        return Result.success(service.handleDiff(dto));
    }

    /**
     * 渠道对账汇总（本地口径 vs 渠道口径）
     */
    @PreAuthorize("hasAuthority('finance:payChannel:list')")
    @GetMapping("/summary")
    public Result<PayChannelSummaryVO> summary(@RequestParam(required = false) LocalDate billDate) {
        return Result.success(service.summary(billDate));
    }
}


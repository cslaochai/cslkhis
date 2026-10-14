package com.mis.controller;

import com.mis.common.base.PageResult;
import com.mis.common.base.Result;
import com.mis.dto.MonthlyOpDTO;
import com.mis.entity.JinbMonthly;
import com.mis.service.MonthlyService;
import com.mis.vo.MonthlyResultVO;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

/**
 * 月度结算：申报 → 审核 → 拨付 → 年终清算
 */
@RestController
@RequestMapping("/monthly")
public class MonthlyController {

    private final MonthlyService monthlyService;

    public MonthlyController(MonthlyService monthlyService) {
        this.monthlyService = monthlyService;
    }

    /** 月度申报（医院发起；平台汇总当月有效结算单） */
    @PostMapping("/declare")
    public Result<MonthlyResultVO> declare(@RequestBody @Valid MonthlyOpDTO dto) {
        return Result.success(monthlyService.declare(dto.getMonth()));
    }

    /** 平台智能审核（拒付/扣款 + 质保金预留） */
    @PostMapping("/audit")
    public Result<MonthlyResultVO> audit(@RequestBody @Valid MonthlyOpDTO dto) {
        return Result.success(monthlyService.audit(dto.getMonth()));
    }

    /** 月度基金拨付 */
    @PostMapping("/allocate")
    public Result<MonthlyResultVO> allocate(@RequestBody @Valid MonthlyOpDTO dto) {
        return Result.success(monthlyService.allocate(dto.getMonth()));
    }

    /** 年终清算（质量保证金返还） */
    @PostMapping("/yearSettle")
    public Result<MonthlyResultVO> yearSettle(@RequestBody @Valid MonthlyOpDTO dto) {
        return Result.success(monthlyService.yearSettle(dto.getMonth()));
    }

    /** 查询月度结算结果（应收 vs 实收） */
    @GetMapping("/result")
    public Result<MonthlyResultVO> result(@RequestParam String month) {
        return Result.success(monthlyService.result(month));
    }

    /** 月度结算单分页 */
    @GetMapping("/listPage")
    public Result<PageResult<JinbMonthly>> listPage(@RequestParam(defaultValue = "1") int pageNum,
                                                    @RequestParam(defaultValue = "10") int pageSize) {
        return Result.success(monthlyService.page(pageNum, pageSize));
    }
}

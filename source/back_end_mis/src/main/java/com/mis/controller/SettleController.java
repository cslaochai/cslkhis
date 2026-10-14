package com.mis.controller;

import com.mis.common.base.PageVO;
import com.mis.common.base.Result;
import com.mis.dto.BizNoDTO;
import com.mis.dto.BizPageParam;
import com.mis.service.SettleService;
import com.mis.vo.DecVO;
import com.mis.vo.PendingSettleVO;
import com.mis.vo.SettleResultVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 门诊 / 住院 即时结算
 */
@RestController
@RequestMapping("/settle")
@RequiredArgsConstructor
public class SettleController {

    private final SettleService settleService;

    // ---------------- 门诊 ----------------

    @GetMapping("/outpatient/listPage")
    public Result<PageVO<PendingSettleVO>> outpatientPage(BizPageParam query) {
        return Result.success(settleService.pendingPage(1, query));
    }

    @PostMapping("/outpatient/preview")
    public Result<DecVO> outpatientPreview(@RequestBody BizNoDTO dto) {
        return Result.success(settleService.preview(dto.getBizNo()));
    }

    @PostMapping("/outpatient/confirm")
    public Result<SettleResultVO> outpatientConfirm(@RequestBody BizNoDTO dto) {
        return Result.success(settleService.confirm(1, dto.getBizNo()));
    }

    // ---------------- 住院 ----------------

    @GetMapping("/inpatient/listPage")
    public Result<PageVO<PendingSettleVO>> inpatientPage(BizPageParam query) {
        return Result.success(settleService.pendingPage(2, query));
    }

    @PostMapping("/inpatient/preview")
    public Result<DecVO> inpatientPreview(@RequestBody BizNoDTO dto) {
        return Result.success(settleService.preview(dto.getBizNo()));
    }

    @PostMapping("/inpatient/confirm")
    public Result<SettleResultVO> inpatientConfirm(@RequestBody BizNoDTO dto) {
        return Result.success(settleService.confirm(2, dto.getBizNo()));
    }
}

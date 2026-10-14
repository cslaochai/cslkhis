package com.mss.controller;

import com.mss.common.base.PageVO;
import com.mss.common.base.Result;
import com.mss.dto.BizPageParam;
import com.mss.dto.PeriodDTO;
import com.mss.entity.MssRecon;
import com.mss.service.ReconService;
import com.mss.vo.ActionVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 清算对账
 */
@RestController
@RequestMapping("/recon")
@RequiredArgsConstructor
public class ReconController {

    private final ReconService reconService;

    @GetMapping("/listPage")
    public Result<PageVO<MssRecon>> listPage(BizPageParam query) {
        return Result.success(reconService.reconPage(query));
    }

    @PostMapping("/doReconcile")
    public Result<ActionVO> doReconcile(@RequestBody PeriodDTO dto) {
        return Result.success(reconService.doReconcile(dto.getPeriod()));
    }
}

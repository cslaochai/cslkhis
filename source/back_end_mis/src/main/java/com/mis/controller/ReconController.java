package com.mis.controller;

import com.mis.common.base.PageVO;
import com.mis.common.base.Result;
import com.mis.dto.BizPageParam;
import com.mis.dto.PeriodDTO;
import com.mis.entity.MssRecon;
import com.mis.service.ReconService;
import com.mis.vo.ActionVO;
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

package com.mss.controller;

import com.mss.common.base.PageVO;
import com.mss.common.base.Result;
import com.mss.dto.FilingQueryDTO;
import com.mss.dto.FilingUpsertDTO;
import com.mss.dto.IdDTO;
import com.mss.entity.MssFiling;
import com.mss.service.FilingService;
import com.mss.vo.ActionVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 异地就医备案
 */
@RestController
@RequestMapping("/filing")
@RequiredArgsConstructor
public class FilingController {

    private final FilingService filingService;

    @GetMapping("/listPage")
    public Result<PageVO<MssFiling>> listPage(FilingQueryDTO query) {
        return Result.success(filingService.filingPage(query));
    }

    @PostMapping("/upsert")
    public Result<ActionVO> upsert(@RequestBody FilingUpsertDTO dto) {
        return Result.success(filingService.upsert(dto));
    }

    @PostMapping("/revoke")
    public Result<ActionVO> revoke(@RequestBody IdDTO dto) {
        return Result.success(filingService.revoke(Long.valueOf(dto.getId())));
    }
}

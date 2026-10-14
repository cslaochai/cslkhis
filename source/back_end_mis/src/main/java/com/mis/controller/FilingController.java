package com.mis.controller;

import com.mis.common.base.PageVO;
import com.mis.common.base.Result;
import com.mis.dto.FilingQueryDTO;
import com.mis.dto.FilingUpsertDTO;
import com.mis.dto.IdDTO;
import com.mis.entity.MssFiling;
import com.mis.service.FilingService;
import com.mis.vo.ActionVO;
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

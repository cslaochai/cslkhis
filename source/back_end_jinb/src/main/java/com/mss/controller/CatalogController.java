package com.mss.controller;

import com.mss.common.base.PageVO;
import com.mss.common.base.Result;
import com.mss.dto.CatalogQueryDTO;
import com.mss.entity.MssCatalog;
import com.mss.service.CatalogService;
import com.mss.vo.CatalogStatsVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 医保三大目录
 */
@RestController
@RequestMapping("/catalog")
@RequiredArgsConstructor
public class CatalogController {

    private final CatalogService catalogService;

    @GetMapping("/listPage")
    public Result<PageVO<MssCatalog>> listPage(CatalogQueryDTO query) {
        return Result.success(catalogService.catalogPage(query));
    }

    @GetMapping("/stats")
    public Result<CatalogStatsVO> stats() {
        return Result.success(catalogService.catalogStats());
    }
}

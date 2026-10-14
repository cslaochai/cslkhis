package com.mis.controller;

import com.mis.common.base.PageVO;
import com.mis.common.base.Result;
import com.mis.dto.CatalogQueryDTO;
import com.mis.entity.MssCatalog;
import com.mis.service.CatalogService;
import com.mis.vo.CatalogStatsVO;
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

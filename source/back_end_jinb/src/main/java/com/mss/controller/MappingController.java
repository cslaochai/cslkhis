package com.mss.controller;

import com.mss.common.base.PageVO;
import com.mss.common.base.Result;
import com.mss.dto.IdDTO;
import com.mss.dto.MappingMapDTO;
import com.mss.dto.MappingQueryDTO;
import com.mss.entity.MssItemMapping;
import com.mss.service.CatalogService;
import com.mss.vo.ActionVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 院内项目 ↔ 医保目录对照
 */
@RestController
@RequestMapping("/mapping")
@RequiredArgsConstructor
public class MappingController {

    private final CatalogService catalogService;

    @GetMapping("/listPage")
    public Result<PageVO<MssItemMapping>> listPage(MappingQueryDTO query) {
        return Result.success(catalogService.mappingPage(query));
    }

    @PostMapping("/map")
    public Result<ActionVO> map(@RequestBody MappingMapDTO dto) {
        return Result.success(catalogService.map(dto));
    }

    @PostMapping("/unmap")
    public Result<ActionVO> unmap(@RequestBody IdDTO dto) {
        return Result.success(catalogService.unmap(Long.valueOf(dto.getId())));
    }

    @PostMapping("/autoMatch")
    public Result<ActionVO> autoMatch() {
        return Result.success(catalogService.autoMatch());
    }
}

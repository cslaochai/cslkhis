package com.his.charge.controller;


import com.his.charge.dto.YbAutoMatchDTO;
import com.his.charge.dto.YbMapDTO;
import com.his.charge.dto.YbMappingQueryPageDTO;
import com.his.charge.service.YbMappingService;
import com.his.charge.vo.YbAutoMatchResultVO;
import com.his.charge.vo.YbMappingListVO;
import com.his.charge.vo.YbMappingStatsVO;
import com.his.common.base.PageResult;
import com.his.common.base.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 医保目录对照控制器（院内项目 ↔ 国家医保编码）。
 */
@Tag(name = "医保目录对照")
@RestController
@RequestMapping("/charge/ybMapping")
@RequiredArgsConstructor
public class YbMappingController {

    private final YbMappingService mappingService;

    @PreAuthorize("hasAuthority('finance:insuranceMapping:list')")
    @Operation(summary = "对照工作台分页（itemType 必填；未对照行医保字段为 null）")
    @GetMapping("/listPage")
    public Result<PageResult<YbMappingListVO>> listPage(@Valid YbMappingQueryPageDTO queryDTO) {
        return Result.success(mappingService.listPage(queryDTO));
    }

    @PreAuthorize("hasAuthority('finance:insuranceMapping:list')")
    @Operation(summary = "各类型对照率统计")
    @GetMapping("/stats")
    public Result<List<YbMappingStatsVO>> stats() {
        return Result.success(mappingService.stats());
    }

    @PreAuthorize("hasAuthority('finance:insuranceMapping:edit')")
    @Operation(summary = "人工对照（已存在旧对照=换对照覆盖）")
    @PostMapping("/map")
    public Result<YbMappingListVO> map(@Valid @RequestBody YbMapDTO dto) {
        return Result.success(mappingService.map(dto));
    }

    @PreAuthorize("hasAuthority('finance:insuranceMapping:edit')")
    @Operation(summary = "解对照（物理删）")
    @PostMapping("/unmap")
    public Result<Void> unmap(@RequestParam Integer itemType, @RequestParam Long itemId) {
        mappingService.unmap(itemType, itemId);
        return Result.success(null);
    }

    @PreAuthorize("hasAuthority('finance:insuranceMapping:edit')")
    @Operation(summary = "自动对照（名称精确匹配且唯一命中才落；itemType 空=全部）")
    @PostMapping("/autoMatch")
    public Result<YbAutoMatchResultVO> autoMatch(@Valid @RequestBody YbAutoMatchDTO dto) {
        return Result.success(mappingService.autoMatch(dto));
    }
}

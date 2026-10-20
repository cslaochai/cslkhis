package com.his.charge.controller;


import com.his.charge.dto.YbAutoMatchDTO;
import com.his.charge.dto.YbMapDTO;
import com.his.charge.dto.YbMappingPageQueryDTO;
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

    private final YbMappingService ybMappingService;

    /**
     * 医保目录对照工作台分页：院内项目与已对照的国家医保编码并列，未对照行医保字段为空。
     * 用于医保目录对照页按项目类型逐条建立院内项目与医保编码的映射。
     */
    @PreAuthorize("hasAuthority('finance:insuranceMapping:list')")
    @Operation(summary = "对照工作台分页（itemType 必填；未对照行医保字段为 null）")
    @GetMapping("/listPage")
    public Result<PageResult<YbMappingListVO>> listPage(@Valid YbMappingPageQueryDTO queryDTO) {
        return Result.success(ybMappingService.listPage(queryDTO));
    }

    /**
     * 统计各类型项目的对照完成率。
     * 用于医保目录对照页顶部展示各类目还需对照的进度。
     */
    @PreAuthorize("hasAuthority('finance:insuranceMapping:list')")
    @Operation(summary = "各类型对照率统计")
    @GetMapping("/stats")
    public Result<List<YbMappingStatsVO>> stats() {
        return Result.success(ybMappingService.stats());
    }

    /**
     * 人工为院内项目指定国家医保编码，已有旧对照则覆盖为新对照。
     * 用于对照工作台逐条手动落映射关系。
     */
    @PreAuthorize("hasAuthority('finance:insuranceMapping:edit')")
    @Operation(summary = "人工对照（已存在旧对照=换对照覆盖）")
    @PostMapping("/map")
    public Result<YbMappingListVO> map(@Valid @RequestBody YbMapDTO dto) {
        return Result.success(ybMappingService.map(dto));
    }

    /**
     * 解除某院内项目的医保对照关系，直接物理删除映射行。
     * 用于对照错误需要清空重对的环节。
     */
    @PreAuthorize("hasAuthority('finance:insuranceMapping:edit')")
    @Operation(summary = "解对照（物理删）")
    @PostMapping("/unmap")
    public Result<Void> unmap(@RequestParam Integer itemType, @RequestParam Long itemId) {
        ybMappingService.unmap(itemType, itemId);
        return Result.success(null);
    }

    /**
     * 批量自动对照：按名称精确匹配且唯一命中才落映射，命中多条或无命中跳过。
     * 用于对照工作台一键预填可自动确定的映射，itemType 为空则处理全部类型。
     */
    @PreAuthorize("hasAuthority('finance:insuranceMapping:edit')")
    @Operation(summary = "自动对照（名称精确匹配且唯一命中才落；itemType 空=全部）")
    @PostMapping("/autoMatch")
    public Result<YbAutoMatchResultVO> autoMatch(@Valid @RequestBody YbAutoMatchDTO dto) {
        return Result.success(ybMappingService.autoMatch(dto));
    }
}

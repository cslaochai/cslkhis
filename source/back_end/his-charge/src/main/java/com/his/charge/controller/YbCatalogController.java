package com.his.charge.controller;

import com.his.charge.dto.YbCatalogQueryPageDTO;
import com.his.charge.dto.YbCatalogUpsertDTO;
import com.his.charge.service.YbCatalogService;
import com.his.charge.vo.BizYbCatalogVO;
import com.his.charge.vo.YbImportResultVO;
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
 * 国家医保目录控制器（本地模拟目录库；正式对接换前置机下载导入，接口不变）。
 */
@Tag(name = "医保目录对照-目录")
@RestController
@RequestMapping("/charge/ybCatalog")
@RequiredArgsConstructor
public class YbCatalogController {

    private final YbCatalogService catalogService;

    @PreAuthorize("hasAuthority('finance:insuranceMapping:list')")
    @Operation(summary = "目录分页（对照候选弹窗复用本接口）")
    @GetMapping("/listPage")
    public Result<PageResult<BizYbCatalogVO>> listPage(@Valid YbCatalogQueryPageDTO queryDTO) {
        return Result.success(catalogService.listPage(queryDTO));
    }

    @PreAuthorize("hasAuthority('finance:insuranceMapping:add')")
    @Operation(summary = "目录新增/修改（id 空=新增；yb_code 唯一）")
    @PostMapping("/upsert")
    public Result<BizYbCatalogVO> upsert(@Valid @RequestBody YbCatalogUpsertDTO dto) {
        return Result.success(catalogService.upsert(dto));
    }

    @PreAuthorize("hasAuthority('finance:insuranceMapping:add')")
    @Operation(summary = "目录批量导入（按 yb_code 幂等，存在即更新）")
    @PostMapping("/importBatch")
    public Result<YbImportResultVO> importBatch(@RequestBody List<YbCatalogUpsertDTO> items) {
        return Result.success(catalogService.importBatch(items));
    }

    @PreAuthorize("hasAuthority('finance:insuranceMapping:edit')")
    @Operation(summary = "目录启停（停用后不可新对照，已对照关系不受影响）")
    @PostMapping("/changeStatus")
    public Result<Void> changeStatus(@RequestParam Long id, @RequestParam Integer status) {
        catalogService.changeStatus(id, status);
        return Result.success(null);
    }
}

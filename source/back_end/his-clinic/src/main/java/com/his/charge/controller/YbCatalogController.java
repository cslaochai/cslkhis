package com.his.charge.controller;


import com.his.charge.dto.YbCatalogImportDTO;
import com.his.charge.dto.YbCatalogPageQueryDTO;
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

/**
 * 国家医保目录控制器（本地模拟目录库；正式对接换前置机下载导入，接口不变）。
 */
@Tag(name = "医保目录对照-目录")
@RestController
@RequestMapping("/charge/ybCatalog")
@RequiredArgsConstructor
public class YbCatalogController {

    private final YbCatalogService ybCatalogService;

    /**
     * 医保目录分页：按编码与名称模糊、类别与启停状态筛选目录条目。
     * 用于「医保目录对照」页选对照候选的弹窗和目录管理弹窗的列表——目录是对照与医保拆分的依据，本身不动钱。
     */
    @PreAuthorize("hasAuthority('finance:insuranceMapping:list')")
    @Operation(summary = "目录分页（对照候选弹窗复用本接口）")
    @GetMapping("/listPage")
    public Result<PageResult<BizYbCatalogVO>> listPage(@Valid YbCatalogPageQueryDTO queryDTO) {
        return Result.success(ybCatalogService.listPage(queryDTO));
    }

    /**
     * 单条目录新增或修改：无主键即新增，医保编码全局唯一。
     * 用于目录管理弹窗保存手工补录或更正的目录条目。
     */
    @PreAuthorize("hasAuthority('finance:insuranceMapping:add')")
    @Operation(summary = "目录新增/修改（id 空=新增；yb_code 唯一）")
    @PostMapping("/upsert")
    public Result<BizYbCatalogVO> upsert(@Valid @RequestBody YbCatalogUpsertDTO dto) {
        return Result.success(ybCatalogService.upsert(dto));
    }

    /**
     * 目录批量导入：按医保编码幂等，已存在即更新，返回新增与更新的条数。
     * 用于「医保目录对照」页的批量导入弹窗，在铺底或拿到新一期目录后一次性刷新本地库。
     */
    @PreAuthorize("hasAuthority('finance:insuranceMapping:add')")
    @Operation(summary = "目录批量导入（按 yb_code 幂等，存在即更新）")
    @PostMapping("/importBatch")
    public Result<YbImportResultVO> importBatch(@Valid @RequestBody YbCatalogImportDTO importDTO) {
        return Result.success(ybCatalogService.importBatch(importDTO.getItems()));
    }

    /**
     * 目录启停：只停用不删除，停用后不能再被新对照，已建立的对照关系照旧生效。
     * 用于目录管理弹窗的启停操作，保证历史账单按当时对照结果仍可追溯。
     */
    @PreAuthorize("hasAuthority('finance:insuranceMapping:edit')")
    @Operation(summary = "目录启停（停用后不可新对照，已对照关系不受影响）")
    @PostMapping("/changeStatus")
    public Result<Void> changeStatus(@RequestParam Long id, @RequestParam Integer status) {
        ybCatalogService.changeStatus(id, status);
        return Result.success(null);
    }
}

package com.his.pharmacy.controller;

import com.his.common.base.PageResult;
import com.his.common.base.Result;
import com.his.pharmacy.dto.SupplierPageQueryDTO;
import com.his.pharmacy.dto.SupplierUpsertDTO;
import com.his.pharmacy.service.SupplierService;
import com.his.pharmacy.vo.SysSupplierSelectListVO;
import com.his.pharmacy.vo.SysSupplierVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 供应商档案控制器：药品供应商的建档、维护与跨页面下拉取数接口入口。
 */
@Tag(name = "供应商管理")
@RestController
@RequestMapping("/supplier")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('pharmacy:supplier:list')")
public class SupplierController {

    private final SupplierService supplierService;

    /**
     * 供应商分页：按关键字（命中编码、名称、联系人）与状态翻页。
     * 用于药房「供应商管理」页查询卡。
     */
    @Operation(summary = "分页查询供应商")
    @PostMapping("/listPage")
    public Result<PageResult<SysSupplierVO>> listPage(@Valid @RequestBody SupplierPageQueryDTO queryDTO) {
        return Result.success(supplierService.page(queryDTO));
    }

    /**
     * 供应商下拉：只返回启用中的档案，不分页一次给全。
     * 采购建单、库存建批、供应商退货选供应商时共用同一参照数据，因此只要求登录、不挂页面权限码。
     */
    @Operation(summary = "供应商下拉列表（只含启用中的）")
    @PreAuthorize("isAuthenticated()")
    @PostMapping("/selectList")
    public Result<List<SysSupplierSelectListVO>> selectList() {
        return Result.success(supplierService.selectList());
    }

    /**
     * 单个供应商档案详情：按主键取全量字段。
     * 页面编辑目前直接用列表行回显，本接口留给需要跨页面按档案主键补全信息的环节。
     */
    @Operation(summary = "获取供应商详情")
    @GetMapping("/getById")
    public Result<SysSupplierVO> getById(@RequestParam Long supplierId) {
        return Result.success(supplierService.getDetailById(supplierId));
    }

    /**
     * 新增或修改供应商：档案主键为空走新增，保存后返回落库的完整档案。
     * 用于供应商管理页的新增/编辑弹框。
     */
    @PreAuthorize("hasAuthority('pharmacy:supplier:add')")
    @Operation(summary = "新增/修改供应商")
    @PostMapping("/upsert")
    public Result<SysSupplierVO> upsert(@Valid @RequestBody SupplierUpsertDTO upsertDTO) {
        Long supplierId = supplierService.upsert(upsertDTO);
        return Result.success(supplierService.getDetailById(supplierId));
    }

    /**
     * 删除供应商：被采购订单引用过的档案拒绝删除。
     * 用于供应商管理页清掉误建的档案；停止合作应当把状态改成停用，让历史单据还能查到供货商。
     */
    @PreAuthorize("hasAuthority('pharmacy:supplier:delete')")
    @Operation(summary = "删除供应商")
    @DeleteMapping("/deleteById")
    public Result<Void> deleteById(@RequestParam Long supplierId) {
        supplierService.deleteById(supplierId);
        return Result.success();
    }
}

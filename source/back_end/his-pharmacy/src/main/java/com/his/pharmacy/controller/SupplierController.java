package com.his.pharmacy.controller;

import com.his.common.base.PageResult;
import com.his.common.base.Result;
import com.his.pharmacy.dto.SupplierQueryPageDTO;
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
 * 供应商管理控制器
 * <p>
 * 口径：编码唯一；被采购订单引用的供应商不可删除（只能停用）；
 * 写接口不做额外的身份入参 —— 操作人服务端从登录态取。
 */
@Tag(name = "供应商管理")
@RestController
@RequestMapping("/supplier")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('pharmacy:supplier:list')")
public class SupplierController {

    private final SupplierService supplierService;

    @Operation(summary = "分页查询供应商")
    @PostMapping("/listPage")
    public Result<PageResult<SysSupplierVO>> listPage(@Valid @RequestBody SupplierQueryPageDTO queryDTO) {
        return Result.success(supplierService.page(queryDTO));
    }

    @Operation(summary = "供应商下拉列表（只含启用中的）")
    @PreAuthorize("isAuthenticated()")
    @PostMapping("/selectList")
    public Result<List<SysSupplierSelectListVO>> selectList() {
        return Result.success(supplierService.selectList());
    }

    @Operation(summary = "获取供应商详情")
    @GetMapping("/getById")
    public Result<SysSupplierVO> getById(@RequestParam Long supplierId) {
        return Result.success(supplierService.getDetailById(supplierId));
    }

    @PreAuthorize("hasAuthority('pharmacy:supplier:add')")
    @Operation(summary = "新增/修改供应商")
    @PostMapping("/upsert")
    public Result<SysSupplierVO> upsert(@Valid @RequestBody SupplierUpsertDTO upsertDTO) {
        Long supplierId = supplierService.upsert(upsertDTO);
        return Result.success(supplierService.getDetailById(supplierId));
    }

    @PreAuthorize("hasAuthority('pharmacy:supplier:delete')")
    @Operation(summary = "删除供应商")
    @DeleteMapping("/deleteById")
    public Result<Void> deleteById(@RequestParam Long supplierId) {
        supplierService.deleteById(supplierId);
        return Result.success();
    }
}

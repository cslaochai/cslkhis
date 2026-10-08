package com.his.pharmacy.controller;

import com.his.common.base.PageResult;
import com.his.common.base.Result;
import com.his.pharmacy.dto.SupplierReturnActionDTO;
import com.his.pharmacy.dto.SupplierReturnQueryPageDTO;
import com.his.pharmacy.dto.SupplierReturnUpsertDTO;
import com.his.pharmacy.service.SupplierReturnService;
import com.his.pharmacy.vo.SupplierReturnVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * 药品供应商退货（sql/154 ③级）
 */
@Tag(name = "药品供应商退货")
@RestController
@RequestMapping("/pharmacy/supplierReturn")
@RequiredArgsConstructor
public class SupplierReturnController {

    private final SupplierReturnService supplierReturnService;

    @Operation(summary = "退货单分页")
    @PostMapping("/listPage")
    @PreAuthorize("hasAuthority('pharmacy:supplierReturn:list')")
    public Result<PageResult<SupplierReturnVO>> listPage(@Valid @RequestBody SupplierReturnQueryPageDTO query) {
        return Result.success(supplierReturnService.listPage(query));
    }

    @Operation(summary = "退货单详情（含明细与退货出库流水）")
    @GetMapping("/getDetailById")
    @PreAuthorize("hasAuthority('pharmacy:supplierReturn:list')")
    public Result<SupplierReturnVO> getDetailById(@RequestParam Long id) {
        return Result.success(supplierReturnService.getDetailById(id));
    }

    @Operation(summary = "建单 / 改明细（仅待退货）")
    @PostMapping("/upsert")
    @PreAuthorize("hasAuthority('pharmacy:supplierReturn:add')")
    public Result<SupplierReturnVO> upsert(@Valid @RequestBody SupplierReturnUpsertDTO dto) {
        return Result.success(dto.getId() == null ? "退货单已生成" : "退货单已保存", supplierReturnService.upsert(dto));
    }

    @Operation(summary = "确认退货（扣批次库存并落 9-退货出库 流水）")
    @PostMapping("/confirmReturn")
    @PreAuthorize("hasAuthority('pharmacy:supplierReturn:edit')")
    public Result<SupplierReturnVO> confirmReturn(@Valid @RequestBody SupplierReturnActionDTO dto) {
        return Result.success("已退货出库", supplierReturnService.confirmReturn(dto));
    }

    @Operation(summary = "作废（仅待退货）")
    @PostMapping("/cancel")
    @PreAuthorize("hasAuthority('pharmacy:supplierReturn:edit')")
    public Result<SupplierReturnVO> cancel(@Valid @RequestBody SupplierReturnActionDTO dto) {
        return Result.success("退货单已作废", supplierReturnService.cancel(dto));
    }

    @Operation(summary = "删除退货单（仅待退货/已作废）")
    @DeleteMapping("/deleteById")
    @PreAuthorize("hasAuthority('pharmacy:supplierReturn:delete')")
    public Result<Void> deleteById(@RequestParam Long id) {
        supplierReturnService.deleteById(id);
        return Result.success("退货单已删除", null);
    }
}

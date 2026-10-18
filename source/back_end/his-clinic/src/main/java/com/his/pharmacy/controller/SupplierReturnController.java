package com.his.pharmacy.controller;

import com.his.common.base.PageResult;
import com.his.common.base.Result;
import com.his.pharmacy.dto.SupplierReturnActionDTO;
import com.his.pharmacy.dto.SupplierReturnPageQueryDTO;
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

    /**
     * 药品供应商退货单分页，可按单号/药品关键字、供应商与单据状态筛。
     * 用于药库侧「药品供应商退货」页的查询卡与退货单表格。
     */
    @Operation(summary = "退货单分页")
    @PostMapping("/listPage")
    @PreAuthorize("hasAuthority('pharmacy:supplierReturn:list')")
    public Result<PageResult<SupplierReturnVO>> listPage(@Valid @RequestBody SupplierReturnPageQueryDTO query) {
        return Result.success(supplierReturnService.listPage(query));
    }

    /**
     * 取一张退货单的头部信息、退货明细以及本单落下的退货出库流水。
     * 用于退货单页的详情弹框与建单弹框回显，对账时靠它核对退给哪个供应商、退了哪几批。
     */
    @Operation(summary = "退货单详情（含明细与退货出库流水）")
    @GetMapping("/getDetailById")
    @PreAuthorize("hasAuthority('pharmacy:supplierReturn:list')")
    public Result<SupplierReturnVO> getDetailById(@RequestParam Long id) {
        return Result.success(supplierReturnService.getDetailById(id));
    }

    /**
     * 建退货单或整单替换退货明细，只作用于待退货状态；明细批次必须已挂本单供应商且可用量够退。
     * 用于药库侧退货建单弹框，从药库批次候选里挑要退的批号（药房批次不挂供应商，选不到也退不了）。
     */
    @Operation(summary = "建单 / 改明细（仅待退货）")
    @PostMapping("/upsert")
    @PreAuthorize("hasAuthority('pharmacy:supplierReturn:add')")
    public Result<SupplierReturnVO> upsert(@Valid @RequestBody SupplierReturnUpsertDTO dto) {
        return Result.success(dto.getId() == null ? "退货单已生成" : "退货单已保存", supplierReturnService.upsert(dto));
    }

    /**
     * 确认退货：按明细逐批扣减药库批次库存并各落一行退货出库流水，单据转为已退货。
     * 用于药库把实物交回供应商时的出库确认；这一步之后库存真的少了，只能反向入库补回。
     */
    @Operation(summary = "确认退货（扣批次库存并落 9-退货出库 流水）")
    @PostMapping("/confirmReturn")
    @PreAuthorize("hasAuthority('pharmacy:supplierReturn:edit')")
    public Result<SupplierReturnVO> confirmReturn(@Valid @RequestBody SupplierReturnActionDTO dto) {
        return Result.success("已退货出库", supplierReturnService.confirmReturn(dto));
    }

    /**
     * 作废一张还没退货的退货单，单据留在台账里可查但不再能确认。
     * 用于退货单页行内作废；已经确认出库的单作废不了，药要回来得反向入库。
     */
    @Operation(summary = "作废（仅待退货）")
    @PostMapping("/cancel")
    @PreAuthorize("hasAuthority('pharmacy:supplierReturn:edit')")
    public Result<SupplierReturnVO> cancel(@Valid @RequestBody SupplierReturnActionDTO dto) {
        return Result.success("退货单已作废", supplierReturnService.cancel(dto));
    }

    /**
     * 删除退货单及其明细，仅待退货与已作废两种状态可删。
     * 用于清掉建错的退货单；已退货出库的单子是库存事实，必须留在台账里对账。
     */
    @Operation(summary = "删除退货单（仅待退货/已作废）")
    @DeleteMapping("/deleteById")
    @PreAuthorize("hasAuthority('pharmacy:supplierReturn:delete')")
    public Result<Void> deleteById(@RequestParam Long id) {
        supplierReturnService.deleteById(id);
        return Result.success("退货单已删除", null);
    }
}

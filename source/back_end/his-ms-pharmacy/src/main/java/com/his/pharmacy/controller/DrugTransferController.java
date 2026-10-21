package com.his.pharmacy.controller;

import com.his.common.base.PageResult;
import com.his.common.base.Result;
import com.his.pharmacy.dto.DrugTransferActionDTO;
import com.his.pharmacy.dto.DrugTransferPageQueryDTO;
import com.his.pharmacy.dto.DrugTransferUpsertDTO;
import com.his.pharmacy.service.DrugTransferService;
import com.his.pharmacy.vo.DrugTransferVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * 药品调拨控制器：药库与药房之间按批次搬货的建单、发出、接收、作废接口入口（sql/154 ②级）。
 */
@Tag(name = "药品调拨")
@RestController
@RequestMapping("/pharmacy/drugTransfer")
@RequiredArgsConstructor
public class DrugTransferController {

    private final DrugTransferService drugTransferService;

    /**
     * 调拨单分页：按单号、方向、状态等条件翻页，列表行只给批次与数量合计。
     * 用于药房「药品调拨（药库 ↔ 药房）」页查询卡。
     */
    @Operation(summary = "调拨单分页")
    @PostMapping("/listPage")
    @PreAuthorize("hasAuthority('pharmacy:drugTransfer:list')")
    public Result<PageResult<DrugTransferVO>> listPage(@Valid @RequestBody DrugTransferPageQueryDTO query) {
        return Result.success(drugTransferService.listPage(query));
    }

    /**
     * 调拨单详情：主单 + 明细批次 + 本单名下的两行库存流水（发出为负、接收为正，合计 0 即账平）。
     * 用于调拨详情弹框，也是确认发出、确认接收前后核对数量的取数口。
     */
    @Operation(summary = "调拨单详情（含明细与两行库存流水）")
    @GetMapping("/getDetailById")
    @PreAuthorize("hasAuthority('pharmacy:drugTransfer:list')")
    public Result<DrugTransferVO> getDetailById(@RequestParam Long id) {
        return Result.success(drugTransferService.getDetailById(id));
    }

    /**
     * 建调拨单或改明细：新建时按选中的发出方批次抓批号、成本价、效期快照，改单只放行「待发出」。
     * 用于药品调拨页的新增/编辑弹框，药库下拨药房与药房退回药库两个方向共用本接口。
     */
    @Operation(summary = "建单（按批次抓快照）/ 改明细（仅待发出）")
    @PostMapping("/upsert")
    @PreAuthorize("hasAuthority('pharmacy:drugTransfer:add')")
    public Result<DrugTransferVO> upsert(@Valid @RequestBody DrugTransferUpsertDTO dto) {
        return Result.success(dto.getId() == null ? "调拨单已生成" : "调拨单已保存", drugTransferService.upsert(dto));
    }

    /**
     * 确认发出：从发出库位扣减批次可用量并落一条调拨出库流水，单据转「待接收」。
     * 用于调拨页对「待发出」单的执行——药库下拨时发出方是药库，药房退回时发出方是药房。
     */
    @Operation(summary = "确认发出（扣发出库位并落 7-调拨出库 流水）")
    @PostMapping("/confirmOut")
    @PreAuthorize("hasAuthority('pharmacy:drugTransfer:edit')")
    public Result<DrugTransferVO> confirmOut(@Valid @RequestBody DrugTransferActionDTO dto) {
        return Result.success("已发出，等待对方库位接收", drugTransferService.confirmOut(dto));
    }

    /**
     * 确认接收：把已发出的数量落到接收库位的批次并补一条调拨入库流水，本单转「已完成」。
     * 用于接收方库位在调拨页执行「待接收」单；药房侧的批次只能由这一步进来。
     */
    @Operation(summary = "确认接收（落到接收库位并落 8-调拨入库 流水）")
    @PostMapping("/confirmIn")
    @PreAuthorize("hasAuthority('pharmacy:drugTransfer:edit')")
    public Result<DrugTransferVO> confirmIn(@Valid @RequestBody DrugTransferActionDTO dto) {
        return Result.success("已接收，本单调拨完成", drugTransferService.confirmIn(dto));
    }

    /**
     * 作废调拨单：仅「待发出」可作废，不动任何库位的库存也不产生流水。
     * 用于调拨页撤销填错的单子；已在途的单只能再开一张反向调拨把货搬回去。
     */
    @Operation(summary = "作废（仅待发出）")
    @PostMapping("/cancel")
    @PreAuthorize("hasAuthority('pharmacy:drugTransfer:edit')")
    public Result<DrugTransferVO> cancel(@Valid @RequestBody DrugTransferActionDTO dto) {
        return Result.success("调拨单已作废", drugTransferService.cancel(dto));
    }

    /**
     * 删除调拨单：仅「待发出」「已作废」可删，主单与明细一并物理清除。
     * 用于调拨页清理误建的单子；已完成与在途的单据是留档凭证，删不掉。
     */
    @Operation(summary = "删除调拨单（仅待发出/已作废）")
    @DeleteMapping("/deleteById")
    @PreAuthorize("hasAuthority('pharmacy:drugTransfer:delete')")
    public Result<Void> deleteById(@RequestParam Long id) {
        drugTransferService.deleteById(id);
        return Result.success("调拨单已删除", null);
    }
}

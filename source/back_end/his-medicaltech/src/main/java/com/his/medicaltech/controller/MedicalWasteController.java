package com.his.medicaltech.controller;

import com.his.common.base.PageResult;
import com.his.common.base.Result;
import com.his.medicaltech.dto.WasteDTO;
import com.his.medicaltech.service.WasteService;
import com.his.medicaltech.vo.WasteVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * 医疗废物登记控制器。登记 → 交接 → 处置三态闭环；已交接后禁删。
 */
@Tag(name = "医疗废物登记")
@RestController
@RequestMapping("/waste/waste")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('asset:waste:list')")
public class MedicalWasteController {

    private final WasteService wasteService;

    /**
     * 医废登记：为科室产生的一批医疗废物建一条交接单，落「已登记」态。
     * 用于医废管理页查询卡右侧「医废登记」弹框提交。
     */
    @PreAuthorize("hasAuthority('asset:waste:add')")
    @Operation(summary = "医废登记")
    @PostMapping("/create")
    public Result<WasteVO> create(@Valid @RequestBody WasteDTO.Create dto) {
        return Result.success("医废登记成功", wasteService.create(dto));
    }

    /**
     * 医废交接：登记交接人，状态由已登记推进到已交接，此后不可逆。
     * 用于医废管理页行内「交接」弹框确认。
     */
    @PreAuthorize("hasAuthority('asset:waste:edit')")
    @Operation(summary = "医废交接（1→2）")
    @PostMapping("/handover")
    public Result<WasteVO> handover(@Valid @RequestBody WasteDTO.Handover dto) {
        return Result.success("交接成功", wasteService.handover(dto));
    }

    /**
     * 处置确认：登记处置公司，状态推进到已处置，闭环结束。
     * 用于医废管理页行内「处置确认」弹框。
     */
    @PreAuthorize("hasAuthority('asset:waste:edit')")
    @Operation(summary = "处置确认（2→3）")
    @PostMapping("/dispose")
    public Result<WasteVO> dispose(@Valid @RequestBody WasteDTO.Dispose dto) {
        return Result.success("处置确认成功", wasteService.dispose(dto));
    }

    /**
     * 删除医废登记：仅未交接的登记可删，已交接的单子是与处置公司的对外凭证。
     * 用于医废管理页「已登记」行的删除操作。
     */
    @PreAuthorize("hasAuthority('asset:waste:delete')")
    @Operation(summary = "医废登记删除（仅已登记状态可删）")
    @DeleteMapping("/deleteById")
    public Result<Void> deleteById(@RequestParam Long id) {
        wasteService.deleteById(id);
        return Result.success("删除成功", null);
    }

    /**
     * 医废登记分页查询：按关键字、医废类别、状态与收集日期区间收口。
     * 用于医废管理页查询卡与表格列表。
     */
    @Operation(summary = "医废登记分页查询")
    @PostMapping("/listPage")
    public Result<PageResult<WasteVO>> listPage(@Valid @RequestBody WasteDTO.PageQuery dto) {
        var page = wasteService.listPage(dto);
        return Result.success(PageResult.of(page.getTotal(), page.getCurrent(), page.getSize(), page.getPages(),
                page.getRecords()));
    }
}

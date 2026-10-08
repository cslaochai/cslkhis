package com.his.operation.controller;

import com.his.common.base.Result;
import com.his.operation.dto.OperationRoomUpsertDTO;
import com.his.operation.service.OperationRoomService;
import com.his.operation.vo.OperationRoomVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 手术间主数据端点
 */
@Tag(name = "手术间主数据")
@RestController
@RequestMapping("/patient/inpatient/operationRoom")
@RequiredArgsConstructor
public class OperationRoomController {

    private final OperationRoomService operationRoomService;

    @PreAuthorize("hasAnyAuthority('ipd:surgery:list', 'ipd:anesthesia:list')")
    @Operation(summary = "全部手术间（含停用；手术间管理表格用）")
    @GetMapping("/listAll")
    public Result<List<OperationRoomVO>> listAll() {
        return Result.success(operationRoomService.listAll());
    }

    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "启用中的手术间（排台下拉候选，通用参照数据不配权限码）")
    @GetMapping("/selectList")
    public Result<List<OperationRoomVO>> selectList() {
        return Result.success(operationRoomService.selectEnabled());
    }

    @PreAuthorize("hasAuthority('ipd:surgery:add')")
    @Operation(summary = "新增/修改手术间（编码与名称全局唯一）")
    @PostMapping("/upsert")
    public Result<String> upsert(@RequestBody @Valid OperationRoomUpsertDTO dto) {
        return Result.success("手术间已保存", operationRoomService.upsert(dto));
    }

    @PreAuthorize("hasAuthority('ipd:surgery:delete')")
    @Operation(summary = "删除手术间（物理删；历史申请单是文本快照不受影响）")
    @DeleteMapping("/deleteById")
    public Result<Void> deleteById(@RequestParam Long id) {
        operationRoomService.deleteById(id);
        return Result.success("手术间已删除", null);
    }
}

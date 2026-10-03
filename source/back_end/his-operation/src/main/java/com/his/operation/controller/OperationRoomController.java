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
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 手术间主数据端点（sql/134）。
 *
 * <p>鉴权口径（AGENTS §4）：注解只标方法不标类；{@code selectList} 是排台下拉的
 * 通用参照数据，只要求登录，不挂页面权限码（否则非管理岗一进排台弹窗下拉就 403）。
 */
@Tag(name = "手术间主数据")
@RestController
@RequestMapping("/patient/inpatient/operationRoom")
@RequiredArgsConstructor
public class OperationRoomController {

    private final OperationRoomService roomService;

    @PreAuthorize("hasAnyAuthority('ipd:surgery:list', 'ipd:anesthesia:list')")
    @Operation(summary = "全部手术间（含停用；手术间管理表格用）")
    @GetMapping("/listAll")
    public Result<List<OperationRoomVO>> listAll() {
        return Result.success(roomService.listAll());
    }

    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "启用中的手术间（排台下拉候选，通用参照数据不配权限码）")
    @GetMapping("/selectList")
    public Result<List<OperationRoomVO>> selectList() {
        return Result.success(roomService.selectEnabled());
    }

    @PreAuthorize("hasAuthority('ipd:surgery:add')")
    @Operation(summary = "新增/修改手术间（编码与名称全局唯一）")
    @PostMapping("/upsert")
    public Result<String> upsert(@RequestBody @Valid OperationRoomUpsertDTO dto) {
        return Result.success("手术间已保存", roomService.upsert(dto));
    }

    @PreAuthorize("hasAuthority('ipd:surgery:delete')")
    @Operation(summary = "删除手术间（物理删；历史申请单是文本快照不受影响）")
    @DeleteMapping("/deleteById")
    public Result<Void> deleteById(@RequestParam Long id) {
        roomService.deleteById(id);
        return Result.success("手术间已删除", null);
    }
}

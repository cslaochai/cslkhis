package com.his.miniapp.controller;

import com.his.common.base.PageResult;
import com.his.common.base.Result;
import com.his.miniapp.dto.TicketHandleDTO;
import com.his.miniapp.dto.TicketSearchDTO;
import com.his.miniapp.service.MiniServiceTicketAdminService;
import com.his.miniapp.vo.MiniServiceMessageListVO;
import com.his.miniapp.vo.MiniServiceDetailVO;
import com.his.miniapp.vo.MiniTicketStatsVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * 院内工单受理（客服工作台）。
 */
@Tag(name = "院内-工单受理")
@RestController
@RequestMapping("/miniapp/service/admin")
@RequiredArgsConstructor
public class MiniServiceAdminController {

    private final MiniServiceTicketAdminService miniServiceTicketAdminService;

    @Operation(summary = "工单列表（待受理优先排序）")
    @PostMapping("/listPage")
    @PreAuthorize("hasAuthority('service:ticket:list')")
    public Result<PageResult<MiniServiceMessageListVO>> listPage(@Valid @RequestBody TicketSearchDTO dto) {
        return Result.success(miniServiceTicketAdminService.adminPage(dto));
    }

    @Operation(summary = "工作台统计（待受理 / 处理中 / 已办结 / 超时未受理）")
    @PostMapping("/stats")
    @PreAuthorize("hasAuthority('service:ticket:list')")
    public Result<MiniTicketStatsVO> stats() {
        return Result.success(miniServiceTicketAdminService.stats());
    }

    @Operation(summary = "工单详情（含全量流转记录，含内部备注）")
    @GetMapping("/getById")
    @PreAuthorize("hasAuthority('service:ticket:list')")
    public Result<MiniServiceDetailVO> getById(@RequestParam Long id) {
        return Result.success(miniServiceTicketAdminService.detail(id));
    }

    @Operation(summary = "受理 / 回复 / 办结 / 关闭 / 内部备注（操作人取登录人）")
    @PostMapping("/handle")
    @PreAuthorize("hasAuthority('service:ticket:handle')")
    public Result<Integer> handle(@RequestBody @Valid TicketHandleDTO dto) {
        miniServiceTicketAdminService.handle(dto);
        return Result.success(1);
    }
}

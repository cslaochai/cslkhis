package com.his.miniapp.controller;

import com.his.common.base.PageResult;
import com.his.common.base.Result;
import com.his.miniapp.dto.TicketHandleDTO;
import com.his.miniapp.dto.TicketSearchDTO;
import com.his.miniapp.service.MiniappServiceTicketAdminService;
import com.his.miniapp.vo.ServiceMessageListVO;
import com.his.miniapp.vo.ServiceTicketDetailVO;
import com.his.miniapp.vo.TicketStatsVO;
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
public class MiniappServiceTicketAdminController {

    private final MiniappServiceTicketAdminService miniappServiceTicketAdminService;

    @Operation(summary = "工单列表（待受理优先排序）")
    @PostMapping("/listPage")
    @PreAuthorize("hasAuthority('service:ticket:list')")
    public Result<PageResult<ServiceMessageListVO>> listPage(@Valid @RequestBody TicketSearchDTO dto) {
        return Result.success(miniappServiceTicketAdminService.adminPage(dto));
    }

    @Operation(summary = "工作台统计（待受理 / 处理中 / 已办结 / 超时未受理）")
    @PostMapping("/stats")
    @PreAuthorize("hasAuthority('service:ticket:list')")
    public Result<TicketStatsVO> stats() {
        return Result.success(miniappServiceTicketAdminService.stats());
    }

    @Operation(summary = "工单详情（含全量流转记录，含内部备注）")
    @GetMapping("/getById")
    @PreAuthorize("hasAuthority('service:ticket:list')")
    public Result<ServiceTicketDetailVO> getById(@RequestParam Long id) {
        return Result.success(miniappServiceTicketAdminService.detail(id));
    }

    @Operation(summary = "受理 / 回复 / 办结 / 关闭 / 内部备注（操作人取登录人）")
    @PostMapping("/handle")
    @PreAuthorize("hasAuthority('service:ticket:handle')")
    public Result<Integer> handle(@RequestBody @Valid TicketHandleDTO dto) {
        miniappServiceTicketAdminService.handle(dto);
        return Result.success(1);
    }
}

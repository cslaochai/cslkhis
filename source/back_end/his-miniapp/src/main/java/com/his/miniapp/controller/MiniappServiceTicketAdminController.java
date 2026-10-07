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
 *
 * <p><b>为什么必须有这一端</b>：sql/216 只有患者能留言、没有受理端，
 * 那是半截闭环 —— 留言落库了，但没有人对它负责，患者也不知道有没有人看。
 * 这一端把「谁接了这张单、什么时候办的、办了什么」变成可追的记录。
 *
 * <p>鉴权与患者端分开：患者端只认 PATIENT，这里只认 {@code service:ticket:*}。
 */
@Tag(name = "院内-工单受理")
@RestController
@RequestMapping("/miniapp/service/admin")
@RequiredArgsConstructor
public class MiniappServiceTicketAdminController {

    private final MiniappServiceTicketAdminService ticketAdminService;

    @Operation(summary = "工单列表（待受理优先排序）")
    @PostMapping("/listPage")
    @PreAuthorize("hasAuthority('service:ticket:list')")
    public Result<PageResult<ServiceMessageListVO>> listPage(@Valid @RequestBody TicketSearchDTO dto) {
        return Result.success(ticketAdminService.adminPage(dto));
    }

    @Operation(summary = "工作台统计（待受理 / 处理中 / 已办结 / 超时未受理）")
    @PostMapping("/stats")
    @PreAuthorize("hasAuthority('service:ticket:list')")
    public Result<TicketStatsVO> stats() {
        return Result.success(ticketAdminService.stats());
    }

    @Operation(summary = "工单详情（含全量流转记录，含内部备注）")
    @GetMapping("/getById")
    @PreAuthorize("hasAuthority('service:ticket:list')")
    public Result<ServiceTicketDetailVO> getById(@RequestParam Long id) {
        return Result.success(ticketAdminService.detail(id));
    }

    @Operation(summary = "受理 / 回复 / 办结 / 关闭 / 内部备注（操作人取登录人）")
    @PostMapping("/handle")
    @PreAuthorize("hasAuthority('service:ticket:handle')")
    public Result<Integer> handle(@RequestBody @Valid TicketHandleDTO dto) {
        ticketAdminService.handle(dto);
        return Result.success(1);
    }
}

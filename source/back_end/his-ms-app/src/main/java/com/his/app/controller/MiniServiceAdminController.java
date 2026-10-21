package com.his.app.controller;

import com.his.common.base.PageResult;
import com.his.common.base.Result;
import com.his.app.dto.TicketHandleUpsertDTO;
import com.his.app.dto.TicketPageQueryDTO;
import com.his.app.service.MiniServiceTicketAdminService;
import com.his.app.vo.MiniServiceMessageListVO;
import com.his.app.vo.MiniServiceDetailVO;
import com.his.app.vo.MiniTicketStatsVO;
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

    /**
     * 工单分页：按条件检索患者提交的客服工单，待受理的排在前面。
     * 用于院内「工单受理」客服工作台的工单清单。
     */
    @Operation(summary = "工单列表（待受理优先排序）")
    @PostMapping("/listPage")
    @PreAuthorize("hasAuthority('service:ticket:list')")
    public Result<PageResult<MiniServiceMessageListVO>> listPage(@Valid @RequestBody TicketPageQueryDTO pageQueryDTO) {
        return Result.success(miniServiceTicketAdminService.listPage(pageQueryDTO));
    }

    /**
     * 工单统计：按待受理、处理中、已办结、超时未受理四个口径汇总条数。
     * 用于院内「工单受理」工作台顶部的状态概览卡。
     */
    @Operation(summary = "工作台统计（待受理 / 处理中 / 已办结 / 超时未受理）")
    @PostMapping("/stats")
    @PreAuthorize("hasAuthority('service:ticket:list')")
    public Result<MiniTicketStatsVO> stats() {
        return Result.success(miniServiceTicketAdminService.stats());
    }

    /**
     * 工单详情：取一张工单的完整内容与全部流转记录，受理侧可见内部备注。
     * 用于院内「工单受理」工作台打开某张工单时的处理面板。
     */
    @Operation(summary = "工单详情（含全量流转记录，含内部备注）")
    @GetMapping("/getById")
    @PreAuthorize("hasAuthority('service:ticket:list')")
    public Result<MiniServiceDetailVO> getById(@RequestParam Long id) {
        return Result.success(miniServiceTicketAdminService.detail(id));
    }

    /**
     * 工单受理处理：一次接口承载受理、回复、办结、关闭与内部备注，操作人取当前登录人。
     * 用于院内「工单受理」工作台处理面板上的各动作按钮。
     */
    @Operation(summary = "受理 / 回复 / 办结 / 关闭 / 内部备注（操作人取登录人）")
    @PostMapping("/handle")
    @PreAuthorize("hasAuthority('service:ticket:handle')")
    public Result<Integer> handle(@RequestBody @Valid TicketHandleUpsertDTO upsertDTO) {
        miniServiceTicketAdminService.handle(upsertDTO);
        return Result.success(1);
    }
}

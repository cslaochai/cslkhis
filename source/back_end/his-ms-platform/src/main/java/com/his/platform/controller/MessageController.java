package com.his.platform.controller;

import com.his.common.base.PageResult;
import com.his.common.base.Result;
import com.his.platform.dto.MessagePageQueryDTO;
import com.his.platform.service.SysMessageService;
import com.his.platform.vo.MessageTypeCountVO;
import com.his.platform.vo.SysMessageVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;

/**
 * 消息通知控制器
 */
@Tag(name = "消息通知")
@RestController
@RequestMapping("/system/message")
@RequiredArgsConstructor
public class MessageController {

    private final SysMessageService sysMessageService;

    /**
     * 建立站内信实时推送长连接，新消息到达即下推一帧（含最新未读数）。
     * 用于顶栏消息铃铛；连接断开后由前端退回未读数轮询。
     * 通用能力（顶栏铃铛基础功能），登录即可用：类级权限注解会静默覆盖所有方法，
     * 未授权角色连未读数都 403（AGENTS.md §8.1 G5b 反模式），故按方法标注。
     */
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "SSE 实时推送连接（新消息即时通知，鉴权走 Authorization 头）")
    @GetMapping(value = "/sse", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter sse() {
        return sysMessageService.subscribe();
    }

    /**
     * 当前登录者的未读消息条数。
     * 用于顶栏铃铛徽标初值，以及实时推送断线后 5 秒轮询刷新。
     */
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "查询未读消息数量")
    @GetMapping("/unread/count")
    public Result<Integer> unreadCount() {
        return Result.success(sysMessageService.countUnreadOfCurrentUser());
    }

    /**
     * 当前登录者的消息分页。
     * 用于消息中心页列表，以及顶栏消息抽屉的首屏取数。
     */
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "查询消息列表")
    @GetMapping("/listPage")
    public Result<PageResult<SysMessageVO>> listPage(@Valid MessagePageQueryDTO queryDTO) {
        return Result.success(sysMessageService.queryMessagePage(queryDTO));
    }

    /**
     * 当前登录者消息按业务类型的分组计数。
     * 供消息列表按类型页签渲染各自的数量徽标。
     */
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "按业务类型分组统计消息数（抽屉 Tab 徽标用）")
    @GetMapping("/typeCounts")
    public Result<List<MessageTypeCountVO>> typeCounts() {
        return Result.success(sysMessageService.typeCountsOfCurrentUser());
    }

    /**
     * 标记单条消息为已读。
     * 用于消息中心页与工作台的阅读处理、顶栏抽屉点开某条消息时回写。
     */
    @PreAuthorize("hasAuthority('portal:messages:edit')")
    @Operation(summary = "标记消息已读")
    @PostMapping("/read")
    public Result<Void> read(@RequestParam Long messageId) {
        sysMessageService.markRead(messageId);
        return Result.success("标记成功", null);
    }

    /**
     * 把当前登录者全部未读消息一次置为已读。
     * 供消息列表的「全部已读」动作回写阅读状态。
     */
    @PreAuthorize("hasAuthority('portal:messages:edit')")
    @Operation(summary = "标记所有消息已读")
    @PostMapping("/readAll")
    public Result<Void> readAll() {
        sysMessageService.markAllRead();
        return Result.success("全部已读", null);
    }
}

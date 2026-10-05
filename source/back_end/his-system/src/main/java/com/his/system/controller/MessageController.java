package com.his.system.controller;

import com.his.common.base.PageResult;
import com.his.common.base.Result;
import com.his.system.dto.MessageQueryPageDTO;
import com.his.system.service.SysMessageService;
import com.his.system.vo.MessageTypeCountVO;
import com.his.system.vo.SysMessageVO;
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
 * <p>
 * 收件人口径（站内信存的是员工身份，不是用户身份）在 {@link SysMessageService} 一处实现，
 * 本控制器不传身份。
 */
@Tag(name = "消息通知")
@RestController
@RequestMapping("/system/message")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('portal:messages:view')")
public class MessageController {

    private final SysMessageService messageService;

    @Operation(summary = "SSE 实时推送连接（新消息即时通知，鉴权走 Authorization 头）")
    @GetMapping(value = "/sse", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter sse() {
        // 注意：返回值刻意不是 Result —— SSE 响应体是事件流，包 {code,data} 壳会让前端流解析失败
        return messageService.subscribe();
    }

    @Operation(summary = "查询未读消息数量")
    @GetMapping("/unread/count")
    public Result<Integer> unreadCount() {
        return Result.success(messageService.countUnreadOfCurrentUser());
    }

    @Operation(summary = "查询消息列表")
    @GetMapping("/listPage")
    public Result<PageResult<SysMessageVO>> listPage(@Valid MessageQueryPageDTO queryDTO) {
        return Result.success(messageService.queryMessagePage(queryDTO));
    }

    @Operation(summary = "按业务类型分组统计消息数（抽屉 Tab 徽标用）")
    @GetMapping("/typeCounts")
    public Result<List<MessageTypeCountVO>> typeCounts() {
        return Result.success(messageService.typeCountsOfCurrentUser());
    }

    @PreAuthorize("hasAuthority('portal:messages:edit')")
    @Operation(summary = "标记消息已读")
    @PostMapping("/read")
    public Result<Void> read(@RequestParam Long messageId) {
        messageService.markRead(messageId);
        return Result.success("标记成功", null);
    }

    @PreAuthorize("hasAuthority('portal:messages:edit')")
    @Operation(summary = "标记所有消息已读")
    @PostMapping("/readAll")
    public Result<Void> readAll() {
        messageService.markAllRead();
        return Result.success("全部已读", null);
    }
}

package com.his.miniapp.controller;

import com.his.common.base.PageResult;
import com.his.common.base.Result;
import com.his.miniapp.service.MiniappMessageService;
import com.his.miniapp.vo.MessageListVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 患者端消息中心（小程序二期）。
 * 口径：只看发给当前登录患者的消息，站内信通道与微信场景留痕均可见。
 */
@Tag(name = "患者端-消息中心")
@RestController
@RequestMapping("/miniapp/message")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('PATIENT')")
public class MiniappMessageController {

    private final MiniappMessageService messageService;

    @Operation(summary = "我的消息（分页）")
    @PostMapping("/listPage")
    public Result<PageResult<MessageListVO>> listPage(@RequestBody MessagePageDTO dto) {
        return Result.success(messageService.myPage(dto.getPageNum(), dto.getPageSize()));
    }

    @Operation(summary = "未读数（角标）")
    @GetMapping("/unreadCount")
    public Result<Long> unreadCount() {
        return Result.success(messageService.unreadCount());
    }

    @Operation(summary = "标记已读（只允许标自己的消息）")
    @PostMapping("/markRead")
    public Result<Integer> markRead(@RequestBody @Valid MarkReadDTO dto) {
        return Result.success(messageService.markRead(dto.getMessageIds()));
    }

    @Data
    public static class MessagePageDTO {
        private Integer pageNum = 1;
        private Integer pageSize = 10;
    }

    @Data
    public static class MarkReadDTO {
        @NotEmpty(message = "messageIds不能为空")
        private List<String> messageIds;
    }
}

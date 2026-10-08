package com.his.miniapp.controller;

import com.his.common.base.PageResult;
import com.his.common.base.Result;
import com.his.miniapp.dto.MarkReadDTO;
import com.his.miniapp.dto.MessagePageDTO;
import com.his.miniapp.service.MiniappMessageService;
import com.his.miniapp.vo.MessageListVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;


/**
 * 患者端消息中心
 */
@Tag(name = "患者端-消息中心")
@RestController
@RequestMapping("/miniapp/message")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('PATIENT')")
public class MiniappMessageController {

    private final MiniappMessageService miniappMessageService;

    @Operation(summary = "我的消息（分页）")
    @PostMapping("/listPage")
    public Result<PageResult<MessageListVO>> listPage(@Valid @RequestBody MessagePageDTO dto) {
        return Result.success(miniappMessageService.myPage(dto));
    }

    @Operation(summary = "未读数（角标）")
    @GetMapping("/unreadCount")
    public Result<Long> unreadCount() {
        return Result.success(miniappMessageService.unreadCount());
    }

    @Operation(summary = "标记已读（只允许标自己的消息）")
    @PostMapping("/markRead")
    public Result<Integer> markRead(@RequestBody @Valid MarkReadDTO dto) {
        return Result.success(miniappMessageService.markRead(dto.getMessageIds()));
    }


}

package com.his.miniapp.controller;

import com.his.common.base.PageResult;
import com.his.common.base.Result;
import com.his.miniapp.dto.MarkReadDTO;
import com.his.miniapp.dto.MessagePageDTO;
import com.his.miniapp.service.MiniMessageService;
import com.his.miniapp.vo.MiniMessageListVO;
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
public class MiniMessageController {

    private final MiniMessageService miniMessageService;

    @Operation(summary = "我的消息（分页）")
    @PostMapping("/listPage")
    public Result<PageResult<MiniMessageListVO>> listPage(@Valid @RequestBody MessagePageDTO dto) {
        return Result.success(miniMessageService.myPage(dto));
    }

    @Operation(summary = "未读数（角标）")
    @GetMapping("/unreadCount")
    public Result<Long> unreadCount() {
        return Result.success(miniMessageService.unreadCount());
    }

    @Operation(summary = "标记已读（只允许标自己的消息）")
    @PostMapping("/markRead")
    public Result<Integer> markRead(@RequestBody @Valid MarkReadDTO dto) {
        return Result.success(miniMessageService.markRead(dto.getMessageIds()));
    }


}

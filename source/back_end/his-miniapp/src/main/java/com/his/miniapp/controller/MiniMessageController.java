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

    /**
     * 我的消息分页：当前登录患者收到的站内信与微信触达留痕，按发送时间倒序。
     * 用于小程序「我的消息」页的列表加载与上拉翻页。
     */
    @Operation(summary = "我的消息（分页）")
    @PostMapping("/listPage")
    public Result<PageResult<MiniMessageListVO>> listPage(@Valid @RequestBody MessagePageDTO dto) {
        return Result.success(miniMessageService.myPage(dto));
    }

    /**
     * 未读消息数：只统计当前登录患者名下未读的消息条数。
     * 供消息入口的未读角标展示；当前小程序页面尚未接该取数，按未读提醒流程预留。
     */
    @Operation(summary = "未读数（角标）")
    @GetMapping("/unreadCount")
    public Result<Long> unreadCount() {
        return Result.success(miniMessageService.unreadCount());
    }

    /**
     * 标记消息已读：批量把消息置为已读，只允许标本人名下的消息。
     * 用于小程序「我的消息」页拉到列表后对未读条目自动消读。
     */
    @Operation(summary = "标记已读（只允许标自己的消息）")
    @PostMapping("/markRead")
    public Result<Integer> markRead(@RequestBody @Valid MarkReadDTO dto) {
        return Result.success(miniMessageService.markRead(dto.getMessageIds()));
    }


}

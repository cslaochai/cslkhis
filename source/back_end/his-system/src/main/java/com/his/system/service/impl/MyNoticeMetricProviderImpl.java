package com.his.system.service.impl;

import com.his.system.service.MyNoticeMetricProvider;
import com.his.system.service.MessageMetricSupport;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.his.system.entity.CurrentUser;
import com.his.system.provider.WorkbenchMetricProvider;
import com.his.system.entity.SysMessage;
import com.his.system.service.SysMessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import com.his.system.service.MyTodoMetricProvider;

/**
 * 卡片 {@code myNotice}：我未读的通知型站内信。
 *
 * <p>与 {@link MyTodoMetricProvider} 分两张卡、不合并成一个数字：通知型 {@code handle_status}
 * 为 NULL、靠 {@code read_status} 闭环，两类混在一起计数会让数字失去意义。
 *
 * <p>口径与顶栏未读数的差异（刻意）：{@code /system/message/unread/count} 统计所有
 * {@code read_status=0}（含待办型），本卡只统计通知型，所以本卡数字通常小于顶栏角标。
 */
@Service
@RequiredArgsConstructor
public class MyNoticeMetricProviderImpl implements WorkbenchMetricProvider, MyNoticeMetricProvider {

    private static final int TOP_N = 8;

    private final SysMessageService messageService;

    @Override
    public String widgetCode() {
        return "myNotice";
    }

    @Override
    public Map<String, Object> summary(CurrentUser user) {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("total", 0);
        data.put("items", Collections.emptyList());

        Long receiverId = MessageMetricSupport.receiverId(user);
        if (receiverId == null) {
            return data;
        }
        data.put("total", messageService.count(unreadNotice(receiverId)));
        List<SysMessage> rows = messageService.list(unreadNotice(receiverId)
                .last("ORDER BY FIELD(severity, 'urgent', 'warning', 'info'), send_time DESC, message_id DESC LIMIT " + TOP_N));
        data.put("items", rows.stream().map(MessageMetricSupport::of).collect(Collectors.toList()));
        return data;
    }

    private LambdaQueryWrapper<SysMessage> unreadNotice(Long receiverId) {
        return new LambdaQueryWrapper<SysMessage>()
                .eq(SysMessage::getReceiverId, receiverId)
                .eq(SysMessage::getSendStatus, 1)
                .isNull(SysMessage::getHandleStatus)
                .eq(SysMessage::getReadStatus, 0);
    }
}

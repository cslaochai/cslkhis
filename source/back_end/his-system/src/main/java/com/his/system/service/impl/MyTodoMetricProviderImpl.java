package com.his.system.service.impl;

import com.his.system.service.MyTodoMetricProvider;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.his.security.entity.CurrentUser;
import com.his.security.provider.WorkbenchMetricProvider;
import com.his.system.entity.SysMessage;
import com.his.system.service.SysMessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import com.his.system.service.MessageMetricSupport;

/**
 * 卡片 {@code myTodo}：我当前未办结的站内信（待办型）。
 *
 * <p>「待办 vs 通知」在后端没有 kind 列，靠 {@code handle_status} 区分：待办型写入时带
 * {@code handle_status=0}，通知型该列为 NULL（见 {@code SysMessage#handleStatus} 注释）。
 * 所以这里 {@code eq(handle_status,0)} 天然只捞待办，不会把 145 条通知污染进数字。
 * 展示名/归属岗位在前端 {@code lib/messageCatalog.js} 单点定义，本卡不复制映射。
 */
@Service
@RequiredArgsConstructor
public class MyTodoMetricProviderImpl implements WorkbenchMetricProvider, MyTodoMetricProvider {

    /** 卡片只给前 N 条，"查看全部"走消息抽屉的 listPage */
    private static final int TOP_N = 8;

    private final SysMessageService messageService;

    @Override
    public String widgetCode() {
        return "myTodo";
    }

    @Override
    public Map<String, Object> summary(CurrentUser user) {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("total", 0);
        data.put("urgentTotal", 0);
        data.put("items", Collections.emptyList());

        Long receiverId = MessageMetricSupport.receiverId(user);
        if (receiverId == null) {
            return data;
        }
        data.put("total", messageService.count(pending(receiverId)));
        data.put("urgentTotal", messageService.count(pending(receiverId).eq(SysMessage::getSeverity, "urgent")));
        // 危急值(severity=urgent)永远置顶；二级键 message_id 兜底同秒顺序漂移
        List<SysMessage> rows = messageService.list(pending(receiverId)
                .last("ORDER BY FIELD(severity, 'urgent', 'warning', 'info'), send_time DESC, message_id DESC LIMIT " + TOP_N));
        data.put("items", rows.stream().map(MessageMetricSupport::of).collect(Collectors.toList()));
        return data;
    }

    private LambdaQueryWrapper<SysMessage> pending(Long receiverId) {
        return new LambdaQueryWrapper<SysMessage>()
                .eq(SysMessage::getReceiverId, receiverId)
                .eq(SysMessage::getSendStatus, 1)
                .eq(SysMessage::getHandleStatus, 0);
    }
}

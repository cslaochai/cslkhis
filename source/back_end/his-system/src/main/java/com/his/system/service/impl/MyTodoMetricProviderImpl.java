package com.his.system.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.his.system.entity.CurrentUser;
import com.his.system.entity.SysMessage;
import com.his.system.provider.WorkbenchMetricProvider;
import com.his.system.service.MessageMetricSupport;
import com.his.system.service.MyTodoMetricProvider;
import com.his.system.service.SysMessageService;
import com.his.system.vo.WorkbenchMyTodoVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 卡片 myTodo：我当前未办结的站内信（待办型）。
 */
@Service
@RequiredArgsConstructor
public class MyTodoMetricProviderImpl implements WorkbenchMetricProvider, MyTodoMetricProvider {

    /**
     * 卡片只给前 N 条，"查看全部"走消息抽屉的 listPage
     */
    private static final int TOP_N = 8;

    private final SysMessageService sysMessageService;

    @Override
    public String widgetCode() {
        return "myTodo";
    }

    /**
     * VO 转 Map 是 SPI 边界上的一次性适配（父接口 {@code WorkbenchMetricProvider#summary}
     * 签名固定为 {@code Map<String, Object>}，his-medicaltech 等模块另有 5 个子接口实现它）；
     * 用字段名做键，与前端 {@code data.total / data.urgentTotal / data.items} 逐项对齐。
     */
    @Override
    public Map<String, Object> summary(CurrentUser user) {
        WorkbenchMyTodoVO vo = new WorkbenchMyTodoVO();
        vo.setTotal(0L);
        vo.setUrgentTotal(0L);
        vo.setItems(Collections.emptyList());

        Long receiverId = MessageMetricSupport.receiverId(user);
        if (receiverId != null) {
            vo.setTotal(sysMessageService.count(pending(receiverId)));
            vo.setUrgentTotal(sysMessageService.count(pending(receiverId).eq(SysMessage::getSeverity, "urgent")));
            List<SysMessage> rows = sysMessageService.list(pending(receiverId)
                    .last("ORDER BY FIELD(severity, 'urgent', 'warning', 'info'), send_time DESC, message_id DESC LIMIT " + TOP_N));
            vo.setItems(rows.stream().map(MessageMetricSupport::of).collect(Collectors.toList()));
        }
        return BeanUtil.beanToMap(vo);
    }

    private LambdaQueryWrapper<SysMessage> pending(Long receiverId) {
        return new LambdaQueryWrapper<SysMessage>()
                .eq(SysMessage::getReceiverId, receiverId)
                .eq(SysMessage::getSendStatus, 1)
                .eq(SysMessage::getHandleStatus, 0);
    }
}

package com.his.system.service;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import com.his.common.util.DateFormats;
import com.his.system.entity.CurrentUser;
import com.his.system.entity.SysMessage;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 待办/通知两张卡共用的出参装配。
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class MessageMetricSupport {

    /**
     * 收件人口径：消息通知.receiver_id 存的是<b>员工ID</b>，不是用户的ID
     * （写入侧全是申请医师ID 之类的 employeeId）。拿不到员工档案才回落 userId，
     * 否则会出现"未读数恒为 0 且零报错"。
     */
    public static Long receiverId(CurrentUser user) {
        if (user == null) {
            return null;
        }
        return user.getEmployeeId() != null ? user.getEmployeeId() : user.getUserId();
    }

    public static Item of(SysMessage message) {
        Item item = new Item();
        item.setMessageId(message.getMessageId() == null ? null : String.valueOf(message.getMessageId()));
        item.setBizType(message.getBizType());
        item.setTitle(message.getTitle());
        item.setContent(message.getContent());
        item.setSeverity(message.getSeverity());
        item.setBizId(message.getBizId() == null ? null : String.valueOf(message.getBizId()));
        item.setPayload(message.getPayload());
        item.setSendTime(text(message.getSendTime()));
        return item;
    }

    public static String text(LocalDateTime time) {
        return time == null ? null : DateFormats.DATETIME_MINUTE.format(time);
    }

    /**
     * 卡片里的一条消息（字段与 lib/messageCatalog.js 的 bizType 对齐，展示名由前端查表）
     */
    @Data
    public static class Item {
        /** 已是字符串：雪花 ID 出 Map 会被 JS 丢精度，见类注释 */
        private String messageId;
        private String bizType;
        private String title;
        private String content;
        private String severity;
        private String bizId;
        private String payload;
        private String sendTime;
    }
}

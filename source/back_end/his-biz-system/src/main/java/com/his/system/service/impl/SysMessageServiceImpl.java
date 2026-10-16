package com.his.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.his.common.base.PageResult;
import com.his.common.exception.BusinessException;
import com.his.common.util.TextUtil;
import com.his.system.dto.MessageQueryPageDTO;
import com.his.system.entity.CurrentUser;
import com.his.system.entity.SysMessage;
import com.his.system.enums.BizTypeEnum;
import com.his.system.enums.ChannelEnum;
import com.his.system.mapper.SysMessageMapper;
import com.his.system.service.RedisSequenceService;
import com.his.system.service.SysMessageService;
import com.his.system.support.MessagePushService;
import com.his.system.utils.UserUtils;
import com.his.system.vo.MessageTypeCountVO;
import com.his.system.vo.SysMessageVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 消息通知服务实现
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SysMessageServiceImpl extends ServiceImpl<SysMessageMapper, SysMessage> implements SysMessageService {

    private final MessagePushService messagePushService;
    private final RedisSequenceService redisSequenceService;

    @Override
    public boolean sendSystemMessage(Long receiverId, String receiverName, String title, String content,
                                     String bizType, Long bizId) {
        return sendSystemMessage(receiverId, receiverName, title, content, bizType, bizId, null, null, null);
    }

    @Override
    public boolean sendSystemMessage(Long receiverId, String receiverName, String title, String content,
                                     String bizType, Long bizId, String severity, String payload, Integer handleStatus) {
        SysMessage message = new SysMessage();
        message.setMessageNo(redisSequenceService.generateMessageNo());
        message.setChannel(ChannelEnum.SYSTEM.getChannel());
        message.setReceiverId(receiverId);
        message.setReceiverName(receiverName);
        message.setTitle(title);
        message.setContent(content);
        message.setBizType(bizType);
        message.setBizId(bizId);
        message.setSeverity(BizTypeEnum.isValidSeverity(severity) ? severity : "info");
        message.setPayload(payload);
        message.setHandleStatus(handleStatus);
        message.setSendStatus(1); // 已发送
        message.setSendTime(LocalDateTime.now());
        message.setReadStatus(0); // 未读
        boolean saved = this.save(message);
        if (saved) {
            try {
                messagePushService.pushNewMessage(receiverId, message.getTitle(),
                        message.getBizType(), message.getSeverity(), countUnread(receiverId));
            } catch (Exception ignored) {
                // 推送通道的任何异常都不外抛
            }
        }
        return saved;
    }

    @Override
    public PageResult<SysMessageVO> queryMessagePage(MessageQueryPageDTO queryDTO) {
        LambdaQueryWrapper<SysMessage> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysMessage::getReceiverId, currentReceiverId())
                .eq(SysMessage::getSendStatus, 1)
                .eq(queryDTO.getReadStatus() != null, SysMessage::getReadStatus, queryDTO.getReadStatus())
                .eq(queryDTO.getHandleStatus() != null, SysMessage::getHandleStatus, queryDTO.getHandleStatus())
                .eq(TextUtil.hasText(queryDTO.getBizType()), SysMessage::getBizType, queryDTO.getBizType())
                .and(TextUtil.hasText(queryDTO.getKeyword()), w -> w
                        .like(SysMessage::getTitle, queryDTO.getKeyword())
                        .or().like(SysMessage::getContent, queryDTO.getKeyword()))
                .last("ORDER BY FIELD(severity, 'urgent', 'warning', 'info'), send_time DESC, message_id DESC");

        Page<SysMessage> page = this.page(new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize()), wrapper);

        List<SysMessageVO> voList = page.getRecords().stream().map(m -> {
            SysMessageVO vo = new SysMessageVO();
            BeanUtils.copyProperties(m, vo);
            return vo;
        }).collect(Collectors.toList());

        return PageResult.of(page.getTotal(), page.getCurrent(), page.getSize(), page.getPages(), voList);
    }

    @Override
    public SseEmitter subscribe() {
        return messagePushService.register(currentReceiverId());
    }

    @Override
    public int countUnreadOfCurrentUser() {
        return countUnread(currentReceiverId());
    }

    @Override
    public List<MessageTypeCountVO> typeCountsOfCurrentUser() {
        return typeCounts(currentReceiverId());
    }

    @Override
    public void markRead(Long messageId) {
        SysMessage message = this.getById(messageId);
        if (message != null && message.getReadStatus() == 0) {
            message.setReadStatus(1);
            message.setReadTime(LocalDateTime.now());
            this.updateById(message);
        }
    }

    @Override
    public void markAllRead() {
        LambdaQueryWrapper<SysMessage> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysMessage::getReceiverId, currentReceiverId())
                .eq(SysMessage::getReadStatus, 0)
                .eq(SysMessage::getSendStatus, 1);

        SysMessage update = new SysMessage();
        update.setReadStatus(1);
        update.setReadTime(LocalDateTime.now());
        this.update(update, wrapper);
    }

    @Override
    public Long currentReceiverId() {
        CurrentUser currentUser = UserUtils.getCurrentUser();
        if (currentUser == null) {
            throw new BusinessException("当前用户信息不存在");
        }
        return currentUser.getEmployeeId();
    }

    @Override
    public int countUnread(Long receiverId) {
        QueryWrapper<SysMessage> wrapper = new QueryWrapper<>();
        wrapper.eq("receiver_id", receiverId)
                .eq("send_status", 1)
                .eq("read_status", 0);
        return (int) this.count(wrapper);
    }

    @Override
    public List<MessageTypeCountVO> typeCounts(Long receiverId) {
        return baseMapper.selectTypeCounts(receiverId);
    }

    @Override
    public List<Long> receiverIdsOfBiz(String bizType, Long bizId) {
        QueryWrapper<SysMessage> wrapper = new QueryWrapper<>();
        wrapper.select("DISTINCT receiver_id")
                .eq("biz_type", bizType)
                .eq("biz_id", bizId)
                .eq("send_status", 1);
        // list 而不是 listObjs：BIGINT 经 listObjs 可能回 BigInteger，走实体 getter 拿到的一定是 Long
        Set<Long> ids = new LinkedHashSet<>();
        for (SysMessage m : this.list(wrapper)) {
            if (m.getReceiverId() != null) {
                ids.add(m.getReceiverId());
            }
        }
        return List.copyOf(ids);
    }
}

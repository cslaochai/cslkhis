package com.his.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.his.common.base.PageResult;
import com.his.common.service.RedisSequenceService;
import com.his.common.util.TextUtil;
import com.his.system.entity.CurrentUser;
import com.his.system.utils.UserUtils;
import com.his.system.enums.BizTypeEnum;
import com.his.system.dto.MessageQueryPageDTO;
import com.his.system.enums.ChannelEnum;
import com.his.system.entity.SysMessage;
import com.his.system.entity.SysUser;
import com.his.system.mapper.SysMessageMapper;
import com.his.system.mapper.SysUserMapper;
import com.his.system.service.SysMessageService;
import com.his.system.support.MessagePushService;
import com.his.system.vo.MessageTypeCountVO;
import com.his.system.vo.SysMessageVO;
import com.his.system.service.WechatSubscribeSender;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
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
    private final WechatSubscribeSender wechatSubscribeSender;
    private final SysUserMapper sysUserMapper;
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
        // severity 落库前做一次枚举校验：非法值一律按 info 落，不能让脏码值进排序
        message.setSeverity(BizTypeEnum.isValidSeverity(severity) ? severity : "info");
        message.setPayload(payload);
        message.setHandleStatus(handleStatus);
        message.setSendStatus(1); // 已发送
        message.setSendTime(LocalDateTime.now());
        message.setReadStatus(0); // 未读
        boolean saved = this.save(message);
        if (saved) {
            // 实时推送（SSE 在线即达）。推送失败绝不能影响消息落库的结果 ——
            // 消息已经发出去（收件箱可见），推送只是「更快知道」的手段。
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
    public boolean sendWechatMessage(Long receiverUserId, String receiverName, String scene, String page,
                                     Map<String, String> data, String title, String content,
                                     String bizType, Long bizId) {
        SysUser receiver = receiverUserId == null ? null : sysUserMapper.selectById(receiverUserId);
        String openid = receiver == null ? null : receiver.getOpenid();

        SysMessage message = new SysMessage();
        message.setMessageNo(redisSequenceService.generateMessageNo());
        message.setChannel(ChannelEnum.WECHAT.getChannel());
        message.setReceiverId(receiverUserId);
        message.setReceiverName(receiverName);
        message.setTitle(title);
        message.setContent(content);
        message.setBizType(bizType);
        message.setBizId(bizId);
        message.setSeverity("info");
        message.setReadStatus(0);
        // 先发送后落库：本渠道没有「落库即送达」的语义，先 send 才能把真实结果写进 send_status；
        // 反过来先落库再改状态，进程中途挂掉会留下永远停在"已发送"的假成功。
        String failReason = wechatSubscribeSender.send(openid, scene, page, data);
        boolean success = failReason == null;
        message.setSendStatus(success ? 1 : 2);
        message.setSendTime(LocalDateTime.now());
        message.setErrorMsg(failReason);
        try {
            this.save(message);
        } catch (Exception e) {
            // 留痕失败只记日志 —— 消息可能已送达微信，不能让记账问题升级成业务异常
            log.error("微信订阅消息留痕失败 scene={} receiver={}", scene, receiverUserId, e);
        }
        return success;
    }

    @Override
    public boolean sendWechatToPatient(Long patientId, String scene, String page, Map<String, String> data,
                                       String title, String content, String bizType, Long bizId) {
        if (patientId == null) {
            return false;
        }
        SysUser user = sysUserMapper.selectOne(new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getPatientId, patientId)
                .eq(SysUser::getUserType, 3)
                .last("LIMIT 1"));
        if (user == null) {
            // 患者没有小程序账号：没有收件人，谈不上送达，留 debug 日志即可
            log.debug("sendWechatToPatient：患者 {} 无对应账号，跳过 scene={}", patientId, scene);
            return false;
        }
        return sendWechatMessage(user.getId(), user.getRealName(), scene, page, data,
                title, content, bizType, bizId);
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
                // 紧急度置顶 + 发送时间倒序，再补主键做二级键：
                // 同秒多行顺序不稳定会造成翻页重复/丢行，分页排序必须补唯一键兜底。
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
        return currentUser.getEmployeeId() != null ? currentUser.getEmployeeId() : currentUser.getUserId();
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

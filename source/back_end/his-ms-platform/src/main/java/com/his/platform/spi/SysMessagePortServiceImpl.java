package com.his.platform.spi;

import com.his.spi.service.SysMessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * {@link SysMessageService} 窄端口实现：只暴露跨模块投递与追发判重方法，
 * 委托平台内部完整版 {@link com.his.platform.service.SysMessageService}。
 */
@Service
@RequiredArgsConstructor
public class SysMessagePortServiceImpl implements SysMessageService {

    private final com.his.platform.service.SysMessageService messageService;

    @Override
    public boolean sendSystemMessage(Long receiverId, String receiverName, String title, String content,
                                     String bizType, Long bizId) {
        return messageService.sendSystemMessage(receiverId, receiverName, title, content, bizType, bizId);
    }

    @Override
    public boolean sendSystemMessage(Long receiverId, String receiverName, String title, String content,
                                     String bizType, Long bizId, String severity, String payload, Integer handleStatus) {
        return messageService.sendSystemMessage(receiverId, receiverName, title, content,
                bizType, bizId, severity, payload, handleStatus);
    }

    @Override
    public List<Long> receiverIdsOfBiz(String bizType, Long bizId) {
        return messageService.receiverIdsOfBiz(bizType, bizId);
    }

    @Override
    public long countSentSince(String bizType, Long bizId, LocalDateTime since) {
        return messageService.countSentSince(bizType, bizId, since);
    }

    @Override
    public boolean sentWithinHours(String bizType, Long bizId, Long receiverId, String severity, int hours) {
        return messageService.sentWithinHours(bizType, bizId, receiverId, severity, hours);
    }

    @Override
    public void closeTodoByBiz(String bizType, Long bizId, Integer targetStatus) {
        messageService.closeTodoByBiz(bizType, bizId, targetStatus);
    }
}

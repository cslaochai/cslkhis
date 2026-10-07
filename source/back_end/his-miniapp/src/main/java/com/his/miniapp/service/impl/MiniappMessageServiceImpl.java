package com.his.miniapp.service.impl;

import com.his.common.base.PageResult;
import com.his.common.exception.BusinessException;
import com.his.miniapp.dto.MessagePageDTO;
import com.his.miniapp.mapper.MiniappMessageMapper;
import com.his.miniapp.service.MiniappMessageService;
import com.his.miniapp.vo.MessageListVO;
import com.his.miniapp.vo.MessageRowVO;
import com.his.system.entity.CurrentUser;
import com.his.system.utils.UserUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MiniappMessageServiceImpl implements MiniappMessageService {

    private final MiniappMessageMapper miniappMessageMapper;

    private static MessageListVO toVO(MessageRowVO row) {
        MessageListVO vo = new MessageListVO();
        vo.setMessageId(row.getMessageId());
        vo.setMessageNo(row.getMessageNo());
        vo.setChannel(row.getChannel());
        vo.setReceiverId(row.getReceiverId());
        vo.setReceiverName(row.getReceiverName());
        vo.setTitle(row.getTitle());
        vo.setContent(row.getContent());
        vo.setBizType(row.getBizType());
        vo.setBizId(row.getBizId());
        vo.setSeverity(row.getSeverity());
        vo.setReadStatus(row.getReadStatus());
        vo.setSendStatus(row.getSendStatus());
        vo.setSendTime(row.getSendTime());
        return vo;
    }

    @Override
    public PageResult<MessageListVO> myPage(MessagePageDTO dto) {
        CurrentUser operatorUser = UserUtils.getCurrentUser();
        if (operatorUser == null) {
            throw new BusinessException("当前用户信息不存在");
        }
        Long userId = operatorUser.getUserId();
        int size = dto.getPageSize();
        int current = dto.getPageNum();
        List<MessageRowVO> rows = miniappMessageMapper.selectMyMessages(userId, (current - 1) * size, size);
        long total = miniappMessageMapper.countMyMessages(userId);
        List<MessageListVO> records = rows.stream().map(MiniappMessageServiceImpl::toVO).toList();
        return PageResult.of(total, current, size, (total + size - 1) / size, records);
    }

    @Override
    public long unreadCount() {
        CurrentUser operatorUser = UserUtils.getCurrentUser();
        if (operatorUser == null) {
            throw new BusinessException("当前用户信息不存在");
        }
        return miniappMessageMapper.countUnread(operatorUser.getUserId());
    }

    @Override
    public int markRead(List<Long> messageIds) {
        CurrentUser operatorUser = UserUtils.getCurrentUser();
        if (operatorUser == null) {
            throw new BusinessException("当前用户信息不存在");
        }
        return miniappMessageMapper.markRead(messageIds, operatorUser.getUserId());
    }
}

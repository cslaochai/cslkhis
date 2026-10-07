package com.his.miniapp.service.impl;

import com.his.common.base.PageResult;
import com.his.common.exception.BusinessException;
import com.his.miniapp.mapper.MiniappMessageMapper;
import com.his.system.entity.CurrentUser;
import com.his.miniapp.service.MiniappMessageService;
import com.his.miniapp.support.RawRowValues;
import com.his.miniapp.vo.MessageListVO;
import com.his.system.utils.UserUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class MiniappMessageServiceImpl implements MiniappMessageService {

    private static final int DEFAULT_PAGE_SIZE = 10;
    private static final int MAX_PAGE_SIZE = 50;

    private final MiniappMessageMapper messageMapper;

    @Override
    public PageResult<MessageListVO> myPage(Integer pageNum, Integer pageSize) {
        CurrentUser operatorUser = UserUtils.getCurrentUser();
        if (operatorUser == null) {
            throw new BusinessException("当前用户信息不存在");
        }
        Long userId = operatorUser.getUserId();
        int size = pageSize == null || pageSize < 1 || pageSize > MAX_PAGE_SIZE ? DEFAULT_PAGE_SIZE : pageSize;
        int current = pageNum == null || pageNum < 1 ? 1 : pageNum;
        List<Map<String, Object>> rows = messageMapper.selectMyMessages(userId, (current - 1) * size, size);
        long total = messageMapper.countMyMessages(userId);
        List<MessageListVO> records = rows.stream().map(MiniappMessageServiceImpl::toVO).toList();
        return PageResult.of(total, current, size, (total + size - 1) / size, records);
    }

    @Override
    public long unreadCount() {
        CurrentUser operatorUser = UserUtils.getCurrentUser();
        if (operatorUser == null) {
            throw new BusinessException("当前用户信息不存在");
        }
        return messageMapper.countUnread(operatorUser.getUserId());
    }

    @Override
    public int markRead(List<String> messageIds) {
        // 数字白名单校验：拼 IN 列表前拒绝任何非数字（messageIds 是字符串形态的 BIGINT）
        for (String id : messageIds) {
            if (id == null || !id.matches("\\d{1,20}")) {
                throw new BusinessException("非法的消息ID");
            }
        }
        CurrentUser operatorUser = UserUtils.getCurrentUser();
        if (operatorUser == null) {
            throw new BusinessException("当前用户信息不存在");
        }
        return messageMapper.markRead(String.join(",", messageIds), operatorUser.getUserId());
    }

    private static MessageListVO toVO(Map<String, Object> row) {
        MessageListVO vo = new MessageListVO();
        vo.setMessageId(RawRowValues.text(row, "messageId"));
        vo.setMessageNo(RawRowValues.text(row, "messageNo"));
        vo.setChannel(RawRowValues.text(row, "channel"));
        vo.setReceiverId(RawRowValues.text(row, "receiverId"));
        vo.setReceiverName(RawRowValues.text(row, "receiverName"));
        vo.setTitle(RawRowValues.text(row, "title"));
        vo.setContent(RawRowValues.text(row, "content"));
        vo.setBizType(RawRowValues.text(row, "bizType"));
        vo.setBizId(RawRowValues.text(row, "bizId"));
        vo.setSeverity(RawRowValues.text(row, "severity"));
        vo.setReadStatus(RawRowValues.integer(row, "readStatus"));
        vo.setSendStatus(RawRowValues.integer(row, "sendStatus"));
        vo.setSendTime(RawRowValues.text(row, "sendTime"));
        return vo;
    }
}

package com.his.miniapp.service.impl;

import com.his.common.base.PageResult;
import com.his.common.exception.BusinessException;
import com.his.miniapp.dto.MessagePageDTO;
import com.his.miniapp.mapper.MiniMessageMapper;
import com.his.miniapp.service.MiniMessageService;
import com.his.miniapp.vo.MiniMessageListVO;
import com.his.system.entity.CurrentUser;
import com.his.system.utils.UserUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MiniMessageServiceImpl implements MiniMessageService {

    private final MiniMessageMapper miniMessageMapper;

    @Override
    public PageResult<MiniMessageListVO> myPage(MessagePageDTO dto) {
        CurrentUser operatorUser = UserUtils.getCurrentUser();
        if (operatorUser == null) {
            throw new BusinessException("当前用户信息不存在");
        }
        Long userId = operatorUser.getUserId();
        int size = dto.getPageSize();
        int current = dto.getPageNum();
        List<MiniMessageListVO> records = miniMessageMapper.selectMyMessages(userId, (current - 1) * size, size);
        long total = miniMessageMapper.countMyMessages(userId);
        return PageResult.of(total, current, size, (total + size - 1) / size, records);
    }

    @Override
    public long unreadCount() {
        CurrentUser operatorUser = UserUtils.getCurrentUser();
        if (operatorUser == null) {
            throw new BusinessException("当前用户信息不存在");
        }
        return miniMessageMapper.countUnread(operatorUser.getUserId());
    }

    @Override
    public int markRead(List<Long> messageIds) {
        CurrentUser operatorUser = UserUtils.getCurrentUser();
        if (operatorUser == null) {
            throw new BusinessException("当前用户信息不存在");
        }
        return miniMessageMapper.markRead(messageIds, operatorUser.getUserId());
    }
}

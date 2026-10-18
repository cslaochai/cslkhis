package com.his.miniapp.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.his.common.base.PageResult;
import com.his.common.entity.CurrentUser;
import com.his.common.exception.BusinessException;
import com.his.miniapp.dto.MessagePageDTO;
import com.his.miniapp.mapper.MiniMessageMapper;
import com.his.miniapp.service.MiniMessageService;
import com.his.miniapp.vo.MiniMessageListVO;
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
        IPage<MiniMessageListVO> paged = miniMessageMapper.selectMyMessages(new Page<>(current, size), userId);
        return PageResult.of(paged.getTotal(), current, size, paged.getPages(), paged.getRecords());
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

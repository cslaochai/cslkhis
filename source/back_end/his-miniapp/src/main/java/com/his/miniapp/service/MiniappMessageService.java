package com.his.miniapp.service;

import com.his.common.base.PageResult;
import com.his.miniapp.vo.MessageListVO;

import java.util.List;

/**
 * 患者端消息中心读侧：归属一律取当前登录账号，不接收前端传的接收人。
 */
public interface MiniappMessageService {

    PageResult<MessageListVO> myPage(Integer pageNum, Integer pageSize);

    long unreadCount();

    /**
     * 批量标记已读（幂等，只动自己的消息）。
     *
     * @return 实际被置为已读的条数
     */
    int markRead(List<Long> messageIds);
}

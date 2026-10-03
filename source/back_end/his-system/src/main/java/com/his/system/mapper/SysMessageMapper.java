package com.his.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.his.system.entity.SysMessage;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 消息/通知Mapper
 */
@Mapper
public interface SysMessageMapper extends BaseMapper<SysMessage> {

    /**
     * 查询收件人的未读消息数量
     * <p>
     * 入参是 {@code receiver_id} 的值，即<b>员工ID</b>（用户.emp_id），不是用户的ID
     * —— 详见 {@code MessageController} 的收件人口径说明。方法名刻意用 {@code ReceiverId}
     * 而不是 {@code UserId}，避免下一个调用者按「用户ID」的直觉传错值。
     */
    @Select("SELECT COUNT(*) FROM sys_message WHERE receiver_id = #{receiverId} AND read_status = 0 AND send_status = 1")
    int countUnreadByReceiverId(@Param("receiverId") Long receiverId);
}

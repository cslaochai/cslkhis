package com.his.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.his.system.entity.SysMessage;
import com.his.system.vo.MessageTypeCountVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

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

    /**
     * 按业务类型分组的消息计数（抽屉 Tab / 分组徽标）。
     *
     * <p>{@code send_status = 1} 只认发送成功的：草稿箱里的消息不该占用户的未读数，
     * 否则「有多少条要我处理」会被自己没发出去的草稿污染。
     *
     * <p>{@code unread} 用 {@code SUM(read_status = 0)} 而不是 {@code COUNT(CASE WHEN ...)}：
     * 两者等价，但前者不会在无未读的行上返回 NULL，驱动差异更少。
     */
    @Select("SELECT biz_type AS bizType, COUNT(*) AS total, COALESCE(SUM(read_status = 0), 0) AS unread "
            + "FROM sys_message "
            + "WHERE receiver_id = #{receiverId} AND send_status = 1 "
            + "GROUP BY biz_type")
    List<MessageTypeCountVO> selectTypeCounts(@Param("receiverId") Long receiverId);
}

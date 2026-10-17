package com.his.miniapp.mapper;

import com.his.miniapp.vo.MiniMessageListVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * 患者端消息中心读侧（消息通知属 his-system，跨模块用裸 SQL）。
 */
@Mapper
public interface MiniMessageMapper {

    String COLS = """
            message_id AS messageId,
            message_no AS messageNo,
            channel,
            receiver_id AS receiverId,
            receiver_name AS receiverName,
            title,
            content,
            biz_type AS bizType,
            biz_id AS bizId,
            severity,
            read_status AS readStatus,
            send_status AS sendStatus,
            DATE_FORMAT(send_time, '%Y-%m-%d %H:%i:%s') AS sendTime
            """;

    /**
     * 我的消息（分页）。send_status=2 为通道未启用的留痕消息，患者端消息中心兜底可见。
     */
    @Select("SELECT " + COLS + """
            FROM sys_message
            WHERE receiver_id = #{userId}
            ORDER BY send_time IS NULL ASC, send_time DESC, message_id DESC
            LIMIT #{offset}, #{size}
            """)
    List<MiniMessageListVO> selectMyMessages(@Param("userId") Long userId,
                                             @Param("offset") int offset,
                                             @Param("size") int size);

    @Select("SELECT COUNT(*) FROM sys_message WHERE receiver_id = #{userId}")
    long countMyMessages(@Param("userId") Long userId);

    @Select("SELECT COUNT(*) FROM sys_message WHERE receiver_id = #{userId} AND read_status = 0")
    long countUnread(@Param("userId") Long userId);

    /**
     * 标记已读（只允许标自己的消息；幂等）。
     *
     * <p>ids 走 {@code <foreach>} 逐个绑参，不拼字符串：入参已经是 {@code List<Long>}，
     * 一个非数字进不来，拼 IN 列表反而是给自己开注入面。
     */
    @Update("""
            <script>
            UPDATE sys_message SET read_status = 1, read_time = NOW()
            WHERE message_id IN
            <foreach collection="ids" item="id" open="(" separator="," close=")">#{id}</foreach>
            AND receiver_id = #{userId} AND read_status = 0
            </script>
            """)
    int markRead(@Param("ids") List<Long> ids, @Param("userId") Long userId);
}

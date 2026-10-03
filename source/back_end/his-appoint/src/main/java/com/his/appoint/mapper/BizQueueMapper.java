package com.his.appoint.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.his.appoint.entity.BizQueue;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

import java.time.LocalDate;

/**
 * 排队信息Mapper
 */
@Mapper
public interface BizQueueMapper extends BaseMapper<BizQueue> {

    /**
     * 排班换诊室 → 把该班次「今天还没看完」的队列行一起搬到新诊室。
     *
     * <p>为什么需要它：诊室是<b>排班级</b>属性（医生-诊室绑定在排班上），队列行上的 room_id
     * 只是入队时的快照。只在排班上改诊室、不同步队列，就会出现「排班说 305、患者手上
     * 的号还是 302」的分裂 —— 患者照旧走错房间。
     *
     * <p>只搬未结束的（2 候诊中 / 3 就诊中）：已就诊/过号/退号的行是<b>历史留痕</b>，
     * 追改会把「当时到底在哪个诊室看的」改掉，也让门诊日志与事实不符。
     *
     * @return 被搬动的队列行数
     */
    @Update("""
            UPDATE biz_queue q
            JOIN biz_appoint_info r ON r.id = q.regist_id
               SET q.room_id = #{roomId}, q.room_name = #{roomName}
             WHERE r.schedule_id = #{scheduleId}
               AND q.visit_date = #{visitDate}
               AND q.queue_status IN (2, 3)
               AND q.del_flag = 0
               AND r.del_flag = 0
            """)
    int updateRoomBySchedule(@Param("scheduleId") Long scheduleId,
                             @Param("visitDate") LocalDate visitDate,
                             @Param("roomId") Long roomId,
                             @Param("roomName") String roomName);
}

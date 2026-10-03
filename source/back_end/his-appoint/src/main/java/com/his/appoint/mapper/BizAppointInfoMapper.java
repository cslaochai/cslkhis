package com.his.appoint.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.his.appoint.entity.BizAppointInfo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

import java.time.LocalDate;

/**
 * 挂号信息Mapper
 */
@Mapper
public interface BizAppointInfoMapper extends BaseMapper<BizAppointInfo> {

    /**
     * 排班换诊室 → 同步该班次「今天还没结束」的挂号快照。
     *
     * <p>为什么要同步挂号表：挂号信息的诊室ID/room_name 是<b>门诊日志的诊室来源</b>
     * （见 OpdLogMapper.xml 的 {@code r.room_id, r.room_name}）。只改队列不改挂号，
     * 门诊日志与分诊台就会各说一个诊室。
     *
     * <p>只同步未结束的（1 已挂号 / 2 已签到 / 3 已接诊）；4 已就诊 / 5 已退号 / 6 已过号
     * 保持当时的快照，历史不能被追改。
     *
     * <p>注意实体 {@link BizAppointInfo} 里没有 roomId 字段（表里有列），所以只能走裸 SQL，
     * 不能拼 LambdaUpdateWrapper。
     *
     * @return 被更新的挂号行数
     */
    @Update("""
            UPDATE biz_appoint_info
               SET room_id = #{roomId}, room_name = #{roomName}
             WHERE schedule_id = #{scheduleId}
               AND visit_date = #{visitDate}
               AND regist_status IN (1, 2, 3)
               AND del_flag = 0
            """)
    int updateRoomBySchedule(@Param("scheduleId") Long scheduleId,
                             @Param("visitDate") LocalDate visitDate,
                             @Param("roomId") Long roomId,
                             @Param("roomName") String roomName);
}

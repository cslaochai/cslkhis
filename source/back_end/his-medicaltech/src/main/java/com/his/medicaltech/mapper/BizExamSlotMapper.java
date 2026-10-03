package com.his.medicaltech.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.his.medicaltech.entity.BizExamSlot;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.time.LocalDate;
import java.util.List;

@Mapper
public interface BizExamSlotMapper extends BaseMapper<BizExamSlot> {

    /**
     * 锁定某设备某日的全部号源格（按 start_time 升序）。
     *
     * <p>锁粒度选「设备 + 整天」而不是「单个格子」：一次预约要跨若干格，
     * 逐格加锁的先后顺序取决于项目时长，两笔并发请求互相等就成死锁。
     * 整天一把锁 + 固定顺序，同一台设备同一天的占号天然串行。
     */
    @Select("SELECT * FROM biz_exam_slot WHERE device_id = #{deviceId} AND slot_date = #{slotDate} "
            + "AND del_flag = 0 ORDER BY start_time ASC FOR UPDATE")
    List<BizExamSlot> selectDayForUpdate(@Param("deviceId") Long deviceId, @Param("slotDate") LocalDate slotDate);

    /**
     * 按绝对值回写占用数（读在锁内，写在锁内，所以给绝对值而不是增量）。
     *
     * <p>不用 {@code used_source = used_source + 1} 这种自增写法：MySQL 里同一条 UPDATE 中
     * 后面的赋值表达式看到的是前面赋值已更新过的列值，{@code available_source = total - used_source}
     * 到底算的是新值还是旧值完全取决于列的书写顺序 —— 两列必须永远一致，这种歧义不能留。
     * expectUsed 做乐观兜底：与读到的值不符说明锁没生效，宁可抛错也不静默写歪。
     */
    @Update("UPDATE biz_exam_slot SET used_source = #{used}, available_source = #{available}, update_time = NOW() "
            + "WHERE id = #{id} AND del_flag = 0 AND used_source = #{expectUsed}")
    int updateUsed(@Param("id") Long id, @Param("expectUsed") Integer expectUsed,
                   @Param("used") Integer used, @Param("available") Integer available);
}

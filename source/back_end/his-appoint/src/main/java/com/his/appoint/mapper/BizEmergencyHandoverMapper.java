package com.his.appoint.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.his.appoint.entity.BizEmergencyHandover;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;

/**
 * 急诊交班单 Mapper。
 */
@Mapper
public interface BizEmergencyHandoverMapper extends BaseMapper<BizEmergencyHandover> {

    /**
     * 交出人在本科室上一次交班的结束时刻（滚动区间起点）。
     *
     * <p>用 {@code MAX(period_end)} 而不是"最近一条记录"：取最大值可免疫乱序插入。
     * 查不到（从未交班）返回 null，由调用方回落到当日 00:00:00。
     */
    @Select("SELECT MAX(period_end) FROM biz_emergency_handover "
            + " WHERE del_flag = 0 AND from_emp_id = #{empId} AND dept_id = #{deptId}")
    LocalDateTime selectLastPeriodEnd(@Param("empId") Long empId, @Param("deptId") Long deptId);
}

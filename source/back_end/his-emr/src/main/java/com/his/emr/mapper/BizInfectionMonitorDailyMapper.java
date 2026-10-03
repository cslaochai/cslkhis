package com.his.emr.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.his.emr.entity.BizInfectionMonitorDaily;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface BizInfectionMonitorDailyMapper extends BaseMapper<BizInfectionMonitorDaily> {

    /**
     * 同日打卡查重（含软删行）——打卡表无唯一键，防重靠 service + 本查询。
     */
    @Select("SELECT COUNT(*) FROM biz_infection_monitor_daily"
            + " WHERE monitor_id = #{monitorId} AND monitor_date = #{monitorDate}")
    int countByMonitorAndDate(@Param("monitorId") Long monitorId, @Param("monitorDate") String monitorDate);

    /**
     * 某监测的导管日（有效打卡数）。
     */
    @Select("SELECT COUNT(*) FROM biz_infection_monitor_daily WHERE monitor_id = #{monitorId} AND del_flag = 0")
    int countCatheterDays(@Param("monitorId") Long monitorId);
}

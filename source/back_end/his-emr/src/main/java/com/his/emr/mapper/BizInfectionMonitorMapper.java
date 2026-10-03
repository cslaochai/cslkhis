package com.his.emr.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.his.emr.entity.BizInfectionMonitor;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.Map;

@Mapper
public interface BizInfectionMonitorMapper extends BaseMapper<BizInfectionMonitor> {

    /**
     * 按编号查（含软删行）——监测编号有唯一键，查重必须含软删行。
     */
    @Select("SELECT COUNT(*) FROM biz_infection_monitor WHERE monitor_no = #{monitorNo}")
    int countByNoIncludingDeleted(@Param("monitorNo") String monitorNo);

    /**
     * 住院登记科室（目标性监测主要发生在住院段；入院记录的科室ID = 当前科室，主键 admission_id）。
     */
    @Select("SELECT dept_id AS deptId, (SELECT dept_name FROM sys_department d WHERE d.id = a.dept_id) AS deptName"
            + " FROM biz_admission a WHERE a.admission_id = #{inpId} AND a.del_flag = 0")
    Map<String, Object> selectInpDept(@Param("inpId") Long inpId);
}

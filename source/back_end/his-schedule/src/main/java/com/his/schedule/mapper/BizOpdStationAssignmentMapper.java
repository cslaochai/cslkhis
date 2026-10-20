package com.his.schedule.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.his.schedule.entity.BizOpdStationAssignment;
import com.his.schedule.vo.OpdAssignCandidateVO;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDate;
import java.util.List;

/**
 * 门诊工位派岗记录 Mapper。
 */
@Mapper
public interface BizOpdStationAssignmentMapper extends BaseMapper<BizOpdStationAssignment> {

    /**
     * 物理删单行：唯一键(日期,工位,班次,员工)不含删除标志，软删行会占键，重派必撞键。
     */
    @Delete("DELETE FROM biz_opd_station_assignment WHERE id = #{id}")
    int purgeById(@Param("id") Long id);

    /**
     * 派岗候选：指定日期「已发布（或已变更）+ 上班」的出勤人，可按岗位类别、单个员工收窄。
     *
     * <p>刻意不收科室数据范围：派岗本身就是把人从所属单元挪到门诊公共位，
     * 按登录人科室过滤会把要借调的那个人直接滤没。
     */
    @Select("""
            <script>
            SELECT DISTINCT b.employee_id AS employeeId, b.emp_code AS empCode,
                   b.employee_name AS employeeName, b.staff_type AS staffType, b.org_name AS orgName
              FROM biz_schedule b
             WHERE b.del_flag = 0
               AND b.schedule_date = #{assignDate}
               AND b.duty_status = 1
               AND b.publish_status IN (2, 4)
            <if test="staffType != null">
               AND b.staff_type = #{staffType}
            </if>
            <if test="employeeId != null">
               AND b.employee_id = #{employeeId}
            </if>
             ORDER BY b.emp_code, b.employee_id
            </script>
            """)
    List<OpdAssignCandidateVO> selectOnDutyCandidates(@Param("assignDate") LocalDate assignDate,
                                                     @Param("staffType") Integer staffType,
                                                     @Param("employeeId") Long employeeId);
}

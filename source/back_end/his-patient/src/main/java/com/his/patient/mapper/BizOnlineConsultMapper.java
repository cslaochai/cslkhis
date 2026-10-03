package com.his.patient.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.his.patient.entity.BizOnlineConsult;
import com.his.patient.vo.OnlineConsultVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

/**
 * 互联网线上问诊 Mapper。
 */
@Mapper
public interface BizOnlineConsultMapper extends BaseMapper<BizOnlineConsult> {

    @Select("""
            <script>
            SELECT o.* FROM biz_online_consult o
             WHERE o.del_flag = 0
               <if test="keyword != null and keyword != ''">
                 AND (o.consult_no LIKE CONCAT('%', #{keyword}, '%')
                   OR o.patient_name LIKE CONCAT('%', #{keyword}, '%'))
               </if>
               <if test="consultType != null"> AND o.consult_type = #{consultType}</if>
               <if test="status != null"> AND o.status = #{status}</if>
               <if test="deptId != null"> AND o.dept_id = #{deptId}</if>
               <if test="doctorId != null"> AND o.doctor_id = #{doctorId}</if>
               <if test="waitingOnly != null and waitingOnly == true"> AND o.status = 1</if>
             ORDER BY o.status ASC, o.id DESC
            </script>
            """)
    List<OnlineConsultVO> selectOnlinePage(IPage<OnlineConsultVO> page,
                                           @Param("keyword") String keyword,
                                           @Param("consultType") Integer consultType,
                                           @Param("status") Integer status,
                                           @Param("deptId") Long deptId,
                                           @Param("doctorId") Long doctorId,
                                           @Param("waitingOnly") Boolean waitingOnly);

    @Select("SELECT o.* FROM biz_online_consult o WHERE o.id = #{id} AND o.del_flag = 0")
    OnlineConsultVO selectOnlineById(@Param("id") Long id);

    @Select("SELECT d.dept_name FROM sys_department d WHERE d.id = #{deptId} AND d.del_flag = 0")
    String selectDeptName(@Param("deptId") Long deptId);

    @Select("SELECT o.status AS k, COUNT(*) AS c FROM biz_online_consult o WHERE o.del_flag = 0 GROUP BY o.status")
    List<Map<String, Object>> countByStatus();
}

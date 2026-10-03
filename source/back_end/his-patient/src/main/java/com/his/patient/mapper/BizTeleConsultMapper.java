package com.his.patient.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.his.patient.entity.BizTeleConsult;
import com.his.patient.vo.TeleConsultVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

/**
 * 远程会诊 Mapper。科室名走科室裸 SQL（同模块无该实体，不引入依赖）。
 */
@Mapper
public interface BizTeleConsultMapper extends BaseMapper<BizTeleConsult> {

    @Select("""
            <script>
            SELECT t.* FROM biz_tele_consult t
             WHERE t.del_flag = 0
               <if test="keyword != null and keyword != ''">
                 AND (t.consult_no LIKE CONCAT('%', #{keyword}, '%')
                   OR t.patient_name LIKE CONCAT('%', #{keyword}, '%')
                   OR t.expert_name LIKE CONCAT('%', #{keyword}, '%'))
               </if>
               <if test="consultType != null"> AND t.consult_type = #{consultType}</if>
               <if test="status != null"> AND t.status = #{status}</if>
               <if test="applyDeptId != null"> AND t.apply_dept_id = #{applyDeptId}</if>
               <if test="urgentOnly != null and urgentOnly == true"> AND t.is_urgent = 1</if>
               <if test="openOnly != null and openOnly == true"> AND t.status IN (1, 2)</if>
             ORDER BY t.status ASC, t.id DESC
            </script>
            """)
    List<TeleConsultVO> selectTelePage(IPage<TeleConsultVO> page,
                                       @Param("keyword") String keyword,
                                       @Param("consultType") Integer consultType,
                                       @Param("status") Integer status,
                                       @Param("applyDeptId") Long applyDeptId,
                                       @Param("urgentOnly") Boolean urgentOnly,
                                       @Param("openOnly") Boolean openOnly);

    @Select("SELECT t.* FROM biz_tele_consult t WHERE t.id = #{id} AND t.del_flag = 0")
    TeleConsultVO selectTeleById(@Param("id") Long id);

    @Select("SELECT d.dept_name FROM sys_department d WHERE d.id = #{deptId} AND d.del_flag = 0")
    String selectDeptName(@Param("deptId") Long deptId);

    @Select("SELECT t.status AS k, COUNT(*) AS c FROM biz_tele_consult t WHERE t.del_flag = 0 GROUP BY t.status")
    List<Map<String, Object>> countByStatus();
}

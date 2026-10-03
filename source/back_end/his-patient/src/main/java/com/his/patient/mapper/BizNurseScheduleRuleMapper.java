package com.his.patient.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.his.patient.entity.BizNurseScheduleRule;
import com.his.patient.vo.NurseScheduleVO;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 病区护理人力配置标准 Mapper（sql/166）。
 *
 * <p>规则行是排班校验的依据，也是护理部「这个病区每天至少要几个后夜班」的书面口径。
 * 自定义 SQL 不受 {@code @TableLogic} 覆盖，需要 {@code del_flag = 0} 的自行加。
 */
@Mapper
public interface BizNurseScheduleRuleMapper extends BaseMapper<BizNurseScheduleRule> {

    /**
     * 本病区规则行（<b>含停用行</b>）：校验侧只认 status=1，但维护页要能看见被停用的那几条，
     * 否则「为什么这条标准不生效」在界面上无从发现。
     */
    @Select("""
            SELECT id, ward_id AS wardId, ward_name AS wardName, shift_id AS shiftId, shift_name AS shiftName,
                   min_staff AS minStaff, max_staff AS maxStaff, max_week_hours AS maxWeekHours,
                   max_consecutive_night_days AS maxConsecutiveNightDays,
                   max_consecutive_work_days AS maxConsecutiveWorkDays, status, remark
              FROM biz_nurse_schedule_rule
             WHERE del_flag = 0 AND ward_id = #{wardId}
             ORDER BY shift_id, id
            """)
    List<NurseScheduleVO.Rule> selectRulesByWard(@Param("wardId") Long wardId);

    /** 全部病区规则（按病区聚合的校验用，一次捞完避免逐病区 N 次查询） */
    @Select("""
            <script>
            SELECT id, ward_id AS wardId, ward_name AS wardName, shift_id AS shiftId, shift_name AS shiftName,
                   min_staff AS minStaff, max_staff AS maxStaff, max_week_hours AS maxWeekHours,
                   max_consecutive_night_days AS maxConsecutiveNightDays,
                   max_consecutive_work_days AS maxConsecutiveWorkDays, status, remark
              FROM biz_nurse_schedule_rule
             WHERE del_flag = 0 AND status = 1
            <if test="wardIds != null">
              AND ward_id IN <foreach collection="wardIds" item="w" open="(" separator="," close=")">#{w}</foreach>
            </if>
             ORDER BY ward_id, shift_id, id
            </script>
            """)
    List<NurseScheduleVO.Rule> selectEnabledRules(@Param("wardIds") List<Long> wardIds);

    @Select("""
            SELECT id, ward_id AS wardId, ward_name AS wardName, shift_id AS shiftId, shift_name AS shiftName,
                   min_staff AS minStaff, max_staff AS maxStaff, max_week_hours AS maxWeekHours,
                   max_consecutive_night_days AS maxConsecutiveNightDays,
                   max_consecutive_work_days AS maxConsecutiveWorkDays, status, remark
              FROM biz_nurse_schedule_rule
             WHERE del_flag = 0 AND id = #{id}
            """)
    NurseScheduleVO.Rule selectRuleById(@Param("id") Long id);

    /**
     * 物理删规则行：唯一键 {@code uk_ward_shift_rule(ward_id, shift_id)} 不含 del_flag，
     * 软删后「给同一病区同一班次重建标准」必然 Duplicate entry（AGENTS §3）。
     */
    @Delete("DELETE FROM biz_nurse_schedule_rule WHERE id = #{id}")
    int purgeById(@Param("id") Long id);
}

package com.his.patient.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.his.patient.entity.BizPatientGuardian;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface BizPatientGuardianMapper extends BaseMapper<BizPatientGuardian> {

    /**
     * 取软删的绑定行（重绑复活用）。
     *
     * <p>必须裸 SQL：@TableLogic 会给 Wrapper 查询自动拼 del_flag=0，
     * 用 selectOne 查 del_flag=1 是「条件与框架过滤器互斥」，永远查不到。
     */
    @Select("SELECT * FROM biz_patient_guardian WHERE user_id = #{userId} AND patient_id = #{patientId} AND del_flag = 1 LIMIT 1")
    BizPatientGuardian selectSoftDeleted(@Param("userId") Long userId, @Param("patientId") Long patientId);

    /**
     * 复活软删绑定（同上，UPDATE 语句也会被自动拼 del_flag=0，只能裸 SQL）。
     * isDefault 由调用方算好传入（0/1 直接落列）。
     */
    @Update("UPDATE biz_patient_guardian SET del_flag = 0, status = 1, relation = #{relation}, " +
            "is_default = #{isDefault}, update_by = 'mini-guardian', update_time = NOW() " +
            "WHERE id = #{id} AND del_flag = 1")
    int reviveSoftDeleted(@Param("id") Long id, @Param("relation") Integer relation, @Param("isDefault") Integer isDefault);
}

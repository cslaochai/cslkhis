package com.his.patient.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.his.patient.entity.BizAttendingRelation;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 住院管床关系 Mapper。
 */
@Mapper
public interface BizAttendingRelationMapper extends BaseMapper<BizAttendingRelation> {

    /**
     * 物理删单行。
     *
     * <p>本表唯一键 {@code uk_adm_rel_emp(admission_id, relation_type, employee_id)} <b>不含删除标志</b>：
     * 软删后同一医生若被重新指派给同一次住院，「删掉的那条」仍在键位上，插入必撞重复键。
     * 结束一段关系请用「置失效」（status=0 + expire_time），不要删行。
     */
    @Delete("DELETE FROM biz_attending_relation WHERE id = #{id}")
    int purgeById(@Param("id") Long id);
}

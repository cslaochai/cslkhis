package com.his.patient.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.his.patient.entity.BizChargeNurseRelation;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 住院责任护士关系 Mapper。
 */
@Mapper
public interface BizChargeNurseRelationMapper extends BaseMapper<BizChargeNurseRelation> {

    /**
     * 物理删单行。
     *
     * <p>唯一键 {@code uk_adm_nurse(admission_id, relation_type, nurse_id)} 不含删除标志：
     * 软删后同一护士被重新指派给同一次住院会撞重复键。结束关系用「置失效」（status=0 + expire_time）。
     */
    @Delete("DELETE FROM biz_charge_nurse_relation WHERE id = #{id}")
    int purgeById(@Param("id") Long id);
}

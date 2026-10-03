package com.his.medicaltech.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.his.medicaltech.entity.BizLisEqaSample;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 质评盲样 Mapper
 */
@Mapper
public interface BizLisEqaSampleMapper extends BaseMapper<BizLisEqaSample> {

    /**
     * 物理删盲样台账行。
     *
     * <p>uk_sample（批次+样品序号+项目编码+仪器）唯一键不含 del_flag，
     * 走 BaseMapper 的 deleteById（@TableLogic 软删）会留下占位行，
     * 重新登记同一组合必撞 Duplicate entry —— 所以登记错了只能真的删掉。
     */
    @Delete("DELETE FROM biz_lis_eqa_sample WHERE id = #{id}")
    int purgeById(@Param("id") Long id);
}

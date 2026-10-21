package com.his.outpatient.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.his.outpatient.entity.BizOpdStation;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 门诊公共工位字典 Mapper。
 */
@Mapper
public interface BizOpdStationMapper extends BaseMapper<BizOpdStation> {

    /**
     * 物理删单行：唯一键是工位编码且不含删除标志，软删后复用同码必撞键。
     */
    @Delete("DELETE FROM biz_opd_station WHERE id = #{id}")
    int purgeById(@Param("id") Long id);
}

package com.his.medicaltech.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.his.medicaltech.entity.BizFilmSpec;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 胶片规格价目 Mapper（sql/138）。
 */
@Mapper
public interface BizFilmSpecMapper extends BaseMapper<BizFilmSpec> {

    /**
     * 物理删（uk_spec_code 不含 del_flag → 软删会让同编码规格再也建不出来）。
     */
    @Delete("DELETE FROM biz_film_spec WHERE id = #{id}")
    int purgeById(@Param("id") Long id);
}

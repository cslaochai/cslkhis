package com.his.patient.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.his.patient.entity.BizVisit;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 就诊次 Mapper
 */
@Mapper
public interface BizVisitMapper extends BaseMapper<BizVisit> {

    /**
     * 当天已生成的就诊次编号条数（用于序号）
     */
    @Select("SELECT COUNT(*) FROM biz_visit WHERE visit_no LIKE CONCAT(#{prefix}, '%')")
    long countByVisitNoPrefix(@Param("prefix") String prefix);
}

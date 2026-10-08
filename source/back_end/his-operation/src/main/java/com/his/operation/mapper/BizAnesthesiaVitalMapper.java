package com.his.operation.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.his.operation.entity.BizAnesthesiaVital;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 麻醉生命体征 Mapper（只增不改）。
 */
@Mapper
public interface BizAnesthesiaVitalMapper extends BaseMapper<BizAnesthesiaVital> {

    @Select("""
            SELECT COUNT(*) FROM biz_anesthesia_vital
            WHERE record_id = #{recordId} AND sample_time = #{sampleTime}
            """)
    long countSameTime(@Param("recordId") Long recordId, @Param("sampleTime") java.time.LocalDateTime sampleTime);

    @Select("""
            SELECT COUNT(*) FROM biz_anesthesia_vital
            WHERE record_id = #{recordId} AND id <> #{excludeId} AND sample_time = #{sampleTime}
            """)
    long countSameTimeExclude(@Param("recordId") Long recordId,
                              @Param("sampleTime") java.time.LocalDateTime sampleTime,
                              @Param("excludeId") Long excludeId);
}

package com.his.operation.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.his.operation.entity.BizAnesthesiaVital;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 麻醉生命体征 Mapper（只增不改）。
 *
 * <p>存在性的唯一判定是 {@code (record_id, sample_time)}：
 * 同一时刻再插一条会得到不同的自增值，靠 id 判重等于没判 —— 必须用业务键计数。
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

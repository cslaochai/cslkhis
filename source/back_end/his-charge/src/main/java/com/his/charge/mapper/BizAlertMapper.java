package com.his.charge.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.his.charge.entity.BizAlert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;

/**
 * 告警 Mapper（P3 用它记住院欠费提醒）。
 */
@Mapper
public interface BizAlertMapper extends BaseMapper<BizAlert> {

    /**
     * 同类型告警在指定时间之后的条数（欠费提醒按"每天一次"去重，避免刷屏）
     */
    @Select("""
            SELECT COUNT(*)
            FROM biz_alert
            WHERE alert_type = #{alertType}
              AND remark LIKE CONCAT(#{keyword}, '%')
              AND notify_time >= #{since}
            """)
    long countRecent(@Param("alertType") String alertType,
                     @Param("keyword") String keyword,
                     @Param("since") LocalDateTime since);

    /**
     * 当天已生成的告警号条数（单号序号用）
     */
    @Select("SELECT COUNT(*) FROM biz_alert WHERE alert_no LIKE CONCAT(#{prefix}, '%')")
    long countByAlertNoPrefix(@Param("prefix") String prefix);
}

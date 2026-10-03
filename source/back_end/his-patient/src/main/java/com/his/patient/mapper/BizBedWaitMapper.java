package com.his.patient.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.his.patient.entity.BizBedWait;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 等床队列 Mapper
 *
 * <p><b>排序口径在 Java 侧（Wrapper orderBy）维护</b>：priority DESC → register_time ASC → id ASC。
 * 分页必须带二级键 id —— 同一个 (priority, register_time) 组合下只有 id 能保证翻页不重不漏。
 */
@Mapper
public interface BizBedWaitMapper extends BaseMapper<BizBedWait> {

    @Select("SELECT COUNT(*) FROM biz_bed_wait WHERE del_flag = 0 AND wait_no LIKE CONCAT(#{prefix}, '%')")
    long countByWaitNoPrefix(@Param("prefix") String prefix);

    /** 等待中的记录：用于算全局排队位次（seq），不含分页 */
    @Select("""
            SELECT id FROM biz_bed_wait
             WHERE del_flag = 0 AND wait_status = 0
             ORDER BY priority DESC, register_time ASC, id ASC
            """)
    List<Long> selectWaitingIds();
}

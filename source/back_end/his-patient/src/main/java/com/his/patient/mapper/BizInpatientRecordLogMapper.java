package com.his.patient.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.his.patient.entity.BizInpatientRecordLog;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 住院文书修改日志 Mapper。
 */
@Mapper
public interface BizInpatientRecordLogMapper extends BaseMapper<BizInpatientRecordLog> {

    /**
     * 某单据的全部日志（不分页 —— 修改轨迹断页没有意义）
     */
    @Select("""
            SELECT * FROM biz_inpatient_record_log l
            WHERE l.del_flag = 0
              AND l.doc_type = #{docType}
              AND l.record_id = #{recordId}
            ORDER BY l.create_time ASC, l.id ASC
            """)
    List<BizInpatientRecordLog> selectByRecord(@Param("docType") Integer docType,
                                               @Param("recordId") Long recordId);
}

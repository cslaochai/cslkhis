package com.his.patient.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.his.patient.entity.BizInpatientRecordLog;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 住院文书修改日志 Mapper。
 *
 * <p>分页查询走 {@code LambdaQueryWrapper}（无 JOIN，写 @Select 反而要手拼 IN 字符串，
 * 那是经典的注入与截断陷阱）。这里只留一个"按单据取全"的方法 —— 修改轨迹必须给全，
 * 详情页不提供分页。
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

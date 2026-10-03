package com.his.emr.mapper;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.his.emr.dto.OutpatientLogQueryDTO;
import com.his.emr.vo.OutpatientLogListVO;
import com.his.emr.vo.OutpatientLogStatsVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 门诊日志（法规台账）只读查询。列表与统计共用 XML 里的同一段 WHERE/推导片段，
 * 所以走 XML mapper（注解 @Select 没法共享 sql 片段）。
 */
@Mapper
public interface OutpatientLogMapper {

    IPage<OutpatientLogListVO> selectLogPage(IPage<OutpatientLogListVO> page,
                                             @Param("q") OutpatientLogQueryDTO q,
                                             @Param("regex") String reportableRegex);

    OutpatientLogStatsVO selectLogStats(@Param("q") OutpatientLogQueryDTO q,
                                        @Param("regex") String reportableRegex);
}

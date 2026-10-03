package com.his.appoint.mapper;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.his.appoint.dto.OpdLogQueryDTO;
import com.his.appoint.vo.OpdLogListVO;
import com.his.appoint.vo.OpdLogStatsVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 门诊日志 Mapper（SQL 在 {@code resources/mapper/OpdLogMapper.xml}）。
 *
 * <p>单独一个 Mapper 而不是挂在 {@code BizAppointInfoMapper} 上：这一组查询的
 * 返回类型不是实体，而且列表与统计共用 WHERE 片段，用 XML 更清楚。
 */
@Mapper
public interface OpdLogMapper {

    /**
     * 门诊日志分页（基表=挂号，LEFT JOIN 队列）。
     */
    IPage<OpdLogListVO> selectOpdLogPage(IPage<OpdLogListVO> page, @Param("q") OpdLogQueryDTO q);

    /**
     * 门诊日志统计条，与分页同一套 WHERE，保证「统计跟着筛选走」。
     */
    OpdLogStatsVO selectOpdLogStats(@Param("q") OpdLogQueryDTO q);
}

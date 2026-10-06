package com.his.charge.mapper;

import com.his.charge.entity.BizYbInspection;
import com.his.charge.service.impl.YbInspectionServiceImpl;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 医保飞检批次 Mapper（分页与状态流转见 YbInspectionServiceImpl）。
 */
@Mapper
public interface BizYbInspectionMapper extends BaseMapper<BizYbInspection> {
}

package com.his.patient.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.his.patient.entity.BizInpatientOperation;
import org.apache.ibatis.annotations.Mapper;

/** 住院手术操作明细 Mapper */
@Mapper
public interface BizInpatientOperationMapper extends BaseMapper<BizInpatientOperation> {
}

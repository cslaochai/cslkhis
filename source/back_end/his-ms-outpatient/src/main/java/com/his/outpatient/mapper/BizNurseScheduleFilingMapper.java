package com.his.outpatient.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.his.outpatient.entity.BizNurseScheduleFiling;
import org.apache.ibatis.annotations.Mapper;

/**
 * 护理排班周备案（只 upsert 不删，无删除方法）
 */
@Mapper
public interface BizNurseScheduleFilingMapper extends BaseMapper<BizNurseScheduleFiling> {
}

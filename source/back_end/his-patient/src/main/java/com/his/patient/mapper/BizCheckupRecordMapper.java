package com.his.patient.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.his.patient.entity.BizCheckupRecord;
import org.apache.ibatis.annotations.Mapper;

/**
 * 体检登记 Mapper。体检编号含时间戳+随机段，天然含软删不撞唯一键。
 */
@Mapper
public interface BizCheckupRecordMapper extends BaseMapper<BizCheckupRecord> {
}

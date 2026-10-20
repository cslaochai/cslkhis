package com.his.charge.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.his.charge.entity.BizYbDeductLog;
import org.apache.ibatis.annotations.Mapper;

/**
 * 扣款处理留痕 Mapper（只插入、只按单据捞，永不更新删除）。
 */
@Mapper
public interface BizYbDeductLogMapper extends BaseMapper<BizYbDeductLog> {
}

package com.his.platform.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.his.platform.entity.SysPrintRecord;
import org.apache.ibatis.annotations.Mapper;

/**
 * 打印台账 Mapper。唯一键 uk_print_no(biz_type,biz_id,print_no) 兜并发重打。
 */
@Mapper
public interface SysPrintRecordMapper extends BaseMapper<SysPrintRecord> {
}

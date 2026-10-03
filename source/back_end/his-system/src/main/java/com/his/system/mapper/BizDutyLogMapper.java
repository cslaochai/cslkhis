package com.his.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.his.system.entity.BizDutyLog;
import org.apache.ibatis.annotations.Mapper;

/**
 * 值班日志 Mapper
 *
 * <p>与 {@link BizDutyRosterMapper} 相反，这里<b>不需要物理删</b>：总值班日志没有唯一键，
 * 软删的 {@code del_flag=1} 不会挡住任何写入，而值班记录是要留档的（评审要翻历史交班本）。
 */
@Mapper
public interface BizDutyLogMapper extends BaseMapper<BizDutyLog> {
}

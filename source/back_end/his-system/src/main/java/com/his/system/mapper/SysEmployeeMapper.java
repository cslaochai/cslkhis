package com.his.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.his.system.entity.SysEmployee;
import org.apache.ibatis.annotations.Mapper;

/**
 * 员工Mapper
 */
@Mapper
public interface SysEmployeeMapper extends BaseMapper<SysEmployee> {
}

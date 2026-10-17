package com.his.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.his.system.entity.SysEquipment;
import org.apache.ibatis.annotations.Mapper;

/**
 * 设备档案 Mapper（医疗设备台账为 49 号铺底既有表，本模块为该表唯一 Java 归属）。
 */
@Mapper
public interface SysEquipmentMapper extends BaseMapper<SysEquipment> {
}

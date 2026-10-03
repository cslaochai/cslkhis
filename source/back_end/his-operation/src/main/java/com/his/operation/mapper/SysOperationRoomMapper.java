package com.his.operation.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.his.operation.entity.SysOperationRoom;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 手术间主数据 Mapper。
 *
 * <p>{@code uk_room_code}/{@code uk_room_name} 均不含 del_flag，而 BaseMapper 的
 * {@code deleteById} 因 {@code @TableLogic} 是软删 → 软删行继续占唯一键，
 * 同码重建必 Duplicate entry（L12 同款坑）。本表删除一律走下面的物理删。
 */
@Mapper
public interface SysOperationRoomMapper extends BaseMapper<SysOperationRoom> {

    /** 物理删除手术间（撞的是 uk_room_code/uk_room_name：软删占键，删档无价值） */
    @Delete("DELETE FROM sys_operation_room WHERE id = #{roomId}")
    int purgeById(@Param("roomId") Long roomId);
}

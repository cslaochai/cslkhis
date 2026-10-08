package com.his.operation.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.his.operation.entity.SysOperationRoom;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 手术间主数据 Mapper。
 */
@Mapper
public interface SysOperationRoomMapper extends BaseMapper<SysOperationRoom> {

    /**
     * 物理删除手术间（撞的是 uk_room_code/uk_room_name：软删占键，删档无价值）
     */
    @Delete("DELETE FROM sys_operation_room WHERE id = #{roomId}")
    int purgeById(@Param("roomId") Long roomId);
}

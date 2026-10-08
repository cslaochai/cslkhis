package com.his.operation.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.his.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 手术间主数据（手术间）—— 排台总表的"台"。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_operation_room")
public class SysOperationRoom extends BaseEntity {

    /**
     * 手术间编码（OR01、OR02…）
     */
    private String roomCode;

    /**
     * 手术间名称（排台仍按名称文本快照落申请单）
     */
    private String roomName;

    /**
     * 位置（楼层/区域）
     */
    private String location;

    /**
     * 总表列顺序（升序）
     */
    private Integer sortOrder;

    /**
     * 状态（1-启用 0-停用）
     */
    private Integer status;
}

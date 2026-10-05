package com.his.operation.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.his.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 手术间主数据（手术间）—— 排台总表的"台"。
 *
 * <p>在 sql/134 之前，手术间只是手术申请单.operation_room 的一列自由文本，
 * 下拉候选靠 distinct 历史值 —— 全院有几个手术间、开没开，没有任何地方回答得了。
 *
 * <p>⚠ 唯一键（room_code / room_name）不含 del_flag，且本表删除走<b>物理删</b>
 * （{@code SysOperationRoomMapper.purgeById}）：软删留下的行会继续占键，
 * 同码重建必然 Duplicate entry。停用请用 status=0，不要删。
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

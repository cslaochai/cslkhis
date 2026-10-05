package com.his.operation.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.io.Serializable;

/**
 * 手术间主数据出参（排台总表的列头 + 手术间管理表格）。
 */
@Data
public class OperationRoomVO implements Serializable {

    /**
     * 主键ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    /**
     * 手术间编码
     */
    private String roomCode;

    /**
     * 手术间名称
     */
    private String roomName;

    /**
     * 位置
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

    /**
     * 状态文本
     */
    private String statusText;

    /**
     * 备注
     */
    private String remark;
}

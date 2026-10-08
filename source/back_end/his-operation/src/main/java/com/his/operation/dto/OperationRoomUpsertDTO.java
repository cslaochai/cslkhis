package com.his.operation.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serializable;

/**
 * 手术间主数据入参（新增 / 修改）。
 */
@Data
public class OperationRoomUpsertDTO implements Serializable {

    /**
     * 手术间ID（为空 = 新增；不为空 = 修改）
     */
    private Long id;

    /**
     * 手术间编码（必填，如 OR01）
     */
    @NotBlank(message = "手术间编码不能为空")
    @Size(max = 32, message = "手术间编码过长")
    private String roomCode;

    /**
     * 手术间名称（必填，如 1号手术间；排台快照用它）
     */
    @NotBlank(message = "手术间名称不能为空")
    @Size(max = 64, message = "手术间名称过长")
    private String roomName;

    /**
     * 位置（楼层/区域，可空）
     */
    @Size(max = 200, message = "位置过长")
    private String location;

    /**
     * 总表列顺序（空按 1）
     */
    private Integer sortOrder;

    /**
     * 状态：1-启用 0-停用（空按启用）
     */
    private Integer status;

    /**
     * 备注
     */
    @Size(max = 500, message = "备注过长")
    private String remark;
}

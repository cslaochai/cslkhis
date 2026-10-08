package com.his.emr.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;

/**
 * 处理跟踪登记入参（追加一条流水，可同时推进状态）。
 */
@Data
public class DisputeFollowDTO implements Serializable {

    @NotNull(message = "单据ID不能为空")
    private Long id;

    @NotBlank(message = "处理动作不能为空")
    private String action;

    /**
     * 处理说明
     */
    @NotBlank(message = "处理说明不能为空")
    private String content;

    /**
     * 目标状态:2-调查中 3-处理中（不传=仅记录流水不改变状态）
     */
    private Integer toStatus;
}

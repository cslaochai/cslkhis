package com.his.emr.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;

/**
 * 纠纷/投诉动作入参（受理 / 补封存 / 撤销 —— 都只需要单据ID + 可选说明）。
 */
@Data
public class DisputeActionDTO implements Serializable {

    @NotNull(message = "单据ID不能为空")
    private Long id;

    /**
     * 动作说明（撤销必填）
     */
    private String content;
}

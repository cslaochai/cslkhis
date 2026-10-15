package com.his.emr.dto;

import com.his.common.validation.AllowedValues;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;

/**
 * 发放单状态推进入参（标记已推送 / 标记患者拒答）。
 */
@Data
public class SurveyDispatchActionDTO implements Serializable {

    @NotNull(message = "发放单ID不能为空")
    private Long id;

    /**
     * 动作:1-标记已推送 2-标记已拒答
     */
    @NotNull(message = "动作不能为空")
    @AllowedValues(values = {"1", "2"}, message = "动作不合法：1-标记已推送 2-标记已拒答")
    private Integer action;

    /**
     * 说明（拒答原因等，服务端截到 500）
     */
    private String remark;
}

package com.his.operation.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;

/**
 * 术前核对入参（三方核查的可核对部分）。
 */
@Data
public class OperationPreopCheckDTO implements Serializable {

    /**
     * 手术申请单ID（必填）
     */
    @NotNull(message = "手术申请单ID不能为空")
    private Long applyId;

    /**
     * 术前核对要点码（逗号分隔，如 1,2,3,4；必填，且必须含 1/2/3/4）
     */
    private String checkItems;

    /**
     * 术前核对补充说明（异常项写这里）
     */
    private String preopNote;

    /**
     * 备注
     */
    private String remark;
}

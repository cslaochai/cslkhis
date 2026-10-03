package com.his.emr.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 不良事件 PDCA 流转入参（处理 / 整改 / 结案共用）
 */
@Data
public class AdverseEventActionDTO {

    /** 事件主键 */
    @NotNull(message = "事件ID不能为空")
    private Long id;

    /** 处理意见 / 整改措施 / 验证结论（按接口语义） */
    @NotBlank(message = "意见内容不能为空")
    private String remark;
}

package com.his.emr.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 代煎单作废入参（本表没有删除路径，作废是唯一让它停止流转的动作）
 *
 * <p>原因只加 @NotBlank 不加 @Size：入参层的 400 会抢在服务端截断之前，
 * 等于把「药师粘贴了一长段说明」变成请求失败（AGENTS §3）。
 */
@Data
public class TcmDecoctCancelDTO {

    @NotNull(message = "代煎单ID不能为空")
    private Long id;

    /** 原因 */
    @NotBlank(message = "作废必须填写原因")
    private String reason;
}

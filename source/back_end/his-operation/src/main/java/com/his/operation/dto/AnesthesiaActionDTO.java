package com.his.operation.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;

/**
 * 麻醉记录单动作（提交 / 审核 / 计费）共用入参。
 *
 * <p>三个动作同形 {@code (id, remark)}，刻意不为每个动作建一个只有一句话不同的 DTO ——
 * 那是把接口数量当成设计成本的分摊方式，只会让前端多维护三个形状。
 */
@Data
public class AnesthesiaActionDTO implements Serializable {

    /** 麻醉记录单ID / PACU 记录ID（按动作语义取用） */
    @NotNull(message = "记录ID不能为空")
    private Long id;

    /** 备注 */
    private String remark;
}

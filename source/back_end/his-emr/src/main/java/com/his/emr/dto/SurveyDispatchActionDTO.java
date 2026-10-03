package com.his.emr.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;

/**
 * 发放单状态推进入参（标记已推送 / 标记患者拒答）。
 *
 * <p>只有「未回收」的三种状态能被人工推进：已回收由答卷写入，已过期由时间现算，
 * 手工把单置成已回收等于伪造回收率。
 */
@Data
public class SurveyDispatchActionDTO implements Serializable {

    @NotNull(message = "发放单ID不能为空")
    private Long id;

    /** 动作:1-标记已推送 2-标记已拒答 */
    @NotNull(message = "动作不能为空")
    private Integer action;

    /** 说明（拒答原因等，服务端截到 500） */
    private String remark;
}

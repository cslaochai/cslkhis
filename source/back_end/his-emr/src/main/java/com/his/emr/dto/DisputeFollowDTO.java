package com.his.emr.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;

/**
 * 处理跟踪登记入参（追加一条流水，可同时推进状态）。
 *
 * <p>toStatus 由前端传「推进到哪一档」，服务端只接受 2调查中 / 3处理中两个合法目标，
 * 不接受直接跳 4已结案（结案必须走 close，要收口赔偿与责任认定）。
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

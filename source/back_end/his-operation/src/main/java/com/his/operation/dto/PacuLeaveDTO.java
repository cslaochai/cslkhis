package com.his.operation.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * PACU 出室入参。
 *
 * <p><b>Aldrete &lt; 9 出室必须写明原因，且去向不能是「回病房」</b>：
 * 不达标回病房就是把"还没醒的人"当成"醒了的人"交出去，
 * 这类事故复盘时最常说的一句话就是"当时评分没到就走了"。
 */
@Data
public class PacuLeaveDTO implements Serializable {

    @NotNull(message = "PACU 记录ID不能为空")
    private Long pacuId;

    /**
     * 出室时间：不传取当前时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime leaveTime;

    /**
     * 出室去向（1-回病房 2-转ICU 3-继续留观）
     */
    @NotNull(message = "出室去向不能为空")
    private Integer disposition;

    /**
     * 未达出室标准时的说明（Aldrete < 9 且去向=回病房时必填）
     */
    private String note;

    /**
     * 备注
     */
    private String remark;
}

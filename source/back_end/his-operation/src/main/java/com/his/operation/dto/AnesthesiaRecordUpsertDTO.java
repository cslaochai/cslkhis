package com.his.operation.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 开立麻醉记录单入参。
 *
 * <p><b>麻醉方式可以不传</b>：不传就取手术申请单上登记的 {@code anesthesia_type}。
 * 允许不同是因为"排了腰硬、上台后发现要改全麻"是真实且常见的事 ——
 * 申请单上是计划，记录单上是事实，<b>以记录单为准</b>（与首页手术明细取"实际做的"同一口径）。
 */
@Data
public class AnesthesiaRecordUpsertDTO implements Serializable {

    /**
     * 手术申请单ID
     */
    @NotNull(message = "手术申请单ID不能为空")
    private Long applyId;

    /**
     * 麻醉医师ID（员工ID，姓名服务端查）
     */
    private Long anesthetistId;

    /**
     * 麻醉助手姓名（多人逗号分隔）
     */
    private String assistantAnesthetistName;

    /**
     * 麻醉方式（1-全麻 2-椎管内 3-神经阻滞 4-局麻 5-其他）
     */
    private Integer anesthesiaType;

    /**
     * ASA 分级：不传则取访视单结论
     */
    private Integer asaGrade;

    /**
     * 入室时间：不传则取当前时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime enterRoomTime;

    /**
     * 备注
     */
    private String remark;
}

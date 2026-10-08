package com.his.operation.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.his.operation.entity.BizAnesthesiaRecord;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 麻醉记录单出参。
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class AnesthesiaRecordVO extends BizAnesthesiaRecord {

    /**
     * 患者编号
     */
    private String patientNo;

    private String admissionNo;

    /**
     * 来源访视单号（急诊超前麻醉时为空，此时 visitPending=true）
     */
    private String visitNo;

    private Integer visitStatus;

    /**
     * 访视结论：1-可施行麻醉 2-暂缓 3-需会诊
     */
    private Integer visitConclusion;

    private String visitConclusionText;

    /**
     * 手术申请单侧快照
     */
    private String plannedOperationName;

    private String actualOperationName;

    private String operationRoom;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime plannedStartTime;

    private Integer operationStatus;

    private String operationStatusText;

    /**
     * 申请单上的麻醉方式（与记录单上的可能不同：临时改全麻时以记录单为准）
     */
    private Integer applyAnesthesiaType;

    private String applyAnesthesiaTypeText;

    private String surgeonName;

    private Integer isEmergency;

    private String emergencyText;

    // 文案
    private String recordStatusText;
    private String anesthesiaTypeText;
    private String asaText;
    private String airwayDeviceText;
    private String ventilationText;
    private String effectText;
    private String dispositionText;
    private String chargeStatusText;

    // 时长（分钟）
    /**
     * 麻醉时长 = 麻醉开始 → 麻醉结束
     */
    private Long anesthesiaMinutes;

    private String anesthesiaDurationText;

    /**
     * 手术时长 = 切皮 → 关腹
     */
    private Long operationMinutes;

    private String operationDurationText;

    /**
     * 计费用的麻醉监护小时数（不足 1 小时按 1 小时）
     */
    private BigDecimal billHours;

    // 明细
    private List<AnesthesiaVitalVO> vitals;

    private List<AnesthesiaMedVO> meds;

    private Integer vitalCount;

    private Integer medCount;

    // 能力位
    /**
     * 记录中：可补充体征与用药
     */
    private Boolean canEditVitals;
    /**
     * 记录中：可提交（提交后固化）
     */
    private Boolean canSubmit;
    /**
     * 已提交：可审核
     */
    private Boolean canAudit;
    /**
     * 已审核且去向为入PACU：可开 PACU 复苏单
     */
    private Boolean canOpenPacu;
    /**
     * 已提交/已审核且未计费：可计费
     */
    private Boolean canCharge;

    /**
     * 待补术前访视（急诊超前麻醉）：记录存在但没有来源访视单
     */
    private Boolean visitPending;

    private String warningText;
}

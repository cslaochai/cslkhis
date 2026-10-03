package com.his.operation.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.his.operation.entity.BizAnesthesiaVisit;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

/**
 * 麻醉术前访视单出参。
 *
 * <p>{@code can*} 一组由后端判定，<b>前端不要按 visitStatus/结论自己 switch</b>：
 * "能不能改、能不能作为麻醉依据"只有服务端知道（尤其是急诊超前麻醉的场景）。
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class AnesthesiaVisitVO extends BizAnesthesiaVisit {

    /** 来自手术申请单的选修快照（列表里显示"这台做什么手术"，免二次请求） */
    private String admissionNo;

    /** 患者编号 */
    private String patientNo;

    /** 主刀医师姓名（来自手术申请单） */
    private String surgeonName;

    /** 计划开始时间（来自手术申请单） */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private java.time.LocalDateTime plannedStartTime;

    private String operationRoom;

    /** 手术申请状态：0-待排期 1-已排期 2-术前核对完成 3-已完成 4-已取消 */
    private Integer operationStatus;

    private String operationStatusText;

    // 文案
    private String asaText;
    private String asaFullText;
    private String mallampatiText;
    private String neckMobilityText;
    private String npoText;
    private String conclusionText;
    private String visitStatusText;
    private String difficultAirwayText;
    private String anesthesiaTypeText;
    private String emergencyText;

    /** BMI（体重 / 身高²；任一缺失就为空，不猜） */
    private BigDecimal bmi;

    // 能力位
    /** 草稿可继续编辑；已完成后不再改（评估结论出账后就不再是草稿） */
    private Boolean canEdit;
    /** 可以据此开立麻醉记录（结论=可施行麻醉且已完成） */
    private Boolean canOpenRecord;
    /** 可以完成访视（草稿 → 已完成；结论必填） */
    private Boolean canFinish;

    /** 提示文案（如：困难气道已标记，必须写明备选方案） */
    private String warningText;
}

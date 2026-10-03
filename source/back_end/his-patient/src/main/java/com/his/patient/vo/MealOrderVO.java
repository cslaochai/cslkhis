package com.his.patient.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 订餐配送出参（食堂配送单/签收台）。
 *
 * <p>{@code canAdvance / canCancel / nextStatusText} 由服务层按状态机现算 —— 按钮可用性是业务规则，
 * 让前端自己判就会出现"点得动、后端拒"的错位。
 */
@Data
public class MealOrderVO implements Serializable {

    /** 主键 */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    /** 订餐单号 */
    private String mealNo;

    /** 入院ID */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long admissionId;
    private String admissionNo;

    /** 患者ID */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long patientId;
    /** 患者编号（快照） */
    private String patientNo;
    /** 患者姓名（快照） */
    private String patientName;

    /** 科室ID（快照） */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long deptId;
    /** 科室名称（快照） */
    private String deptName;

    /** 病区ID */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long wardId;
    /** 病区名称（快照） */
    private String wardName;
    /** 床号（快照） */
    private String bedNo;

    /** 来源膳食方案ID */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long dietPlanId;

    /** 饮食类型码 */
    private String dietCode;
    /** 饮食名称（快照） */
    private String dietName;

    /** 就餐日期 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate mealDate;

    /** 餐次（1-早餐 2-午餐 3-晚餐 4-加餐） */
    private Integer mealType;
    private String mealTypeText;

    /** 份数 */
    private Integer quantity;
    /** 配餐内容/食谱 */
    private String dishContent;

    /** 配餐状态（0-待配餐 1-已配餐 2-已配送 3-已签收 4-已取消） */
    private Integer deliverStatus;
    private String deliverStatusText;

    /** 下一步状态文案（"配餐/配送/签收"，终态为空） */
    private String nextStatusText;

    private Integer canAdvance;
    private Integer canCancel;

    /** 配餐完成时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime prepareTime;
    /** 配送出仓时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime deliverTime;
    /** 配送人（员工ID） */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long deliverById;
    /** 配送人姓名（快照） */
    private String deliverByName;
    /** 签收时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime signTime;
    /** 签收人（患者/家属/护士姓名） */
    private String signBy;
    /** 退订时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime cancelTime;
    /** 退订原因（停餐/出院/拒餐/转科等，必填） */
    private String cancelReason;

    /** 来源（1-按膳食方案批量生成 2-手工加订） */
    private Integer source;
    private String sourceText;

    /** 备注 */
    private String remark;
}

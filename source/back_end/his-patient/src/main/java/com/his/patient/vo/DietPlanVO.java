package com.his.patient.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 膳食方案出参。
 *
 * <p>{@code confirmStatus} 是本页的主角：待接收 = 医生已经开了膳食医嘱但食堂还不知道，
 * 列表默认按它排在最前。
 */
@Data
public class DietPlanVO implements Serializable {

    /**
     * 主键
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    /**
     * 膳食方案编号
     */
    private String dietNo;

    /**
     * 入院ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long admissionId;
    private String admissionNo;

    /**
     * 患者ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long patientId;
    /**
     * 患者编号（快照）
     */
    private String patientNo;
    /**
     * 患者姓名（快照）
     */
    private String patientName;
    /**
     * 性别（1-男 2-女 9-未知）
     */
    private Integer gender;
    /**
     * 年龄
     */
    private Integer age;

    /**
     * 科室ID（快照）
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long deptId;
    /**
     * 科室名称（快照）
     */
    private String deptName;

    /**
     * 病区ID（快照）
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long wardId;
    /**
     * 病区名称（快照）
     */
    private String wardName;
    /**
     * 床号（快照）
     */
    private String bedNo;

    /**
     * 在院状态：1-在院 0-已出院
     */
    private Integer admitStatus;

    /**
     * 来源医嘱ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long orderId;
    /**
     * 来源医嘱号
     */
    private String orderNo;

    /**
     * 来源（1-医嘱校对派生 2-营养师手工登记）
     */
    private Integer source;
    private String sourceText;

    /**
     * 饮食类型码
     */
    private String dietCode;
    /**
     * 饮食类别（1-基本饮食 2-治疗饮食 3-诊断试验饮食 4-营养支持）
     */
    private Integer dietCategory;
    private String dietCategoryText;
    /**
     * 饮食名称
     */
    private String dietName;

    /**
     * 给食途径：1-口服 2-管饲 3-静脉
     */
    private Integer route;
    private String routeText;
    /**
     * 该途径是否需要食堂订餐（1-是 0-否），由 route 现算
     */
    private Integer needsMeal;

    /**
     * 管饲/输注方式说明
     */
    private String feedWay;

    /**
     * 每日热量目标 kcal
     */
    private Integer calorieTarget;
    /**
     * 每日蛋白目标 g
     */
    private Integer proteinTarget;
    /**
     * 每日液体量 ml
     */
    private Integer fluidTarget;

    /**
     * 供应餐次
     */
    private String mealTypes;
    /**
     * 餐次文案（"早餐、午餐、晚餐"／"不订餐"）
     */
    private String mealTypesText;

    /**
     * 开始时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime startTime;
    /**
     * 停止时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime stopTime;

    /**
     * 方案状态（1-执行中 2-已停止 3-已作废）
     */
    private Integer planStatus;
    private String planStatusText;

    /**
     * 营养科接收状态（0-待接收 1-已接收 2-已退回）
     */
    private Integer confirmStatus;
    private String confirmStatusText;

    /**
     * 接收/退回时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime confirmTime;
    /**
     * 接收人
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long confirmerId;
    /**
     * 接收人姓名（快照）
     */
    private String confirmerName;
    /**
     * 退回原因
     */
    private String rejectReason;

    /**
     * 备注
     */
    private String remark;

    /**
     * 今日订餐条数（按 meal_date=今天统计，方案页"订餐"列用）
     */
    private Integer todayMealCount;
}

package com.his.operation.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.his.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import com.baomidou.mybatisplus.annotation.TableLogic;


/**
 * 麻醉记录单—— 一台手术一份（UNIQUE apply_id）。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("biz_anesthesia_record")
public class BizAnesthesiaRecord extends BaseEntity {
    /** 逻辑删除标志（0 未删除 1 已删除） */
    @TableLogic
    private Integer delFlag;

    /** 备注 */
    private String remark;



    /**
     * 麻醉记录单号（MZ + yyyyMMdd + 4位序号）
     */
    private String recordNo;

    /**
     * 手术申请单ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long applyId;

    /**
     * 手术申请单号
     */
    private String applyNo;

    /**
     * 入院ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long admissionId;

    /**
     * 患者ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long patientId;

    /**
     * 患者姓名
     */
    private String patientName;

    /**
     * 性别（1-男 2-女）
     */
    private Integer gender;

    /**
     * 年龄
     */
    private Integer age;

    /**
     * 来源术前访视单ID（急诊抢救可空，届时会标"待补访视"）
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long visitId;

    /**
     * 麻醉方式（1-全麻 2-椎管内 3-神经阻滞 4-局麻 5-其他）
     */
    private Integer anesthesiaType;

    /**
     * ASA 分级
     */
    private Integer asaGrade;

    /**
     * 麻醉医师ID（员工ID）
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long anesthetistId;

    /**
     * 麻醉医师姓名
     */
    private String anesthetistName;

    /**
     * 麻醉助手姓名
     */
    private String assistantAnesthetistName;

    /**
     * 麻醉方法描述
     */
    private String anesthesiaMethodDetail;

    /**
     * 气道管理方式（0-无 1-气管插管 2-喉罩 3-面罩 4-其他）
     */
    private Integer airwayDevice;

    /**
     * 气道器具规格
     */
    private String airwayDeviceSpec;

    /**
     * 通气方式（1-自主呼吸 2-辅助通气 3-控制通气）
     */
    private Integer ventilationMode;

    /**
     * 入手术室时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime enterRoomTime;

    /**
     * 麻醉开始时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime anesthesiaStartTime;

    /**
     * 手术开始时间（切皮）
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime operationStartTime;

    /**
     * 手术结束时间（关腹/关胸）
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime operationEndTime;

    /**
     * 麻醉结束时间（停药）
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime anesthesiaEndTime;

    /**
     * 出手术室时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime leaveRoomTime;

    /**
     * 晶体液入量（ml）
     */
    private Integer crystalloid;

    /**
     * 胶体液入量（ml）
     */
    private Integer colloid;

    /**
     * 异体输血量（ml）
     */
    private Integer bloodTransfusion;

    /**
     * 自体血回输量（ml）
     */
    private Integer autotransfusion;

    /**
     * 术中尿量（ml）
     */
    private Integer urineOutput;

    /**
     * 术中出血量（ml）
     */
    private Integer bloodLoss;

    /**
     * 是否发生麻醉不良事件（0-无 1-有）
     */
    private Integer adverseEventFlag;

    /**
     * 不良事件经过与处理
     */
    private String adverseEventNote;

    /**
     * 麻醉效果（1-满意 2-欠佳 3-失败改麻醉方式）
     */
    private Integer anesthesiaEffect;

    /**
     * 术后去向（1-回病房 2-入PACU 3-入ICU）
     */
    private Integer postopDisposition;

    /**
     * 记录状态（0-记录中 1-已提交 2-已审核）
     */
    private Integer recordStatus;

    /**
     * 提交人ID（员工ID）
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long submitDoctorId;

    /**
     * 提交人姓名
     */
    private String submitDoctorName;

    /**
     * 提交时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime submitTime;

    /**
     * 审核人ID（员工ID）
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long auditDoctorId;

    /**
     * 审核人姓名
     */
    private String auditDoctorName;

    /**
     * 审核时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime auditTime;

    /**
     * 计费状态（0-未计费 1-已计费 2-计费异常）
     */
    private Integer chargeStatus;

    /**
     * 记账单号（费用记账流水的费用编号，本次计费最后一笔）
     */
    private String feeNo;

    /**
     * 本次计入金额（元）
     */
    private BigDecimal chargedAmount;

    /**
     * 计费失败原因
     */
    private String chargeFailReason;
}

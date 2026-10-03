package com.his.medicaltech.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.his.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 血库交叉配血记录
 *
 * <p>status：1 待配血 / 2 已配血 / 3 已复核 / 4 已作废。
 * 复核人不得与配血人同一人；配血相合的复核通过后血袋自动置「已预留」。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("biz_blood_crossmatch")
public class BizBloodCrossmatch extends BaseEntity {

    /** 配血编号（唯一，PX+yyyyMMdd+序） */
    private String matchNo;

    /** 用血申请单号 */
    private String applyNo;

    /** 患者ID */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long patientId;

    /** 患者号 */
    private String patientNo;

    /** 患者姓名 */
    private String patientName;

    /** 患者血型（1-A 2-B 3-O 4-AB） */
    private Integer patientBloodType;

    /** 患者 Rh（1-阳性 2-阴性） */
    private Integer patientRhType;

    /** 血袋号 */
    private String bagNo;

    /** 血袋血型 */
    private Integer bagBloodType;

    /** 血袋 Rh */
    private Integer bagRhType;

    /** 血液成分 */
    private Integer componentType;

    /** 血量（ml） */
    private Integer volume;

    /** 配血方法（1-盐水法 2-凝聚胺法 3-抗人球蛋白法 4-微柱凝胶法） */
    private Integer method;

    /** 配血结果（1-相合 2-不相合 3-可疑凝集） */
    private Integer result;

    /** 配血结论 */
    private String conclusion;

    /** 状态（1-待配血 2-已配血 3-已复核 4-已作废） */
    private Integer status;

    /** 配血人 */
    private String operator;

    /** 配血时间 */
    private LocalDateTime matchTime;

    /** 复核人 */
    private String verifier;

    /** 复核时间 */
    private LocalDateTime verifyTime;
}

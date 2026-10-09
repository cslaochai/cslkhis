package com.his.patient.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.his.common.base.BaseEntity;
import com.his.patient.enums.NoticeStatusEnum;
import com.his.patient.enums.NoticeTypeEnum;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;
import com.baomidou.mybatisplus.annotation.TableLogic;


/**
 * 病危/病重通知单（病危重通知回执，sql/161）。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("biz_critical_notice")
public class BizCriticalNotice extends BaseEntity {
    /** 逻辑删除标志（0 未删除 1 已删除） */
    @TableLogic
    private Integer delFlag;

    /** 备注 */
    private String remark;



    /**
     * 单号（BT+yyyyMMdd+4位）
     */
    private String noticeNo;

    /**
     * 住院记录ID
     */
    private Long admissionId;
    /**
     * 患者ID
     */
    private Long patientId;
    /**
     * 患者姓名
     */
    private String patientName;
    /**
     * 患者编号
     */
    private String patientNo;
    /**
     * 性别（1-男 2-女 9-未知）
     */
    private Integer gender;
    /**
     * 年龄
     */
    private Integer age;
    /**
     * 开单科室ID
     */
    private Long deptId;
    /**
     * 开单科室名称
     */
    private String deptName;
    /**
     * 病区名称
     */
    private String wardName;
    /**
     * 床位号
     */
    private String bedNo;
    /**
     * 住院号
     */
    private String admissionNo;

    /**
     * 通知类别（1-病危 2-病重）
     */
    private Integer noticeType;
    /**
     * 患者神志（1-清醒 2-嗜睡 3-意识模糊 4-昏迷 9-其他）
     */
    private Integer consciousnessStatus;
    /**
     * 目前诊断
     */
    private String clinicalDiagnosis;
    /**
     * 病情及危险因素
     */
    private String conditionDesc;
    /**
     * 可能的病情变化与预警事项
     */
    private String warningMatters;
    /**
     * 医方已采取/拟采取的诊治措施与配合要求
     */
    private String doctorMeasures;
    /**
     * 告知时间
     */
    private LocalDateTime notifyTime;

    /**
     * 告知医师ID
     */
    private Long doctorId;
    /**
     * 告知医师姓名
     */
    private String doctorName;
    /**
     * 见证医师ID
     */
    private Long witnessDoctorId;
    /**
     * 见证医师姓名（可空）
     */
    private String witnessDoctorName;

    /**
     * 签收人姓名
     */
    private String signerName;
    /**
     * 签收人与患者关系
     */
    private Integer signerRelation;
    /**
     * 签收人证件号
     */
    private String signerIdCard;
    /**
     * 签收人联系电话
     */
    private String signerPhone;
    /**
     * 家属手写签名（base64 PNG dataURL，回执打印原样贴图）
     */
    private String signerSignature;
    /**
     * 签收时间
     */
    private LocalDateTime acknowledgeTime;

    /**
     * 状态（1-草稿 2-已签发 3-已签收 4-已作废）
     */
    private Integer noticeStatus;
    /**
     * 签发
     */
    private LocalDateTime issueTime;
    /**
     * 最后打印人
     */
    private String printerName;
    /**
     * 回执打印次数
     */
    private Integer printCount;
    /**
     * 最后打印时间
     */
    private LocalDateTime lastPrintTime;
    /**
     * 作废原因
     */
    private String voidReason;
    /**
     * 作废经办人
     */
    private String voidBy;
    /**
     * 作废时间
     */
    private LocalDateTime voidTime;

    /**
     * 电子签名状态（0-未签名 1-已签名 2-签名已作废，ObjectSignStatus 同码）
     */
    private Integer signStatus;
    /**
     * 当前有效签名ID
     */
    private Long signId;
    /**
     * 签名时刻
     */
    private LocalDateTime signedTime;

    public static String statusText(Integer status) {
        if (status == null) {
            return "—";
        }
        return NoticeStatusEnum.getText(status);
    }

    public static String typeText(Integer type) {
        if (type == null) {
            return "—";
        }
        return NoticeTypeEnum.getText(type);
    }
}

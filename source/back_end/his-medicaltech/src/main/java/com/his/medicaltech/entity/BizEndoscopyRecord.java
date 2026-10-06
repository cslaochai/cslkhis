package com.his.medicaltech.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.his.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 内镜检查记录
 *
 * <p>状态机：1 已登记 → 2 已签到 → 3 检查中 → 4 已出报告 → 5 已审核 → 6 已发布；7 已取消。
 * 与超声共用字典 his_endous_status。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("biz_endoscopy_record")
public class BizEndoscopyRecord extends BaseEntity {

    /**
     * 内镜检查号（唯一，NJ+yyyyMMdd+序）
     */
    private String recordNo;

    /**
     * 患者ID
     */
    private Long patientId;

    /**
     * 患者号
     */
    private String patientNo;

    /**
     * 患者姓名
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
     * 就诊日期
     */
    private LocalDate visitDate;

    /**
     * 申请科室ID
     */
    private Long applyDeptId;

    /**
     * 申请科室
     */
    private String applyDeptName;

    /**
     * 申请医生ID
     */
    private Long applyDoctorId;

    /**
     * 申请医生
     */
    private String applyDoctorName;

    /**
     * 临床诊断
     */
    private String clinicalDiagnosis;

    /**
     * 内镜类型（1-胃镜 2-肠镜 3-支气管镜 4-膀胱镜 5-宫腔镜 6-喉镜 7-ERCP 8-胶囊内镜）
     */
    private Integer endoType;

    /**
     * 麻醉方式（1-无麻醉 2-表面麻醉 3-静脉麻醉 4-全身麻醉）
     */
    private Integer anesthesiaMethod;

    /**
     * 检查部位 / 到达范围
     */
    private String bodyPart;

    /**
     * 检查目的
     */
    private String examPurpose;

    /**
     * 肠道准备质量 Boston 评分（0~9，肠镜适用）
     */
    private Integer bowelPrepScore;

    /**
     * 幽门螺杆菌（0-未查 1-阴性 2-阳性，胃镜适用）
     */
    private Integer hpResult;

    /**
     * 内镜所见
     */
    private String findings;

    /**
     * 内镜诊断
     */
    private String diagnosis;

    /**
     * 建议
     */
    private String suggestion;

    /**
     * 是否活检（0-否 1-是）
     */
    private Integer biopsyFlag;

    /**
     * 活检部位
     */
    private String biopsyPart;

    /**
     * 活检块数
     */
    private Integer biopsyCount;

    /**
     * 关联病理号（活检送检后回填）
     */
    private String pathologyOrderNo;

    /**
     * 内镜医师
     */
    private String endoscopist;

    /**
     * 检查时间
     */
    private LocalDateTime executeTime;

    /**
     * 状态（1-已登记 2-已签到 3-检查中 4-已出报告 5-已审核 6-已发布 7-已取消）
     */
    private Integer status;

    /**
     * 报告医师
     */
    private String reportBy;

    /**
     * 报告时间
     */
    private LocalDateTime reportTime;

    /**
     * 审核医师（不得与报告医师同一人）
     */
    private String auditBy;

    /**
     * 审核时间
     */
    private LocalDateTime auditTime;

    /**
     * 发布人
     */
    private String publishBy;

    /**
     * 发布时间
     */
    private LocalDateTime publishTime;

    /**
     * 取消时间
     */
    private LocalDateTime cancelTime;

    /**
     * 取消原因
     */
    private String cancelReason;
}

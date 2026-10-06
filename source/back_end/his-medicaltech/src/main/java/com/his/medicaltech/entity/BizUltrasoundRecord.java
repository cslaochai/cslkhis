package com.his.medicaltech.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.his.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 超声检查记录
 *
 * <p>状态机与内镜一致（共用字典 his_endous_status）：
 * 1 已登记 → 2 已签到 → 3 检查中 → 4 已出报告 → 5 已审核 → 6 已发布；7 已取消。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("biz_ultrasound_record")
public class BizUltrasoundRecord extends BaseEntity {

    /**
     * 超声检查号（唯一，CS+yyyyMMdd+序）
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
     * 超声类型（1-腹部 2-心脏 3-妇产 4-血管 5-浅表器官 6-肌骨 7-腔内）
     */
    private Integer usType;

    /**
     * 检查部位
     */
    private String bodyPart;

    /**
     * 检查目的
     */
    private String examPurpose;

    /**
     * 超声所见
     */
    private String findings;

    /**
     * 超声提示（结论）
     */
    private String conclusion;

    /**
     * 建议
     */
    private String suggestion;

    /**
     * 检查医师
     */
    private String sonographer;

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

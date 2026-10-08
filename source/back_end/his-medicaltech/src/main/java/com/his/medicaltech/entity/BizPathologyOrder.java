package com.his.medicaltech.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.his.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 病理检查主单
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("biz_pathology_order")
public class BizPathologyOrder extends BaseEntity {

    /**
     * 病理号（唯一，BL+yyyyMMdd+序）
     */
    private String orderNo;

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
     * 病理检查类型（1-常规石蜡 2-术中冰冻 3-细胞学 4-免疫组化 5-疑难会诊）
     */
    private Integer examType;

    /**
     * 标本类型（活检/切除/穿刺/脱落细胞等）
     */
    private String specimenType;

    /**
     * 取材部位
     */
    private String specimenPart;

    /**
     * 是否冰冻（0-否 1-是）
     */
    private Integer isFrozen;

    /**
     * 术中冰冻快速诊断结果
     */
    private String frozenResult;

    /**
     * 标本接收时间
     */
    private LocalDateTime receiveTime;

    /**
     * 标本接收人
     */
    private String receiveBy;

    /**
     * 取材时间
     */
    private LocalDateTime samplingTime;

    /**
     * 取材人
     */
    private String samplingBy;

    /**
     * 包埋时间
     */
    private LocalDateTime embeddingTime;

    /**
     * 包埋人
     */
    private String embeddingBy;

    /**
     * 制片（切片）
     */
    private LocalDateTime sliceTime;

    /**
     * 制片人
     */
    private String sliceBy;

    /**
     * 肉眼所见
     */
    private String grossFindings;

    /**
     * 镜下所见
     */
    private String microscopyFindings;

    /**
     * 免疫组化 / 特殊染色结果
     */
    private String ihcResult;

    /**
     * 病理诊断
     */
    private String diagnosis;

    /**
     * 建议
     */
    private String suggestion;

    /**
     * 状态（1-已登记 2-已接收标本 3-已取材 4-已制片 5-已初诊 6-已审核 7-已发布 8-已取消）
     */
    private Integer status;

    /**
     * 初诊医师
     */
    private String reportBy;

    /**
     * 初诊时间
     */
    private LocalDateTime reportTime;

    /**
     * 审核医师（不得与初诊同一人）
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

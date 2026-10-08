package com.his.patient.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * VTE 预防措施记录（一次住院 × 一个措施码一条）。
 */
@Data
@TableName("biz_vte_prevent")
public class BizVtePrevent {

    /**
     * 主键
     */
    @TableId(type = IdType.ASSIGN_ID)
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    /**
     * 措施记录编号 VP+yyyyMMdd+4位
     */
    private String preventNo;

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
     * 患者编号
     */
    private String patientNo;

    /**
     * 患者姓名
     */
    private String patientName;

    /**
     * 科室ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long deptId;

    /**
     * 科室名称
     */
    private String deptName;

    /**
     * 病区ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long wardId;

    /**
     * 病区名称
     */
    private String wardName;

    /**
     * 床号
     */
    private String bedNo;

    /**
     * 来源评估单ID（风险证据链：这个措施是按哪一次 Caprini 评估开的）
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long assessmentId;

    /**
     * Caprini 总分（登记时最新评估快照）
     */
    private Integer capriniScore;

    /**
     * 风险等级（1-低 2-中 3-高 4-极高）
     */
    private Integer riskLevel;

    /**
     * 措施码：BASIC / PHYSICAL / DRUG
     */
    private String measureCode;

    /**
     * 措施类别（1-基础预防 2-物理预防 3-药物预防）
     */
    private Integer measureType;

    /**
     * 措施名称
     */
    private String measureName;

    /**
     * 计划执行日期
     */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate planDate;

    /**
     * 落实状态（0-待落实 1-已落实 2-禁忌未用 3-患者拒绝）
     */
    private Integer executeStatus;

    /**
     * 落实时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime executeTime;

    /**
     * 执行人（员工ID）
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long executorId;

    /**
     * 执行人姓名
     */
    private String executorName;

    /**
     * 未落实原因（禁忌/拒绝必填）
     */
    private String reason;

    /**
     * 创建人
     */
    private String createBy;
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
    /**
     * 更新人
     */
    private String updateBy;
    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
    /**
     * 删除标志（0-正常 1-删除）
     */
    private Integer delFlag;
    /**
     * 备注
     */
    private String remark;
}

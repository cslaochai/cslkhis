package com.his.patient.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * VTE 风险名单行（中高危名单 / 全院风险一览）。
 *
 * <p>preventStatus 是名单最有用的一个字段：护士扫一眼就知道"这个人还没落实"，
 * 而不是点进去翻三条措施记录自己数。0-未登记 1-部分落实 2-已落实。
 */
@Data
public class VteRiskListVO {

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
     * 床位号
     */
    private String bedNo;

    /**
     * 入院时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime admitTime;

    /**
     * 在院状态：1-在院 0-已出院
     */
    private Integer admitStatus;

    /**
     * 诊断
     */
    private String diagnosis;

    /**
     * 来源评估单（最新一条 Caprini）
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long assessmentId;

    private Integer capriniScore;

    private Integer riskLevel;

    private String riskLevelText;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime assessTime;

    private String assessNurseName;

    /**
     * 按风险等级应落实的措施码
     */
    private List<String> recommendCodes;

    private String recommendText;

    /**
     * 三条措施各自的当前状态（没登记的类型不出，前端按 recommendCodes 对齐展示）
     */
    private List<VteMeasureStateVO> measures;

    /**
     * 已落实条数
     */
    private Integer doneCount;

    /**
     * 应落实条数
     */
    private Integer recommendCount;

    /**
     * 0-未登记 1-部分落实 2-已落实
     */
    private Integer preventStatus;

    private String preventStatusText;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime latestExecuteTime;
}

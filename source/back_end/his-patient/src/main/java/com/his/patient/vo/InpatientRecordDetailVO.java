package com.his.patient.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 住院病历文书详情 VO（含全部结构化要素）。
 */
@Data
public class InpatientRecordDetailVO implements Serializable {

    /**
     * 文书ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    /**
     * 病历文书号
     */
    private String recordNo;

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
     * 性别（1-男 2-女）
     */
    private Integer gender;

    /**
     * 性别文案
     */
    private String genderText;

    /**
     * 年龄
     */
    private Integer age;

    /**
     * 年龄单位（1-岁 2-月 3-天）
     */
    private Integer ageUnit;

    /**
     * 年龄单位文案
     */
    private String ageUnitText;

    /**
     * 年龄展示（如 "65岁"）
     */
    private String ageText;

    /**
     * 科室名称
     */
    private String deptName;

    /**
     * 病区名称
     */
    private String wardName;

    /**
     * 床号
     */
    private String bedNo;

    // 文书本体

    /**
     * 文书类型
     */
    private Integer recordType;

    /**
     * 文书类型文案
     */
    private String recordTypeText;

    /**
     * 文书标题
     */
    private String recordTitle;

    /**
     * 记录时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime recordTime;

    // 结构化要素：病史

    /**
     * 主诉
     */
    private String chiefComplaint;

    /**
     * 现病史
     */
    private String presentIllness;

    /**
     * 既往史
     */
    private String pastHistory;

    /**
     * 个人史（含婚育、烟酒、职业）
     */
    private String personalHistory;

    /**
     * 家族史
     */
    private String familyHistory;

    /**
     * 过敏史
     */
    private String allergyHistory;

    // 结构化要素：生命体征

    /**
     * 体温（℃）
     */
    private BigDecimal temperature;

    /**
     * 脉搏（次/分）
     */
    private Integer pulse;

    /**
     * 呼吸（次/分）
     */
    private Integer respiration;

    /**
     * 收缩压（mmHg）
     */
    private Integer systolicPressure;

    /**
     * 舒张压（mmHg）
     */
    private Integer diastolicPressure;

    /**
     * 身高（cm）
     */
    private BigDecimal height;

    /**
     * 体重（kg）
     */
    private BigDecimal weight;

    /**
     * 生命体征一行摘要（如 "T36.8℃ P78次/分 R18次/分 BP120/80mmHg"；全空时为 "—"）
     */
    private String vitalSignsText;

    // 结构化要素：体格检查

    /**
     * 一般情况（神志/发育/营养/体位/面容）
     */
    private String generalCondition;

    /**
     * 皮肤黏膜
     */
    private String skinMucosa;

    /**
     * 头颈部
     */
    private String headNeck;

    /**
     * 胸部及肺
     */
    private String chestLung;

    /**
     * 心脏
     */
    private String heart;

    /**
     * 腹部
     */
    private String abdomen;

    /**
     * 脊柱四肢
     */
    private String spineLimbs;

    /**
     * 神经系统
     */
    private String nervousSystem;

    /**
     * 专科检查
     */
    private String specialistExam;

    // 结构化要素：结论

    /**
     * 辅助检查
     */
    private String auxiliaryExam;

    /**
     * 诊断名称
     */
    private String diagnosisName;

    /**
     * 诊断编码
     */
    private String diagnosisCode;

    /**
     * 诊疗计划 / 处理意见
     */
    private String treatmentPlan;

    /**
     * 病程记录正文
     */
    private String courseNote;

    // 状态与结构化率

    /**
     * 文书状态（1-草稿 2-已提交 3-已归档）
     */
    private Integer recordStatus;

    /**
     * 文书状态文案
     */
    private String recordStatusText;

    /**
     * 书写医生姓名
     */
    private String doctorName;

    /**
     * 提交时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime submitTime;

    /**
     * 归档时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime archiveTime;

    /**
     * 归档人姓名
     */
    private String archiveByName;

    /**
     * 备注
     */
    private String remark;

    /**
     * 已填结构化要素数
     */
    private Integer structuredFilled;

    /**
     * 应填结构化要素数（分母，按文书类型算）
     */
    private Integer structuredTotal;

    /**
     * 结构化率（百分数）
     */
    private BigDecimal structuredRate;

    /**
     * 结构化率文案
     */
    private String structuredRateText;

    /**
     * 缺失要素中文名
     */
    private List<String> missingLabels;

    /**
     * 要素明细（要素编码 / 中文名 / 分组 / 是否已填 / 当前值），供前端画"结构化体检表"
     */
    private List<ElementVO> elements;

    /**
     * 是否可编辑
     */
    private Boolean canEdit;

    /**
     * 是否可提交
     */
    private Boolean canSubmit;

    /**
     * 是否可归档
     */
    private Boolean canArchive;

    /**
     * 签名状态（0-未签名 1-已签名 2-签名已失效）
     */
    private Integer signStatus;

    /**
     * 签名状态文案
     */
    private String signStatusText;

    /**
     * 当前有效签名ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long signId;

    /**
     * 最近一次签名时刻
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime signedTime;

    /**
     * 签名对编辑的约束提示：已签名时给出"为什么改不了、该怎么改"，
     * 让前端不必自己拼这段话（后端给文案，前端只展示 —— 与 P1 医嘱按钮同一口径）
     */
    private String signLockHint;

    /**
     * 单个结构化要素的明细
     */
    @Data
    public static class ElementVO implements Serializable {

        /**
         * 要素编码（= 库列名）
         */
        private String code;

        /**
         * 要素中文名
         */
        private String label;

        /**
         * 所属分组编码
         */
        private String group;

        /**
         * 所属分组中文名
         */
        private String groupLabel;

        /**
         * 是否已填
         */
        private Boolean filled;

        /**
         * 当前值（已填时给，数值列转成字符串便于统一展示）
         */
        private String value;
    }
}

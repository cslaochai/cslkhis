package com.his.operation.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 住院手术申请出参。
 */
@Data
public class OperationApplyVO implements Serializable {

    /**
     * 手术申请单ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

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
     * 入院号
     */
    private String admissionNo;

    /**
     * 入院状态（1-在院 0-已出院）—— 已出院的手术单不允许再排台/完成
     */
    private Integer admitStatus;

    /**
     * 入院状态文案
     */
    private String admitStatusText;

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
     * 患者姓名（申请时快照）
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
     * 申请科室ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long applyDeptId;

    /**
     * 申请科室名称
     */
    private String applyDeptName;

    /**
     * 申请时所在病区名称
     */
    private String applyWardName;

    /**
     * 申请时床号
     */
    private String applyBedNo;

    /**
     * 申请医生ID（员工ID）
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long applyDoctorId;

    /**
     * 申请医生姓名
     */
    private String applyDoctorName;

    /**
     * 申请时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime applyTime;

    // 拟施手术

    /**
     * 拟施手术编码
     */
    private String plannedOperationCode;

    /**
     * 拟施手术名称
     */
    private String plannedOperationName;

    /**
     * 手术级别（1-一级 2-二级 3-三级 4-四级）
     */
    private Integer operationLevel;

    /**
     * 手术级别文案
     */
    private String operationLevelText;

    /**
     * 切口等级（0-0类 1-Ⅰ类 2-Ⅱ类 3-Ⅲ类）
     */
    private Integer incisionLevel;

    /**
     * 切口等级文案
     */
    private String incisionLevelText;

    /**
     * 麻醉方式（1-全麻 2-椎管内 3-神经阻滞 4-局麻 5-其他）
     */
    private Integer anesthesiaType;

    /**
     * 麻醉方式文案
     */
    private String anesthesiaTypeText;

    /**
     * 术前诊断
     */
    private String preopDiagnosis;

    /**
     * 手术指征/理由
     */
    private String operationReason;

    /**
     * 是否急诊手术（0-否 1-是）
     */
    private Integer isEmergency;

    /**
     * 急诊文案
     */
    private String isEmergencyText;

    /**
     * 是否主要手术：0-否 1-是
     */
    private Integer isMain;

    /**
     * 主要手术文案
     */
    private String isMainText;

    // 排台

    /**
     * 手术间
     */
    private String operationRoom;

    /**
     * 计划开始时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime plannedStartTime;

    /**
     * 计划结束时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime plannedEndTime;

    /**
     * 计划时段文案（如 2026-09-19 09:00~11:00）
     */
    private String plannedTimeText;

    /**
     * 主刀医师ID（员工ID）
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long surgeonId;

    /**
     * 主刀医师姓名
     */
    private String surgeonName;

    /**
     * 助手姓名
     */
    private String assistantName;

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
     * 排台操作人姓名
     */
    private String scheduleDoctorName;

    /**
     * 排台时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime scheduleTime;

    /**
     * 排台备注
     */
    private String scheduleRemark;

    // 术前核对

    /**
     * 术前核对要点码
     */
    private String preopCheckItems;

    /**
     * 术前核对要点文案（列表展示用）
     */
    private String preopCheckItemsText;

    /**
     * 术前核对补充说明
     */
    private String preopNote;

    /**
     * 术前核对人姓名
     */
    private String preopCheckDoctorName;

    /**
     * 术前核对时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime preopCheckTime;

    // 术中 / 术后

    /**
     * 实际手术编码
     */
    private String actualOperationCode;

    /**
     * 实际手术名称（与拟施不一致时以它为准，首页记的也是它）
     */
    private String actualOperationName;

    /**
     * 实际开始时间（切皮）
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime operationStartTime;

    /**
     * 实际结束时间（关腹/关胸）
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime operationEndTime;

    /**
     * 术中出血量（ml）
     */
    private Integer bloodLoss;

    /**
     * 术中所见
     */
    private String intraopFindings;

    /**
     * 手术经过/操作步骤
     */
    private String intraopProcedure;

    /**
     * 术后处理与注意事项
     */
    private String postopNote;

    /**
     * 标本送检
     */
    private String specimenSent;

    // 完成 / 回写锚点

    /**
     * 完成录入人姓名
     */
    private String finishDoctorName;

    /**
     * 手术完成时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime finishTime;

    /**
     * 回写病案首页手术明细ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long operationId;

    /**
     * 回写的住院病历ID（record_type=5 手术记录）
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long recordId;

    /**
     * 回写的病历号（病历里查得到这台手术的证据）
     */
    private String recordNo;

    /**
     * 状态（0-待排期 1-已排期 2-术前核对完成 3-已完成 4-已取消）
     */
    private Integer operationStatus;

    /**
     * 状态文案
     */
    private String operationStatusText;

    /**
     * 取消原因
     */
    private String cancelReason;

    /**
     * 取消人姓名
     */
    private String cancelDoctorName;

    /**
     * 取消时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime cancelTime;

    /**
     * 备注
     */
    private String remark;

    // 以下为服务层算出的展示 / 可用性字段

    /**
     * 手术时长（分钟）：结束 - 开始
     */
    private Long durationMinutes;

    /**
     * 手术时长文案
     */
    private String durationText;

    /**
     * 等待/流转耗时文案（待排期 = 申请后已等待；已排期 = 距计划开始）
     */
    private String waitText;

    /**
     * 该单是否已卡住（术前核对完成但长时间未完成，24 小时）—— 查询时算，不落状态列
     */
    private Boolean stalled;

    /**
     * 卡住提示文案
     */
    private String stalledText;

    /**
     * 可否修改申请（仅待排期）
     */
    private Boolean canEdit;

    /**
     * 可否排台（待排期）
     */
    private Boolean canSchedule;

    /**
     * 可否术前核对（已排期）
     */
    private Boolean canPreopCheck;

    /**
     * 可否完成（术前核对完成）
     */
    private Boolean canFinish;

    /**
     * 可否取消（待排期 / 已排期）
     */
    private Boolean canCancel;

    /**
     * 术前核对要点字典（前端渲染勾选框用；键为码值，值为文案）
     */
    private List<CheckItem> checkItemOptions;

    /**
     * 三方安全核查已完成轮数（0~3）。
     *
     * <p>只在排台总表（scheduleMatrix）里批量回填，普通分页不查 —— 避免列表接口 N+1。
     */
    private Integer safetyCheckPhases;

    /**
     * 术前核对要点
     */
    @Data
    public static class CheckItem implements Serializable {
        /**
         * 码值
         */
        private Integer code;
        /**
         * 文案
         */
        private String label;
        /**
         * 是否必核项
         */
        private Boolean required;
    }
}

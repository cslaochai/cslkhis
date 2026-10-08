package com.his.emr.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 纠纷/投诉主单 VO。
 *
 * <p>三条口径：
 * <ol>
 *   <li><b>按钮可用性由后端给</b>（canEdit / canAccept / canFollow / canClose / canRevoke / canSeal），
 *       前端不按 status 码值 switch —— 后端加一档状态，前端 switch 会静默渲染成"看着正常"的错按钮。</li>
 *   <li>投诉人电话出参<b>脱敏</b>（库里存明文，出参中间四位打星）。</li>
 *   <li>码值文案一律走字典（his_dispute_*），VO 不重复造中文，避免两套说法。</li>
 * </ol>
 */
@Data
public class DisputeCaseVO implements Serializable {

    /**
     * 主键ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    /**
     * 单据编号
     */
    private String caseNo;

    /**
     * 类型（1-服务投诉 2-医疗纠纷 3-医疗损害争议 4-其他）
     */
    private Integer caseType;

    /**
     * 来源（1-来电 2-来访 3-来信 4-政务热线 5-上级交办 6-院内发现 7-其他）
     */
    private Integer sourceType;

    /**
     * 等级（1-一般 2-较大 3-重大）
     */
    private Integer level;

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
     * 关联住院ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long admissionId;

    /**
     * 被投诉科室ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long deptId;

    /**
     * 被投诉科室名称
     */
    private String deptName;

    /**
     * 涉及人员
     */
    private String involvedStaff;

    /**
     * 投诉人姓名（可为患者本人/家属/其他）
     */
    private String complainant;

    /**
     * 与患者关系（1-本人 2-家属 3-代理人 4-其他）
     */
    private Integer complainantRel;

    /**
     * 脱敏后的联系电话
     */
    private String complainantTel;

    /**
     * 事件发生时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime occurTime;

    /**
     * 事件发生地点
     */
    private String occurPlace;

    /**
     * 投诉/纠纷内容
     */
    private String content;

    /**
     * 投诉人诉求
     */
    private String demand;

    /**
     * 状态（1-待受理 2-调查中 3-处理中 4-已结案 5-已撤销）
     */
    private Integer status;

    /**
     * 是否需封存病历（0-否 1-是）
     */
    private Integer needSeal;

    /**
     * 封存状态（0-未申请 1-已封存 2-待归档后封存）
     */
    private Integer sealStatus;

    /**
     * 已封存病案ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long archiveId;

    /**
     * 封存时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime sealTime;

    /**
     * 处理途径（1-院内协商 2-医调委调解 3-行政调解 4-司法鉴定 5-诉讼 6-其他）
     */
    private Integer dealType;

    /**
     * 责任认定（1-无责 2-轻微责任 3-次要责任 4-主要责任 5-完全责任）
     */
    private Integer dutyType;

    /**
     * 赔偿/补偿金额
     */
    private BigDecimal compensation;

    /**
     * 调查结论/处理结果
     */
    private String conclusion;

    /**
     * 登记人
     */
    private String registerBy;

    /**
     * 登记时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime registerTime;

    /**
     * 受理人
     */
    private String acceptBy;

    /**
     * 受理时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime acceptTime;

    /**
     * 结案人
     */
    private String closeBy;

    /**
     * 结案时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime closeTime;

    /**
     * 撤销原因
     */
    private String revokeReason;

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;

    /**
     * 备注
     */
    private String remark;

    /**
     * 处理跟踪台账（仅详情回填）
     */
    private List<DisputeFlowVO> flows;

    /**
     * 受理至今天数（未受理为 0；已结案=受理→结案天数，服务端算）
     */
    private Integer openDays;

    /**
     * 是否可编辑（仅待受理）
     */
    private Boolean canEdit;

    /**
     * 是否可受理（仅待受理）
     */
    private Boolean canAccept;

    /**
     * 是否可登记处理跟踪（调查中/处理中）
     */
    private Boolean canFollow;

    /**
     * 是否可结案（调查中/处理中）
     */
    private Boolean canClose;

    /**
     * 是否可撤销（非终态）
     */
    private Boolean canRevoke;

    /**
     * 是否可补封存（需封存但受理时暂无已归档病历）
     */
    private Boolean canSeal;

    /**
     * 是否可删除（仅待受理且无跟踪流水）
     */
    private Boolean canDelete;
}

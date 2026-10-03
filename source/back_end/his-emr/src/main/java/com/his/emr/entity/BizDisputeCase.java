package com.his.emr.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 医疗纠纷 / 投诉登记主单。
 *
 * <p>字段与医疗纠纷投诉主单完全对齐（实体多一列 → 全表 select 500）。
 *
 * <p>状态机单向：1待受理 → 2调查中 → 3处理中 → 4已结案（终态）；未结案 → 5已撤销（终态）。
 */
@Data
@TableName("biz_dispute_case")
public class BizDisputeCase {

    /** 主键ID */
    @TableId(type = IdType.ASSIGN_ID)
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    /** 单据编号（DS+yyyyMMdd+4位） */
    private String caseNo;

    /** 类型（1-服务投诉 2-医疗纠纷 3-医疗损害争议 4-其他） */
    private Integer caseType;

    /** 来源:1-来电 2-来访 3-来信 4-政务热线 5-上级交办 6-院内发现 7-其他 */
    private Integer sourceType;

    /** 等级（1-一般 2-较大 3-重大） */
    private Integer level;

    /** 患者ID */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long patientId;

    /** 患者编号（快照） */
    private String patientNo;

    /** 患者姓名（快照） */
    private String patientName;

    /** 关联住院ID */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long admissionId;

    /** 被投诉科室ID */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long deptId;

    /** 被投诉科室名称（快照） */
    private String deptName;

    /** 涉及人员 */
    private String involvedStaff;

    /** 投诉人姓名（可为患者本人/家属/其他） */
    private String complainant;

    /** 与患者关系（1-本人 2-家属 3-代理人 4-其他） */
    private Integer complainantRel;

    /** 投诉人联系电话 */
    private String complainantTel;

    /** 事件发生时间 */
    private LocalDateTime occurTime;

    /** 事件发生地点 */
    private String occurPlace;

    /** 投诉/纠纷内容 */
    private String content;

    /** 投诉人诉求 */
    private String demand;

    /** 状态（1-待受理 2-调查中 3-处理中 4-已结案 5-已撤销） */
    private Integer status;

    /** 是否需封存病历（0-否 1-是） */
    private Integer needSeal;

    /** 封存状态（0-未申请 1-已封存 2-待归档后封存） */
    private Integer sealStatus;

    /** 已封存病案ID */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long archiveId;

    /** 封存时间 */
    private LocalDateTime sealTime;

    /** 处理途径:1-院内协商 2-医调委调解 3-行政调解 4-司法鉴定 5-诉讼 6-其他 */
    private Integer dealType;

    /** 责任认定（1-无责 2-轻微责任 3-次要责任 4-主要责任 5-完全责任） */
    private Integer dutyType;

    /** 赔偿/补偿金额 */
    private BigDecimal compensation;

    /** 调查结论/处理结果 */
    private String conclusion;

    /** 登记人 */
    private String registerBy;

    /** 登记时间 */
    private LocalDateTime registerTime;

    /** 受理人 */
    private String acceptBy;

    /** 受理时间 */
    private LocalDateTime acceptTime;

    /** 结案人 */
    private String closeBy;

    /** 结案时间 */
    private LocalDateTime closeTime;

    /** 撤销原因 */
    private String revokeReason;

    /** 创建人 */
    private String createBy;

    /** 创建时间 */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /** 更新人 */
    private String updateBy;

    /** 更新时间 */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    /** 删除标志（0-正常 1-删除） */
    @TableField(fill = FieldFill.INSERT)
    private Integer delFlag;

    /** 备注 */
    private String remark;
}

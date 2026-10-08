package com.his.patient.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.his.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 住院死亡登记簿（住院死亡登记簿，sql/157）。
 *
 * <p>证明回答「死因是什么」，登记回答「这个人死了之后院内怎么处理的」：
 * 死亡类型（是否外部原因）、报没报公安、遗体交给谁、家属领了哪几联、有没有纠纷。
 * 本表是「非正常死亡是否报案」与「遗体去向」的唯一事实来源。
 *
 * <p>硬闸：{@code deathType ≠ 1}（非疾病死亡/死因不明）必须 {@code policeFlag = 1} 才能置「已登记」，
 * 这是法定要求（非正常死亡须经公安、司法部门由法医出具或核实），不是院内偏好。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("biz_death_registration")
public class BizDeathRegistration extends BaseEntity implements Serializable {

    /**
     * 死亡登记号（RG + yyyyMMdd + 4 位）
     */
    private String registerNo;

    /**
     * 住院记录ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long admissionId;

    /**
     * 死亡证明ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long certId;

    /**
     * 患者ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long patientId;

    /**
     * 死者姓名
     */
    private String patientName;

    /**
     * 死亡时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime deathTime;

    /**
     * 死亡科室ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long deathDeptId;

    /**
     * 死亡科室名称
     */
    private String deathDeptName;

    /**
     * 死亡床位号
     */
    private String deathBedNo;

    /**
     * 死亡类型（1-疾病死亡 2-非疾病死亡）
     */
    private Integer deathType;

    /**
     * 是否已报公安/司法（0-否 1-是）
     */
    private Integer policeFlag;

    /**
     * 受理公安机关
     */
    private String policeOrg;

    /**
     * 公安受理/案件编号
     */
    private String policeCaseNo;

    /**
     * 报案时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime policeReportTime;

    /**
     * 是否由法医出具/检验（0-否 1-是）
     */
    private Integer forensicFlag;

    /**
     * 尸体处理方式（1-殡仪馆接运 2-家属自行处理 3-病理解剖 4-其他）
     */
    private Integer bodyDisposal;

    /**
     * 遗体接运/接收单位
     */
    private String bodyUnit;

    /**
     * 遗体移出时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime bodyTransportTime;

    /**
     * 办理人/近亲属姓名
     */
    private String relativeName;

    /**
     * 与死者关系
     */
    private String relativeRelation;

    /**
     * 联系电话
     */
    private String relativePhone;

    /**
     * 家属已领取联次（his_death_cert_copy 多值逗号分隔：1-记录联 2-户籍联 3-殡葬联 4-家属联）
     */
    private String receivedCopies;

    /**
     * 领取时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime receiveTime;

    /**
     * 是否存在医疗纠纷/患方异议（0-否 1-是）
     */
    private Integer disputeFlag;

    /**
     * 纠纷/异议情况
     */
    private String disputeDesc;

    /**
     * 状态（1-草稿 2-已登记 3-已作废）
     */
    private Integer registerStatus;

    /**
     * 登记人ID（值班医师/病区护士/防保科）
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long registrarId;

    /**
     * 登记人姓名
     */
    private String registrarName;

    /**
     * 登记（确认）
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime registerTime;

    /**
     * 作废原因
     */
    private String voidReason;

    /**
     * 作废时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime voidTime;
}

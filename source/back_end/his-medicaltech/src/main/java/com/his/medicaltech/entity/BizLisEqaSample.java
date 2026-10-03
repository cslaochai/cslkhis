package com.his.medicaltech.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.his.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 室间质评盲样台账（室间质评盲样）
 *
 * <p>粒度：批次 × 样品序号 × 检验项目 × 检测仪器。留了仪器这个维度，
 * 是因为「同一个盲样在两台机上测出来不一样」本身就是 EQA 要抓的问题（室间差），
 * 不落到行级别就比不出来。
 *
 * <p>两条独立的状态轴，别混：
 * <ul>
 *   <li>status（业务流转）：0 待检测 → 1 已检测 → 2 已上报 → 3 已回报</li>
 *   <li>result_status（成绩好坏）：0-未判定 1-满意 2-尚可 3-不合格，只在成绩回报时由服务端写</li>
 * </ul>
 * 流转到 3 之前 result_status 恒为 0 —— 靶值还没回来，谈"合不合格"就是编的。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("biz_lis_eqa_sample")
public class BizLisEqaSample extends BaseEntity {

    /** 质评批次ID */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long planId;

    /** 质评批次号（冗余） */
    private String planNo;

    /** 组织方下发的盲样原始编号 */
    private String sampleNo;

    /** 第几个样品（1..N） */
    private Integer sampleSeq;

    /** 检验项目ID */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long itemId;

    /** 检验项目编码 */
    private String itemCode;

    /** 检验项目名称 */
    private String itemName;

    /** 检测仪器（室间差按此分组；表上 NOT NULL DEFAULT ''，不做 nullable 是为了唯一键能生效） */
    private String instrumentName;

    /** 检测方法学 */
    private String methodName;

    /** 盲样接收日期 */
    private LocalDate receiveDate;

    /** 盲样接收人 */
    private String receiveBy;

    /** 本室测定值 */
    private BigDecimal testValue;

    /** 检测人 */
    private String testBy;

    /** 检测时间 */
    private LocalDateTime testTime;

    /** 1-超过批次上报截止日才上报 */
    private Integer overdueFlag;

    /** 回报靶值 / 组均值 */
    private BigDecimal targetValue;

    /** 回报组标准差（SDI 口径用） */
    private BigDecimal groupSd;

    /** 允许总误差 TEa（%） */
    private BigDecimal tea;

    /** 可接受范围下限 */
    private BigDecimal targetMin;

    /** 可接受范围上限 */
    private BigDecimal targetMax;

    /** SDI =（测定值-靶值）/组SD */
    private BigDecimal sdi;

    /** 偏倚% =（测定值-靶值）/靶值×100 */
    private BigDecimal biasRate;

    /** 判定口径（0-无法判定 1-SDI 2-允许总误差 3-可接受范围） */
    private Integer judgeMode;

    /** 判定结果（0-未判定 1-满意 2-尚可 3-不合格） */
    private Integer resultStatus;

    /** 流转状态（0-待检测 1-已检测 2-已上报 3-已回报） */
    private Integer status;

    /** 整改：0-无需 1-待整改 2-已整改（只有不合格项才会置 1） */
    private Integer handleStatus;

    /** 不合格原因分析 */
    private String handleCause;

    /** 纠正措施 */
    private String handleMeasure;

    /** 整改人 */
    private String handleBy;

    /** 整改时间 */
    private LocalDateTime handleTime;

    /** 复核人（不得与整改人同一人） */
    private String reviewBy;

    /** 复核时间 */
    private LocalDateTime reviewTime;
}

package com.his.charge.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.his.common.base.PageParam;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;
import java.util.List;

/**
 * 结算账单分页查询入参
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class BillQueryPageDTO extends PageParam {

    /**
     * 关键字：账单号 / 患者姓名 / 就诊单号模糊匹配
     */
    private String keyword;

    /**
     * 患者ID
     */
    private Long patientId;

    private Integer encounterType;

    /**
     * 就诊标识
     */
    private Long encounterId;

    /**
     * 只看还没收齐的账单（1-待支付 / 2-部分支付）：患者端待缴列表用。
     * 单独一个 billStatus 表达不了"还没付清"这个业务概念（它是两态），
     * 让每个调用方各自写 IN (1,2) 迟早有人写成 =1，把部分支付的患者漏掉。
     */
    private Boolean unpaidOnly;

    /**
     * 账单状态（字典 his_bill_status：1-待支付 2-部分支付 3-已支付 4-已作废 5-已退费）
     */
    private Integer billStatus;

    /**
     * 账单状态集合（多选，退费申请发起页用它筛「收过钱的账单」= 3-已支付 / 5-已退费）。
     * 与 billStatus 同时给时本字段优先 —— 单值是集合 size=1 的特例，语义不冲突。
     */
    private List<Integer> billStatusList;

    /**
     * 账单类型（字典 his_bill_type）
     */
    private Integer billType;

    /**
     * 结算方式（字典 his_settlement_mode：1-自费 2-医保）
     */
    private Integer settlementMode;

    /**
     * 结算人（收费员）员工ID，班结/个人台账用
     */
    private Long billById;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate beginDate;

    /**
     * 结束日期
     */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate endDate;
}

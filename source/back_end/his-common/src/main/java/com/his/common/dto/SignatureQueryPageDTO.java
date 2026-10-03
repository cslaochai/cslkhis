package com.his.common.dto;

import com.his.common.base.PageParam;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 签名记录分页查询入参。
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class SignatureQueryPageDTO extends PageParam {

    /** 签名对象类型（1-住院病历 2-门诊病历 3-住院医嘱） */
    private Integer bizType;

    /** 签名对象ID */
    private Long bizId;

    /** 对象单号（快照） */
    private String bizNo;

    /** 签名人员工ID */
    private Long signerId;

    /** 签名场景（1-提交 2-归档 3-开立 4-校对 5-补签） */
    private Integer signScene;

    /** 签名状态（1-有效 2-已作废） */
    private Integer signStatus;

    /** 最近一次验签结果（0-未校验 1-通过 2-失败） */
    private Integer verifyStatus;

    /** 时间来源（1-本机时钟 2-院内授时服务器 3-第三方TSA） */
    private Integer timeSource;

    /** 签名时间起（含） */
    private String beginTime;

    /** 签名时间止（含） */
    private String endTime;

    /** 关键字：签名人姓名 / 患者姓名 / 对象单号 */
    private String keyword;
}

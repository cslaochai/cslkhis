package com.his.clinical.dto;

import com.his.common.enums.QcTriggerSourceEnum;
import com.his.clinical.support.QcOperator;
import lombok.Data;

/**
 * 系统触发质控入参：结诊收尾、定时补扫这类没有登录态的入口用它发起一次质控。
 *
 * <p>为什么不复用执行质控入参：那个是质控页的 HTTP 契约，把操作人与触发来源塞进去，
 * 等于任何人都能在报文里指定「质控人」，而质控单要进考核与整改流程。
 */
@Data
public class QcSystemExecuteDTO {

    /**
     * 病历来源（OUTPATIENT-门诊病历 INPATIENT-住院文书）
     */
    private String recordSource;

    /**
     * 病历ID
     */
    private Long recordId;

    /**
     * 质控类型，为空按综合质控跑
     */
    private Integer qcType;

    /**
     * 触发来源（结诊自动 / 定时补扫）
     */
    private QcTriggerSourceEnum triggerSource;

    /**
     * 发起本次质控的操作人快照
     */
    private QcOperator operator;
}

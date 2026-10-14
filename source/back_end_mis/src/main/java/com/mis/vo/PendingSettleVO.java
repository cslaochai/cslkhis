package com.mis.vo;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.mis.entity.MssFeeItem;
import com.mis.entity.MssPendingSettle;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.List;

/**
 * 待结算业务出参（含费用明细）。
 * bizNo 为业务号，门诊即就诊号 visitNo、住院即住院号 admissionNo，按 bizType 派生输出。
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class PendingSettleVO extends MssPendingSettle implements Serializable {

    private List<MssFeeItem> items;

    public String getVisitNo() {
        return getBizType() != null && getBizType() == 1 ? getBizNo() : null;
    }

    public String getAdmissionNo() {
        return getBizType() != null && getBizType() == 2 ? getBizNo() : null;
    }

    /** 避免基类 bizNo 与派生号并存造成混淆，不直接输出 bizNo（业务号由 visitNo/admissionNo 表达） */
    @JsonIgnore
    public String getBizNo() {
        return super.getBizNo();
    }
}

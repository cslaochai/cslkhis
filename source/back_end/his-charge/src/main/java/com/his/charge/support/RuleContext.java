package com.his.charge.support;


import com.his.charge.config.ComplianceProperties;
import com.his.charge.entity.BizInsuranceSettlement;
import com.his.charge.entity.BizSettlementDiagnosis;
import com.his.charge.entity.BizSettlementOperation;
import com.his.charge.entity.SysDrgGroup;
import lombok.Data;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * 规则执行上下文。
 */
@Data
public class RuleContext {

    /**
     * 待审的结算清单
     */
    private BizInsuranceSettlement settlement;

    /**
     * 依据包
     */
    private SettlementEvidence evidence;

    /**
     * 清单诊断明细（审核对象）
     */
    private List<BizSettlementDiagnosis> diagnoses = new ArrayList<>();

    /**
     * 清单手术操作明细（审核对象）
     */
    private List<BizSettlementOperation> operations = new ArrayList<>();

    /**
     * 本院启用的医保 ICD-10 编码集合。
     * 空集合表示「目录不可用」，此时 A02 必须返回不适用，而不是把全部编码判为非法。
     */
    private Set<String> enabledIcdCodes = Collections.emptySet();

    /**
     * 命中的 DRG 分组（清单 drgCode 在分组表里查到才有值），可能为 null
     */
    private SysDrgGroup drgGroup;

    /**
     * 窗口期内同一患者的其他结算清单（分解住院判定用）
     */
    private List<BizInsuranceSettlement> recentSettlements = new ArrayList<>();

    /**
     * 阈值配置
     */
    private ComplianceProperties properties;

    /**
     * 分组表是否已有可用数据（为空时 D 组整体不适用）
     */
    private boolean drgTableReady;

    /**
     * 取主要诊断，可能为 null
     */
    public BizSettlementDiagnosis mainDiagnosis() {
        for (BizSettlementDiagnosis d : diagnoses) {
            if (d.getDiagType() != null && d.getDiagType() == 1) {
                return d;
            }
        }
        return diagnoses.isEmpty() ? null : diagnoses.get(0);
    }

    /**
     * 主要手术操作，可能为 null
     */
    public BizSettlementOperation mainOperation() {
        for (BizSettlementOperation o : operations) {
            if (o.getIsMain() != null && o.getIsMain() == 1) {
                return o;
            }
        }
        return operations.isEmpty() ? null : operations.get(0);
    }
}

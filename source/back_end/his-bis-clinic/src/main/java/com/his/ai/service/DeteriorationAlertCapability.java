package com.his.ai.service;

import com.his.ai.vo.DeteriorationExplainVO;
import com.his.ai.vo.DeteriorationScanVO;

import java.util.List;

/**
 * 危重预警·病情恶化评分（G-12）。
 */
public interface DeteriorationAlertCapability {

    /**
     * 病区扫描：每个有体征的在院患者评一次分（纯代码，无模型调用、无审计行）
     */
    List<DeteriorationScanVO> wardScan(Long wardId);

    /**
     * 单患者明细：评分 + 达预警阈值时调模型给观察建议
     */
    DeteriorationExplainVO explain(Long admissionId);
}

package com.his.charge.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.his.charge.dto.DrgSimDTO;
import com.his.charge.vo.DrgDiagPrefixVO;
import com.his.charge.vo.DrgSimVO;

import java.util.List;

public interface DrgSimService {

    DrgSimVO.SimResult simulate(DrgSimDTO.Simulate dto);

    DrgSimVO.SummaryListVO simulateBatch(DrgSimDTO.SimulateBatch dto);

    IPage<DrgSimVO.ResultRow> resultPage(DrgSimDTO.ResultQuery dto);

    DrgSimVO.SummaryListVO summaryList(Integer limit, String version, Integer extraSkipped);

    List<DrgSimVO.GroupRow> groupList(String version);

    /**
     * 方案健康检查：方案头溯源 + 真组数量比对 + 缺失集合清单
     */
    DrgSimVO.SchemeHealth schemeHealth(String version);

    /**
     * 实时聚合 DI_xxx 集合的 ICD-10 前3位前缀分布（只读，仅用于前端目录页展示提示）。
     *
     * @param version 分组方案版本（3.0 / chs-drg-2.0）
     * @param mdcCode MDC 编码（如 MDCB、MDCP、MDCA）；MDCA 先期分组无 DI 集合时返回空列表
     */
    List<DrgDiagPrefixVO> mdcDiagPrefixes(String version, String mdcCode);

    /**
     * 通过住院 encounterId（= 首页 admission_id）触发 DRG 分组。
     *
     * <p>用于住院出院结算落库事务内自动触发分组判定 —— 结算链路里首页可能还没建、主诊断
     * 编码可能为空，这两种情况都静默跳过（返回 null 或 UNGROUPED 结果），绝不阻塞结算主流程。
     *
     * @param encounterId 住院 encounterId（= biz_inpatient_summary.admission_id）
     * @param version     分组方案版本；不传走默认版本
     * @return 分组结果；找不到首页 / 主诊断为空时返回 null
     */
    DrgSimVO.SimResult simulateByEncounter(Long encounterId, String version);
}

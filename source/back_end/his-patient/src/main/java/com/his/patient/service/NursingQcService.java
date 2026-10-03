package com.his.patient.service;

import com.his.common.base.PageResult;
import com.his.patient.dto.NursingQcDTO;
import com.his.patient.vo.NurseQcVO;

import java.util.List;

/**
 * 护理质控服务（sql/168）：护理部视角的质量账。
 *
 * <p>三块能力对应护理部要问的三个问题：
 * <ol>
 *   <li><b>这个月做得怎么样</b> —— 月度 KPI（四条指标）+ 趋势 + 病区对比；</li>
 *   <li><b>数据从哪来</b> —— 检查表录入与确认（合格率类指标的唯一事实来源）；</li>
 *   <li><b>数字定稿</b> —— 台账重算与上报/退回（已上报的行不被重算冲掉）。</li>
 * </ol>
 */
public interface NursingQcService {

    List<NurseQcVO.Ward> wardSelectList(String keyword);

    List<NurseQcVO.Inspector> inspectorSelectList(Long wardId, String keyword);

    List<NurseQcVO.ItemDef> itemSelectList(Integer category);

    PageResult<NurseQcVO.CheckRow> checkListPage(NursingQcDTO.CheckQueryPage query);

    NurseQcVO.CheckDetail getDetailById(Long id);

    NurseQcVO.SaveResult checkUpsert(NursingQcDTO.CheckUpsert dto);

    NurseQcVO.SaveResult checkStatus(NursingQcDTO.CheckStatus dto);

    void checkDeleteById(Long id);

    PageResult<NurseQcVO.LedgerRow> ledgerListPage(NursingQcDTO.LedgerQueryPage query);

    List<NurseQcVO.Kpi> monthMetrics(NursingQcDTO.MonthQuery query);

    List<NurseQcVO.Kpi> trend(NursingQcDTO.TrendQuery query);

    List<NurseQcVO.LedgerRow> wardCompare(NursingQcDTO.CompareQuery query);

    NurseQcVO.RecalcResult recalc(NursingQcDTO.RecalcCommand command);

    NurseQcVO.ReportResult report(NursingQcDTO.ReportCommand command);

    void ledgerDeleteById(Long id);
}

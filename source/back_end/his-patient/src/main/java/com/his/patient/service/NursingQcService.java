package com.his.patient.service;

import com.his.common.base.PageResult;
import com.his.patient.dto.NursingQcDTO;
import com.his.patient.vo.NurseQcVO;

import java.util.List;

/**
 * 护理质控服务（sql/168）：护理部视角的质量账。
 */
public interface NursingQcService {

    List<NurseQcVO.Ward> wardSelectList(String keyword);

    List<NurseQcVO.Inspector> inspectorSelectList(Long wardId, String keyword);

    List<NurseQcVO.ItemDef> itemSelectList(Integer category);

    PageResult<NurseQcVO.CheckRow> checkListPage(NursingQcDTO.CheckQueryPageDTO query);

    NurseQcVO.CheckDetail getDetailById(Long id);

    NurseQcVO.SaveResult checkUpsert(NursingQcDTO.CheckUpsertDTO dto);

    NurseQcVO.SaveResult checkStatus(NursingQcDTO.CheckStatus dto);

    void checkDeleteById(Long id);

    PageResult<NurseQcVO.LedgerRow> ledgerListPage(NursingQcDTO.LedgerQueryPageDTO query);

    List<NurseQcVO.Kpi> monthMetrics(NursingQcDTO.MonthQueryDTO query);

    List<NurseQcVO.Kpi> trend(NursingQcDTO.TrendQueryDTO query);

    List<NurseQcVO.LedgerRow> wardCompare(NursingQcDTO.CompareQueryDTO query);

    NurseQcVO.RecalcResult recalc(NursingQcDTO.RecalcCommandDTO command);

    NurseQcVO.ReportResult report(NursingQcDTO.ReportCommandDTO command);

    void ledgerDeleteById(Long id);
}

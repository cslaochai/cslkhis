package com.his.emr.service;

import com.his.common.base.PageResult;
import com.his.emr.dto.InfectiousReportDTO;
import com.his.emr.vo.InfectiousReportVO;

import java.util.List;

/**
 * 传染病报告卡服务：填卡→审核→退报重报→直报（报文预览）+ 时限催报。
 */
public interface InfectiousReportService {

    PageResult<InfectiousReportVO.Row> page(InfectiousReportDTO.QueryPage q);

    InfectiousReportVO.Detail getDetailById(Long id);

    List<InfectiousReportVO.DiseaseSelectListVO> diseaseSelectList(String keyword);

    InfectiousReportVO.Stats stats();

    Long upsert(InfectiousReportDTO.Upsert dto);

    void audit(InfectiousReportDTO.Audit dto);

    void returnCard(InfectiousReportDTO.ReturnCard dto);

    String directReport(Long id);

    int notifyOverdue();
}

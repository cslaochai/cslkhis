package com.his.drgdip.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.his.drgdip.dto.DipSimDTO;
import com.his.drgdip.vo.DipSimVO;

import java.util.List;

public interface DipSimService {

    DipSimVO.SimResult simulate(DipSimDTO.Simulate dto);

    DipSimVO.SummaryListVO simulateBatch(DipSimDTO.SimulateBatch dto);

    IPage<DipSimVO.ResultRow> resultPage(DipSimDTO.ResultQuery dto);

    DipSimVO.SummaryListVO summaryList(Integer limit, String version, Integer extraSkipped);

    List<DipSimVO.DiseaseRow> diseaseList(String version);
}

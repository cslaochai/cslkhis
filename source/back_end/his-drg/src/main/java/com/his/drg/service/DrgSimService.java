package com.his.drg.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.his.drg.dto.DrgSimDTO;
import com.his.drg.vo.DrgSimVO;

import java.util.List;

public interface DrgSimService {

    DrgSimVO.SimResult simulate(DrgSimDTO.Simulate dto);

    DrgSimVO.SummaryListVO simulateBatch(DrgSimDTO.SimulateBatch dto);

    IPage<DrgSimVO.ResultRow> resultPage(DrgSimDTO.ResultQuery dto);

    DrgSimVO.SummaryListVO summaryList(Integer limit, String version, Integer extraSkipped);

    List<DrgSimVO.GroupRow> groupList(String version);
}

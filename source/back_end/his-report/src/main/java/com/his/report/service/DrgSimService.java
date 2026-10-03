package com.his.report.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.his.report.dto.DrgSimDTO;
import com.his.report.vo.DrgSimVO;
import java.util.List;

public interface DrgSimService {

    DrgSimVO.SimResult simulate(DrgSimDTO.Simulate dto);

    DrgSimVO.SummaryListVO simulateBatch(DrgSimDTO.SimulateBatch dto);

    IPage<DrgSimVO.ResultRow> resultPage(DrgSimDTO.ResultQuery dto);

    DrgSimVO.SummaryListVO summaryList(Integer limit, Integer extraSkipped);

    List<DrgSimVO.GroupRow> groupList();
}

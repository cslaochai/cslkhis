package com.his.medicaltech.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.his.medicaltech.dto.DrgSimDTO;
import com.his.medicaltech.vo.DrgSimVO;

import java.util.List;

public interface DrgSimService {

    DrgSimVO.SimResult simulate(DrgSimDTO.Simulate dto);

    DrgSimVO.SummaryListVO simulateBatch(DrgSimDTO.SimulateBatch dto);

    IPage<DrgSimVO.ResultRow> resultPage(DrgSimDTO.ResultQuery dto);

    DrgSimVO.SummaryListVO summaryList(Integer limit, Integer extraSkipped);

    List<DrgSimVO.GroupRow> groupList();
}

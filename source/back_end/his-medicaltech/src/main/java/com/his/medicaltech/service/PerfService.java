package com.his.medicaltech.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.his.medicaltech.dto.PerfDTO;
import com.his.medicaltech.vo.PerfVO;

public interface PerfService {

    PerfVO.CostRow saveCost(PerfDTO.CostSave dto);

    IPage<PerfVO.CostRow> costPage(PerfDTO.CostQuery dto);

    PerfVO.RevenueInfo revenueInfo(Long deptId, String month);

    PerfVO.PerfRow calc(PerfDTO.PerfCalc dto);

    IPage<PerfVO.PerfRow> perfPage(PerfDTO.PerfQuery dto);
}

package com.his.medicaltech.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.his.medicaltech.dto.StatReportDTO;
import com.his.medicaltech.vo.StatReportVO;

public interface StatReportService {

    StatReportVO.Detail generate(StatReportDTO.Generate dto);

    StatReportVO.Row submit(Long id);

    StatReportVO.Row voidReport(Long id, String reason);

    IPage<StatReportVO.Row> listPage(StatReportDTO.QueryPage dto);

    StatReportVO.Detail getDetailById(Long id);
}

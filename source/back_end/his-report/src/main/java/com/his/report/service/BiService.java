package com.his.report.service;

import com.his.report.vo.BiNationalVO;
import com.his.report.vo.BiOverviewVO;

public interface BiService {

    BiOverviewVO overview();

    BiNationalVO nationalMetrics();
}

package com.his.medicaltech.service;

import com.his.medicaltech.vo.BiNationalVO;
import com.his.medicaltech.vo.BiOverviewVO;

public interface BiService {

    BiOverviewVO overview();

    BiNationalVO nationalMetrics();
}

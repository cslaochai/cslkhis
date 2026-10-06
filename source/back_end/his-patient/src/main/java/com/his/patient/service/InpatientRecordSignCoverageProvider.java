package com.his.patient.service;

import com.his.common.enums.SignBizTypeEnum;
import com.his.common.service.SignCoverageProvider;

public interface InpatientRecordSignCoverageProvider extends SignCoverageProvider {

    SignBizTypeEnum bizType();

    SignCoverage coverage();
}

package com.his.patient.service;

import com.his.common.enums.SignBizTypeEnum;
import com.his.system.service.SignCoverageProvider;

public interface InpatientRecordSignCoverageProvider extends SignCoverageProvider {

    SignBizTypeEnum bizType();

    SignCoverage coverage();
}

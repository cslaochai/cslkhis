package com.his.patient.service;

import com.his.common.service.SignCoverageProvider;
import com.his.common.enums.SignBizType;

public interface InpatientRecordSignCoverageProvider extends SignCoverageProvider {

    SignBizType bizType();

    SignCoverage coverage();
}

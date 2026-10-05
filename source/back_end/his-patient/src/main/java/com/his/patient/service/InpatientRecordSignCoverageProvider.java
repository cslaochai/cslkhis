package com.his.patient.service;

import com.his.common.enums.SignBizType;
import com.his.common.service.SignCoverageProvider;

public interface InpatientRecordSignCoverageProvider extends SignCoverageProvider {

    SignBizType bizType();

    SignCoverage coverage();
}

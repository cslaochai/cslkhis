package com.his.emr.service;

import com.his.common.service.SignCoverageProvider;
import com.his.common.enums.SignBizType;

public interface OutpatientRecordSignCoverageProvider extends SignCoverageProvider {

    SignBizType bizType();

    SignCoverage coverage();
}

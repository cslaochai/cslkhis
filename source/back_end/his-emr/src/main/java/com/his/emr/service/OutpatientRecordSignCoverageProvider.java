package com.his.emr.service;

import com.his.common.enums.SignBizType;
import com.his.common.service.SignCoverageProvider;

public interface OutpatientRecordSignCoverageProvider extends SignCoverageProvider {

    SignBizType bizType();

    SignCoverage coverage();
}

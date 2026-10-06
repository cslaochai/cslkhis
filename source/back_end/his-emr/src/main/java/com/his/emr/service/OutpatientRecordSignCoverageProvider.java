package com.his.emr.service;

import com.his.common.enums.SignBizTypeEnum;
import com.his.common.service.SignCoverageProvider;

public interface OutpatientRecordSignCoverageProvider extends SignCoverageProvider {

    SignBizTypeEnum bizType();

    SignCoverage coverage();
}

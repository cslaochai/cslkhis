package com.his.emr.service;

import com.his.common.enums.SignBizTypeEnum;
import com.his.system.service.SignCoverageProvider;

public interface OutpatientRecordSignCoverageProvider extends SignCoverageProvider {

    SignBizTypeEnum bizType();

    SignCoverage coverage();
}

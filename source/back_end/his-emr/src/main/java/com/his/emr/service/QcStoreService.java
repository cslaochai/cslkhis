package com.his.emr.service;

import com.his.emr.entity.BizQualityControl;
import com.his.emr.support.QcResult;
import com.his.emr.support.QcSnapshot;

public interface QcStoreService {

    BizQualityControl save(QcSnapshot snapshot, QcResult result, Integer qcType, String operator);
}

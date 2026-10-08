package com.his.emr.service;

import com.his.emr.entity.BizQualityControl;
import com.his.emr.vo.QcResultVO;
import com.his.emr.support.QcSnapshot;

public interface QcStoreService {

    BizQualityControl save(QcSnapshot snapshot, QcResultVO result, Integer qcType, String operator);
}

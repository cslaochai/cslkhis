package com.his.emr.vo;

import lombok.Data;

import java.util.List;

/**
 * 患者端「我的慢病档案」出参：建档全集 + 长处方资格。
 */
@Data
public class ChronicMyRecordsVO {

    /**
     * 建档记录（不按认定状态过滤，作废/待认定都在内）
     */
    private List<ChronicRecordListVO> records;

    /**
     * 长处方资格：存在一条已认定档案即具备
     */
    private Boolean longRxEligible;
}

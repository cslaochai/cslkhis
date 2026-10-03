package com.his.emr.vo;

import lombok.Data;

import java.util.List;

/**
 * 患者端「我的慢病档案」出参：建档全集 + 长处方资格。
 *
 * <p>资格跟列表一次给全是因为这个入口只有一个：分开拉就成了「先取档案、再判资格」两次往返，
 * 而资格本身只是「有没有一条已认定档案」，没有独立查询的价值。
 */
@Data
public class ChronicMyRecordsVO {

    /** 建档记录（不按认定状态过滤，作废/待认定都在内） */
    private List<ChronicRecordListVO> records;

    /** 长处方资格：存在一条已认定档案即具备 */
    private Boolean longRxEligible;
}

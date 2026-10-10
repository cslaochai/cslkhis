package com.his.patient.dto;

import lombok.Data;

import java.util.List;

/**
 * 全局患者搜索的「就诊范围」权重。
 */
@Data
public class PatientSearchScopeDTO {

    /**
     * 今日有门诊就诊的患者（全部科室）
     */
    private List<Long> todayIds;

    /**
     * 今日有门诊就诊、且属于当前登录用户本人或本科室的患者（排最前）
     */
    private List<Long> myTodayIds;
}

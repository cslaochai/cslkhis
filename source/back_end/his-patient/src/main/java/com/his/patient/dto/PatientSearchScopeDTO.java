package com.his.patient.dto;

import lombok.Data;

import java.util.List;

/**
 * 全局患者搜索的「就诊范围」权重。
 *
 * <p>为什么需要它：列表是分页的，排序权重必须在 SQL 里参与 ORDER BY。
 * 只把「今日就诊」传下去不足以还原医生的真实意图 —— 医生搜一个姓，最想要的是
 * **今天挂我号 / 到我科**的那一个，而不是随便哪个今天在别的科室就诊的患者。
 *
 * <p>权重顺序：{@link #myTodayIds} → {@link #todayIds} → 其余（按建档时间倒序）。
 */
@Data
public class PatientSearchScopeDTO {

    /** 今日有门诊就诊的患者（全部科室） */
    private List<Long> todayIds;

    /** 今日有门诊就诊、且属于当前登录用户本人或本科室的患者（排最前） */
    private List<Long> myTodayIds;
}

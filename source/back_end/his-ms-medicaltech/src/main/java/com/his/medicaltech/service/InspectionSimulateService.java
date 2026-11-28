package com.his.medicaltech.service;

import java.util.List;

/**
 * 检查设备仿真回传服务：模拟 CT / DR / MRI / 超声等设备在患者签到后排队出结果，
 * 定时把随机生成的检查所见与结论回写 HIS。仿真只产出文本结果，不生成影像图片。
 */
public interface InspectionSimulateService {

    /**
     * 取已签到 / 检查中且尚无结果的检查单 id（先等先排，限量返回）。
     */
    List<Long> pendingIds(int maxPerRound);

    /**
     * 对单张检查单生成仿真结果并落库：回写检查记录 + 建待审核报告。
     * 每单独立事务，一单失败不影响其他单。
     *
     * @return 本轮是否成功回传；状态已被他人处理或无结果可写时返回 false
     */
    boolean simulateOne(Long recordId);
}

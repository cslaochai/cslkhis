package com.his.medicaltech.trigger;

import com.his.common.support.SystemOperatorContext;
import com.his.medicaltech.service.InspectionSimulateService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 检查设备仿真回传定时扫描。
 *
 * <p>模拟 CT / DR / MRI / 超声等设备通道：患者签到后进入待采集队列，本任务每 30 秒扫一次
 * 已签到 / 检查中且尚无结果的检查单，逐单生成仿真所见与结论回写 HIS，并落一张待审核报告。
 * 每单独立事务，一单失败不拖垮整轮；报告不自动审核定稿，留给诊断医师在工作站复核签名。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class InspectionSimulateTrigger {

    /** 单轮最多回传张数：排队积压时分批消化，避免一轮灌库 */
    private static final int MAX_PER_ROUND = 10;

    private final InspectionSimulateService inspectionSimulateService;

    @Scheduled(initialDelay = 20_000, fixedDelay = 30_000)
    public void simulatePending() {
        List<Long> ids;
        try {
            SystemOperatorContext.bindSystemTask();
            ids = inspectionSimulateService.pendingIds(MAX_PER_ROUND);
        } catch (Exception e) {
            log.error("[检查仿真] 待回传队列扫描失败，下一轮重试：{}", e.getMessage(), e);
            SystemOperatorContext.clear();
            return;
        }
        int done = 0;
        for (Long id : ids) {
            try {
                if (inspectionSimulateService.simulateOne(id)) {
                    done++;
                }
            } catch (Exception e) {
                // 单张异常不拖垮整轮（单据被并发取消 / 状态已变等），失败单留待下一轮
                log.warn("[检查仿真] 记录 {} 回传异常，跳过：{}", id, e.getMessage());
            }
        }
        SystemOperatorContext.clear();
        if (done > 0) {
            log.info("[检查仿真] 本轮回传检查结果 {} 张", done);
        }
    }
}

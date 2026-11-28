package com.his.iot.support;

import com.his.common.exception.BusinessException;
import com.his.iot.service.RobotDispatchService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 机器人待调度队列补派。
 * <p>
 * 派单是机器人唯一的驱动方式，所以"落单时没车"不能等于"这单就沉了"：这里每 10 秒扫一次
 * status=1 的任务单，按「急送优先 + 先等先派」逐单走一遍人工派车的同一条路径
 * （选车 → 锁车 → 复核在途 → 下发）。每单单独一个事务，一单失败不影响其他单。
 * <p>
 * 触发方式只留定时扫描，不在机器人回传事件里同步调用：事件链路可能本身在事务里，
 * 同事务再派车会把行锁持有时间拉长，10 秒的补派延迟远小于一趟配送时长，够用。
 */
@Component
@RequiredArgsConstructor
public class RobotDispatchQueueTask {

    private static final Logger log = LoggerFactory.getLogger(RobotDispatchQueueTask.class);

    /** 补派操作人：定时任务没有登录态，按「能力名」落系统值，回查能看出这不是人干的。 */
    private static final String OPERATOR = "system:robotDispatch";
    private static final long OPERATOR_ID = 0L;
    /** 一轮最多补派多少单：车队 10 台，排到 10 单还没车说明队伍已堵，再多扫也只是空转。 */
    private static final int MAX_PER_ROUND = 10;

    private final RobotDispatchService dispatchService;

    @Scheduled(initialDelay = 20_000, fixedDelay = 10_000)
    public void dispatchPending() {
        List<Long> ids;
        try {
            ids = dispatchService.pendingOrderIds(MAX_PER_ROUND);
        } catch (Exception e) {
            log.warn("待调度队列扫描失败，下一轮重试：{}", e.getMessage());
            return;
        }
        for (Long id : ids) {
            try {
                dispatchService.assignAs(id, null, OPERATOR, OPERATOR_ID);
            } catch (BusinessException e) {
                if (isNoCandidate(e)) {
                    // 这一单装不下/没人可派（例如超载大单），跳过它，后面的小单还能走
                    log.debug("任务单 {} 暂无可派机器人：{}", id, e.getMessage());
                    continue;
                }
                // 通道不可用 / 状态已变：整轮停等，不对后面的单重复打同一个失败点
                log.debug("任务单 {} 补派停止本轮：{}", id, e.getMessage());
                return;
            } catch (Exception e) {
                // 单张单异常不拖垮整轮（例如单据被并发取消）
                log.warn("任务单 {} 补派异常，跳过：{}", id, e.getMessage());
            }
        }
    }

    /** 选车失败（无候选）与通道/状态失败要区别对待：前者跳单，后者停轮。 */
    private boolean isNoCandidate(BusinessException e) {
        return e.getMessage() != null && e.getMessage().startsWith("当前没有可派机器人");
    }
}

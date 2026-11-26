package com.his.iot.support;

import com.his.iot.service.ColdChainArchiveService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 冷链 GSP 归档定时任务：启动 1 分钟后先补 24 小时，之后每 10 分钟补一次。
 * <p>
 * 用 fixedDelay 不用 fixedRate：归档慢了（TDengine 抖动）不会在自己身上叠跑。
 */
@Component
@RequiredArgsConstructor
public class ColdChainArchiveTask {

    private static final Logger log = LoggerFactory.getLogger(ColdChainArchiveTask.class);

    private final ColdChainArchiveService archiveService;

    @Scheduled(initialDelay = 60_000, fixedDelay = 600_000)
    public void archive() {
        try {
            archiveService.archiveRecent();
        } catch (Exception e) {
            // 归档失败不能拖垮应用：下一轮会从 MAX(ts) 继续补，缺口不会扩大
            log.warn("冷链 GSP 归档失败，下一轮重试：{}", e.getMessage());
        }
    }
}

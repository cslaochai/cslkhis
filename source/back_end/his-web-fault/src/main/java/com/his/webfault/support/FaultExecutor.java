package com.his.webfault.support;

import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * 故障线程的唯一起点：统一标记为 daemon，统一计数。
 * <p>
 * daemon 的原因：演练进程被 Ctrl+C / kill 时不能被「还在自旋的故障线程」拖住；
 * 计数的原因：{@code /fault/status} 与硬上限都靠它，防止重复触发把线程数叠爆。
 */
@Component
public class FaultExecutor {

    private final AtomicInteger active = new AtomicInteger();

    public void run(String threadName, Runnable task) {
        active.incrementAndGet();
        Thread thread = new Thread(() -> {
            try {
                task.run();
            } finally {
                active.decrementAndGet();
            }
        }, threadName);
        thread.setDaemon(true);
        thread.start();
    }

    public int activeThreads() {
        return active.get();
    }
}

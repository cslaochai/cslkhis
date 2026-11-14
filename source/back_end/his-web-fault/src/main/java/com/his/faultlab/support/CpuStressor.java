package com.his.faultlab.support;

import com.his.faultlab.config.FaultLabProperties;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Pattern;

/**
 * CPU 打满的两种真实成因：
 * <ol>
 *   <li>灾难性回溯正则：一个 24~27 个字符的入参就能让一个核跑满几秒，QPS 还很低，
 *       监控上表现为「CPU 高但流量不高」——业务里典型是姓名/药品名格式校验写了 {@code ^(a+)+$}；</li>
 *   <li>死循环 / 无界重试：线程一直自旋，jstack 里看到同一行代码反复出现。</li>
 * </ol>
 * 两者都带截止时间，到点自动退出，不会把机器永久占住。
 */
@Component
public class CpuStressor {

    private static final Pattern BACKTRACKING_PATTERN = Pattern.compile("^(a+)+$");

    private final FaultLabProperties props;
    private final FaultExecutor executor;
    private final AtomicBoolean stopping = new AtomicBoolean(false);

    public CpuStressor(FaultLabProperties props, FaultExecutor executor) {
        this.props = props;
        this.executor = executor;
    }

    /**
     * 启动灾难性回溯：一个线程反复用构造好的入参触发指数级回溯。
     *
     * @param seconds 持续秒数，受 maxCpuSeconds 限制
     * @param chars   入参长度，18~27；每 +1 回溯步数翻倍，27 已足够把单核打满
     */
    public String startRegex(int seconds, int chars) {
        if (!props.isEnabled()) {
            return "REJECTED: fault.enabled=false";
        }
        if (executor.activeThreads() >= props.getMaxCpuThreads()) {
            return "REJECTED: 已达 maxCpuThreads=" + props.getMaxCpuThreads();
        }
        int length = clamp(chars, 18, 27);
        int duration = clamp(seconds, 1, props.getMaxCpuSeconds());
        stopping.set(false);
        executor.run("fault-cpu-regex", () -> {
            String input = "a".repeat(length) + "!";
            long deadline = System.currentTimeMillis() + duration * 1000L;
            long rounds = 0;
            while (!stopping.get() && System.currentTimeMillis() < deadline) {
                BACKTRACKING_PATTERN.matcher(input).matches();
                rounds++;
            }
            System.out.printf("[fault-lab] cpu-regex finished: chars=%d rounds=%d%n", length, rounds);
        });
        return "STARTED: 1 thread, chars=" + length + ", max " + duration + "s";
    }

    /**
     * 纯自旋：模拟 while(true) 里没有 IO/锁的代码路径。
     */
    public String startSpin(int threads, int seconds) {
        if (!props.isEnabled()) {
            return "REJECTED: fault.enabled=false";
        }
        int remaining = props.getMaxCpuThreads() - executor.activeThreads();
        if (remaining <= 0) {
            return "REJECTED: 已达 maxCpuThreads=" + props.getMaxCpuThreads();
        }
        int count = clamp(threads, 1, remaining);
        int duration = clamp(seconds, 1, props.getMaxCpuSeconds());
        stopping.set(false);
        for (int i = 0; i < count; i++) {
            int index = i;
            executor.run("fault-cpu-spin-" + index, () -> {
                long deadline = System.currentTimeMillis() + duration * 1000L;
                long sink = 1;
                while (!stopping.get() && System.currentTimeMillis() < deadline) {
                    sink = sink * 31 + index;
                }
                if (sink == 42) {
                    System.out.println(sink);
                }
            });
        }
        return "STARTED: " + count + " threads, max " + duration + "s";
    }

    /** 手动停止（正则单次调用不可中断，最迟在下一次循环判断时退出） */
    public String stop() {
        stopping.set(true);
        return "STOPPING: 故障线程将在当前一次循环后退出";
    }

    private int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }
}

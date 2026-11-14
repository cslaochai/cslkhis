package com.his.faultlab.support;

import com.his.faultlab.config.FaultLabProperties;
import org.springframework.stereotype.Component;

import java.util.Arrays;

/**
 * GC 压力：大对象（报表 PDF、影像缩略图）每秒几十 MB 地创建又丢弃。
 * 堆占用不高、但 Young GC 从 30 秒一次变成 1 秒一次，单次停顿把 P99 拉起来。
 * <p>
 * 与 {@link HeapLeaker} 的区别：这里对象可回收，jstat 的 OU 不涨、FGC 不涨，只有 YGC 频率暴涨。
 */
@Component
public class GcPressure {

    private static final int CHUNK_MB = 2;

    private final FaultLabProperties props;
    private final FaultExecutor executor;

    public GcPressure(FaultLabProperties props, FaultExecutor executor) {
        this.props = props;
        this.executor = executor;
    }

    public String start(int seconds, int mbPerSecond) {
        if (!props.isEnabled()) {
            return "REJECTED: fault.enabled=false";
        }
        int duration = Math.max(1, Math.min(props.getMaxCpuSeconds(), seconds));
        int rate = Math.max(4, Math.min(128, mbPerSecond));
        executor.run("fault-gc-pressure", () -> {
            long deadline = System.currentTimeMillis() + duration * 1000L;
            long created = 0;
            while (System.currentTimeMillis() < deadline) {
                long secondDeadline = System.currentTimeMillis() + 1000L;
                while (System.currentTimeMillis() < secondDeadline) {
                    byte[] chunk = new byte[CHUNK_MB * 1024 * 1024];
                    Arrays.fill(chunk, (byte) 0x01);
                    created += CHUNK_MB;
                }
                // 这里不做限速：一秒内能造多少就造多少，rate 只用于提示预期量
            }
            System.out.printf("[fault-lab] gc-pressure finished: created≈%dMB, target=%dMB/s%n", created, rate);
        });
        return "STARTED: 约 " + rate + "MB/s 分配，持续 " + duration + "s（watch: jstat -gcutil <pid> 1000）";
    }
}

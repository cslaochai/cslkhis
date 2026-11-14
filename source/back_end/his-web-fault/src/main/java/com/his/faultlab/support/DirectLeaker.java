package com.his.faultlab.support;

import com.his.faultlab.config.FaultLabProperties;
import org.springframework.stereotype.Component;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 堆外内存泄漏：模拟 Netty / NIO / 压缩库只 allocateDirect 不释放，
 * 典型现象是「堆 dump 正常、GC 正常，但容器 RSS 一直涨，最后被 K8s OOMKilled」。
 * <p>
 * 排查钥匙是 NMT（-XX:NativeMemoryTracking=summary）+ jcmd VM.native_memory，
 * 或 JMX 的 BufferPool 指标（Direct / Mapped）。
 */
@Component
public class DirectLeaker {

    private static final int CHUNK_MB = 16;

    private final FaultLabProperties props;
    private final List<ByteBuffer> retained = Collections.synchronizedList(new ArrayList<>());
    private final AtomicLong retainedBytes = new AtomicLong();

    public DirectLeaker(FaultLabProperties props) {
        this.props = props;
    }

    public String leak(int targetMb) {
        if (!props.isEnabled()) {
            return "REJECTED: fault.enabled=false";
        }
        long target = (long) clamp(targetMb, 16, props.getMaxRetainMb()) * 1024 * 1024;
        byte[] filler = new byte[CHUNK_MB * 1024 * 1024];
        int allocated = 0;
        try {
            while (retainedBytes.get() < target) {
                ByteBuffer buffer = ByteBuffer.allocateDirect(CHUNK_MB * 1024 * 1024);
                // 写一遍才会真正提交物理页，只 allocate 看不到 RSS 增长
                buffer.put(filler);
                retained.add(buffer);
                retainedBytes.addAndGet(filler.length);
                allocated += CHUNK_MB;
            }
            return "RETAINED: +" + allocated + "MB direct, total=" + retainedMb() + "MB";
        } catch (OutOfMemoryError error) {
            // MaxDirectMemorySize 默认等于 -Xmx，超了就是这个错
            return "OOME(direct): " + error.getMessage() + ", 已保留 " + retainedMb() + "MB";
        }
    }

    public void release() {
        retained.clear();
        retainedBytes.set(0);
        System.gc();
        try {
            // 给 Cleaner 线程一点时间归还 DirectByteBuffer
            Thread.sleep(300);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    public long retainedMb() {
        return retainedBytes.get() / (1024 * 1024);
    }

    private int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }
}

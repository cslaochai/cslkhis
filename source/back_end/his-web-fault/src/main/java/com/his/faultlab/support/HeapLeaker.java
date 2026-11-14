package com.his.faultlab.support;

import com.his.faultlab.config.FaultLabProperties;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 堆内泄漏：模拟「本地缓存没有上限」——患者画像/字典快照被塞进静态 Map，只增不减。
 * <p>
 * 与 {@link DirectLeaker} 的区别：这里每一块都在 Java 堆上，MAT 的 dominator tree 能直接指到
 * 这个 {@code retained} 列表；堆外泄漏在堆里只有几百字节的 DirectByteBuffer 壳，堆 dump 看不出来。
 */
@Component
public class HeapLeaker {

    private final FaultLabProperties props;
    /** 故意保留引用：这就是「泄漏」本身 */
    private final List<byte[]> retained = Collections.synchronizedList(new ArrayList<>());
    private final AtomicLong retainedBytes = new AtomicLong();
    private volatile boolean lastOom;

    public HeapLeaker(FaultLabProperties props) {
        this.props = props;
    }

    /**
     * 渐进泄漏：每块 heapChunkMb，保留到目标值。
     */
    public String leak(int targetMb) {
        if (!props.isEnabled()) {
            return "REJECTED: fault.enabled=false";
        }
        long target = (long) clamp(targetMb, 1, props.getMaxRetainMb()) * 1024 * 1024;
        int chunkMb = props.getHeapChunkMb();
        int allocated = 0;
        while (retainedBytes.get() < target) {
            long remainMb = (target - retainedBytes.get()) / (1024 * 1024);
            int sizeMb = (int) Math.min(chunkMb, Math.max(1, remainMb));
            byte[] chunk = new byte[sizeMb * 1024 * 1024];
            // 不写一遍的话只是保留了虚拟地址，RSS/老年代不会真涨
            Arrays.fill(chunk, (byte) 0x5A);
            retained.add(chunk);
            retainedBytes.addAndGet(chunk.length);
            allocated += sizeMb;
        }
        return "RETAINED: +" + allocated + "MB, total=" + retainedMb() + "MB";
    }

    /**
     * 真 OOM：一直分配到 JVM 抛 OutOfMemoryError 为止。
     * 触发后默认立刻释放，让进程还能响应（线上真 OOM 往往进程僵死，这里是为了能继续演示）。
     */
    public String oom(int capMb, boolean releaseAfter) {
        if (!props.isEnabled()) {
            return "REJECTED: fault.enabled=false";
        }
        long cap = (long) clamp(capMb, 16, props.getMaxRetainMb()) * 1024 * 1024;
        long allocated = 0;
        lastOom = false;
        try {
            while (allocated < cap) {
                byte[] chunk = new byte[4 * 1024 * 1024];
                Arrays.fill(chunk, (byte) 0x5A);
                retained.add(chunk);
                allocated += chunk.length;
                retainedBytes.addAndGet(chunk.length);
            }
            return "CAP-REACHED: 未触发 OOM，已保留 " + mb(allocated) + "MB（调大 mb 或调小 -Xmx 再试）";
        } catch (OutOfMemoryError error) {
            lastOom = true;
            System.out.println("[fault-lab] OOME captured: " + error.getMessage());
            return "OOME: " + error.getMessage();
        } finally {
            if (releaseAfter || lastOom) {
                release();
            }
        }
    }

    public void release() {
        retained.clear();
        retainedBytes.set(0);
        System.gc();
    }

    public long retainedMb() {
        return mb(retainedBytes.get());
    }

    public boolean lastOom() {
        return lastOom;
    }

    private long mb(long bytes) {
        return bytes / (1024 * 1024);
    }

    private int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }
}

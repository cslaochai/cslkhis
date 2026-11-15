package com.his.webfault.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 演练沙箱的硬上限。所有上限都是「防止演练把自己电脑搞死」的保险丝，
 * 与业务配置无关；改大之前先确认 -Xmx 与物理内存。
 */
@ConfigurationProperties(prefix = "fault")
public class FaultLabProperties {

    /** 总开关：false 时任何故障都拒绝启动 */
    private boolean enabled = true;

    /** CPU 故障最多同时开几个线程（超过直接拒绝，防止把机器打满） */
    private int maxCpuThreads = 8;

    /** 单次 CPU 故障最长持续时间（秒），到点自动停 */
    private int maxCpuSeconds = 60;

    /** 堆/堆外最多保留多少 MB，到点拒绝继续分配 */
    private int maxRetainMb = 512;

    /** 占住连接/占住锁的最长秒数 */
    private int maxHoldSeconds = 120;

    /** 单次演练最多并发多少请求（线程池耗尽场景一次要 300+） */
    private int maxConcurrency = 400;

    /** 堆泄漏每块分配多少 MB */
    private int heapChunkMb = 8;

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public int getMaxCpuThreads() {
        return maxCpuThreads;
    }

    public void setMaxCpuThreads(int maxCpuThreads) {
        this.maxCpuThreads = maxCpuThreads;
    }

    public int getMaxCpuSeconds() {
        return maxCpuSeconds;
    }

    public void setMaxCpuSeconds(int maxCpuSeconds) {
        this.maxCpuSeconds = maxCpuSeconds;
    }

    public int getMaxRetainMb() {
        return maxRetainMb;
    }

    public void setMaxRetainMb(int maxRetainMb) {
        this.maxRetainMb = maxRetainMb;
    }

    public int getMaxHoldSeconds() {
        return maxHoldSeconds;
    }

    public void setMaxHoldSeconds(int maxHoldSeconds) {
        this.maxHoldSeconds = maxHoldSeconds;
    }

    public int getMaxConcurrency() {
        return maxConcurrency;
    }

    public void setMaxConcurrency(int maxConcurrency) {
        this.maxConcurrency = maxConcurrency;
    }

    public int getHeapChunkMb() {
        return heapChunkMb;
    }

    public void setHeapChunkMb(int heapChunkMb) {
        this.heapChunkMb = heapChunkMb;
    }
}

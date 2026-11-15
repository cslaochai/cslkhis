package com.his.webfault.vo;

/**
 * 演练进程的实时水位。排查时先看这一屏，确认「故障真的发生了」再去看 JVM 指标。
 *
 * @param heapUsedMb         当前堆已用（MB）
 * @param heapMaxMb          堆上限（MB），对应启动参数 -Xmx
 * @param retainedHeapMb     演练代码刻意保留的堆内存（MB），调用 heapRelease 后回落
 * @param retainedDirectMb   JVM direct buffer 已用（MB），用于观察堆外泄漏
 * @param liveThreads        JVM 当前活线程数
 * @param activeFaultThreads 仍在运行的演练线程数
 * @param availableProcessors JVM 可见的 CPU 核数
 * @param vmArgs             启动 JVM 参数，用于确认 -Xmx、NMT 等配置
 */
public record FaultStatusVO(
        long heapUsedMb,
        long heapMaxMb,
        long retainedHeapMb,
        long retainedDirectMb,
        int liveThreads,
        int activeFaultThreads,
        int availableProcessors,
        String vmArgs) {
}

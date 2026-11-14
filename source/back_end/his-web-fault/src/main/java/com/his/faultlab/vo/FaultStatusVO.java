package com.his.faultlab.vo;

/**
 * 演练进程的实时水位。排查时先看这一屏，确认「故障真的发生了」再去看 JVM 指标。
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

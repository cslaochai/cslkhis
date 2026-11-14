package com.his.faultlab.vo;

/**
 * 每次故障操作的统一回执：除了「成功/拒绝」，还带上当时的堆水位与故障线程数，
 * 方便把「点了什么」和「监控上看到什么」对上时间轴。
 */
public record FaultResultVO(
        String scenario,
        String status,
        String message,
        long heapUsedMb,
        long heapMaxMb,
        long retainedHeapMb,
        long retainedDirectMb,
        int activeFaultThreads,
        String hint) {
}

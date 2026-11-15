package com.his.webfault.vo;

/**
 * 每次故障操作的统一回执：除了「成功/拒绝」，还带上当时的堆水位与故障线程数，
 * 方便把「点了什么」和「监控上看到什么」对上时间轴。
 *
 * @param scenario         场景编码，与 /fault/catalog 的 code 对应
 * @param status           执行状态：OK / REJECTED
 * @param message          执行回执，例如 STARTED、RELEASED、SCAN 耗时
 * @param heapUsedMb       返回时刻的堆已用（MB）
 * @param heapMaxMb        返回时刻的堆上限（MB）
 * @param retainedHeapMb   演练代码刻意保留的堆内存（MB）
 * @param retainedDirectMb JVM direct buffer 已用（MB）
 * @param activeFaultThreads 返回时刻仍在运行的演练线程数
 * @param hint             下一步排查建议，例如用 jstack、jmap 还是 EXPLAIN
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

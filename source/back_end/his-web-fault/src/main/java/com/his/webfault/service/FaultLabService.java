package com.his.webfault.service;

import com.his.webfault.vo.FaultResultVO;
import com.his.webfault.vo.FaultStatusVO;
import com.his.webfault.vo.ScenarioVO;

import java.util.List;

/**
 * 故障演练入口的业务收口：Controller 只转参数，所有夹取、上限、状态拼装都在这里。
 * <p>
 * 读方法只返回快照；写方法会启动后台线程或占用资源，统一返回 FaultResultVO：
 * status=OK/REJECTED，message 是执行回执，hint 是下一步排查建议。
 */
public interface FaultLabService {

    /**
     * 实时水位快照：堆、直接内存、线程数、活跃演练线程。
     * 只读，不触发故障，也不写 fault-trigger 日志。
     */
    FaultStatusVO status();

    /**
     * 场景目录：返回前端卡片所需的 code/name/symptom/trigger/observe。
     * 只读，不产生任何副作用。
     */
    List<ScenarioVO> catalog();

    /**
     * 启动灾难性正则回溯，打满单核 CPU。
     * 后台执行，seconds 后自动结束；正则匹配不可中断，stop 只能阻止下一轮。
     *
     * @param seconds 持续时间（秒），实现层按上限夹取
     * @param chars   输入长度，越长回溯越慢
     */
    FaultResultVO cpuRegex(int seconds, int chars);

    /**
     * 启动多个自旋线程，制造多核 CPU 高、接口不返回的现象。
     * 用于 jstack 两次采样对比，找出一直在同一行的自旋线程。
     *
     * @param threads 自旋线程数
     * @param seconds 持续时间（秒）
     */
    FaultResultVO cpuSpin(int threads, int seconds);

    /**
     * 停止自旋线程；正则线程不可中断，会在当前一次匹配结束后退出。
     */
    FaultResultVO cpuStop();

    /**
     * 堆内泄漏：保留 mb 大小的 byte[]，模拟无上限本地缓存。
     * 可重复调用累加，调用 heapRelease 前不会自动释放。
     *
     * @param mb 本次保留的堆内存（MB）
     */
    FaultResultVO heapLeak(int mb);

    /**
     * 真 OOM：持续分配直到抛出 OutOfMemoryError，并捕获异常。
     * releaseAfter=true 时随后释放堆，false 则保留现场。
     *
     * @param mb           目标分配量（MB）
     * @param releaseAfter 捕获 OOME 后是否立即释放
     */
    FaultResultVO heapOom(int mb, boolean releaseAfter);

    /**
     * 释放堆内演练对象，并主动触发一次 System.gc()，便于观察堆回落。
     */
    FaultResultVO heapRelease();

    /**
     * 堆外内存泄漏：分配 DirectByteBuffer，制造「堆正常、RSS 上涨」的现象。
     * 用于演示 NMT 与 BufferPool direct 的排查路径。
     *
     * @param mb 本次分配的堆外内存（MB）
     */
    FaultResultVO directLeak(int mb);

    /**
     * 释放 DirectByteBuffer，等 Cleaner 归还物理内存。
     */
    FaultResultVO directRelease();

    /**
     * 单请求变慢：当前请求线程 sleep，同步占住一个 Tomcat worker。
     *
     * @param delayMs 阻塞时长（毫秒）
     */
    FaultResultVO threadSlow(int delayMs);

    /**
     * 线程耗尽：异步提交 requests 个阻塞任务，每个占住一个 worker。
     * 方法本身立即返回；Tomcat maxThreads=200，超过后新请求开始排队。
     *
     * @param requests 并发请求数
     * @param delayMs  每个请求占用 worker 的时长（毫秒）
     */
    FaultResultVO threadLoad(int requests, int delayMs);

    /**
     * 下游变慢：当前请求线程同步等待 delayMs，模拟第三方接口被拖住。
     *
     * @param delayMs 等待时长（毫秒）
     */
    FaultResultVO threadDownstream(int delayMs);

    /**
     * 初始化演练库：H2 内存库建 20 万行、无索引的挂号表。
     * 首次调用插入数据，重复调用幂等，不重复建。
     */
    FaultResultVO dbInit();

    /**
     * 慢 SQL：对 20 万行表做姓名 LIKE 全表扫描 + 排序。
     * 未初始化时会自动调用 dbInit，不必先手动初始化。
     *
     * @param keyword 姓名关键字
     */
    FaultResultVO dbSlowQuery(String keyword);

    /**
     * 连接池耗尽：并发借走 Hikari 连接并各 hold 一段时间。
     * 异步任务立即返回；超过池上限的请求在 connectionTimeout 后失败。
     *
     * @param requests 并发占连接数
     * @param holdMs   每条连接占用时长（毫秒）
     */
    FaultResultVO dbHold(int requests, int holdMs);

    /**
     * 连接池水位：只读 active/idle/total/waiting/max 与获取失败次数。
     * 首次调用会先借还一次连接触发 Hikari 建池，否则 MXBean 还是 null。
     */
    FaultResultVO dbPoolStatus();

    /**
     * Java 死锁：两个线程按相反顺序加锁，制造永久 BLOCKED。
     * 返回触发结果，现场尽快用 jcmd Thread.print 抓取。
     */
    FaultResultVO deadlock();

    /**
     * 缓存击穿：并发请求同时未命中，全部回源，下游调用数被放大。
     * 这是内存模拟，不依赖真实 Redis。
     *
     * @param concurrency 并发请求数
     */
    FaultResultVO cacheStampede(int concurrency);

    /**
     * 缓存击穿修复版：同一 key 只允许一个请求回源，其余复用结果。
     * 与 cacheStampede 使用相同并发数，对比 downstreamCalls 差异。
     *
     * @param concurrency 并发请求数
     */
    FaultResultVO cacheSingleFlight(int concurrency);

    /**
     * GC 压力：按 mbPerSecond 持续分配大对象，制造 YGC 频率暴涨、P99 抖动。
     * 异步执行，seconds 后停止；重点看 jstat 的 YGC/YGCT 增速。
     *
     * @param seconds     持续时间（秒）
     * @param mbPerSecond 每秒分配量（MB）
     */
    FaultResultVO gcPressure(int seconds, int mbPerSecond);
}

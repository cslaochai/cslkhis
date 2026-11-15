package com.his.webfault.controller;

import com.his.webfault.service.FaultLabService;
import com.his.webfault.vo.FaultResultVO;
import com.his.webfault.vo.FaultStatusVO;
import com.his.webfault.vo.ScenarioVO;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 演练触发器。演练进程只绑 127.0.0.1，不对外；路径统一 /fault/{场景}/{动作}。
 * 这里的接口故意「一点就应」，参数全部带上限，由 service 层夹取。
 */
@RestController
@RequestMapping("/fault")
public class FaultLabController {

    private final FaultLabService faultLabService;

    public FaultLabController(FaultLabService faultLabService) {
        this.faultLabService = faultLabService;
    }

    /**
     * 场景目录：前端 10 张卡片的数据源，只读，不触发任何故障。
     */
    @GetMapping("/catalog")
    public List<ScenarioVO> catalog() {
        return faultLabService.catalog();
    }

    /**
     * 实时水位：堆、直接内存、线程数、活跃演练线程。
     * 前端每 5 秒轮询一次；GET 不写 fault-trigger 日志，避免刷屏。
     */
    @GetMapping("/status")
    public FaultStatusVO status() {
        return faultLabService.status();
    }

    /**
     * CPU 打满：构造灾难性回溯的正则。
     * 后台线程执行，seconds 后自行结束；正则匹配不可中断，cpuStop 只能阻止下一轮。
     *
     * @param seconds 持续时间（秒）
     * @param chars   正则输入长度，越长回溯越慢
     */
    @PostMapping("/cpu/regex")
    public FaultResultVO cpuRegex(@RequestParam(defaultValue = "20") int seconds,
                                  @RequestParam(defaultValue = "25") int chars) {
        return faultLabService.cpuRegex(seconds, chars);
    }

    /**
     * CPU 打满：启动多个自旋线程，制造多核一起高、接口不返回的现象。
     * 用于演示 jstack 两次采样对比，找出一直在同一行的自旋线程。
     *
     * @param threads 自旋线程数
     * @param seconds 持续时间（秒）
     */
    @PostMapping("/cpu/spin")
    public FaultResultVO cpuSpin(@RequestParam(defaultValue = "4") int threads,
                                 @RequestParam(defaultValue = "20") int seconds) {
        return faultLabService.cpuSpin(threads, seconds);
    }

    /**
     * 停止自旋线程；正则线程不可中断，会在当前一次匹配结束后退出。
     */
    @PostMapping("/cpu/stop")
    public FaultResultVO cpuStop() {
        return faultLabService.cpuStop();
    }

    /**
     * 堆内泄漏：保留 mb 大小的 byte[]，模拟无上限本地缓存。
     * 可重复调用累加；配合 /heap/release 释放。
     *
     * @param mb 本次保留的堆内存（MB）
     */
    @PostMapping("/heap/leak")
    public FaultResultVO heapLeak(@RequestParam(defaultValue = "128") int mb) {
        return faultLabService.heapLeak(mb);
    }

    /**
     * 真 OOM：持续分配直到抛出 OutOfMemoryError，并捕获异常。
     * releaseAfter=true 时随后释放堆，false 则保留现场；线上真 OOM 往往会进程僵死。
     *
     * @param mb           目标分配量（MB）
     * @param releaseAfter 捕获 OOME 后是否立即释放
     */
    @PostMapping("/heap/oom")
    public FaultResultVO heapOom(@RequestParam(defaultValue = "256") int mb,
                                 @RequestParam(defaultValue = "true") boolean releaseAfter) {
        return faultLabService.heapOom(mb, releaseAfter);
    }

    /**
     * 释放堆内演练对象，并主动触发一次 System.gc()，便于观察堆回落。
     */
    @PostMapping("/heap/release")
    public FaultResultVO heapRelease() {
        return faultLabService.heapRelease();
    }

    /**
     * 堆外内存泄漏：分配 DirectByteBuffer，制造「堆正常、RSS 上涨」的现象。
     * 用于演示 NMT 与 BufferPool direct 的排查路径。
     *
     * @param mb 本次分配的堆外内存（MB）
     */
    @PostMapping("/offheap/leak")
    public FaultResultVO offheapLeak(@RequestParam(defaultValue = "128") int mb) {
        return faultLabService.directLeak(mb);
    }

    /**
     * 释放 DirectByteBuffer，等 Cleaner 归还物理内存。
     */
    @PostMapping("/offheap/release")
    public FaultResultVO offheapRelease() {
        return faultLabService.directRelease();
    }

    /**
     * 单请求变慢：当前线程 sleep，占住一个 Tomcat worker。
     * 这是同步阻塞，调用方会一直等到 delayMs 结束。
     *
     * @param delayMs 阻塞时长（毫秒）
     */
    @PostMapping("/thread/slow")
    public FaultResultVO threadSlow(@RequestParam(defaultValue = "3000") int delayMs) {
        return faultLabService.threadSlow(delayMs);
    }

    /**
     * 线程耗尽：异步发起 requests 个请求，每个占住一个 worker 并 sleep。
     * Tomcat maxThreads=200，超过后新请求开始排队，用于演示 busyThreads 顶满。
     *
     * @param requests 并发请求数
     * @param delayMs  每个请求占用 worker 的时长（毫秒）
     */
    @PostMapping("/thread/load")
    public FaultResultVO threadLoad(@RequestParam(defaultValue = "300") int requests,
                                    @RequestParam(defaultValue = "3000") int delayMs) {
        return faultLabService.threadLoad(requests, delayMs);
    }

    /**
     * 下游变慢：当前请求同步等待 delayMs，模拟调用第三方接口被拖住。
     *
     * @param delayMs 等待时长（毫秒）
     */
    @PostMapping("/thread/downstream")
    public FaultResultVO threadDownstream(@RequestParam(defaultValue = "3000") int delayMs) {
        return faultLabService.threadDownstream(delayMs);
    }

    /**
     * 初始化演练库：H2 内存库建 20 万行、无索引的挂号表。
     * 首次调用会插入数据，耗时数秒；重复调用返回 ALREADY-INITIALIZED，不重复建。
     */
    @PostMapping("/db/init")
    public FaultResultVO dbInit() {
        return faultLabService.dbInit();
    }

    /**
     * 慢 SQL：对 20 万行表做姓名 LIKE 全表扫描 + 排序，模拟「模糊查询没加索引」。
     * 内部会自动初始化演练数据，不必先调 /db/init。
     *
     * @param keyword 姓名关键字
     */
    @PostMapping("/db/slowQuery")
    public FaultResultVO dbSlowQuery(@RequestParam(defaultValue = "张") String keyword) {
        return faultLabService.dbSlowQuery(keyword);
    }

    /**
     * 连接池耗尽：并发借走 Hikari 连接并各 hold 一段时间，第 11 个请求等 connectionTimeout 后失败。
     * 并发数和 hold 时长由 service 按配置夹取，避免把演练进程彻底压死。
     *
     * @param requests 并发占连接数
     * @param holdMs   每条连接占用时长（毫秒）
     */
    @PostMapping("/db/hold")
    public FaultResultVO dbHold(@RequestParam(defaultValue = "20") int requests,
                                @RequestParam(defaultValue = "4000") int holdMs) {
        return faultLabService.dbHold(requests, holdMs);
    }

    /**
     * 连接池水位：只读 active/idle/total/waiting/max 与获取失败次数。
     * 首次调用会先借还一次连接触发 Hikari 建池，否则 MXBean 还是 null。
     */
    @PostMapping("/db/poolStatus")
    public FaultResultVO dbPoolStatus() {
        return faultLabService.dbPoolStatus();
    }

    /**
     * Java 死锁：两个线程按相反顺序加锁，制造永久 BLOCKED。
     * 触发后尽快用 jcmd Thread.print 抓现场，搜索 "Found one Java-level deadlock"。
     */
    @PostMapping("/deadlock")
    public FaultResultVO deadlock() {
        return faultLabService.deadlock();
    }

    /**
     * 缓存击穿：并发请求同时未命中，全部回源，下游调用数被放大。
     * 这是内存模拟，不依赖真实 Redis；用于和 singleflight 做对照。
     *
     * @param concurrency 并发请求数
     */
    @PostMapping("/cache/stampede")
    public FaultResultVO cacheStampede(@RequestParam(defaultValue = "40") int concurrency) {
        return faultLabService.cacheStampede(concurrency);
    }

    /**
     * 缓存击穿修复版：同一 key 只允许一个请求回源，其余请求复用结果。
     * 与 /cache/stampede 使用相同并发数，对比 downstreamCalls 的差异。
     *
     * @param concurrency 并发请求数
     */
    @PostMapping("/cache/singleflight")
    public FaultResultVO cacheSingleFlight(@RequestParam(defaultValue = "40") int concurrency) {
        return faultLabService.cacheSingleFlight(concurrency);
    }

    /**
     * GC 压力：按 mbPerSecond 持续分配大对象，制造 YGC 频率暴涨、P99 抖动。
     * 堆总量不一定高，重点看 jstat 的 YGC/YGCT 增速。
     *
     * @param seconds      持续时间（秒）
     * @param mbPerSecond  每秒分配量（MB）
     */
    @PostMapping("/gc/pressure")
    public FaultResultVO gcPressure(@RequestParam(defaultValue = "20") int seconds,
                                    @RequestParam(defaultValue = "32") int mbPerSecond) {
        return faultLabService.gcPressure(seconds, mbPerSecond);
    }
}

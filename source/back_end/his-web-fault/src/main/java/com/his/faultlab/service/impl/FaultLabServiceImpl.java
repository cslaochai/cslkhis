package com.his.faultlab.service.impl;

import com.his.faultlab.service.FaultLabService;
import com.his.faultlab.support.CacheStampedeSimulator;
import com.his.faultlab.support.CpuStressor;
import com.his.faultlab.support.DeadlockMaker;
import com.his.faultlab.support.DbStressor;
import com.his.faultlab.support.DirectLeaker;
import com.his.faultlab.support.FaultExecutor;
import com.his.faultlab.support.GcPressure;
import com.his.faultlab.support.HeapLeaker;
import com.his.faultlab.support.ThreadStressor;
import com.his.faultlab.vo.FaultResultVO;
import com.his.faultlab.vo.FaultStatusVO;
import com.his.faultlab.vo.ScenarioVO;
import org.springframework.stereotype.Service;

import java.lang.management.BufferPoolMXBean;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;
import java.util.List;

@Service
public class FaultLabServiceImpl implements FaultLabService {

    private final CpuStressor cpuStressor;
    private final HeapLeaker heapLeaker;
    private final DirectLeaker directLeaker;
    private final ThreadStressor threadStressor;
    private final DbStressor dbStressor;
    private final DeadlockMaker deadlockMaker;
    private final CacheStampedeSimulator cacheSimulator;
    private final GcPressure gcPressure;
    private final FaultExecutor executor;

    public FaultLabServiceImpl(CpuStressor cpuStressor,
                               HeapLeaker heapLeaker,
                               DirectLeaker directLeaker,
                               ThreadStressor threadStressor,
                               DbStressor dbStressor,
                               DeadlockMaker deadlockMaker,
                               CacheStampedeSimulator cacheSimulator,
                               GcPressure gcPressure,
                               FaultExecutor executor) {
        this.cpuStressor = cpuStressor;
        this.heapLeaker = heapLeaker;
        this.directLeaker = directLeaker;
        this.threadStressor = threadStressor;
        this.dbStressor = dbStressor;
        this.deadlockMaker = deadlockMaker;
        this.cacheSimulator = cacheSimulator;
        this.gcPressure = gcPressure;
        this.executor = executor;
    }

    @Override
    public FaultStatusVO status() {
        MemoryMXBean memory = ManagementFactory.getMemoryMXBean();
        MemoryUsage heap = memory.getHeapMemoryUsage();
        long directUsed = ManagementFactory.getPlatformMXBeans(BufferPoolMXBean.class).stream()
                .filter(pool -> "direct".equals(pool.getName()))
                .mapToLong(BufferPoolMXBean::getMemoryUsed)
                .sum();
        return new FaultStatusVO(
                heap.getUsed() / (1024 * 1024),
                heap.getMax() / (1024 * 1024),
                heapLeaker.retainedMb(),
                directUsed / (1024 * 1024),
                ManagementFactory.getThreadMXBean().getThreadCount(),
                executor.activeThreads(),
                Runtime.getRuntime().availableProcessors(),
                ManagementFactory.getRuntimeMXBean().getInputArguments().toString());
    }

    @Override
    public List<ScenarioVO> catalog() {
        return List.of(
                new ScenarioVO("cpu-regex", "CPU 打满：正则灾难性回溯",
                        "CPU 单核 100%，QPS 很低；同一时刻只有一个请求慢，jstack 里线程停在 Pattern$matcher",
                        "POST /fault/cpu/regex?seconds=20&chars=25",
                        "top -Hp <pid> 找高 CPU 线程 → printf %x → jstack 里对 nid"),
                new ScenarioVO("cpu-spin", "CPU 打满：死循环/无界重试",
                        "多核一起高，接口不返回；jstack 同一行代码反复出现",
                        "POST /fault/cpu/spin?threads=4&seconds=20",
                        "arthas profiler / jstack 采样两次对比"),
                new ScenarioVO("heap-leak", "堆内泄漏：本地缓存无上限",
                        "老年代阶梯上涨，FGC 越来越频繁，最终 OOM",
                        "POST /fault/heap/leak?mb=256",
                        "jmap -histo:live / MAT dominator tree 找到持有者"),
                new ScenarioVO("heap-oom", "真 OOM：堆被填满",
                        "OutOfMemoryError: Java heap space，接口 500，进程可能僵死",
                        "POST /fault/heap/oom?mb=512",
                        "heap dump + GC root 路径；-XX:+HeapDumpOnOutOfMemoryError"),
                new ScenarioVO("offheap", "堆外内存泄漏",
                        "堆正常、GC 正常，但容器 RSS 上涨，最终被 OOMKilled",
                        "POST /fault/offheap/leak?mb=256",
                        "NMT: jcmd <pid> VM.native_memory summary；BufferPool direct"),
                new ScenarioVO("thread-exhaust", "线程耗尽：下游慢拖死 Tomcat",
                        "所有接口超时，CPU 不高；jstack 大量 socketRead/WAITING",
                        "POST /fault/thread/load?requests=300&delayMs=3000",
                        "jstack 统计线程状态分布 + Tomcat busyThreads 指标"),
                new ScenarioVO("db-pool", "连接池耗尽：慢 SQL 占满 Hikari",
                        "接口等 3s 后报 Connection is not available，DB 连接数打满",
                        "POST /fault/db/hold?requests=20&holdMs=4000",
                        "hikaricp_connections_active / pending；show processlist"),
                new ScenarioVO("deadlock", "Java 死锁：加锁顺序相反",
                        "接口既不报错也不返回，CPU 不高，线程永久 BLOCKED",
                        "POST /fault/deadlock",
                        "jcmd <pid> Thread.print → Found one Java-level deadlock"),
                new ScenarioVO("cache-stampede", "缓存击穿：热点 key 过期瞬间回源",
                        "下游 QPS 瞬时放大 N 倍，数据库连接被打满",
                        "POST /fault/cache/stampede?concurrency=40",
                        "对比 singleflight 前后 downstreamCalls"),
                new ScenarioVO("gc-pressure", "GC 压力：大对象高频分配",
                        "YGC 频率暴涨、P99 抖动，但堆占用不高",
                        "POST /fault/gc/pressure?seconds=20",
                        "jstat -gcutil 1000 看 YGC/YGCT 增速"));
    }

    @Override
    public FaultResultVO cpuRegex(int seconds, int chars) {
        return result("cpu-regex", cpuStressor.startRegex(seconds, chars),
                "下一步：top -Hp <pid> 找最高 CPU 线程，printf '%x' 得到 nid，再到 jstack 输出里搜 nid");
    }

    @Override
    public FaultResultVO cpuSpin(int threads, int seconds) {
        return result("cpu-spin", cpuStressor.startSpin(threads, seconds),
                "下一步：jstack 采样两次（间隔 3s），diff 两次都在同一行的线程就是自旋线程");
    }

    @Override
    public FaultResultVO cpuStop() {
        return result("cpu-stop", cpuStressor.stop(), "正则线程不可中断，会在当前一次匹配结束后退出");
    }

    @Override
    public FaultResultVO heapLeak(int mb) {
        return result("heap-leak", heapLeaker.leak(mb),
                "下一步：jcmd <pid> GC.heap_info 看老年代增长；jmap -histo:live 看 byte[] 数量");
    }

    @Override
    public FaultResultVO heapOom(int mb, boolean releaseAfter) {
        return result("heap-oom", heapLeaker.oom(mb, releaseAfter),
                "OOME 已捕获，堆随后释放；线上真 OOM 通常进程僵死，需要重启 + dump 分析");
    }

    @Override
    public FaultResultVO heapRelease() {
        heapLeaker.release();
        return result("heap-release", "RELEASED", "已清空保留的 byte[]，并触发一次 System.gc()");
    }

    @Override
    public FaultResultVO directLeak(int mb) {
        return result("offheap", directLeaker.leak(mb),
                "下一步：jcmd <pid> VM.native_memory summary（需 -XX:NativeMemoryTracking=summary）");
    }

    @Override
    public FaultResultVO directRelease() {
        directLeaker.release();
        return result("offheap-release", "RELEASED", "已释放 DirectByteBuffer，等 Cleaner 归还物理内存");
    }

    @Override
    public FaultResultVO threadSlow(int delayMs) {
        return result("thread-slow", threadStressor.slow(delayMs), "单个请求占住一个 Tomcat worker");
    }

    @Override
    public FaultResultVO threadLoad(int requests, int delayMs) {
        return result("thread-exhaust", threadStressor.load(requests, delayMs),
                "下一步：jstack <pid> | grep -c 'socketRead'；同时看 Tomcat busyThreads 是否顶到 maxThreads");
    }

    @Override
    public FaultResultVO threadDownstream(int delayMs) {
        return result("thread-downstream", threadStressor.downstream(delayMs), "当前请求线程同步等下游");
    }

    @Override
    public FaultResultVO dbInit() {
        return result("db-init", dbStressor.initData(), "演练表已就绪：biz_registration 20 万行、无索引");
    }

    @Override
    public FaultResultVO dbSlowQuery(String keyword) {
        return result("db-slow", dbStressor.slowQuery(keyword),
                "下一步：EXPLAIN 看 type=ALL；H2 里看 SCAN 行数，MySQL 里看 rows 与 Using filesort");
    }

    @Override
    public FaultResultVO dbHold(int requests, int holdMs) {
        return result("db-pool", dbStressor.hold(requests, holdMs),
                "下一步：/actuator/metrics/hikaricp.connections.active + 再发一次请求观察等待与失败");
    }

    @Override
    public FaultResultVO dbPoolStatus() {
        return result("db-pool-status", dbStressor.poolStatus(),
                "active 顶到 max 且 waiting>0 = 连接池耗尽；线上再看 show processlist 与慢查询日志");
    }

    @Override
    public FaultResultVO deadlock() {
        return result("deadlock", deadlockMaker.trigger(),
                "下一步：10 秒内 jcmd <pid> Thread.print，搜索 'Found one Java-level deadlock'");
    }

    @Override
    public FaultResultVO cacheStampede(int concurrency) {
        var result = cacheSimulator.run(concurrency, false);
        return result("cache-stampede",
                "STAMPEDE: 并发=" + concurrency + ", 下游调用=" + result.downstreamCalls()
                        + ", 耗时=" + result.elapsedMs() + "ms",
                "对照：POST /fault/cache/singleflight?concurrency=" + concurrency);
    }

    @Override
    public FaultResultVO cacheSingleFlight(int concurrency) {
        var result = cacheSimulator.run(concurrency, true);
        return result("cache-singleflight",
                "SINGLE-FLIGHT: 并发=" + concurrency + ", 下游调用=" + result.downstreamCalls()
                        + ", 耗时=" + result.elapsedMs() + "ms",
                "修复后下游只被打 1 次；生产上还要加 TTL 随机化 + 空值缓存");
    }

    @Override
    public FaultResultVO gcPressure(int seconds, int mbPerSecond) {
        return result("gc-pressure", gcPressure.start(seconds, mbPerSecond),
                "下一步：jstat -gcutil <pid> 1000，观察 YGC 每秒增长与 YGCT 累计");
    }

    private FaultResultVO result(String scenario, String message, String hint) {
        FaultStatusVO status = status();
        return new FaultResultVO(scenario,
                message.startsWith("REJECTED") ? "REJECTED" : "OK",
                message,
                status.heapUsedMb(),
                status.heapMaxMb(),
                status.retainedHeapMb(),
                status.retainedDirectMb(),
                status.activeFaultThreads(),
                hint);
    }
}

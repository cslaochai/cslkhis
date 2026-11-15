# his-web-fault：his-web 故障演练沙箱

> 把「线上才会出现的故障」变成一条 curl 就能复现的确定性事件，然后用 jcmd / arthas / MAT 走一遍排查链路。

## 为什么单独一个模块

| 做法 | 问题 |
|---|---|
| 把故障代码塞进 his-web，用 `@Profile("dev")` 开 | 演练要连 MySQL/Redis/RocketMQ，起服务 1 分钟；误开就是生产事故 |
| 单独进程 + 独立端口 | 不依赖任何中间件，6 秒启动；只绑 `127.0.0.1`，不对外；演练完 Ctrl+C 即净 |

本模块**不被 his-web 依赖**，只随根 pom 一起构建，不进入生产包。

## 启动

```bat
rem Windows：堆故意给小（384m），OOM 场景 3 秒就能复现
cd source\back_end\his-web-fault
mvn -o -DskipTests package
run-web-fault.cmd
```

```bash
# Linux（面试现场用笔记本也可以）
java -Xmx384m -XX:+HeapDumpOnOutOfMemoryError \
     -XX:HeapDumpPath=./run/dumps \
     -XX:NativeMemoryTracking=summary \
     -Xlog:gc*:file=./run/logs/gc.log:time,uptime,level,tags \
     -jar target/his-web-fault-1.0.0.jar --server.port=18090
```

启动后先打一枪健康检查：

```bash
curl http://127.0.0.1:18090/fault/status
```

返回里有 `heapUsedMb / heapMaxMb / retainedHeapMb / retainedDirectMb / activeFaultThreads`，
**先确认故障真的发生了**，再去看 JVM 指标，否则容易对着正常指标讲半天。

## 日志与监控（虚拟机怎么读）

应用日志同时写控制台和 `run/logs/his-web-fault.log`（10MB 滚动，保留 7 份）；`run-web-fault.cmd` 另写 GC 日志 `run/logs/gc.log`。

虚拟机**不会自动读取** Windows 本地文件，任选一种接法：

| 方式 | 做法 | 适用 |
|---|---|---|
| 共享目录 | VMware/VirtualBox 共享文件夹，或 Windows 建 SMB 共享后在 VM `mount -t cifs //<win-ip>/faultlab-logs /mnt/faultlab` | 只想 `tail -f` 看日志 |
| 日志采集 | Windows 装 Filebeat / Promtail / Vector，输入 `run/logs/*.log`，输出到 VM 的 Logstash / Loki | ELK / Loki 监控 |
| 跑在 VM | 把 jar 放到 Linux VM，`java -jar` 启动；Windows 用 `ssh -L 18090:127.0.0.1:18090 user@vm` 隧道 | 要 `top -Hp`、`jstack`、`jstat` 真数据 |

完整节点与拓扑见 `施工手册/14-故障演练/10-日志监控链路.md`。

Actuator 目前只在 `127.0.0.1:18090` 开 `health/info/metrics/threaddump/heapdump/loggers`，没有 Prometheus 端点；
VM 里的 Prometheus 要抓 JVM 指标，需要另加 `micrometer-registry-prometheus` 并把 management 端口单独暴露。

## 场景索引

| # | 场景 | 一句话现象 | 触发 |
|---|---|---|---|
| 01 | CPU 打满：正则灾难性回溯 | 单核 100%，QPS 很低 | `POST /fault/cpu/regex?seconds=20&chars=25` |
| 02 | CPU 打满：死循环 | 多核一起高，接口不返回 | `POST /fault/cpu/spin?threads=4&seconds=20` |
| 03 | 堆内泄漏 → OOM | 老年代阶梯上涨，最终 Java heap space | `POST /fault/heap/leak?mb=128`、`POST /fault/heap/oom?mb=384` |
| 04 | 堆外内存泄漏 | 堆正常、GC 正常，RSS 一直涨 | `POST /fault/offheap/leak?mb=256` |
| 05 | 线程耗尽 | 所有接口超时，CPU 不高 | `POST /fault/thread/load?requests=300&delayMs=10000` |
| 06 | 连接池耗尽 | 等 3s 报 Connection is not available | `POST /fault/db/hold?requests=20&holdMs=4000` |
| 07 | Java 死锁 | 接口既不报错也不返回 | `POST /fault/deadlock` |
| 08 | 缓存击穿 | 下游 QPS 瞬间放大 N 倍 | `POST /fault/cache/stampede?concurrency=40` |
| 09 | GC 压力 | YGC 频率暴涨、P99 抖动 | `POST /fault/gc/pressure?seconds=20&mbPerSecond=32` |

完整目录（含「看什么」）直接问接口：

```bash
curl http://127.0.0.1:18090/fault/catalog
```

## 通用排查链路（所有场景都先走这五步）

```
① 定影响面   哪个接口/哪类用户/从几点开始 → 看 RED 指标（Rate/Error/Duration）
② 分资源域   CPU 高？内存涨？线程满？连接满？磁盘 IO？ → USE（Utilization/Saturation/Errors）
③ 落到进程   top 找 java → 确认是业务进程不是 GC/监控进程
④ 落到线程   线程级 CPU（top -Hp）→ nid 转 16 进制 → jstack 对栈
⑤ 落到代码   栈顶业务类 → 结合 arthas watch/trace 或日志确认入参
```

止损永远在定位之前：限流、摘流量、重启、回滚。**先恢复，再找根因。**

## 工具对照

| 目的 | Linux（线上） | 本机（Windows / JDK 21） |
|---|---|---|
| 进程 | `jps -l` / `ps -ef \| grep java` | `jps -l` |
| 线程级 CPU | `top -Hp <pid>` → `printf '%x' <tid>` | `jcmd <pid> Thread.print` 直接看线程名 |
| 线程栈 | `jstack <pid>` / `jcmd <pid> Thread.print` | `jcmd <pid> Thread.print` |
| 堆直方图 | `jmap -histo:live <pid> \| head -30` | 同左 |
| 堆快照 | `jmap -dump:live,format=b,file=heap.hprof <pid>` | 同左，或 `GET /actuator/heapdump` |
| GC | `jstat -gcutil <pid> 1000` | 同左 |
| 堆外 | `jcmd <pid> VM.native_memory summary` | 同左（启动加 `-XX:NativeMemoryTracking=summary`） |
| 在线诊断 | arthas：`thread -n 3`、`profiler start`、`watch` | arthas 同样可用 |
| 堆分析 | MAT / JProfiler：Dominator Tree、Path to GC Roots | 同左 |

## 安全边界（代码里已经写死）

- `server.address=127.0.0.1`：同网段其他机器调不到，接口无鉴权也不会被滥用。
- 每类故障都有硬上限：CPU 线程 ≤ 8、单次 ≤ 60s；保留内存 ≤ 512MB；并发 ≤ 400。
- 故障线程全部 daemon，Ctrl+C 不会被拖住。
- **死锁场景不可自愈**（和线上一样），只能重启演练进程；代码里限制为只能触发一次。
- 演练数据在内存 H2 里，20 万行 `biz_registration`，不连院内库。

## 目录

```
his-web-fault/
├── run-web-fault.cmd               # 带 JVM 参数的启动脚本（OOM dump / NMT / GC 日志）
└── src/main/java/com/his/webfault/
    ├── controller/FaultLabController.java     # 所有触发器（POST）
    ├── service/impl/FaultLabServiceImpl.java  # 参数夹取 + 状态拼装
    ├── support/                               # 每类故障一个类，互不复用
    │   ├── CpuStressor.java
    │   ├── HeapLeaker.java / DirectLeaker.java
    │   ├── ThreadStressor.java / SlowSocketServer.java
    │   ├── DbStressor.java
    │   ├── DeadlockMaker.java
    │   ├── CacheStampedeSimulator.java
    │   └── GcPressure.java
    └── vo/                                    # 回执与目录
```

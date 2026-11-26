package com.his.iot.support;

import com.his.iot.config.IotProperties;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 遥测落库线程：内存队列 + 定时批量 INSERT 到 TDengine。
 * <p>
 * 存在的理由只有一个：MQTT 回调是「每条来一次」，TDengine 适合「一批写一次」。两者之间必须有一个缓冲，
 * 否则每秒几百条单行 INSERT 全耗在 SQL 解析和网络往返上。
 * <p>
 * 队列是缓冲不是仓库：涨到 20 万条说明落库跟不上（时序库挂了），宁可丢点也不能把 HIS 堆内存拖爆。
 * 只有 {@code his.iot.ingest-enabled=true} 才启动；联调期若另有独立落库进程，两边会写重，靠开关互斥。
 */
@Component
@RequiredArgsConstructor
public class TsdbTelemetryWriter {

    private static final Logger log = LoggerFactory.getLogger(TsdbTelemetryWriter.class);
    private static final int QUEUE_LIMIT = 200_000;
    /** 每 N 次刷写打一行统计（周期 1s 时约 30 秒一次），不为此再养一个调度线程 */
    private static final int REPORT_EVERY = 30;

    private final IotProperties properties;

    private final Queue<TelemetryPoint> queue = new ConcurrentLinkedQueue<>();
    /**
     * 队列长度自维护。{@code ConcurrentLinkedQueue.size()} 是遍历整条链，逐条 offer 时调它等于每条都数一遍队列：
     * 越积压越慢，正好在压力最大的时候最慢。
     */
    private final AtomicLong pending = new AtomicLong();
    private final AtomicLong received = new AtomicLong();
    private final AtomicLong written = new AtomicLong();
    private final AtomicLong failed = new AtomicLong();
    private final AtomicLong unsupportedType = new AtomicLong();
    private final AtomicLong dropped = new AtomicLong();

    private ScheduledExecutorService flusher;
    private Connection conn;
    private int flushes;

    @PostConstruct
    public void start() {
        if (!properties.isIngestEnabled()) {
            log.info("物联网订阅落库未开启（his.iot.ingest-enabled=false），HIS 只提供查询接口");
            return;
        }
        try {
            Class.forName("com.taosdata.jdbc.rs.RestfulDriver");
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException("缺少 taos-jdbcdriver，检查 his-ms-iot 依赖", e);
        }
        long period = Math.max(1, properties.getFlushMs());
        flusher = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "iot-tsdb-flush");
            t.setDaemon(true);
            return t;
        });
        flusher.scheduleWithFixedDelay(this::flushSafely, period, period, TimeUnit.MILLISECONDS);
        log.info("物联网落库线程启动：{} -> {}，周期 {}ms，映射 {} 类设备",
                properties.getTsdb().getDatabase(), properties.getTsdb().getUrl(),
                period, DeviceTypeSchema.size());
    }

    /**
     * 订阅侧收到一条遥测后投递到这里；未开启落库时静默丢弃（订阅侧不会启动）
     */
    public void offer(TelemetryPoint point) {
        received.incrementAndGet();
        if (pending.get() >= QUEUE_LIMIT) {
            dropped.incrementAndGet();
            return;
        }
        queue.add(point);
        pending.incrementAndGet();
    }

    private void flushSafely() {
        try {
            flush();
        } catch (Exception e) {
            failed.incrementAndGet();
            log.warn("物联网落库异常：{}", e.getMessage());
            closeConnection();
        } finally {
            if (++flushes % REPORT_EVERY == 0) {
                log.info("物联网落库统计 接收={} 写入={} 失败={} 队列={} 未知类型={} 队列满丢弃={}",
                        received.get(), written.get(), failed.get(), pending.get(),
                        unsupportedType.get(), dropped.get());
            }
        }
    }

    private void flush() {
        if (queue.isEmpty()) {
            return;
        }
        // 一条 INSERT 只能打一张子表，按「类型|设备号」分组后逐组拼 SQL
        Map<String, List<TelemetryPoint>> byDevice = new LinkedHashMap<>();
        int taken = 0;
        TelemetryPoint p;
        while (taken < properties.getMaxBatchPerFlush() && (p = queue.poll()) != null) {
            taken++;
            pending.decrementAndGet();
            if (DeviceTypeSchema.of(p.deviceType()) == null) {
                unsupportedType.incrementAndGet();
                continue;
            }
            byDevice.computeIfAbsent(p.deviceType() + '|' + p.deviceNo(), k -> new ArrayList<>()).add(p);
        }
        if (byDevice.isEmpty()) {
            return;
        }

        Connection c = connection();
        int chunk = Math.max(1, properties.getMaxRowsPerInsert());
        for (List<TelemetryPoint> points : byDevice.values()) {
            TelemetryPoint any = points.getFirst();
            DeviceTypeSchema.Schema schema = DeviceTypeSchema.of(any.deviceType());
            for (int from = 0; from < points.size(); from += chunk) {
                List<TelemetryPoint> slice = points.subList(from, Math.min(from + chunk, points.size()));
                String sql = TelemetrySqlBuilder.build(properties.getTsdb().getDatabase(), schema,
                        any.deviceType(), any.deviceNo(), properties.getTsdb().getDefaultDeptId(), slice);
                try (Statement st = c.createStatement()) {
                    st.executeUpdate(sql);
                    written.addAndGet(slice.size());
                } catch (SQLException ex) {
                    failed.addAndGet(slice.size());
                    log.warn("物联网写入失败 {}/{}：{}；SQL 片段：{}", any.deviceType(), any.deviceNo(),
                            ex.getMessage(), abbreviate(sql));
                    closeConnection();
                    // 连接已坏，本轮剩余批次交给下一秒重试
                    return;
                }
            }
        }
    }

    private Connection connection() {
        if (conn != null) {
            try {
                if (!conn.isClosed()) {
                    return conn;
                }
            } catch (SQLException e) {
                // 连接状态已不可信，关掉重建
                closeConnection();
            }
        }
        IotProperties.Tsdb tsdb = properties.getTsdb();
        try {
            conn = DriverManager.getConnection(tsdb.getUrl(), tsdb.getUser(), tsdb.getPassword());
            log.info("已连接物联网时序库：{}", tsdb.getUrl());
            return conn;
        } catch (SQLException e) {
            failed.incrementAndGet();
            log.warn("连接物联网时序库失败：{}", e.getMessage());
            throw new IllegalStateException("时序库连接不可用", e);
        }
    }

    private void closeConnection() {
        if (conn == null) {
            return;
        }
        try {
            conn.close();
        } catch (Exception ignored) {
            // 关不掉也不影响下一秒重建
        }
        conn = null;
    }

    private static String abbreviate(String sql) {
        return sql.length() <= 220 ? sql : sql.substring(0, 220) + "...";
    }

    @PreDestroy
    public void stop() {
        if (flusher != null) {
            flusher.shutdownNow();
        }
        closeConnection();
    }
}

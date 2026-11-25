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
 * 只有 {@code his.iot.ingest-enabled=true} 时才启动。联调期若已有独立落库进程在跑，
 * 两边同时写会把同一条遥测写两遍——所以默认关闭，由开关而不是代码保证唯一写入方。
 * <p>
 * 队列只是缓冲：涨到 20 万条说明落库跟不上（时序库挂了），宁可丢点也不能把 HIS 堆内存拖爆。
 */
@Component
@RequiredArgsConstructor
public class TsdbTelemetryWriter {

    private static final Logger log = LoggerFactory.getLogger(TsdbTelemetryWriter.class);
    private static final int QUEUE_LIMIT = 200_000;

    private final IotProperties properties;

    private final Queue<TelemetryPoint> queue = new ConcurrentLinkedQueue<>();
    private final AtomicLong received = new AtomicLong();
    private final AtomicLong written = new AtomicLong();
    private final AtomicLong failed = new AtomicLong();
    private final AtomicLong unsupportedType = new AtomicLong();
    private final AtomicLong dropped = new AtomicLong();

    private ScheduledExecutorService flusher;
    private ScheduledExecutorService reporter;
    private Connection conn;

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
        flusher = Executors.newSingleThreadScheduledExecutor(r -> daemon(r, "iot-tsdb-flush"));
        flusher.scheduleWithFixedDelay(this::flushSafely,
                properties.getFlushMs(), properties.getFlushMs(), TimeUnit.MILLISECONDS);

        reporter = Executors.newSingleThreadScheduledExecutor(r -> daemon(r, "iot-tsdb-report"));
        reporter.scheduleWithFixedDelay(this::report, 30, 30, TimeUnit.SECONDS);

        log.info("物联网落库线程启动：{} -> {}，周期 {}ms，映射 {} 类设备",
                properties.getTsdb().getDatabase(), properties.getTsdb().getUrl(),
                properties.getFlushMs(), DeviceTypeSchema.size());
    }

    private static Thread daemon(Runnable r, String name) {
        Thread t = new Thread(r, name);
        t.setDaemon(true);
        return t;
    }

    /**
     * 订阅侧收到一条遥测后投递到这里；未开启落库时静默丢弃（订阅侧不会启动）
     */
    public void offer(TelemetryPoint point) {
        received.incrementAndGet();
        if (queue.size() > QUEUE_LIMIT) {
            dropped.incrementAndGet();
            return;
        }
        queue.add(point);
    }

    private void flushSafely() {
        try {
            flush();
        } catch (Exception e) {
            failed.incrementAndGet();
            log.warn("物联网落库异常：{}", e.getMessage());
            closeConnection();
        }
    }

    private void flush() {
        if (queue.isEmpty()) {
            return;
        }
        Map<String, List<TelemetryPoint>> byDevice = new LinkedHashMap<>();
        int taken = 0;
        while (taken < properties.getMaxBatchPerFlush()) {
            TelemetryPoint p = queue.poll();
            if (p == null) {
                break;
            }
            taken++;
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
        for (Map.Entry<String, List<TelemetryPoint>> e : byDevice.entrySet()) {
            List<TelemetryPoint> points = e.getValue();
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
        conn = c;
    }

    private Connection connection() {
        if (conn != null) {
            try {
                if (!conn.isClosed()) {
                    return conn;
                }
            } catch (SQLException ignored) {
                // 连接已坏，走重建
            }
        }
        IotProperties.Tsdb tsdb = properties.getTsdb();
        try {
            conn = DriverManager.getConnection(tsdb.getUrl(), tsdb.getUser(), tsdb.getPassword());
            log.info("已连接物联网时序库：{}", tsdb.getUrl());
        } catch (SQLException e) {
            failed.incrementAndGet();
            log.warn("连接物联网时序库失败：{}", e.getMessage());
            throw new IllegalStateException("时序库连接不可用", e);
        }
        return conn;
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

    private void report() {
        log.info("物联网落库统计 接收={} 写入={} 失败={} 队列={} 未知类型={} 队列满丢弃={}",
                received.get(), written.get(), failed.get(), queue.size(), unsupportedType.get(), dropped.get());
    }

    private static String abbreviate(String sql) {
        return sql.length() <= 220 ? sql : sql.substring(0, 220) + "...";
    }

    @PreDestroy
    public void stop() {
        if (flusher != null) {
            flusher.shutdownNow();
        }
        if (reporter != null) {
            reporter.shutdownNow();
        }
        closeConnection();
    }
}

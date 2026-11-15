package com.his.webfault.support;

import com.his.webfault.config.FaultLabProperties;
import com.zaxxer.hikari.HikariDataSource;
import com.zaxxer.hikari.HikariPoolMXBean;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 连接池耗尽：Hikari 10 个连接被「慢 SQL」各占 3~5 秒，第 11 个请求等 connectionTimeout 后直接失败。
 * <p>
 * 用内存 H2 + 20 万行无索引表造真实的全表扫描，不连院内库；
 * 台账字段照 HIS 门诊挂号表命名，讲的时候能对上业务。
 */
@Component
public class DbStressor {

    private final DataSource dataSource;
    private final FaultLabProperties props;
    private final FaultExecutor executor;
    private final AtomicBoolean initialized = new AtomicBoolean(false);
    private final AtomicInteger failedAcquires = new AtomicInteger();

    public DbStressor(DataSource dataSource, FaultLabProperties props, FaultExecutor executor) {
        this.dataSource = dataSource;
        this.props = props;
        this.executor = executor;
    }

    /** 惰性建 20 万行：不在启动期拖慢演练进程 */
    public String initData() {
        if (!initialized.compareAndSet(false, true)) {
            return "ALREADY-INITIALIZED";
        }
        try (Connection connection = dataSource.getConnection()) {
            connection.createStatement().execute("""
                    CREATE TABLE IF NOT EXISTS biz_registration (
                      id BIGINT PRIMARY KEY,
                      patient_name VARCHAR(32),
                      dept_name VARCHAR(32),
                      reg_time TIMESTAMP,
                      status INT
                    )
                    """);
            try (PreparedStatement count = connection.prepareStatement("SELECT COUNT(*) FROM biz_registration")) {
                var rs = count.executeQuery();
                if (rs.next() && rs.getLong(1) > 100000) {
                    return "ALREADY-SEEDED";
                }
            }
            connection.setAutoCommit(false);
            try (PreparedStatement insert = connection.prepareStatement(
                    "INSERT INTO biz_registration(id, patient_name, dept_name, reg_time, status) VALUES (?,?,?,?,?)")) {
                String[] surnames = {"张", "李", "王", "刘", "陈", "杨", "赵", "周", "吴", "徐"};
                String[] depts = {"内科", "外科", "儿科", "妇科", "骨科", "眼科", "口腔科", "急诊科"};
                for (int i = 0; i < 200_000; i++) {
                    insert.setLong(1, i + 1);
                    insert.setString(2, surnames[i % surnames.length] + "某" + (i % 1000));
                    insert.setString(3, depts[i % depts.length]);
                    insert.setTimestamp(4, new java.sql.Timestamp(1_700_000_000_000L + i * 60_000L));
                    insert.setInt(5, i % 4);
                    insert.addBatch();
                    if (i % 5000 == 0) {
                        insert.executeBatch();
                    }
                }                insert.executeBatch();
            }
            connection.commit();
            return "SEEDED 200000 rows";
        } catch (SQLException e) {
            initialized.set(false);
            throw new IllegalStateException("初始化演练数据失败", e);
        }
    }

    /**
     * 全表扫描 + 排序：对应线上「挂号列表加了个姓名模糊查询，还没索引」。
     */
    public String slowQuery(String keyword) {
        initData();
        long start = System.currentTimeMillis();
        int total;
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(
                     "SELECT COUNT(*) FROM biz_registration WHERE patient_name LIKE ?")) {
            statement.setString(1, "%" + keyword + "%");
            var rs = statement.executeQuery();
            rs.next();
            total = rs.getInt(1);
        } catch (SQLException e) {
            return "SQL-FAILED: " + e.getMessage();
        }
        long countMs = System.currentTimeMillis() - start;
        long orderStart = System.currentTimeMillis();
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(
                     "SELECT id, patient_name, dept_name, reg_time FROM biz_registration "
                             + "WHERE patient_name LIKE ? ORDER BY reg_time DESC LIMIT 3000")) {
            statement.setString(1, "%" + keyword + "%");
            var rs = statement.executeQuery();
            int rows = 0;
            while (rs.next()) {
                rows++;
            }
            return "SCAN: like=%" + keyword + "% matched=" + total
                    + ", count=" + countMs + "ms, paged=" + rows + " rows in "
                    + (System.currentTimeMillis() - orderStart) + "ms";
        } catch (SQLException e) {
            return "SQL-FAILED: " + e.getMessage();
        }
    }

    /**
     * 占住连接：并发 N 个任务各借一条连接并 sleep，超过池上限的任务会在 connectionTimeout 后失败。
     */
    public String hold(int requests, int holdMs) {
        if (!props.isEnabled()) {
            return "REJECTED: fault.enabled=false";
        }
        int count = Math.max(1, Math.min(props.getMaxConcurrency(), requests));
        int hold = Math.max(200, Math.min(props.getMaxHoldSeconds() * 1000, holdMs));
        failedAcquires.set(0);
        for (int i = 0; i < count; i++) {
            executor.run("fault-db-hold-" + i, () -> {
                try (Connection connection = dataSource.getConnection()) {
                    Thread.sleep(hold);
                } catch (Exception e) {
                    failedAcquires.incrementAndGet();
                    System.out.println("[fault-lab] acquire failed: " + e.getClass().getSimpleName());
                }
            });
        }
        return "STARTED: " + count + " tasks, each holds a connection " + hold + "ms";
    }

    public int failedAcquires() {
        return failedAcquires.get();
    }

    /**
     * 池水位：active 顶到 maximumPoolSize、waiting 持续上涨，就是「连接池耗尽」的直接证据。
     * 线上对应 Hikari 的 hikaricp_connections_active / hikaricp_connections_pending 指标。
     */
    public String poolStatus() {
        HikariDataSource hikari;
        try {
            hikari = dataSource.unwrap(HikariDataSource.class);
            // Hikari 是懒初始化：先借还一次连接，否则 pool / MXBean 都还是 null
            try (Connection ignored = dataSource.getConnection()) {
                // 只触发建池，不执行 SQL
            }
        } catch (SQLException e) {
            return "POOL-UNWRAP-FAILED: " + e.getMessage();
        }
        HikariPoolMXBean pool = hikari.getHikariPoolMXBean();
        if (pool == null) {
            return "POOL-MBEAN-DISABLED";
        }
        return "active=" + pool.getActiveConnections()
                + ", idle=" + pool.getIdleConnections()
                + ", total=" + pool.getTotalConnections()
                + ", waiting=" + pool.getThreadsAwaitingConnection()
                + ", max=" + hikari.getMaximumPoolSize()
                + ", acquireFailures=" + failedAcquires.get();
    }
}

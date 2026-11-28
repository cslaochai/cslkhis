package com.his.platform.support;

import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.io.BufferedWriter;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.GZIPOutputStream;

/**
 * 审计流水表的分区维护与归档（sys_oper_log / sys_field_change_log，见 sql 增量 438）。
 *
 * <p>只做两件事，都不碰业务数据内容：
 * <ol>
 *   <li><b>预建分区</b>：每月 1 号把未来 {@value #PARTITION_AHEAD_MONTHS} 个月的分区补出来。
 *       漏建的下场是新数据全落 pmax——不报错，但按时间归档永远挑不出它，等于静默丢账。</li>
 *   <li><b>归档过期分区</b>：已闭月且超出保留期的分区，整段导出 CSV.gz → 记 sha256 与行数到
 *       sys_audit_archive_index → 校验通过后 DROP PARTITION。删的是"整段"，不是 DELETE，
 *       毫秒级完成，锁表时间与数据量无关。</li>
 * </ol>
 *
 * <p><b>为什么必须先导出再删</b>：法规要求历次操作痕迹可查询可追溯（电子病历应用管理规范第 14 条，
 * 国卫办医政函〔2025〕262 号）。热层只保最近一段，更老的必须落成可读文件 + 台账，
 * 否则"腾空间"就等于"销毁证据"。
 *
 * <p><b>默认关闭归档</b>（{@code his.audit.archive.enabled=false}）：DROP PARTITION 不可回滚，
 * 上线前要先把归档目录纳入备份与对象存储，确认后再打开开关。分区预建不受开关影响，始终执行。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AuditTableMaintainTask {

    /**
     * 两张审计流水表：sys_oper_log 按 oper_time 分区，sys_field_change_log 按 change_time 分区。
     */
    private static final List<String> AUDIT_TABLES = List.of("sys_oper_log", "sys_field_change_log");

    /** 热层保留月数：接口日志 12 个月，字段留痕 36 个月（更长年限靠归档文件 + 台账） */
    private static final Map<String, Integer> KEEP_MONTHS = Map.of(
            "sys_oper_log", 12,
            "sys_field_change_log", 36);

    private static final int PARTITION_AHEAD_MONTHS = 3;
    private static final String PMAX = "pmax";
    private static final String ARCHIVED_BY = "system:auditArchive";
    private static final Pattern DATE_IN_DESC = Pattern.compile("(\\d{4}-\\d{2}-\\d{2})");
    private static final DateTimeFormatter PART_SUFFIX = DateTimeFormatter.ofPattern("yyyyMM");

    /** 归档不并发：两张表错峰由同一个方法串行处理，重入直接跳过 */
    private static final AtomicBoolean ARCHIVING = new AtomicBoolean(false);

    private final DataSource dataSource;
    private final JdbcTemplate jdbcTemplate;

    @Value("${his.audit.archive.enabled:false}")
    private boolean archiveEnabled;

    @Value("${his.audit.archive.dir:upload/audit-archive}")
    private String archiveDir;

    /**
     * 每月 1 号 03:00 预建分区。
     */
    @Scheduled(cron = "0 0 3 1 * ?")
    public void maintainPartitions() {
        AUDIT_TABLES.forEach(table -> {
            try {
                ensurePartitions(table);
            } catch (Exception e) {
                log.error("审计表分区预建失败 table={}", table, e);
            }
        });
    }

    /**
     * 每月 1 号 03:30 归档超出保留期的已闭月分区；开关关闭时只打一条 info。
     */
    @Scheduled(cron = "0 30 3 1 * ?")
    public void archiveOldPartitions() {
        if (!archiveEnabled) {
            log.info("审计归档未开启（his.audit.archive.enabled=false），仅预建分区；"
                    + "确认归档目录已纳入备份后再打开");
            return;
        }
        if (!ARCHIVING.compareAndSet(false, true)) {
            log.warn("上一次审计归档仍在执行，本次跳过");
            return;
        }
        try {
            Path dir = Path.of(archiveDir).toAbsolutePath();
            Files.createDirectories(dir);
            AUDIT_TABLES.forEach(table -> {
                try {
                    archiveTable(table, dir);
                } catch (Exception e) {
                    log.error("审计表归档失败 table={}", table, e);
                }
            });
        } catch (Exception e) {
            log.error("审计归档目录准备失败 dir={}", archiveDir, e);
        } finally {
            ARCHIVING.set(false);
        }
    }

    // ---------------------------------------------------------------- 分区

    /** 把分区补齐到「当前月 + AHEAD 个月」，缺的从 pmax 里切出来 */
    private void ensurePartitions(String table) {
        LocalDate targetEnd = LocalDate.now().withDayOfMonth(1).plusMonths(PARTITION_AHEAD_MONTHS + 1);
        LocalDate boundary = lastBoundary(table);
        while (boundary.isBefore(targetEnd)) {
            String partition = "p" + boundary.format(PART_SUFFIX);
            LocalDate upper = boundary.plusMonths(1);
            jdbcTemplate.execute("ALTER TABLE `" + table + "` REORGANIZE PARTITION " + PMAX
                    + " INTO (PARTITION `" + partition + "` VALUES LESS THAN ('" + upper + "'), "
                    + "PARTITION " + PMAX + " VALUES LESS THAN (MAXVALUE))");
            log.info("审计表补分区 table={} partition={} upper={}", table, partition, upper);
            boundary = upper;
        }
    }

    /** 已定义分区的最大上界（不含 pmax 的 MAXVALUE） */
    private LocalDate lastBoundary(String table) {
        List<Part> parts = listPartitions(table);
        if (parts.isEmpty()) {
            throw new IllegalStateException("表 " + table + " 未分区，先执行 sql 增量 438");
        }
        LocalDate last = null;
        for (Part p : parts) {
            if (p.upper() != null) {
                last = p.upper();
            }
        }
        if (last == null) {
            throw new IllegalStateException("表 " + table + " 只有 pmax 分区，无法推算上界");
        }
        return last;
    }

    /**
     * 读分区清单。上界从 PARTITION_DESCRIPTION 里抠日期；pmax 是 MAXVALUE，upper 记 null。
     * start 由「上一个分区的上界」推出，p_hist 记 LocalDate.MIN 的哨兵（导出时落成 1970-01-01）。
     */
    private List<Part> listPartitions(String table) {
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(
                "SELECT PARTITION_NAME, PARTITION_DESCRIPTION FROM information_schema.PARTITIONS "
                        + "WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = ? AND PARTITION_NAME IS NOT NULL "
                        + "ORDER BY PARTITION_ORDINAL_POSITION", table);
        List<Part> parts = new ArrayList<>();
        LocalDate prevUpper = LocalDate.of(1970, 1, 1);
        for (Map<String, Object> row : rows) {
            String name = String.valueOf(row.get("PARTITION_NAME"));
            String desc = row.get("PARTITION_DESCRIPTION") == null ? "" : String.valueOf(row.get("PARTITION_DESCRIPTION"));
            Matcher m = DATE_IN_DESC.matcher(desc);
            LocalDate upper = m.find() ? LocalDate.parse(m.group(1)) : null;
            parts.add(new Part(name, prevUpper, upper));
            if (upper != null) {
                prevUpper = upper;
            }
        }
        return parts;
    }

    // ---------------------------------------------------------------- 归档

    private void archiveTable(String table, Path dir) throws Exception {
        int keepMonths = KEEP_MONTHS.get(table);
        LocalDate cutoff = LocalDate.now().withDayOfMonth(1).minusMonths(keepMonths);
        for (Part p : listPartitions(table)) {
            // pmax 永不归档；上界还没走到保留线以内的（含当前月）也不动
            if (p.upper() == null || p.upper().isAfter(cutoff)) {
                continue;
            }
            archivePartition(table, p, keepMonths, dir);
        }
    }

    private void archivePartition(String table, Part part, int keepMonths, Path dir) throws Exception {
        List<String> indexed = jdbcTemplate.queryForList(
                "SELECT file_path FROM sys_audit_archive_index WHERE table_name = ? AND partition_name = ?",
                String.class, table, part.name());

        if (!indexed.isEmpty()) {
            // 上次归档到"写了台账但删分区失败"：文件在就直接补删，文件丢了宁可不删
            Path file = Path.of(indexed.get(0));
            if (!Files.exists(file) || Files.size(file) == 0) {
                log.error("归档台账存在但文件缺失，拒绝删分区 table={} partition={} file={}", table, part.name(), file);
                return;
            }
            dropPartition(table, part.name());
            return;
        }

        String fileName = table + "-" + part.name() + ".csv.gz";
        Path file = dir.resolve(fileName);
        long rows = exportPartition(table, part.name(), file);
        long size = Files.size(file);
        if (size <= 0) {
            Files.deleteIfExists(file);
            throw new IllegalStateException("导出文件为空，放弃归档 table=" + table + " partition=" + part.name());
        }
        String sha256 = sha256(file);

        jdbcTemplate.update("INSERT INTO sys_audit_archive_index "
                        + "(id, table_name, partition_name, period_start, period_end, row_count, file_path, file_size, "
                        + "sha256, archived_by, archived_time, retention_months) "
                        + "VALUES (?,?,?,?,?,?,?,?,?,?,now(),?)",
                IdWorker.getId(), table, part.name(), part.start().atStartOfDay(), part.upper().atStartOfDay(),
                rows, file.toString(), size, sha256, ARCHIVED_BY, keepMonths);

        log.info("审计分区已归档 table={} partition={} rows={} size={} sha256={}",
                table, part.name(), rows, size, sha256);
        dropPartition(table, part.name());
    }

    private void dropPartition(String table, String partition) {
        jdbcTemplate.execute("ALTER TABLE `" + table + "` DROP PARTITION `" + partition + "`");
        log.info("审计分区已下线 table={} partition={}", table, partition);
    }

    /**
     * 整分区导出为 CSV.gz。<b>必须走 setFetchSize(Integer.MIN_VALUE)</b>：
     * MySQL 驱动默认把整个结果集拉进堆，一个月几百万行会直接把 JVM 撑爆。
     *
     * @return 实际写出的数据行数（不含表头）
     */
    private long exportPartition(String table, String partition, Path file) throws Exception {
        String sql = "SELECT * FROM `" + table + "` PARTITION (`" + partition + "`)";
        long rows = 0;
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setFetchSize(Integer.MIN_VALUE);
            try (ResultSet rs = ps.executeQuery();
                 OutputStream out = new GZIPOutputStream(Files.newOutputStream(file));
                 BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(out, StandardCharsets.UTF_8))) {
                ResultSetMetaData meta = rs.getMetaData();
                int cols = meta.getColumnCount();
                StringBuilder line = new StringBuilder();
                for (int i = 1; i <= cols; i++) {
                    if (i > 1) {
                        line.append(',');
                    }
                    line.append(csv(meta.getColumnLabel(i)));
                }
                writer.write(line.append('\n').toString());
                while (rs.next()) {
                    line.setLength(0);
                    for (int i = 1; i <= cols; i++) {
                        if (i > 1) {
                            line.append(',');
                        }
                        line.append(csv(rs.getString(i)));
                    }
                    writer.write(line.append('\n').toString());
                    rows++;
                }
            }
        }
        return rows;
    }

    private static String csv(String value) {
        if (value == null) {
            return "";
        }
        if (value.indexOf(',') < 0 && value.indexOf('"') < 0
                && value.indexOf('\n') < 0 && value.indexOf('\r') < 0) {
            return value;
        }
        return '"' + value.replace("\"", "\"\"") + '"';
    }

    private static String sha256(Path file) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        try (InputStream in = new DigestInputStream(Files.newInputStream(file), digest)) {
            byte[] buf = new byte[8192];
            while (in.read(buf) > -1) {
                // 读完即得摘要
            }
        }
        byte[] hash = digest.digest();
        StringBuilder sb = new StringBuilder(hash.length * 2);
        for (byte b : hash) {
            sb.append(Character.forDigit((b >> 4) & 0xF, 16)).append(Character.forDigit(b & 0xF, 16));
        }
        return sb.toString();
    }

    /** 分区元数据：name + [start, upper)。pmax 的 upper 为 null */
    private record Part(String name, LocalDate start, LocalDate upper) {
    }
}

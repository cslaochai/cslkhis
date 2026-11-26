package com.his.iot.support;

import com.his.common.exception.BusinessException;
import com.his.iot.config.IotProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * 时序库（TDengine REST）查询小工具。
 * <p>
 * REST 连接是无状态的 HTTP：不共享 Connection，也不依赖 {@code USE}（库名必须在 SQL 里带前缀，
 * 由 {@link #database()} 统一提供）。
 * <p>
 * 返回 Map 只在 support 内部使用，出参由 service 转成 VO，不给 Controller 透 Map。
 */
@Component
@RequiredArgsConstructor
public class TsdbQuerySupport {

    private static final String DRIVER = "com.taosdata.jdbc.rs.RestfulDriver";

    private final IotProperties properties;

    /**
     * 库名前缀，例如 {@code iot_raw}；拼 SQL 时写 {@code iot_raw.st_xxx}
     */
    public String database() {
        return properties.getTsdb().getDatabase();
    }

    /**
     * 执行一条查询，把结果按「列名小写 → 值」摊平。TDengine 会把列别名转小写，这里保持同一口径。
     */
    public List<Map<String, Object>> query(String sql) {
        return queryBatch(List.of(sql)).getFirst();
    }

    /**
     * 同一连接里顺序跑多条 SQL。
     * <p>
     * 设备总览按超级表逐类查（11 条），每条都新建 Connection 就是 11 次 TCP + HTTP 握手；
     * 这里的收益全在少握手，不在少解析。
     */
    public List<List<Map<String, Object>>> queryBatch(List<String> sqls) {
        IotProperties.Tsdb tsdb = properties.getTsdb();
        try {
            Class.forName(DRIVER);
            try (Connection conn = DriverManager.getConnection(tsdb.getUrl(), tsdb.getUser(), tsdb.getPassword())) {
                List<List<Map<String, Object>>> batches = new ArrayList<>(sqls.size());
                for (String sql : sqls) {
                    batches.add(read(conn, sql));
                }
                return batches;
            }
        } catch (SQLException e) {
            throw new BusinessException(500, "物联网时序库查询失败：" + e.getMessage());
        } catch (ClassNotFoundException e) {
            throw new BusinessException(500, "缺少 taos-jdbcdriver，检查 his-ms-iot 依赖");
        }
    }

    private static List<Map<String, Object>> read(Connection conn, String sql) throws SQLException {
        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            ResultSetMetaData md = rs.getMetaData();
            int columns = md.getColumnCount();
            List<Map<String, Object>> rows = new ArrayList<>();
            while (rs.next()) {
                Map<String, Object> row = new LinkedHashMap<>(columns);
                for (int c = 1; c <= columns; c++) {
                    row.put(md.getColumnLabel(c).toLowerCase(Locale.ROOT), rs.getObject(c));
                }
                rows.add(row);
            }
            return rows;
        }
    }
}

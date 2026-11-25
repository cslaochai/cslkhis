package com.his.iot.service.impl;

import com.his.common.exception.BusinessException;
import com.his.iot.config.IotProperties;
import com.his.iot.dto.IotHistoryQueryDTO;
import com.his.iot.dto.IotLatestQueryDTO;
import com.his.iot.service.IotTelemetryService;
import com.his.iot.support.DeviceTypeSchema;
import com.his.iot.support.TsdbQuerySupport;
import com.his.iot.vo.IotHistoryPointVO;
import com.his.iot.vo.IotLatestVO;
import com.his.iot.vo.IotMetricVO;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 遥测查询实现。
 * <p>
 * 全部走 TDengine REST：
 * <ul>
 *   <li>最新值 = 超级表 {@code LAST_ROW(列)} + {@code PARTITION BY device_no}，一类设备一条 SQL 拿回全部在线设备</li>
 *   <li>历史值 = 超级表带 {@code device_no} 过滤 + 时间范围 + {@code ORDER BY ts}，行数由 limit 收口</li>
 * </ul>
 * 子表是首次写入时自动建的，新设备上线不需要改代码。
 */
@Service
@RequiredArgsConstructor
public class IotTelemetryServiceImpl implements IotTelemetryService {

    private static final Logger log = LoggerFactory.getLogger(IotTelemetryServiceImpl.class);
    private static final DateTimeFormatter SQL_TS = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");
    private static final ZoneId ZONE = ZoneId.of("Asia/Shanghai");

    private final TsdbQuerySupport tsdb;
    private final IotProperties properties;

    @Override
    public List<IotLatestVO> latestList(IotLatestQueryDTO query) {
        List<DeviceTypeSchema.Schema> schemas = resolveSchemas(query.getDeviceType());
        List<IotLatestVO> result = new ArrayList<>();
        for (DeviceTypeSchema.Schema schema : schemas) {
            // 一张超级表一条 SQL 取全部设备最新值：PARTITION BY 标签 device_no，一台设备一行。
            // 不要退化成「先查设备清单再逐台 LAST_ROW」——63 台设备就是 63 次往返。
            List<Map<String, Object>> rows = tsdb.query(latestSql(schema));
            for (Map<String, Object> row : rows) {
                Object deviceNo = row.get("device_no");
                if (deviceNo == null) {
                    continue;
                }
                IotLatestVO vo = new IotLatestVO();
                vo.setDeviceType(schema.deviceType());
                vo.setDeviceTypeLabel(schema.label());
                vo.setDeviceNo(String.valueOf(deviceNo));
                vo.setTs(parseTs(row.get("ts")));
                vo.setMetrics(metrics(schema, row));
                result.add(vo);
            }
        }
        log.debug("物联网最新值查询完成：类型={}，返回 {} 台", query.getDeviceType(), result.size());
        return result;
    }

    /**
     * 最新值 SQL。TDengine 的 {@code LAST_ROW(列)} 出参列名是 {@code last_row(列)}，
     * 必须显式 {@code AS} 起别名，否则按列名取值的逻辑全部取空。
     */
    private String latestSql(DeviceTypeSchema.Schema schema) {
        StringBuilder sql = new StringBuilder(200);
        sql.append("SELECT device_no, LAST_ROW(ts) AS ts");
        for (DeviceTypeSchema.Field f : schema.fields()) {
            sql.append(", LAST_ROW(").append(f.column()).append(") AS ").append(f.column());
        }
        sql.append(" FROM ").append(tsdb.database()).append('.').append(schema.stable())
                .append(" PARTITION BY device_no");
        return sql.toString();
    }

    @Override
    public List<IotHistoryPointVO> historyList(IotHistoryQueryDTO query) {
        DeviceTypeSchema.Schema schema = DeviceTypeSchema.of(query.getDeviceType());
        if (schema == null) {
            throw new BusinessException(400, "设备类型取值不合法：" + query.getDeviceType());
        }
        if (query.getStartTime().isAfter(query.getEndTime())) {
            throw new BusinessException(400, "开始时间不能晚于结束时间");
        }

        int limit = query.getLimit() == null ? 2000 : query.getLimit();
        limit = Math.min(limit, properties.getMaxHistoryRows());

        StringBuilder sql = new StringBuilder(160);
        sql.append("SELECT ts");
        for (DeviceTypeSchema.Field f : schema.fields()) {
            sql.append(',').append(f.column());
        }
        sql.append(" FROM ").append(tsdb.database()).append('.').append(schema.stable())
                .append(" WHERE device_no = '").append(escape(query.getDeviceNo()))
                .append("' AND ts >= '").append(query.getStartTime().format(SQL_TS))
                .append("' AND ts <= '").append(query.getEndTime().format(SQL_TS))
                .append("' ORDER BY ts ASC LIMIT ").append(limit);

        List<Map<String, Object>> rows = tsdb.query(sql.toString());
        List<IotHistoryPointVO> points = new ArrayList<>(rows.size());
        for (Map<String, Object> row : rows) {
            IotHistoryPointVO p = new IotHistoryPointVO();
            p.setTs(parseTs(row.get("ts")));
            p.setMetrics(metrics(schema, row));
            points.add(p);
        }
        return points;
    }

    private List<DeviceTypeSchema.Schema> resolveSchemas(String deviceType) {
        if (deviceType == null || deviceType.isBlank()) {
            return List.copyOf(DeviceTypeSchema.all());
        }
        DeviceTypeSchema.Schema schema = DeviceTypeSchema.of(deviceType);
        if (schema == null) {
            throw new BusinessException(400, "设备类型取值不合法：" + deviceType);
        }
        return List.of(schema);
    }

    private List<IotMetricVO> metrics(DeviceTypeSchema.Schema schema, Map<String, Object> row) {
        List<IotMetricVO> metrics = new ArrayList<>(schema.fields().size());
        for (DeviceTypeSchema.Field f : schema.fields()) {
            Object v = row.get(f.column());
            if (v == null) {
                continue;
            }
            switch (f.kind()) {
                case DOUBLE, INT -> {
                    Double numeric = v instanceof Number n ? n.doubleValue() : parseNumber(v);
                    metrics.add(IotMetricVO.number(f.key(), f.label(), f.unit(), numeric));
                }
                case BOOL -> {
                    boolean on = v instanceof Boolean b ? b : Boolean.parseBoolean(String.valueOf(v));
                    metrics.add(IotMetricVO.text(f.key(), f.label(), f.unit(), on ? "是" : "否"));
                }
                case STRING -> metrics.add(IotMetricVO.text(f.key(), f.label(), f.unit(),
                        String.valueOf(v)));
            }
        }
        return metrics;
    }

    private static Double parseNumber(Object v) {
        try {
            return Double.valueOf(String.valueOf(v));
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * REST 返回的时间是 UTC ISO（如 {@code 2026-11-25T02:06:29.947Z}），本地看板要的是东八区。
     * 也兼容驱动直接给 {@code yyyy-MM-dd HH:mm:ss} 文本的情况。
     */
    private static LocalDateTime parseTs(Object raw) {
        if (raw == null) {
            return null;
        }
        String text = String.valueOf(raw);
        try {
            if (text.endsWith("Z")) {
                return LocalDateTime.ofInstant(Instant.parse(text), ZONE);
            }
            return LocalDateTime.parse(text.replace(' ', 'T'));
        } catch (Exception e) {
            log.debug("遥测时间解析失败，原值：{}", text);
            return null;
        }
    }

    private static String escape(String s) {
        return s == null ? "" : s.replace("'", "''");
    }
}

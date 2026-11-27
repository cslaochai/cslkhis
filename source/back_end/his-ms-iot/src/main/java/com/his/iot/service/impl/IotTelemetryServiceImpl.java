package com.his.iot.service.impl;

import com.his.common.enums.IotEventLevelEnum;
import com.his.common.exception.BusinessException;
import com.his.iot.config.IotProperties;
import com.his.iot.dto.IotColdChainRecordQueryDTO;
import com.his.iot.dto.IotHistoryQueryDTO;
import com.his.iot.dto.IotLatestQueryDTO;
import com.his.iot.mapper.IotColdChainTempLogMapper;
import com.his.iot.service.IotEventService;
import com.his.iot.service.IotTelemetryService;
import com.his.iot.support.DeviceTypeSchema;
import com.his.iot.support.TsdbQuerySupport;
import com.his.iot.support.TelemetrySqlBuilder;
import com.his.iot.vo.IotColdChainRecordVO;
import com.his.iot.vo.IotColdChainSummaryVO;
import com.his.iot.vo.IotDeviceTypeVO;
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
import java.util.TreeMap;

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
    /** GSP 温湿度记录粒度：5 分钟一轮，一天 288 条，满足「自动记录、可回溯」的检查口径 */
    private static final String COLD_CHAIN_RECORD_INTERVAL = "5m";
    /**
     * 冷链物理量量程。冷藏柜不可能出现 60℃ 或 110% 湿度：这类读数来自探头故障/脏报文，
     * 记进 GSP 记录会把「药品是否脱冷」的判断带偏，查询侧直接剔除（接入侧同时做丢弃）。
     */
    private static final String COLD_CHAIN_SANITY =
            " AND temp >= -30 AND temp <= 60 AND humidity >= 0 AND humidity <= 100";

    private final TsdbQuerySupport tsdb;
    private final IotProperties properties;
    private final IotColdChainTempLogMapper coldChainArchiveMapper;
    private final IotEventService eventService;

    @Override
    public List<IotDeviceTypeVO> deviceTypes() {
        return DeviceTypeSchema.all().stream()
                .map(schema -> {
                    IotDeviceTypeVO vo = new IotDeviceTypeVO();
                    vo.setType(schema.deviceType());
                    vo.setLabel(schema.label());
                    return vo;
                })
                .toList();
    }

    @Override
    public List<IotLatestVO> latestList(IotLatestQueryDTO query) {
        List<DeviceTypeSchema.Schema> schemas = resolveSchemas(query.getDeviceType());
        // 一张超级表一条 SQL 取全部设备最新值（PARTITION BY 标签 device_no，一台设备一行）；
        // 多条 SQL 共用一次连接，避免 11 类设备 11 次 TCP 握手
        List<List<Map<String, Object>>> batches =
                tsdb.queryBatch(schemas.stream().map(this::latestSql).toList());
        // 告警状态以事件中心为准：存在未处理且级别≥2 的事件才算告警，前端不拿遥测快照二次推断
        Map<String, Integer> alarmLevels = eventService.activeAlarmDeviceLevels();
        List<IotLatestVO> result = new ArrayList<>();
        for (int i = 0; i < schemas.size(); i++) {
            DeviceTypeSchema.Schema schema = schemas.get(i);
            for (Map<String, Object> row : batches.get(i)) {
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
                Integer alarmLevel = alarmLevels.get(vo.getDeviceNo());
                vo.setAlarmLevel(alarmLevel);
                vo.setAlarmLevelText(alarmLevel == null ? null : IotEventLevelEnum.getLabel(alarmLevel));
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
        Integer intervalMinutes = query.getIntervalMinutes();
        if (intervalMinutes != null) {
            // 长区间看板/回溯用聚合点：AVG 保留数值指标语义，列名与原始查询一致，前端不用分两套解析。
            // 只对数值列做 AVG——TDengine 不支持对 BOOL/字符串列求平均，混进去整条 SQL 失败且零业务线索。
            sql.append("SELECT _wstart AS ts");
            for (DeviceTypeSchema.Field f : schema.fields()) {
                if (f.kind() == DeviceTypeSchema.Kind.DOUBLE || f.kind() == DeviceTypeSchema.Kind.INT) {
                    sql.append(", AVG(").append(f.column()).append(") AS ").append(f.column());
                }
            }
        } else {
            sql.append("SELECT ts");
            for (DeviceTypeSchema.Field f : schema.fields()) {
                sql.append(',').append(f.column());
            }
        }
        sql.append(" FROM ").append(tsdb.database()).append('.').append(schema.stable())
                .append(" WHERE device_no = '").append(TelemetrySqlBuilder.escape(query.getDeviceNo()))
                .append("' AND ts >= '").append(query.getStartTime().format(SQL_TS))
                .append("' AND ts <= '").append(query.getEndTime().format(SQL_TS)).append('\'')
                .append(sanityFilter(schema))
                .append(intervalMinutes == null ? " ORDER BY ts ASC" : " INTERVAL(" + intervalMinutes + "m)")
                .append(" LIMIT ").append(limit);

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

    @Override
    public List<IotColdChainRecordVO> coldChainRecords(IotColdChainRecordQueryDTO query) {
        List<IotColdChainRecordVO> realtime = coldChainRealtimeRecords(query);
        // 归档表覆盖 TDengine 90 天保留期之外的合规记录；两者按 ts 合并，实时值优先
        List<IotColdChainRecordVO> archived = coldChainArchiveMapper.selectRange(
                query.getDeviceNo(), query.getStartTime(), query.getEndTime());
        return mergeRecords(realtime, archived);
    }

    @Override
    public List<IotColdChainRecordVO> coldChainRealtimeRecords(IotColdChainRecordQueryDTO query) {
        if (query.getStartTime().isAfter(query.getEndTime())) {
            throw new BusinessException(400, "开始时间不能晚于结束时间");
        }
        if (query.getEndTime().isAfter(query.getStartTime().plusDays(31))) {
            throw new BusinessException(400, "单次导出不超过 31 天，请分段导出");
        }
        String sql = "SELECT _wstart AS ts, AVG(temp) AS temp, MIN(temp) AS temp_min, MAX(temp) AS temp_max"
                + ", AVG(humidity) AS humidity, COUNT(*) AS point_count"
                // door/power 是 BOOL，TDengine 聚合前先 CAST：直接 MAX(BOOL) 拿不到值（返回 null 且不报错）
                + ", MAX(CAST(door AS TINYINT)) AS door_ever, MIN(CAST(power AS TINYINT)) AS power_ever"
                + " FROM " + tsdb.database() + ".st_coldchain"
                + " WHERE device_no = '" + TelemetrySqlBuilder.escape(query.getDeviceNo()) + "'"
                + " AND ts >= '" + query.getStartTime().format(SQL_TS) + "'"
                + " AND ts <= '" + query.getEndTime().format(SQL_TS) + "'"
                + COLD_CHAIN_SANITY
                + " INTERVAL(" + COLD_CHAIN_RECORD_INTERVAL + ")";
        List<Map<String, Object>> rows = tsdb.query(sql);
        List<IotColdChainRecordVO> realtime = new ArrayList<>(rows.size());
        for (Map<String, Object> row : rows) {
            IotColdChainRecordVO vo = new IotColdChainRecordVO();
            vo.setTs(parseTs(row.get("ts")));
            vo.setTemp(parseNumber(row.get("temp")));
            vo.setTempMin(parseNumber(row.get("temp_min")));
            vo.setTempMax(parseNumber(row.get("temp_max")));
            vo.setHumidity(parseNumber(row.get("humidity")));
            Object count = row.get("point_count");
            vo.setPointCount(count == null ? 0 : (int) Double.parseDouble(String.valueOf(count)));
            // 窗口内的瞬时状态用聚合还原：开过门 = MAX(door)，断过电 = MIN(power)
            vo.setDoor(toInt(row.get("door_ever")));
            vo.setPower(toInt(row.get("power_ever")));
            realtime.add(vo);
        }
        return realtime;
    }

    @Override
    public List<IotColdChainSummaryVO> coldChainSummary(Integer hours) {
        int windowHours = hours == null ? 24 : Math.min(Math.max(hours, 1), 168);
        return coldChainArchiveMapper.selectSummary(LocalDateTime.now().minusHours(windowHours));
    }

    private static List<IotColdChainRecordVO> mergeRecords(List<IotColdChainRecordVO> realtime,
                                                           List<IotColdChainRecordVO> archived) {
        if (archived.isEmpty()) {
            return realtime;
        }
        Map<LocalDateTime, IotColdChainRecordVO> byTs = new TreeMap<>();
        for (IotColdChainRecordVO record : realtime) {
            if (record.getTs() != null) {
                byTs.put(record.getTs(), record);
            }
        }
        for (IotColdChainRecordVO record : archived) {
            if (record.getTs() != null) {
                byTs.putIfAbsent(record.getTs(), record);
            }
        }
        return new ArrayList<>(byTs.values());
    }

    private static String sanityFilter(DeviceTypeSchema.Schema schema) {
        return "coldchain".equals(schema.deviceType()) ? COLD_CHAIN_SANITY : "";
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

    /**
     * 聚合出来的整型值：驱动可能给 Number，也可能给文本
     */
    private static Integer toInt(Object v) {
        if (v instanceof Number n) {
            return n.intValue();
        }
        if (v == null) {
            return null;
        }
        try {
            return Integer.valueOf(String.valueOf(v));
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private static Double parseNumber(Object v) {        try {
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

}

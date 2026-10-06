package com.his.miniapp.support;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.Map;

/**
 * 跨模块只读取数行的取值收口。
 *
 * <p>裸 SQL 结果行的值类型由驱动决定：同一列可能是 LocalDateTime、Timestamp 或 java.util.Date，
 * 整型列可能是 Byte/Short/Integer。直接强转会在运行期抛 ClassCastException 被兜成 500，
 * 这里统一按 Number/Temporal 分支收敛成出参字段类型。
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class RawRowValues {

    public static Object value(Map<String, Object> row, String key) {
        return row == null ? null : row.get(key);
    }

    /** 嵌套结果行（明细集合的元素）收敛成可取值形状，非 Map 一律当空行处理 */
    @SuppressWarnings("unchecked")
    public static Map<String, Object> asRow(Object value) {
        return value instanceof Map<?, ?> map ? (Map<String, Object>) map : Map.of();
    }

    public static String text(Map<String, Object> row, String key) {
        Object value = value(row, key);
        return value == null ? null : String.valueOf(value);
    }

    public static Integer integer(Map<String, Object> row, String key) {
        Object value = value(row, key);
        return value instanceof Number number ? number.intValue() : null;
    }

    public static Long longValue(Map<String, Object> row, String key) {
        Object value = value(row, key);
        return value instanceof Number number ? number.longValue() : null;
    }

    public static BigDecimal decimal(Map<String, Object> row, String key) {
        Object value = value(row, key);
        if (value == null) {
            return null;
        }
        if (value instanceof BigDecimal decimal) {
            return decimal;
        }
        return new BigDecimal(String.valueOf(value).trim());
    }

    public static LocalDateTime dateTime(Map<String, Object> row, String key) {
        Object value = value(row, key);
        if (value instanceof LocalDateTime dateTime) {
            return dateTime;
        }
        if (value instanceof Timestamp timestamp) {
            return timestamp.toLocalDateTime();
        }
        if (value instanceof LocalDate localDate) {
            return localDate.atStartOfDay();
        }
        if (value instanceof Date date) {
            return LocalDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault());
        }
        return null;
    }

    public static LocalDate date(Map<String, Object> row, String key) {
        Object value = value(row, key);
        if (value instanceof LocalDate localDate) {
            return localDate;
        }
        if (value instanceof java.sql.Date date) {
            return date.toLocalDate();
        }
        LocalDateTime dateTime = dateTime(row, key);
        return dateTime == null ? null : dateTime.toLocalDate();
    }
}

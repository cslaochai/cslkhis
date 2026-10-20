package com.his.common.support;

import com.his.common.util.DateFormats;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 被签内容的**规范化文本**组装器。电子签名能不能验得出来，全看这个类。
 */
public final class CanonicalText {

    /**
     * 规范化格式版本。改动 put 规则/转义规则时必须 +1
     */
    public static final String FORMAT_VERSION = "WN-HIS-SIGN-V1";

    /**
     * 时间统一渲染格式（见 {@link #normalize} 说明）
     */

    private final String bizTag;
    private final Map<String, String> fields = new LinkedHashMap<>();

    private CanonicalText(String bizTag) {
        this.bizTag = bizTag;
    }

    public static CanonicalText create(String bizTag) {
        return new CanonicalText(bizTag == null ? "UNKNOWN" : bizTag);
    }

    /**
     * 单个值的规范化。public 是为了让"签名链"把上一环摘要拼进来时用同一套规则。
     */
    public static String normalize(Object value) {
        if (value == null) {
            return "";
        }
        if (value instanceof LocalDateTime t) {
            return t.format(DateFormats.DATETIME);
        }
        if (value instanceof LocalDate d) {
            return d.format(DateFormats.DATE);
        }
        if (value instanceof BigDecimal b) {
            return b.stripTrailingZeros().toPlainString();
        }
        String s = String.valueOf(value);
        s = s.replace("\r\n", "\n").replace('\r', '\n');
        s = s.replace("\n", "\\n");
        return s.trim();
    }

    public CanonicalText put(String key, Object value) {
        fields.put(key, normalize(value));
        return this;
    }

    public String build() {
        StringBuilder sb = new StringBuilder(256);
        sb.append(FORMAT_VERSION).append('\n');
        sb.append("bizTag=").append(bizTag).append('\n');
        for (Map.Entry<String, String> e : fields.entrySet()) {
            sb.append(e.getKey()).append('=').append(e.getValue()).append('\n');
        }
        return sb.toString();
    }
}

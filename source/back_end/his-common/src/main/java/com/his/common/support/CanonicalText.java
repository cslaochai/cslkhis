package com.his.common.support;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 被签内容的**规范化文本**组装器。电子签名能不能验得出来，全看这个类。
 *
 * <p>规范化必须满足"同一份业务内容永远得到同一个字符串"，否则会出现
 * 「医生什么都没改，验签却失败」这种把整套签名废掉的假警报。为此定死四条：
 *
 * <ol>
 *   <li><b>字段顺序固定</b>（按调用方 put 的顺序，用 LinkedHashMap），不依赖反射/数据库列序。
 *       反射取字段顺序在 JDK 之间都不保证稳定，用它做摘要等于埋雷。</li>
 *   <li><b>null 与空串等价</b>：都序列化成 {@code key=}。否则"没填"和"填了空白"
 *       会算出两个摘要，而这两种在业务上是同一个事实。</li>
 *   <li><b>值首尾去空白，值内换行转义成字面 {@code \n}</b>。
 *       去首尾空白是为了让前端多带一个空格不触发"内容已变更"；
 *       换行转义是为了保证"每行一个字段"这个结构不被病历正文里的换行破坏
 *       （否则两个不同字段可能拼出同一段文本，摘要就失去分辨力）。</li>
 *   <li><b>带格式版本号</b>（{@link #FORMAT_VERSION}）。将来规范化规则变了，
 *       旧签名的摘要必然对不上；有版本号才能一眼看出"是格式升级，不是内容被改"。</li>
 * </ol>
 *
 * <p>用法：
 * <pre>
 * String text = CanonicalText.create("INPATIENT_RECORD")
 *         .put("recordNo", r.getRecordNo())
 *         .put("chiefComplaint", r.getChiefComplaint())
 *         .build();
 * </pre>
 */
public final class CanonicalText {

    /**
     * 规范化格式版本。改动 put 规则/转义规则时必须 +1
     */
    public static final String FORMAT_VERSION = "WN-HIS-SIGN-V1";

    /**
     * 时间统一渲染格式（见 {@link #normalize} 说明）
     */
    public static final DateTimeFormatter TS_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    public static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

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
     *
     * <p><b>时间与数值必须显式格式化，不能靠 {@code toString()}</b>：
     * {@code LocalDateTime.of(2026,9,19,10,30,0).toString()} 得到的是
     * {@code 2026-09-19T10:30}（**秒为 0 时会省略秒**），而
     * {@code ...10,30,15} 得到 {@code 2026-09-19T10:30:15} —— 同一份内容可能算出两种摘要，
     * 而且只在"整分钟"这种最常见的输入上暴露。{@code BigDecimal("10.00").toString()} 是
     * {@code 10.00} 而 {@code stripTrailingZeros()} 后是 {@code 10}，同理。
     * 这里统一按固定格式渲染，是"只有内容真变、摘要才变"的前提。
     */
    public static String normalize(Object value) {
        if (value == null) {
            return "";
        }
        if (value instanceof LocalDateTime t) {
            return t.format(TS_FMT);
        }
        if (value instanceof LocalDate d) {
            return d.format(DATE_FMT);
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

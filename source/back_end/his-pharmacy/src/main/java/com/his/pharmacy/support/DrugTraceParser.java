package com.his.pharmacy.support;

import lombok.AccessLevel;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 药品追溯码解析器（扫码串 → 码制 / 产品标识 / 序列号 / 批号 / 有效期）。
 *
 * <p>支持两类合规码制：
 * <ol>
 *   <li><b>GS1 码</b>（药盒上的 GS1-128 / 二维码，扫码枪输出 HRI 括号串）：
 *       {@code (01)06901234560001(17)290301(10)LOT20260301(21)SN000000000001}；
 *       (01)=GTIN-14 产品标识、(10)=批号、(17)=有效期、(21)=序列号。</li>
 *   <li><b>中国药品追溯码（20 位数字）</b>：{@code 81000001000000000001}，
 *       前 8 位 = 药品本体码（国家药监分配）、后 12 位 = 生产序列号。</li>
 * </ol>
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class DrugTraceParser {

    /**
     * GS1：AI 括号串（只取药品追溯必需的 4 个 AI）
     */
    private static final Pattern BRACKETED = Pattern.compile("\\((01|10|17|21)\\)([^(]*)");
    /**
     * 中国药品追溯码：8 位本体码 + 12 位序列号
     */
    private static final Pattern CN20 = Pattern.compile("^(\\d{8})(\\d{12})$");

    /**
     * 码制：1-GS1 2-中国药品追溯码20位 3-其他/未识别
     */
    public static final int CODE_TYPE_GS1 = 1;
    public static final int CODE_TYPE_CN20 = 2;
    public static final int CODE_TYPE_OTHER = 3;

    @Data
    public static class TraceParts {
        private Integer codeType = CODE_TYPE_OTHER;
        /**
         * 产品标识（GS1 GTIN-14 / 20 位码前 8 位本体码）
         */
        private String drugDi;
        /**
         * 生产序列号
         */
        private String serialNo;
        /**
         * 码内批号（仅 GS1 (10) 有）
         */
        private String batchNo;
        /**
         * 码内有效期（仅 GS1 (17) 有）
         */
        private LocalDate expiryDate;
        /**
         * 是否解析出产品标识（false → 必须人工指定药品）
         */
        private boolean parsed;
    }

    /**
     * 宽松解析：解析不出产品标识返回 parsed=false，绝不猜测。
     */
    public static TraceParts parse(String traceCode) {
        TraceParts parts = new TraceParts();
        if (traceCode == null || traceCode.isBlank()) {
            return parts;
        }
        String raw = traceCode.trim();
        Matcher gs1 = BRACKETED.matcher(raw);
        boolean hasBrackets = false;
        while (gs1.find()) {
            hasBrackets = true;
            String ai = gs1.group(1);
            String value = gs1.group(2) == null ? "" : gs1.group(2).trim();
            switch (ai) {
                case "01" -> parts.drugDi = value;
                case "21" -> parts.serialNo = value;
                case "10" -> parts.batchNo = value;
                case "17" -> parts.expiryDate = parseYymmdd(value);
                default -> {
                }
            }
        }
        if (hasBrackets && parts.drugDi != null && !parts.drugDi.isBlank()) {
            parts.codeType = CODE_TYPE_GS1;
            parts.parsed = true;
            return parts;
        }
        // 不带 AI 括号：只接受"整串 20 位纯数字"这一种确定形态
        Matcher cn = CN20.matcher(raw);
        if (cn.matches()) {
            parts.codeType = CODE_TYPE_CN20;
            parts.drugDi = cn.group(1);
            parts.serialNo = cn.group(2);
            parts.parsed = true;
            return parts;
        }
        parts.codeType = CODE_TYPE_OTHER;
        return parts;
    }

    /**
     * GS1 (17)：YYMMDD（日给 00 表示当月最后一天）；部分企业只给 YYMM
     */
    private static LocalDate parseYymmdd(String value) {
        if (value == null || value.length() < 4 || !value.chars().allMatch(Character::isDigit)) {
            return null;
        }
        try {
            int year = 2000 + Integer.parseInt(value.substring(0, 2));
            int month = Integer.parseInt(value.substring(2, 4));
            if (month < 1 || month > 12) {
                return null;
            }
            int day = value.length() >= 6 ? Integer.parseInt(value.substring(4, 6)) : 1;
            LocalDate first = LocalDate.of(year, month, 1);
            if (day <= 0) {
                return first.withDayOfMonth(first.lengthOfMonth());
            }
            return first.withDayOfMonth(Math.min(day, first.lengthOfMonth()));
        } catch (Exception e) {
            return null;
        }
    }
}

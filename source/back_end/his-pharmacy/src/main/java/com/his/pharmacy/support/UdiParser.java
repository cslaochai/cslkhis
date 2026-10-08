package com.his.pharmacy.support;

import com.his.common.util.TextUtil;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.Data;

import java.time.LocalDate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * GS1 UDI 解析器（高值耗材扫码串 → DI/序列号/批号/有效期）。
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class UdiParser {

    private static final Pattern BRACKETED = Pattern.compile("\\((0[17]|1[07]|21)\\)([^(]*)");
    private static final Pattern BARE_DI = Pattern.compile("^01(\\d{14})");

    /**
     * 宽松解析：解析不出 DI 返回 parsed=false（登记仍可继续，DI 列留空等人工补录），
     * 强校验由调用方按场景决定。
     */
    public static UdiParts parse(String udiCode) {
        UdiParts parts = new UdiParts();
        if (!TextUtil.hasText(udiCode)) {
            return parts;
        }
        String raw = udiCode.trim();
        Matcher m = BRACKETED.matcher(raw);
        boolean hasBrackets = false;
        while (m.find()) {
            hasBrackets = true;
            String ai = m.group(1);
            String value = m.group(2).trim();
            switch (ai) {
                case "01" -> parts.di = value;
                case "21" -> parts.serial = value;
                case "10" -> parts.batch = value;
                case "17" -> parts.expiryDate = parseYymmdd(value);
                default -> {
                }
            }
        }
        if (!hasBrackets) {
            Matcher bare = BARE_DI.matcher(raw);
            if (bare.find()) {
                parts.di = bare.group(1);
            }
        }
        parts.parsed = TextUtil.hasText(parts.di);
        return parts;
    }

    /**
     * GS1 (17)：YYMMDD；日给 00 表示当月最后一天
     */
    private static LocalDate parseYymmdd(String value) {
        if (value == null || value.length() < 4 || !value.chars().allMatch(Character::isDigit)) {
            return null;
        }
        try {
            int year = 2000 + Integer.parseInt(value.substring(0, 2));
            int month = Integer.parseInt(value.substring(2, 4));
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

    @Data
    public static class UdiParts {
        private String di;
        private String serial;
        private String batch;
        private LocalDate expiryDate;
        /**
         * 括号格式是否完整解析出 DI
         */
        private boolean parsed;
    }
}

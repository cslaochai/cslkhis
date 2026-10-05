package com.his.system.support;

import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 剂量文本解析（单次给药量 / 规格单件含量 / 频次每日次数），统一折算成 <b>mg</b>
 * <p>
 * <b>设计原则：解不出来就返回 null，绝不猜。</b>处方明细的 single_dosage 是医生手填的自由文本
 * （库里现存 {@code ''}、{@code '1'}、{@code '2'}、'1片'、'1支'、{@code '0.5g'}、{@code '11'} 等形态），
 * specification 更是五花八门（0.5g×16片、10ml:1g/支、80mg/5mg×7片、36片/盒、统货）。
 * 解析失败时若按「1 片 / 1mg」兜底，就会批量造出假超量；而审方提示被证实错一次，
 * 药师此后会无视所有提示，这个功能等于报废。所以口径是<b>宁可漏报不可误报</b>。
 * 完整论证见 {@code sql/130} 文件头第四条。
 */
public final class DosageTextParser {

    /**
     * 质量记法（拉丁单位按长度优先排列，否则 'mg' 会被 'g' 抢走前半段）
     */
    private static final Pattern MASS = Pattern.compile(
            "(\\d+(?:\\.\\d+)?)\\s*(mcg|μg|ug|mg|g|微克|毫克|克)(?![A-Za-z])", Pattern.CASE_INSENSITIVE);

    /**
     * 只出现拉丁单位以外还带体积/长度等干扰记法的规格视为不可比
     */
    private static final List<String> SKIP_SPEC_MARKS = List.of("复方", "复合");
    private static final Pattern SKIP_SPEC_UNITS = Pattern.compile("(?i)\\biu\\b|iu/|[0-9]iu|单位");

    /**
     * 规格里「按件计」的线索，没有它就无法断定那个质量值是单件含量
     */
    private static final Pattern PIECE_HINT = Pattern.compile("(片|粒|袋|支|瓶|板|枚|贴|帖|丸|胶囊)");

    /**
     * 单次剂量文本里可以当作「个数」的单位
     */
    private static final List<String> PIECE_UNITS = List.of(
            "片", "粒", "袋", "支", "瓶", "板", "枚", "贴", "帖", "丸", "胶囊", "包");

    private static final BigDecimal THOUSAND = new BigDecimal("1000");

    private DosageTextParser() {
    }

    /**
     * 数值 + 单位 → mg
     *
     * @return null 表示该单位不是质量单位（ml/IU/片/空）
     */
    public static BigDecimal toMg(BigDecimal value, String unit) {
        if (value == null || !StringUtils.hasText(unit)) {
            return null;
        }
        String u = unit.trim().toLowerCase();
        return switch (u) {
            case "mg", "毫克" -> value;
            case "g", "克" -> value.multiply(THOUSAND);
            case "ug", "μg", "mcg", "微克" -> value.divide(THOUSAND, 6, RoundingMode.HALF_UP);
            default -> null;
        };
    }

    /**
     * 规格的<b>单件含量</b>（mg/片、mg/支）
     * <p>
     * 认：0.5g×16片→500、2.5mg×60片→2.5、10ml:1g/支→1000、100mg*30片→100。
     * 一律不认（返回 null）：没有质量值、有两条质量值（80mg/5mg×7片）、复方制剂、IU/单位类规格。
     */
    public static BigDecimal pieceStrengthMg(String specification) {
        if (!StringUtils.hasText(specification)) {
            return null;
        }
        String spec = specification.trim();
        if (SKIP_SPEC_MARKS.stream().anyMatch(spec::contains) || SKIP_SPEC_UNITS.matcher(spec).find()) {
            return null;
        }
        List<String> masses = new ArrayList<>();
        Matcher m = MASS.matcher(spec);
        while (m.find()) {
            masses.add(m.group());
        }
        if (masses.size() != 1) {
            return null;
        }
        // 含 ':' 的注射剂规格（10ml:1g）里那个质量值就是「整支」含量；按 ml 给药的
        // 多剂量瓶会被 singleDoseMg 自己挡掉（ml 不是个数单位），所以这里不会误乘。
        if (!PIECE_HINT.matcher(spec).find() && !spec.contains(":")) {
            return null;
        }
        Matcher single = MASS.matcher(masses.get(0));
        return single.find() ? toMg(new BigDecimal(single.group(1)), single.group(2)) : null;
    }

    /**
     * 单次给药量（mg）
     * <p>
     * 只有三种可判形态：① 带质量单位的文本（{@code 0.5g}→500）；② 数字＋个数单位（2片→2×单件含量）；
     * ③ 纯数字（库里医生就写 {@code 2}，口径同上）。其余（{@code 10ml}、1滴、适量、空、
     * 含两个数字的区间写法）返回 null，表示<b>这条不判</b>。
     */
    public static BigDecimal singleDoseMg(String singleDosage, String specification) {
        if (!StringUtils.hasText(singleDosage)) {
            return null;
        }
        String text = singleDosage.trim()
                .replace("×", "*").replace(" ", "").toLowerCase();
        List<String> numbers = new ArrayList<>();
        Matcher any = Pattern.compile("\\d+(?:\\.\\d+)?").matcher(text);
        while (any.find()) {
            numbers.add(any.group());
        }
        if (numbers.size() != 1) {
            // '1-2片'、'0.5g×2' 这类区间/复合写法：真实给药量不确定，判了就是编
            return null;
        }
        Matcher head = Pattern.compile("^(\\d+(?:\\.\\d+)?)").matcher(text);
        if (!head.find()) {
            return null;
        }
        BigDecimal value = new BigDecimal(head.group(1));
        String rest = text.substring(head.end());
        String unit = leadingUnit(rest);

        if (StringUtils.hasText(unit)) {
            BigDecimal direct = toMg(value, unit);
            if (direct != null) {
                return direct;
            }
            if (!PIECE_UNITS.contains(unit)) {
                return null;
            }
        }
        BigDecimal piece = pieceStrengthMg(specification);
        return piece == null ? null : value.multiply(piece);
    }

    /**
     * 每日给药次数（隔日一次记 0.5）
     *
     * @return null = 频次解析不到、或按需/一次性给药 —— 调用方据此<b>跳过</b>日上限判断，不当作 0 次
     */
    public static BigDecimal timesPerDay(String frequency) {
        if (!StringUtils.hasText(frequency)) {
            return null;
        }
        String text = frequency.trim().toLowerCase().replace(" ", "");
        if (Pattern.compile("(?i)\\b(prn|sos|st|once)\\b").matcher(text).find()
                || text.contains("需要时") || text.contains("按需") || text.contains("立即")
                || text.contains("舌下") || text.contains("雾化吸入时")) {
            return null;
        }
        if (text.contains("隔日") || text.contains("qod")) {
            return new BigDecimal("0.5");
        }
        if (text.contains("剂")) {
            // 中药「每日一剂」的「剂」不是给药次数口径（饮片本身也不在上限表里）
            return null;
        }
        Matcher every = Pattern.compile("每(\\d+)小时|q(\\d+)h").matcher(text);
        if (every.find()) {
            String hours = every.group(1) != null ? every.group(1) : every.group(2);
            BigDecimal h = new BigDecimal(hours);
            if (h.compareTo(BigDecimal.ZERO) <= 0) {
                return null;
            }
            return new BigDecimal("24").divide(h, 4, RoundingMode.HALF_UP).stripTrailingZeros();
        }
        Matcher count = Pattern.compile("(\\d+|[一二三四五六七八九十]+)(次|回)").matcher(text);
        if (count.find()) {
            return cnToDecimal(count.group(1));
        }
        for (String token : List.of("qid", "tid", "bid", "qd", "qn")) {
            if (text.contains(token)) {
                return switch (token) {
                    case "qid" -> new BigDecimal("4");
                    case "tid" -> new BigDecimal("3");
                    case "bid" -> new BigDecimal("2");
                    // qn=每晚一次，同样是一天一次
                    default -> BigDecimal.ONE;
                };
            }
        }
        return null;
    }

    /**
     * 取数值后面的首个单位词（只截字母/中文，其余视为分隔符）
     */
    private static String leadingUnit(String rest) {
        Matcher m = Pattern.compile("^([A-Za-z\\u4e00-\\u9fa5μ]+)").matcher(rest);
        if (!m.find()) {
            return "";
        }
        String s = m.group(1);
        for (String piece : PIECE_UNITS) {
            if (s.startsWith(piece)) {
                return piece;
            }
        }
        return s.length() > 4 ? s.substring(0, 4) : s;
    }

    private static BigDecimal cnToDecimal(String token) {
        return switch (token) {
            case "一" -> BigDecimal.ONE;
            case "二", "两" -> new BigDecimal("2");
            case "三" -> new BigDecimal("3");
            case "四" -> new BigDecimal("4");
            case "五" -> new BigDecimal("5");
            case "六" -> new BigDecimal("6");
            case "七" -> new BigDecimal("7");
            case "八" -> new BigDecimal("8");
            case "九" -> new BigDecimal("9");
            case "十" -> new BigDecimal("10");
            default -> {
                try {
                    yield new BigDecimal(token);
                } catch (NumberFormatException e) {
                    yield null;
                }
            }
        };
    }
}

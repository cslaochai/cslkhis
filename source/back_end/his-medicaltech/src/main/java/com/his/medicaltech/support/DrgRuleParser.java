package com.his.medicaltech.support;

import com.his.common.util.TextUtil;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 分组规则原文解析器：把官方 DSL 编译成 {@link DrgRule}。
 *
 * <p>语法由 3.0 包 871 条规则逐条穷举确定，条件式只有四种形状：
 * <pre>
 *   表达式 := 条件 [ and | or 条件 ]...           括号任意嵌套，实测同层不同时混用 and/or
 *   条件   := 变量组 in [not] 引用组
 *            | 变量 (>=|&lt;=|&gt;|&lt;|=) 数字
 *            | length(集合编号 ∩ {变量组}) (>=|&lt;=|&gt;|&lt;|=) 数字
 *            | 1                                  恒真：官方用它标「本组其余档全不命中时落这一档」
 *   变量   := ZYZD 主诊断 / QTZD 其他诊断 / ZYSS 主手术 / QTSS 其他手术
 *            / NL 周岁 / XB 性别 / XSRTL 出生日龄 / XSRTZ 入院体重
 *   引用   := 集合编号 | MCC | CC（后两者是伪集合，事实来自并发症合并症目录的级别，不在集合表里）
 * </pre>
 *
 * <p><b>{@code length(...)} 的圆括号在分词前换成方括号</b>：通用分词器把圆括号当分组符号切开，
 * 而这条算子的参数里既含 {@code ∩} 又含逗号，切开就变成 {@code length}｜{@code 集合 ∩ {组}}｜{@code >=2}
 * 三个残缺片段（全表只有 1 条这种规则，一旦没读懂就是整档 ADRG 不参与入组）。
 * 方括号不是分词符，换完仍是一个完整原子，{@link #LEN_PAT} 按方括号那一份形状匹配。
 *
 * <p><b>变量组 {A, B} 在 in 左侧按「任一命中」求值，不是「全部命中」</b>：官方把 {ZYSS, QTSS} 当
 * 「手术清单整体」使用，而形如 {@code ZYSS in OP1_x and {ZYSS, QTSS} in OP2_x} 的规则里两个集合互斥
 * （实测 110 组全互斥），按「全部命中」解释这类规则永假。真要表达「两台手术都要」，
 * 官方另写了 {@code length(OP_IC2 ∩ {ZYSS, QTSS})>=2}。
 *
 * <p><b>数值变量缺值一律判不命中</b>：首页没有入院体重、年龄单位不是「天」时算不出出生日龄，
 * 此时相关规则不命中，而不是当成 0 去满足 {@code XSRTZ<1000} 这类条件。
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class DrgRuleParser {

    private static final Pattern IN_PAT = Pattern.compile(
            "^(\\{[A-Z, ]+\\}|[A-Z]+) (not in|in) (\\{[A-Za-z0-9_, ]+\\}|[A-Za-z0-9_]+)$");
    private static final Pattern CMP_PAT = Pattern.compile("^([A-Z]+) *(>=|<=|>|<|=) *(\\d+)$");
    private static final Pattern LEN_PAT = Pattern.compile(
            "^length\\[ *([A-Za-z0-9_]+) *∩ *\\{([A-Z, ]+)\\} *\\] *(>=|<=|>|<|=) *(\\d+)$");
    private static final Pattern LEN_TEXT_PAT = Pattern.compile("length *\\(([^()]*)\\) *(>=|<=|>|<|=) *(\\d+)");

    /**
     * 恒真条件（官方写成 {@code 1}）：不带任何事实判断，只在同组前面的档位全不命中后才轮到它
     */
    private static final DrgRule ALWAYS = (facts, scheme) -> true;

    /**
     * 编译规则原文；空白返回 null（空规则=该行不带条件，兜底语义由分组器按目录层级决定）。
     *
     * @throws IllegalArgumentException 出现上述四种形状之外的条件式，或括号/连接词不完整
     */
    public static DrgRule parse(String text, String sourceCode) {
        if (!TextUtil.hasText(text)) {
            return null;
        }
        String cleaned = text.trim().replaceAll("\\s+", " ");
        cleaned = LEN_TEXT_PAT.matcher(cleaned).replaceAll("length[$1]$2$3");
        Cursor cursor = new Cursor(tokenize(cleaned), sourceCode);
        DrgRule rule = parseOr(cursor);
        if (cursor.pos < cursor.toks.size()) {
            throw new IllegalArgumentException("规则[" + sourceCode + "]有多余内容：" + cleaned);
        }
        return rule;
    }

    // ==================== 求值节点 ====================

    private record Any(List<DrgRule> parts) implements DrgRule {
        @Override
        public boolean matches(DrgFacts facts, DrgScheme scheme) {
            for (DrgRule part : parts) {
                if (part.matches(facts, scheme)) {
                    return true;
                }
            }
            return false;
        }
    }

    private record All(List<DrgRule> parts) implements DrgRule {
        @Override
        public boolean matches(DrgFacts facts, DrgScheme scheme) {
            for (DrgRule part : parts) {
                if (!part.matches(facts, scheme)) {
                    return false;
                }
            }
            return true;
        }
    }

    /**
     * 码类变量落在引用集合内（多变量、多引用都按「任一命中」）
     */
    private record InRef(List<String> vars, List<String> refs, boolean negated) implements DrgRule {
        @Override
        public boolean matches(DrgFacts facts, DrgScheme scheme) {
            boolean hit = false;
            outer:
            for (String var : vars) {
                for (String code : facts.codes(var)) {
                    for (String ref : refs) {
                        if (scheme.matchRef(ref, code, facts.mainDiag())) {
                            hit = true;
                            break outer;
                        }
                    }
                }
            }
            // not in 取反：一个都没命中才算成立；该维度无值也算成立（无手术=主手术不在任何手术集合内）
            return negated != hit;
        }
    }

    private record Compare(String var, Cmp op, long operand) implements DrgRule {
        @Override
        public boolean matches(DrgFacts facts, DrgScheme scheme) {
            Integer actual = facts.number(var);
            return actual != null && op.test(actual, operand);
        }
    }

    /**
     * 手术清单与集合的交集条数比较（联合手术组用）
     */
    private record SetCount(String setCode, List<String> vars, Cmp op, long operand) implements DrgRule {
        @Override
        public boolean matches(DrgFacts facts, DrgScheme scheme) {
            List<String> members = new ArrayList<>();
            for (String var : vars) {
                for (String code : facts.codes(var)) {
                    if (scheme.members(setCode).contains(code) && !members.contains(code)) {
                        members.add(code);
                    }
                }
            }
            return op.test(members.size(), operand);
        }
    }

    /**
     * 规则里出现的比较算子
     */
    private enum Cmp {
        GE(">="), LE("<="), GT(">"), LT("<"), EQ("=");

        private final String token;

        Cmp(String token) {
            this.token = token;
        }

        private static Cmp of(String token) {
            for (Cmp item : values()) {
                if (item.token.equals(token)) {
                    return item;
                }
            }
            throw new IllegalArgumentException("规则里出现未知比较符：" + token);
        }

        private boolean test(long actual, long operand) {
            return switch (this) {
                case GE -> actual >= operand;
                case LE -> actual <= operand;
                case GT -> actual > operand;
                case LT -> actual < operand;
                case EQ -> actual == operand;
            };
        }
    }

    // ==================== 解析 ====================

    private static DrgRule parseAtom(String text, String sourceCode) {
        if ("1".equals(text)) {
            return ALWAYS;
        }
        Matcher len = LEN_PAT.matcher(text);
        if (len.matches()) {
            return new SetCount(len.group(1), splitItems(len.group(2)), Cmp.of(len.group(3)),
                    Long.parseLong(len.group(4)));
        }
        Matcher in = IN_PAT.matcher(text);
        if (in.matches()) {
            return new InRef(splitItems(in.group(1)), splitItems(in.group(3)), "not in".equals(in.group(2)));
        }
        Matcher cmp = CMP_PAT.matcher(text);
        if (cmp.matches()) {
            return new Compare(cmp.group(1), Cmp.of(cmp.group(2)), Long.parseLong(cmp.group(3)));
        }
        throw new IllegalArgumentException("规则[" + sourceCode + "]有无法解析的条件式：" + text);
    }

    /**
     * 变量/引用组：去花括号、按逗号拆、统一大写
     */
    private static List<String> splitItems(String group) {
        String body = group.startsWith("{") ? group.substring(1, group.length() - 1) : group;
        List<String> items = new ArrayList<>();
        for (String item : body.split(",")) {
            String trimmed = item.trim().toUpperCase();
            if (!trimmed.isEmpty() && !items.contains(trimmed)) {
                items.add(trimmed);
            }
        }
        return items;
    }

    private enum Kind {LP, RP, AND, OR, ATOM}

    private record Tok(Kind kind, String text) {
    }

    private static final class Cursor {
        private final List<Tok> toks;
        private final String sourceCode;
        private int pos;

        private Cursor(List<Tok> toks, String sourceCode) {
            this.toks = toks;
            this.sourceCode = sourceCode;
        }

        private Kind peekKind() {
            return pos < toks.size() ? toks.get(pos).kind() : null;
        }

        private String takeAtom() {
            return toks.get(pos++).text();
        }
    }

    /**
     * or 优先级低于 and：先按 or 切，每段再按 and 切
     */
    private static DrgRule parseOr(Cursor cursor) {
        List<DrgRule> parts = new ArrayList<>();
        parts.add(parseAnd(cursor));
        while (cursor.peekKind() == Kind.OR) {
            cursor.pos++;
            parts.add(parseAnd(cursor));
        }
        return parts.size() == 1 ? parts.get(0) : new Any(parts);
    }

    private static DrgRule parseAnd(Cursor cursor) {
        List<DrgRule> parts = new ArrayList<>();
        parts.add(parsePrimary(cursor));
        while (cursor.peekKind() == Kind.AND) {
            cursor.pos++;
            parts.add(parsePrimary(cursor));
        }
        return parts.size() == 1 ? parts.get(0) : new All(parts);
    }

    private static DrgRule parsePrimary(Cursor cursor) {
        Kind kind = cursor.peekKind();
        if (kind == Kind.LP) {
            cursor.pos++;
            DrgRule inner = parseOr(cursor);
            if (cursor.peekKind() != Kind.RP) {
                throw new IllegalArgumentException("规则[" + cursor.sourceCode + "]括号未闭合");
            }
            cursor.pos++;
            return inner;
        }
        if (kind != Kind.ATOM) {
            throw new IllegalArgumentException("规则[" + cursor.sourceCode + "]缺少条件式（结尾多了连接词或括号）");
        }
        return parseAtom(cursor.takeAtom(), cursor.sourceCode);
    }

    private static List<Tok> tokenize(String text) {
        List<Tok> toks = new ArrayList<>();
        StringBuilder buf = new StringBuilder();
        int i = 0;
        while (i < text.length()) {
            char c = text.charAt(i);
            if (c == '(' || c == ')') {
                flush(toks, buf);
                toks.add(new Tok(c == '(' ? Kind.LP : Kind.RP, null));
                i++;
                continue;
            }
            int end = matchWord(text, i, "and");
            if (end > 0) {
                flush(toks, buf);
                toks.add(new Tok(Kind.AND, null));
                i = end;
                continue;
            }
            end = matchWord(text, i, "or");
            if (end > 0) {
                flush(toks, buf);
                toks.add(new Tok(Kind.OR, null));
                i = end;
                continue;
            }
            buf.append(c);
            i++;
        }
        flush(toks, buf);
        return toks;
    }

    private static void flush(List<Tok> toks, StringBuilder buf) {
        String atom = buf.toString().trim();
        if (!atom.isEmpty()) {
            toks.add(new Tok(Kind.ATOM, atom));
        }
        buf.setLength(0);
    }

    /**
     * 连接词必须成词：集合编号里带 or/and 字母时不能被切开
     */
    private static int matchWord(String text, int at, String word) {
        if (!text.regionMatches(true, at, word, 0, word.length())) {
            return -1;
        }
        if (at > 0 && isWordChar(text.charAt(at - 1))) {
            return -1;
        }
        int end = at + word.length();
        if (end < text.length() && isWordChar(text.charAt(end))) {
            return -1;
        }
        return end;
    }

    private static boolean isWordChar(char c) {
        return Character.isLetterOrDigit(c) || c == '_';
    }
}

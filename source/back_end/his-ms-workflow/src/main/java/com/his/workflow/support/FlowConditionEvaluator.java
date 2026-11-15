package com.his.workflow.support;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.his.common.util.TextUtil;
import org.springframework.stereotype.Component;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 流程变量读写与流转条件判断。
 * <p>刻意不做 SpEL/脚本求值：条件只支持「变量路径 运算符 字面量」，避免把库里的配置当代码执行。
 */
@Component
public class FlowConditionEvaluator {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private static final Pattern EXPR_PATTERN =
            Pattern.compile("^([A-Za-z0-9_.\\[\\]-]+)\\s*(==|!=|>=|<=|>|<)\\s*(.*)$");

    private static final String DEFAULT_BRANCH = "DEFAULT";

    /**
     * 解析变量 JSON；空串/非法 JSON 都当空对象，不拦流程。
     */
    public JsonNode parse(String json) {
        if (!TextUtil.hasText(json)) {
            return MAPPER.createObjectNode();
        }
        try {
            JsonNode node = MAPPER.readTree(json);
            return node.isObject() ? node : MAPPER.createObjectNode();
        } catch (Exception ex) {
            return MAPPER.createObjectNode();
        }
    }

    public String write(JsonNode node) {
        try {
            return MAPPER.writeValueAsString(node);
        } catch (Exception ex) {
            return "{}";
        }
    }

    /**
     * 条件是否命中。表达式非法/读不到变量一律返回 false（交给后面的分支或 DEFAULT）。
     */
    public boolean matches(String expression, JsonNode variables) {
        if (!TextUtil.hasText(expression)) {
            return false;
        }
        String expr = expression.trim();
        if (DEFAULT_BRANCH.equalsIgnoreCase(expr) || "TRUE".equalsIgnoreCase(expr)) {
            return true;
        }
        Matcher matcher = EXPR_PATTERN.matcher(expr);
        if (!matcher.matches()) {
            return false;
        }
        JsonNode value = read(variables, matcher.group(1));
        if (value == null || value.isNull() || value.isMissingNode()) {
            return false;
        }
        String literal = unquote(matcher.group(3).trim());
        String op = matcher.group(2);
        String actual = value.isNumber() ? value.asText() : value.asText("");
        if (">".equals(op) || "<".equals(op) || ">=".equals(op) || "<=".equals(op)) {
            Double a = toDouble(actual);
            Double b = toDouble(literal);
            if (a == null || b == null) {
                return false;
            }
            return switch (op) {
                case ">" -> a > b;
                case "<" -> a < b;
                case ">=" -> a >= b;
                default -> a <= b;
            };
        }
        boolean equal = actual.equals(literal);
        return "==".equals(op) == equal;
    }

    private JsonNode read(JsonNode root, String path) {
        JsonNode current = root;
        for (String segment : path.split("\\.")) {
            if (current == null || !current.isObject()) {
                return null;
            }
            current = current.get(segment);
        }
        return current;
    }

    private String unquote(String text) {
        if (text.length() >= 2
                && ((text.startsWith("'") && text.endsWith("'"))
                || (text.startsWith("\"") && text.endsWith("\"")))) {
            return text.substring(1, text.length() - 1);
        }
        return text;
    }

    private Double toDouble(String text) {
        try {
            return Double.valueOf(text);
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    /**
     * 把节点输出挂到变量表：键为节点编码；若输出里带 answer 文本，同步提升为顶层 answer 供患者端直接取。
     */
    public JsonNode mergeNodeOutput(JsonNode variables, String nodeCode, String payloadJson) {
        ObjectNode root = variables.isObject()
                ? (ObjectNode) variables
                : MAPPER.createObjectNode();
        JsonNode payload = parse(payloadJson);
        root.set(nodeCode, payload);
        if (payload.hasNonNull("answer")) {
            root.put("answer", payload.get("answer").asText());
        }
        return root;
    }
}

package com.his.ai.support;

import com.his.common.util.TextUtil;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * 病历文本的标签切分（硬规则层）。
 * <p>
 * <b>存在意义有两个</b>：
 * <ol>
 *   <li><b>降级可用</b>。模型不可用时，抽取不能变成一片空白。医生从外院系统、
 *       模板、上级医院病历里粘贴过来的文本，本身往往就是
 *       主诉：… / 现病史：… 这种带标签的格式，
 *       这类内容规则就能逐字切准 —— 而且比模型改写更忠于原文。</li>
 *   <li><b>规则优先</b>。同一个字段如果规则能从原文按标签切出来，就不用模型的改写版本：
 *       病历是法律文书，<b>逐字原文永远优于模型转述</b>。模型只负责补规则切不出来的部分。</li>
 * </ol>
 * <p>
 * 切分规则刻意保守，三条铁律：
 * <ol>
 *   <li>只有「行首 → 已知别名 → 紧接冒号」才算命中标签；</li>
 *   <li>正文里偶现的「主诉」二字不算标签（必须紧跟冒号）；</li>
 *   <li><b>别名表外的标签（体格检查、月经史…）会封闭当前字段，而不是把内容续到上一个字段上。</b>
 *       病历文本里「体格检查：…」紧跟在「过敏史：…」后面是常态，续接就把整段查体写进了过敏史。</li>
 * </ol>
 * 一句话：<b>宁可漏切也不能错切</b> —— 漏切医生看得见（字段空着），错切医生很可能看不见。
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class EmrTextTagSplitter {

    /**
     * 行首装饰：markdown 标记、项目符号、序号。仅用于匹配标签，不改变原文内容。
     */
    private static final Pattern LEADING_DECORATION =
            Pattern.compile("^[\\s#>*\\-•·【\\[（(]*\\d*[.、）)]*\\s*");

    /**
     * 标签与内容之间的分隔符：中英文冒号，或等号
     */
    private static final Pattern LABEL_SEPARATOR = Pattern.compile("^[:：=]\\s*");

    /**
     * <b>「长得像标签、但不在别名表里」的行首形态</b>：短前缀 + 冒号/等号。
     * <p>
     * 病历文本里这类没见过的标签非常常见（体格检查、月经史、家族遗传病史、输血史…）。
     * 它们的内容<b>绝不能</b>续接到上一个字段上：从外院粘贴的病历里，
     * 「体格检查：…」紧跟在「过敏史：…」后面是常态，一续接就把整段查体写进了过敏史。
     * <p>
     * 所以遇到这种行就封闭当前字段，其内容宁可不抽 ——
     * <b>漏抽医生看得见（字段空着），错抽医生很可能看不见</b>。
     * 这是本类"宁可漏切不能错切"原则的具体落地。
     */
    private static final Pattern UNKNOWN_LABEL = Pattern.compile("^[^\\s：:]{1,10}[:：=]");

    /**
     * 按标签切分文本
     *
     * @param rawText 医生粘贴的自由文本
     * @return 字段 key → 原文内容（保持原文，未做改写）。同一标签出现多次时按出现顺序拼接。
     */
    public static Map<String, String> split(String rawText) {
        Map<String, String> result = new LinkedHashMap<>();
        if (!TextUtil.hasText(rawText)) {
            return result;
        }

        String currentKey = null;
        // 多行内容用换行拼接：逐字保留原文，不做任何"顺句"改写
        StringBuilder buffer = new StringBuilder();

        for (String rawLine : rawText.replace("\r\n", "\n").replace('\r', '\n').split("\n", -1)) {
            String line = rawLine.trim();
            if (line.isEmpty()) {
                continue;
            }

            String matchedKey = matchLabel(line);
            if (matchedKey != null) {
                flush(result, currentKey, buffer);
                currentKey = matchedKey;
                buffer = new StringBuilder(contentAfterLabel(line));
                continue;
            }

            // 别名表外的标签：封闭当前字段，这一节的内容不抽（漏抽优于错抽）
            if (UNKNOWN_LABEL.matcher(strippedOfDecoration(line)).find()) {
                flush(result, currentKey, buffer);
                currentKey = null;
                buffer = new StringBuilder();
                continue;
            }

            if (currentKey != null) {
                if (buffer.length() > 0) {
                    buffer.append('\n');
                }
                buffer.append(line);
            }
        }
        flush(result, currentKey, buffer);
        return result;
    }

    /**
     * 去掉行首装饰（markdown 标记、项目符号、序号）
     */
    private static String strippedOfDecoration(String line) {
        return LEADING_DECORATION.matcher(line).replaceFirst("");
    }

    /**
     * 行首是否为已知字段标签，是则返回字段 key
     */
    private static String matchLabel(String line) {
        String stripped = strippedOfDecoration(line);
        if (stripped.isEmpty()) {
            return null;
        }
        List<Map.Entry<String, EmrFieldCatalog.TextField>> aliases = EmrFieldCatalog.sortedAliases();
        for (Map.Entry<String, EmrFieldCatalog.TextField> entry : aliases) {
            String alias = entry.getKey();
            if (!stripped.startsWith(alias)) {
                continue;
            }
            String rest = stripped.substring(alias.length());
            // 必须紧跟分隔符，避免正文里的「主诉」二字被当成标签
            if (LABEL_SEPARATOR.matcher(rest.trim()).find()) {
                return entry.getValue().key();
            }
        }
        return null;
    }

    private static String contentAfterLabel(String line) {
        String stripped = strippedOfDecoration(line);
        List<Map.Entry<String, EmrFieldCatalog.TextField>> aliases = EmrFieldCatalog.sortedAliases();
        for (Map.Entry<String, EmrFieldCatalog.TextField> entry : aliases) {
            String alias = entry.getKey();
            if (!stripped.startsWith(alias)) {
                continue;
            }
            String rest = stripped.substring(alias.length()).trim();
            if (!LABEL_SEPARATOR.matcher(rest).find()) {
                continue;
            }
            return toContent(rest);
        }
        return stripped;
    }

    /**
     * 去掉「：」前的标签残留（如「主诉 =」这种写法）
     */
    private static String toContent(String rest) {
        String value = rest;
        java.util.regex.Matcher matcher = LABEL_SEPARATOR.matcher(value);
        if (matcher.find()) {
            value = value.substring(matcher.end());
        }
        return value.trim();
    }

    private static void flush(Map<String, String> result, String key, StringBuilder buffer) {
        if (key == null) {
            return;
        }
        String value = buffer.toString().trim();
        if (value.isEmpty()) {
            return;
        }
        result.merge(key, value, (existing, added) -> existing + "\n" + added);
    }
}

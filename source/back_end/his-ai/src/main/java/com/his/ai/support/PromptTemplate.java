package com.his.ai.support;

import com.his.common.util.TextUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 提示词模板渲染器。
 * <p>
 * 模板放 {@code his-ai/src/main/resources/prompts/*.md}，好处是：
 * 调提示词不需要改 Java 代码、不需要重新编译；出问题时能精确定位是哪一版产生的坏结果。
 * <p>
 * 文件格式：
 * <pre>
 * version: 1.0.0
 * updated_at: 2026-09-17
 * （system 段：角色 + 规则 + 输出 JSON 结构，可含 {{变量}}）
 *
 * &lt;!-- user --&gt;
 * （user 段：本次数据，可含 {{变量}}）
 * </pre>
 * 模板按文件名缓存，首次渲染时加载；如需热更新改完文件重启即可（演示环境够用，
 * 生产可改为带 TTL 的缓存）。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PromptTemplate {

    private static final String PROMPT_DIR = "prompts/";
    private static final String USER_MARKER = "<!-- user -->";
    private static final String FRONT_MATTER = "---";
    private static final String DEFAULT_VERSION = "unknown";

    /**
     * 渲染后仍残留的占位符形态。分组 1 取占位符名，只用于日志点名，不回显正文。
     */
    private static final Pattern UNFILLED_PATTERN = Pattern.compile("\\{\\{([A-Za-z0-9_]+)}}");

    private final ResourceLoader resourceLoader;

    private final Map<String, ParsedTemplate> cache = new ConcurrentHashMap<>();

    private static String substitute(String template, Map<String, String> variables, String name) {
        if (!TextUtil.hasText(template) || variables.isEmpty()) {
            return template;
        }
        String result = template;
        for (Map.Entry<String, String> entry : variables.entrySet()) {
            String placeholder = "{{" + entry.getKey() + "}}";
            if (!result.contains(placeholder)) {
                continue;
            }
            String value = entry.getValue() == null ? "" : entry.getValue();
            result = result.replace(placeholder, value);
        }
        // 兜底自检：模板里写了占位符但VO 没这个字段，说明改提示词时忘了加变量，
        // 不打这条日志就会把 {{xxx}} 原文发给模型，输出静默变差且极难定位。
        // 只记未填的占位符名、不记渲染后的正文 —— 正文含患者姓名与诊断等敏感信息，不能进日志。
        String unfilled = unfilledPlaceholders(result);
        if (!unfilled.isEmpty()) {
            log.warn("[AI-提示词] 模板 {} 存在未被变量覆盖的占位符 {}，请检查是否漏了 PromptVariables 字段",
                    name, unfilled);
        }
        return result;
    }

    /**
     * 找出渲染后仍然残留的 {@code {{占位符}}}。
     * <p>用正则而不是 {@code contains("{{")}：患者自述里出现「{{」不该误报，
     * 但「{{xxx}}」这种形态在正文里出现就是漏填。
     */
    private static String unfilledPlaceholders(String rendered) {
        Set<String> names = new LinkedHashSet<>();
        Matcher matcher = UNFILLED_PATTERN.matcher(rendered);
        while (matcher.find()) {
            names.add(matcher.group(1));
        }
        return names.toString();
    }

    /**
     * 渲染提示词
     *
     * @param name      模板名（不含 .md）
     * @param variables 变量表，占位符写法 {{key}}；各能力的字段名即占位符名
     */
    public RenderedPrompt render(String name, PromptVariables variables) {
        ParsedTemplate template = cache.computeIfAbsent(name, this::load);
        Map<String, String> values = variables == null ? Map.of() : variables.toMap();
        return new RenderedPrompt(
                template.name(),
                template.version(),
                substitute(template.systemText(), values, name),
                substitute(template.userText(), values, name));
    }

    /**
     * 取模板版本号
     */
    public String versionOf(String name) {
        return cache.computeIfAbsent(name, this::load).version();
    }

    private ParsedTemplate load(String name) {
        String location = "classpath:" + PROMPT_DIR + name + ".md";
        Resource resource = resourceLoader.getResource(location);
        if (!resource.exists()) {
            throw new IllegalStateException("提示词模板不存在：" + location);
        }
        try (InputStream input = resource.getInputStream()) {
            String raw = new String(input.readAllBytes(), StandardCharsets.UTF_8);
            return parse(name, raw);
        } catch (IOException ex) {
            throw new IllegalStateException("提示词模板读取失败：" + location, ex);
        }
    }

    private ParsedTemplate parse(String name, String raw) {
        String text = raw.replace("\r\n", "\n").trim();
        String version = DEFAULT_VERSION;

        if (text.startsWith(FRONT_MATTER)) {
            int headerEnd = text.indexOf("\n" + FRONT_MATTER, FRONT_MATTER.length());
            if (headerEnd > 0) {
                String header = text.substring(FRONT_MATTER.length(), headerEnd);
                for (String line : header.split("\n")) {
                    int colon = line.indexOf(':');
                    if (colon > 0 && "version".equalsIgnoreCase(line.substring(0, colon).trim())) {
                        version = line.substring(colon + 1).trim();
                    }
                }
                text = text.substring(headerEnd + FRONT_MATTER.length() + 1).trim();
            }
        }

        int markerIndex = text.indexOf(USER_MARKER);
        String systemText = markerIndex >= 0 ? text.substring(0, markerIndex).trim() : text;
        String userText = markerIndex >= 0 ? text.substring(markerIndex + USER_MARKER.length()).trim() : "";

        if (!TextUtil.hasText(systemText)) {
            throw new IllegalStateException("提示词模板缺少 system 段：" + name);
        }
        return new ParsedTemplate(name, version, systemText, userText);
    }

    private record ParsedTemplate(String name, String version, String systemText, String userText) {
    }
}

package com.his.ai.support;

import com.his.ai.entity.SysAiPromptTemplate;
import com.his.ai.mapper.SysAiPromptTemplateMapper;
import com.his.common.enums.YesOrNoEnum;
import com.his.common.util.TextUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;

import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 提示词模板渲染器。数据库优先，classpath 文件兜底。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PromptTemplate {

    private static final String PROMPT_DIR = "prompts/";
    private static final String USER_MARKER = "<!-- user -->";
    private static final String FRONT_MATTER = "---";
    private static final String DEFAULT_VERSION = "unknown";

    private static final Pattern UNFILLED_PATTERN = Pattern.compile("\\{\\{([A-Za-z0-9_]+)}}");

    private final ResourceLoader resourceLoader;

    private final SysAiPromptTemplateMapper promptTemplateMapper;

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
        Set<String> unfilled = unfilledPlaceholders(result);
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
    private static Set<String> unfilledPlaceholders(String rendered) {
        Set<String> names = new LinkedHashSet<>();
        Matcher matcher = UNFILLED_PATTERN.matcher(rendered);
        while (matcher.find()) {
            names.add(matcher.group(1));
        }
        return names;
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
        SysAiPromptTemplate dbRow = promptTemplateMapper.selectOne(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<SysAiPromptTemplate>()
                        .eq(SysAiPromptTemplate::getTemplateCode, name)
                        .eq(SysAiPromptTemplate::getIsActive, YesOrNoEnum.YES.getCode())
                        .last("LIMIT 1"));
        if (dbRow == null || !TextUtil.hasText(dbRow.getContent())) {
            throw new IllegalStateException("提示词模板未配置：" + name + "，请在「RAG知识库 → 提示词管理」中维护");
        }
        return parse(name, dbRow.getContent());
    }

    /**
     * 清缓存：管理端编辑提示词后调，下次渲染时重新加载
     */
    public void evictCache(String name) {
        if (name == null) {
            cache.clear();
        } else {
            cache.remove(name);
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

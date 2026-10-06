package com.his.ai.support;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.util.StringUtils;

import java.util.*;

/**
 * 药品分类与条件同义词词典 —— 硬规则匹配的「领域知识」。
 * <p>
 * <b>为什么必须有这个类</b>：字典里写的是「对青霉素过敏者禁用」，患者过敏史写的是
 * 「阿莫西林过敏」。字面比对永远碰不上，但临床上这是<b>最危险的一类漏报</b>。
 * 所以必须把「青霉素类」这样的类别概念显式建模出来，做双向展开。
 * <p>
 * <b>为什么不交给大模型</b>：这是确定性知识，不是推理。规则能百分百命中的事，
 * 交给概率模型反而是倒退 —— 模型可能这次认出、下次漏掉。
 * 模型应当用在规则写不完的长尾（相互作用、剂量匹配、诊断用药不符）上。
 * <p>
 * 局限说明：本表是围绕当前 81 条演示药品字典人工整理的，覆盖青霉素类、头孢菌素类、
 * 喹诺酮类、大环内酯类、磺胺类、解热镇痛抗炎药、他汀类、二氢吡啶类八个常用类别。
 * 生产环境应改为读药品分类主数据，
 * 而不是在代码里维护 —— 这里之所以先写成常量，是因为当前库中没有该主数据表。
 * <p>
 * <b>一条设计约束</b>：同一个药品只归属到<b>一个</b>类别。
 * 「阿司匹林」既能算非甾体抗炎药、又能算解热镇痛药，如果两边都挂，
 * 同类重复检查就会对同一组药报两次 —— 医生看到重复的报错，只会觉得系统不靠谱。
 * 所以这里按「重复用药检查的实际口径」归并，而不是按药理学的严格分类。
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class DrugClassCatalog {

    private static final List<DrugClass> CLASSES = List.of(
            new DrugClass("青霉素类", "青霉素",
                    List.of("青霉素", "阿莫西林", "氨苄西林", "哌拉西林", "美洛西林", "青霉素G", "青霉素V")),
            new DrugClass("头孢菌素类", "头孢",
                    List.of("头孢", "头孢拉定", "头孢呋辛", "头孢克肟", "头孢曲松", "头孢氨苄", "头孢地尼")),
            new DrugClass("喹诺酮类", "喹诺酮",
                    List.of("喹诺酮", "左氧氟沙星", "氧氟沙星", "环丙沙星", "莫西沙星", "诺氟沙星")),
            new DrugClass("大环内酯类", "大环内酯",
                    List.of("大环内酯", "阿奇霉素", "红霉素", "克拉霉素", "罗红霉素", "螺旋霉素")),
            new DrugClass("磺胺类", "磺胺",
                    List.of("磺胺", "复方磺胺甲噁唑", "磺胺嘧啶", "柳氮磺吡啶")),
            new DrugClass("解热镇痛抗炎药", "解热镇痛",
                    List.of("对乙酰氨基酚", "布洛芬", "阿司匹林", "双氯芬酸", "吲哚美辛", "塞来昔布")),
            new DrugClass("他汀类", "他汀",
                    List.of("他汀", "阿托伐他汀", "瑞舒伐他汀", "辛伐他汀", "普伐他汀")),
            new DrugClass("二氢吡啶类钙拮抗剂", "二氢吡啶",
                    List.of("二氢吡啶", "氨氯地平", "硝苯地平", "非洛地平", "尼莫地平"))
    );
    /**
     * 禁忌描述里的「泛指」词，不构成具体的过敏原，必须剔除，
     * 否则「对本品过敏者禁用」会匹配上患者过敏史里的任何内容。
     */
    private static final Set<String> GENERIC_ALLERGEN_TOKENS = Set.of(
            "本品", "该品", "此品", "本药", "该药", "此药", "药物", "药品", "成分", "辅料");
    /**
     * 基础疾病条件同义词。key 为常见表述，value 为同义表述。
     */
    private static final Map<String, List<String>> CONDITION_SYNONYMS = new LinkedHashMap<>();

    static {
        CONDITION_SYNONYMS.put("肾功能不全",
                List.of("肾功能衰竭", "肾衰", "慢性肾脏病", "尿毒症", "肾小球滤过率下降", "氮质血症"));
        CONDITION_SYNONYMS.put("肝功能不全",
                List.of("肝病", "活动性肝病", "肝硬化", "肝炎", "肝衰竭", "肝功能异常", "转氨酶升高"));
        CONDITION_SYNONYMS.put("溃疡",
                List.of("消化性溃疡", "胃溃疡", "十二指肠溃疡", "活动性溃疡", "消化道溃疡"));
        CONDITION_SYNONYMS.put("哮喘",
                List.of("支气管哮喘", "哮喘发作", "喘息"));
        CONDITION_SYNONYMS.put("青光眼", List.of("闭角型青光眼", "眼压升高"));
        CONDITION_SYNONYMS.put("前列腺肥大", List.of("前列腺增生", "排尿困难"));
        CONDITION_SYNONYMS.put("重症肌无力", List.of("肌无力"));
        CONDITION_SYNONYMS.put("心力衰竭", List.of("心衰", "心功能不全"));
    }

    /**
     * 过敏原的双向展开。
     * <p>
     * 输入「青霉素」应能展开出「阿莫西林」；输入「阿莫西林」也应能展开出「青霉素」。
     * 任何一侧匹配上场，都视为同一类药物过敏。
     */
    public static Set<String> expandAllergen(String allergen) {
        Set<String> expanded = new LinkedHashSet<>();
        if (!StringUtils.hasText(allergen)) {
            return expanded;
        }
        String token = allergen.trim();
        if (isGenericAllergenToken(token)) {
            return expanded;
        }
        expanded.add(token);
        for (DrugClass drugClass : CLASSES) {
            if (matchesClass(token, drugClass)) {
                expanded.add(drugClass.keyword());
                expanded.add(drugClass.name());
                expanded.addAll(drugClass.members());
            }
        }
        return expanded;
    }

    /**
     * 判断一个药品名（或通用名）属于哪些类别
     */
    public static List<String> classesOf(String drugName) {
        List<String> names = new ArrayList<>();
        if (!StringUtils.hasText(drugName)) {
            return names;
        }
        for (DrugClass drugClass : CLASSES) {
            if (matchesClass(drugName, drugClass)) {
                names.add(drugClass.name());
            }
        }
        return names;
    }

    /**
     * 禁忌人群/疾病条件的同义展开
     */
    public static Set<String> expandCondition(String keyword) {
        Set<String> expanded = new LinkedHashSet<>();
        if (!StringUtils.hasText(keyword)) {
            return expanded;
        }
        String token = keyword.trim();
        expanded.add(token);
        for (Map.Entry<String, List<String>> entry : CONDITION_SYNONYMS.entrySet()) {
            String key = entry.getKey();
            List<String> synonyms = entry.getValue();
            boolean related = key.contains(token) || token.contains(key)
                    || synonyms.stream().anyMatch(synonym -> synonym.contains(token) || token.contains(synonym));
            if (related) {
                expanded.add(key);
                expanded.addAll(synonyms);
            }
        }
        return expanded;
    }

    /**
     * 是否为无意义的泛指过敏原
     */
    public static boolean isGenericAllergenToken(String token) {
        if (!StringUtils.hasText(token)) {
            return true;
        }
        String value = token.trim();
        if (GENERIC_ALLERGEN_TOKENS.contains(value)) {
            return true;
        }
        // 「本品及辅料」这类拼接表述
        return value.length() <= 4 && GENERIC_ALLERGEN_TOKENS.stream().anyMatch(value::contains);
    }

    private static boolean matchesClass(String token, DrugClass drugClass) {
        if (token.contains(drugClass.keyword()) || token.contains(drugClass.name())) {
            return true;
        }
        for (String member : drugClass.members()) {
            if (token.contains(member) || member.contains(token)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 药品类别定义
     *
     * @param name    类别名，例如「青霉素类」
     * @param keyword 用于双向识别的类别关键词，例如「青霉素」
     * @param members 该类别下的药品名或通用名片段
     */
    public record DrugClass(String name, String keyword, List<String> members) {
    }
}

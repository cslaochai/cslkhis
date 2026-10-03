package com.his.ai.support;

import com.his.ai.dto.DrugAuditContextDTO;
import com.his.ai.vo.DrugAuditFindingVO;
import com.his.common.support.ClinicalTextMatcher;
import com.his.emr.entity.BizPrescriptionDetail;
import com.his.system.dto.DrugRationalItemDTO;
import com.his.system.entity.SysDrug;
import com.his.system.service.DrugRationalCheckService;
import com.his.system.vo.DrugRationalHitVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 处方审核<b>硬规则层</b>（P0-2 的前半段）。
 * <p>
 * 分工原则：
 * <ul>
 *   <li><b>确定性错误</b>（过敏禁忌、重复用药、皮试缺失、禁忌人群冲突、
 *       知识表里的相互作用与剂量上限）→ 这里。
 *       规则能百分百命中的事交给概率模型是倒退：模型可能这次认出、下次漏掉，
 *       而这类错误一旦漏掉就是医疗事故。</li>
 *   <li><b>长尾问题</b>（知识表尚未收录的相互作用、剂量与年龄/肝肾功能不匹配、诊断用药不符）
 *       → 交给模型。规则写不完的部分才是模型的价值所在。</li>
 * </ul>
 * 相互作用与剂量上限自 sql/130 起有了可维护的知识表（药物相互作用知识库 /
 * 药品剂量上限知识库），因此从「交给模型」上收成本层的 R6/R7 ——
 * 判据不再是模型的印象，而是药师在界面上改得动的那张表，命中理由也才可追溯。
 * <p>
 * 本层产出可能带 {@code errorLevel = 3}，模型层不允许给 3 —— 见 DrugAuditCapability 的说明。
 * <p>
 * 所有字面匹配都走 {@link ClinicalTextMatcher}，带否定语义保护。
 * 直接把 {@code text.contains(...)} 写在这里会被评审打回来，原因见该工具类注释。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DrugHardRuleChecker {

    public static final String SOURCE = "HARD_RULE";
    /**
     * 拦截级：仅硬规则可给出
     */
    public static final int LEVEL_BLOCK = 3;
    /**
     * 知识表命中类型：相互作用（其余为剂量上限的两种），取值见 his-system DrugRationalHitVO
     */
    private static final String HIT_INTERACTION = "INTERACTION";
    /**
     * 提示级：了解即可
     */
    private static final int LEVEL_HINT = 1;

    /**
     * 警告级：建议调整
     */
    private static final int LEVEL_WARN = 2;
    /**
     * 中药饮片不参与「同类重复」检查 —— 一张饮片方里多味同类药材是正常组方，
     * 按化学成分归类反而会大面积误报。
     */
    private static final int DRUG_TYPE_HERBAL_PIECES = 3;
    /**
     * 单次最多返回的硬规则问题数，防止极端处方刷屏
     */
    private static final int MAX_FINDINGS = 10;
    /**
     * 禁忌描述中的过敏表述：「对XXX过敏者禁用」
     */
    private static final Pattern ALLERGY_CLAUSE = Pattern.compile("对\\s*([^，。；;,、]{1,20}?)\\s*过敏者?禁用");
    /**
     * 禁忌描述中的禁忌人群/疾病：「XXX者禁用」「XXX患者禁用」
     */
    private static final Pattern CONDITION_CLAUSE = Pattern.compile("([^，。；;,]{2,20}?)(?:者|患者)禁用");
    private final DrugRationalCheckService drugRationalCheckService;

    private static Set<String> extractAllergens(String contraindication) {
        Set<String> allergens = new LinkedHashSet<>();
        if (!StringUtils.hasText(contraindication)) {
            return allergens;
        }
        Matcher matcher = ALLERGY_CLAUSE.matcher(contraindication);
        while (matcher.find()) {
            String token = matcher.group(1).trim();
            if (!DrugClassCatalog.isGenericAllergenToken(token)) {
                allergens.add(token);
            }
        }
        return allergens;
    }

    // R1 过敏禁忌（拦截级）

    private static Set<String> extractConditions(String contraindication) {
        Set<String> conditions = new LinkedHashSet<>();
        if (!StringUtils.hasText(contraindication)) {
            return conditions;
        }
        Matcher matcher = CONDITION_CLAUSE.matcher(contraindication);
        while (matcher.find()) {
            String token = matcher.group(1).trim();
            // 过敏类表述已由 R1 处理，这里不重复报
            if (token.contains("过敏")) {
                continue;
            }
            conditions.add(token);
        }
        return conditions;
    }

    // R2 重复用药（警告级）

    private static SysDrug drugOf(DrugAuditContextDTO context, BizPrescriptionDetail detail) {
        if (detail == null || detail.getDrugId() == null || context.drugIndex() == null) {
            return null;
        }
        return context.drugIndex().get(detail.getDrugId());
    }

    // R3 同类重复（警告级）

    /**
     * 解析通用名，优先级：药品字典的通用名 → 处方明细的通用名 → 药品名称。
     * 字典是主数据，只有它缺失时才退回单据上的值。
     */
    private static String resolveGenericName(DrugAuditContextDTO context, BizPrescriptionDetail detail) {
        SysDrug drug = drugOf(context, detail);
        if (drug != null && StringUtils.hasText(drug.getGenericName())) {
            return drug.getGenericName().trim();
        }
        String fromDetail = trimToNull(detail.getGenericName());
        return fromDetail != null ? fromDetail : trimToNull(detail.getDrugName());
    }

    // R4 皮试要求（警告级）

    private static boolean isHerbalPieces(SysDrug drug) {
        return drug.getDrugType() != null && drug.getDrugType() == DRUG_TYPE_HERBAL_PIECES;
    }

    // R5 禁忌人群与诊断/既往史冲突（拦截级）

    private static DrugAuditFindingVO finding(int errorLevel, String category, String errorDetail,
                                              String suggestion, String relatedDrugs, String evidence) {
        DrugAuditFindingVO vo = new DrugAuditFindingVO();
        vo.setSource(SOURCE);
        vo.setErrorLevel(errorLevel);
        vo.setCategory(category);
        vo.setErrorDetail(errorDetail);
        vo.setSuggestion(suggestion);
        vo.setRelatedDrugs(relatedDrugs);
        vo.setEvidence(evidence);
        return vo;
    }

    // R6/R7 知识表：药物相互作用与剂量上限

    /**
     * 去重 + 排序。
     * <p>
     * 去重口径是「同一问题别用两种说法说两遍」：按类别 + 问题描述判重，
     * 保留严重程度更高的那条。医生的耐心是有限资源，重复报错会直接消耗掉它。
     */
    private static List<DrugAuditFindingVO> dedupeAndSort(List<DrugAuditFindingVO> findings) {
        Map<String, DrugAuditFindingVO> unique = new LinkedHashMap<>();
        for (DrugAuditFindingVO finding : findings) {
            String key = finding.getCategory() + "|" + finding.getErrorDetail();
            DrugAuditFindingVO exists = unique.get(key);
            if (exists == null || exists.getErrorLevel() < finding.getErrorLevel()) {
                unique.put(key, finding);
            }
        }
        List<DrugAuditFindingVO> result = new ArrayList<>(unique.values());
        result.sort(Comparator.comparingInt(DrugAuditFindingVO::getErrorLevel).reversed());
        return result.size() > MAX_FINDINGS ? new ArrayList<>(result.subList(0, MAX_FINDINGS)) : result;
    }

    private static String trimToNull(String text) {
        return StringUtils.hasText(text) ? text.trim() : null;
    }

    private static String nullToDash(String text) {
        return StringUtils.hasText(text) ? text : "-";
    }

    private static String firstNonBlank(String preferred, String fallback) {
        return StringUtils.hasText(preferred) ? preferred : fallback;
    }

    /**
     * 逐条执行全部硬规则
     */
    public List<DrugAuditFindingVO> check(DrugAuditContextDTO context) {
        List<DrugAuditFindingVO> findings = new ArrayList<>();
        if (context.details() == null || context.details().isEmpty()) {
            return findings;
        }
        checkAllergyContraindication(context, findings);
        checkDuplicateDrug(context, findings);
        checkDuplicateClass(context, findings);
        checkSkinTest(context, findings);
        checkConditionContraindication(context, findings);
        checkKnowledgeBase(context, findings);
        return dedupeAndSort(findings);
    }

    /**
     * 药品禁忌里的过敏原 ↔ 患者过敏史，做<b>双向类别展开</b>后比对。
     * 字典写「对青霉素过敏者禁用」、过敏史写「阿莫西林过敏」，必须能撞上。
     */
    private void checkAllergyContraindication(DrugAuditContextDTO context, List<DrugAuditFindingVO> findings) {
        if (!context.hasUsableAllergyText()) {
            return;
        }
        String allergyText = context.allergyText();
        for (BizPrescriptionDetail detail : context.details()) {
            SysDrug drug = drugOf(context, detail);
            if (drug == null) {
                continue;
            }
            Set<String> allergens = extractAllergens(drug.getContraindication());
            if (allergens.isEmpty()) {
                continue;
            }
            boolean reported = false;
            for (String allergen : allergens) {
                if (reported) {
                    break;
                }
                for (String token : DrugClassCatalog.expandAllergen(allergen)) {
                    if (token.length() < 2) {
                        continue;
                    }
                    if (ClinicalTextMatcher.containsAffirmed(allergyText, token)) {
                        findings.add(finding(LEVEL_BLOCK, "过敏禁忌",
                                String.format("患者过敏史提示对「%s」过敏，处方中「%s」禁忌为「%s」",
                                        token, drug.getDrugName(), drug.getContraindication()),
                                String.format("建议停用或更换「%s」；确需使用须先评估并记录知情同意",
                                        drug.getDrugName()),
                                drug.getDrugName(),
                                "药品禁忌原文：" + drug.getContraindication()));
                        reported = true;
                        break;
                    }
                }
            }
        }
    }

    /**
     * 同一药品（drugId 相同）在一张处方里出现多次，或同一通用名下开了多个不同药品。
     * <p>
     * <b>通用名以药品字典为准，不取处方明细自带的通用名</b>：
     * 实测处方明细现有 51 行的通用名全部为空，
     * 若直接依赖它，这条规则会静默失效 —— 一个不报错的空规则比没有规则更危险，
     * 因为没人知道它没在工作。
     */
    private void checkDuplicateDrug(DrugAuditContextDTO context, List<DrugAuditFindingVO> findings) {
        Map<Long, List<BizPrescriptionDetail>> byDrugId = new LinkedHashMap<>();
        Map<String, Set<Long>> genericToDrugIds = new LinkedHashMap<>();
        Map<String, Set<String>> genericToNames = new LinkedHashMap<>();

        for (BizPrescriptionDetail detail : context.details()) {
            if (detail.getDrugId() != null) {
                byDrugId.computeIfAbsent(detail.getDrugId(), key -> new ArrayList<>()).add(detail);
            }
            String generic = resolveGenericName(context, detail);
            if (generic != null) {
                genericToDrugIds.computeIfAbsent(generic, key -> new LinkedHashSet<>()).add(detail.getDrugId());
                genericToNames.computeIfAbsent(generic, key -> new LinkedHashSet<>()).add(detail.getDrugName());
            }
        }

        for (Map.Entry<Long, List<BizPrescriptionDetail>> entry : byDrugId.entrySet()) {
            List<BizPrescriptionDetail> sameDrug = entry.getValue();
            if (sameDrug.size() < 2) {
                continue;
            }
            String drugName = trimToNull(sameDrug.get(0).getDrugName());
            findings.add(finding(LEVEL_WARN, "重复用药",
                    String.format("「%s」在同一张处方中重复开具 %d 次", nullToDash(drugName), sameDrug.size()),
                    "建议合并为一条，避免重复收费与超量用药",
                    nullToDash(drugName),
                    "处方明细中 drug_id 重复出现"));
        }

        for (Map.Entry<String, Set<Long>> entry : genericToDrugIds.entrySet()) {
            Set<Long> drugIds = entry.getValue();
            // 同一 drugId 重复的情况已由上面报过，这里只关心「不同药品、同一成分」
            if (drugIds.size() < 2) {
                continue;
            }
            Set<String> names = genericToNames.get(entry.getKey());
            findings.add(finding(LEVEL_WARN, "重复用药",
                    String.format("处方中「%s」成分重复：%s", entry.getKey(), String.join("、", names)),
                    "建议只保留一种，避免同一成分叠加超量",
                    String.join("、", names),
                    "多个药品的通用名相同"));
        }
    }

    /**
     * 同一类别下开了多个不同药品，例如同时开硝苯地平与氨氯地平（都是二氢吡啶类）。
     */
    private void checkDuplicateClass(DrugAuditContextDTO context, List<DrugAuditFindingVO> findings) {
        Map<String, Set<String>> classToDrugNames = new LinkedHashMap<>();
        for (BizPrescriptionDetail detail : context.details()) {
            SysDrug drug = drugOf(context, detail);
            if (drug == null || isHerbalPieces(drug)) {
                continue;
            }
            String name = firstNonBlank(drug.getGenericName(), drug.getDrugName());
            if (!StringUtils.hasText(name)) {
                continue;
            }
            for (String className : DrugClassCatalog.classesOf(name)) {
                classToDrugNames.computeIfAbsent(className, key -> new LinkedHashSet<>())
                        .add(drug.getDrugName());
            }
        }
        for (Map.Entry<String, Set<String>> entry : classToDrugNames.entrySet()) {
            if (entry.getValue().size() < 2) {
                continue;
            }
            findings.add(finding(LEVEL_WARN, "重复用药",
                    String.format("同类药物重复使用（%s）：%s", entry.getKey(), String.join("、", entry.getValue())),
                    "同类药物联用一般无额外获益，反而增加不良反应风险，建议二选一",
                    String.join("、", entry.getValue()),
                    "药品分类比对：" + entry.getKey()));
        }
    }

    /**
     * 药品字典要求皮试，但处方明细未标记已做皮试。
     */
    private void checkSkinTest(DrugAuditContextDTO context, List<DrugAuditFindingVO> findings) {
        for (BizPrescriptionDetail detail : context.details()) {
            SysDrug drug = drugOf(context, detail);
            if (drug == null || drug.getIsSkinTest() == null || drug.getIsSkinTest() != 1) {
                continue;
            }
            if (detail.getIsSkinTest() != null && detail.getIsSkinTest() == 1) {
                continue;
            }
            findings.add(finding(LEVEL_WARN, "皮试要求",
                    String.format("「%s」按药品字典要求需做皮试，处方明细未标记皮试", drug.getDrugName()),
                    "开具前请完成皮试并在处方中标记皮试结果",
                    drug.getDrugName(),
                    "sys_drug.is_skin_test = 1"));
        }
    }

    /**
     * 药品禁忌里的疾病条件 ↔ 本次诊断与既往史，做同义词展开后比对。
     * 例：诊断为「慢性肾脏病」而处方含「严重肾功能不全者禁用」的二甲双胍。
     */
    private void checkConditionContraindication(DrugAuditContextDTO context, List<DrugAuditFindingVO> findings) {
        if (!StringUtils.hasText(context.conditionText())) {
            return;
        }
        String conditionText = context.conditionText();
        for (BizPrescriptionDetail detail : context.details()) {
            SysDrug drug = drugOf(context, detail);
            if (drug == null) {
                continue;
            }
            Set<String> conditions = extractConditions(drug.getContraindication());
            if (conditions.isEmpty()) {
                continue;
            }
            boolean reported = false;
            for (String condition : conditions) {
                if (reported) {
                    break;
                }
                for (String token : DrugClassCatalog.expandCondition(condition)) {
                    if (token.length() < 2) {
                        continue;
                    }
                    if (ClinicalTextMatcher.containsAffirmed(conditionText, token)) {
                        findings.add(finding(LEVEL_BLOCK, "禁忌人群",
                                String.format("本次诊断/既往史含「%s」，而「%s」禁忌为「%s」",
                                        token, drug.getDrugName(), drug.getContraindication()),
                                String.format("建议更换「%s」，或经评估后记录使用理由", drug.getDrugName()),
                                drug.getDrugName(),
                                "药品禁忌原文：" + drug.getContraindication()));
                        reported = true;
                        break;
                    }
                }
            }
        }
    }

    /**
     * 相互作用与剂量上限比对全部下沉到 {@link DrugRationalCheckService}（his-system），
     * 本层只做「命中 → 审核项」的翻译。绝不在这儿再写一份匹配逻辑：
     * 审方硬闸（his-emr）与 AI 审核若各判一次，就会出现「AI 说没事、点通过被拒」这种
     * 谁都没错但用户无法理解的结果。
     */
    private void checkKnowledgeBase(DrugAuditContextDTO context, List<DrugAuditFindingVO> findings) {
        List<DrugRationalItemDTO> items = new ArrayList<>();
        for (BizPrescriptionDetail detail : context.details()) {
            DrugRationalItemDTO item = new DrugRationalItemDTO();
            item.setDrugId(detail.getDrugId());
            item.setDrugName(detail.getDrugName());
            item.setGenericName(resolveGenericName(context, detail));
            item.setSpecification(detail.getSpecification());
            item.setSingleDosage(detail.getSingleDosage());
            item.setFrequency(detail.getFrequency());
            items.add(item);
        }
        for (DrugRationalHitVO hit : drugRationalCheckService.check(items)) {
            boolean interaction = HIT_INTERACTION.equals(hit.getHitType());
            String related = interaction
                    ? hit.getDrugNameA() + "、" + hit.getDrugNameB()
                    : nullToDash(hit.getDrugNameA());
            findings.add(finding(interaction && Boolean.TRUE.equals(hit.getBlocked()) ? LEVEL_BLOCK : LEVEL_WARN,
                    interaction ? "药物相互作用" : "剂量上限",
                    hit.getMessage(),
                    StringUtils.hasText(hit.getSuggestion()) ? hit.getSuggestion()
                            : (interaction ? "建议修改处方或经医师评估后记录联用理由" : "请核对极量口径后决定是否调整"),
                    related,
                    "知识库条目 " + hit.getKnowledgeId() + "（" + hit.getHitType() + "）"));
        }
    }
}

package com.his.ai.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.his.ai.constant.AiCapabilityKeys;
import com.his.ai.dto.AiCallDTO;
import com.his.ai.dto.PatientImagingExplainDTO;
import com.his.ai.dto.PatientImagingLlmOutputDTO;
import com.his.ai.entity.SysImagingPlainItem;
import com.his.ai.mapper.SysImagingPlainItemMapper;
import com.his.ai.service.AiExecutionService;
import com.his.ai.service.PatientImagingExplainCapability;
import com.his.ai.support.PatientTextGuard;
import com.his.ai.vo.PatientImagingExplainVO;
import com.his.common.enums.PositiveFlagEnum;
import com.his.common.exception.BusinessException;
import com.his.medicaltech.entity.BizReport;
import com.his.medicaltech.enums.ReportStatusEnum;
import com.his.medicaltech.mapper.BizReportMapper;
import com.his.patient.service.PatientGuardianService;
import com.his.security.CurrentUser;
import com.his.security.UserUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 患者端影像报告解读（大白话版，G-17）。
 * <p>
 * <b>为什么 P6 推翻 P1「检查报告不做解读」的边界：</b>P1 拒绝的理由是
 * 「用词典改写影像文本只会丢信息、还可能误导」——这个理由今天依然成立，
 * 所以本能力<b>仍然不碰词典改写影像文本</b>；改变的是用模型串话替代改写，
 * 且模型输出逐段过 {@link PatientTextGuard}，命中诊断/用药/绝对化表述的段落
 * 单独丢弃。NMPA 三类证红线：只解读、不下诊断结论，模型最大权限是「转述原文」。
 * <p>
 * 三层分工：
 * <ol>
 *   <li><b>事实层（代码）</b>：阴阳性、是否危急、报告是否已发布——biz_report 的列就是事实。</li>
 *   <li><b>词典层（人工维护）</b>：这项检查是查什么、检查前后的注意（可穷举 → 纪律 9，不劳模型）。</li>
 *   <li><b>表达层（模型）</b>：把描述/结论原文串成白话，只有模型能做（开放文本），
 *       所以模型不可用时这些段落缺位是设计内行为，词典与事实照常返回，绝不编白话。</li>
 * </ol>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PatientImagingExplainCapabilityImpl implements PatientImagingExplainCapability {

    private static final String TEMPLATE_NAME = "patient-imaging-explain";

    private static final String BIZ_TYPE = "report";

    /**
     * biz_report.report_type：1-检查 2-检验
     */
    private static final int REPORT_TYPE_EXAM = 1;

    /**
     * 固定免责提示——少了这句话，「肺部有条索影」就会被患者读成诊断结论。
     */
    private static final String ADVICE =
            "以上白话是把报告原文转述给你听的，没有医生逐句复核，不能代替医生的诊断。"
                    + "报告原文请以页面展示为准；有疑问请咨询接诊医生；"
                    + "若报告标注为危急，请立即联系医生或前往急诊。";

    private final BizReportMapper reportMapper;

    private final SysImagingPlainItemMapper imagingPlainItemMapper;

    private final AiExecutionService aiExecutionService;

    private final PatientTextGuard textGuard;

    private final PatientGuardianService patientGuardianService;

    @Override
    public PatientImagingExplainVO execute(PatientImagingExplainDTO dto) {
        CurrentUser user = UserUtils.getCurrentUser();
        if (user == null || user.getPatientId() == null) {
            throw new BusinessException("未获取到就诊人身份，请重新登录");
        }

        BizReport report = reportMapper.selectById(dto.getReportId());
        if (report == null) {
            throw new BusinessException("报告不存在：" + dto.getReportId());
        }
        if (patientGuardianService.patientScopeViolated(report.getPatientId())) {
            // 不区分「报告不存在」和「无权查看」，避免被用来探测报告是否存在
            throw new BusinessException("报告不存在或无权查看：" + dto.getReportId());
        }
        if (report.getReportType() == null || report.getReportType() != REPORT_TYPE_EXAM) {
            throw new BusinessException("该报告不是检查报告；检验报告请使用检验报告解读入口");
        }
        // 未发布的报告文本可能改到一半，解读它等于解读假报告
        if (!Integer.valueOf(ReportStatusEnum.PUBLISHED.getCode()).equals(report.getReportStatus())) {
            throw new BusinessException("报告尚未发布，暂时无法解读");
        }

        PatientImagingExplainVO vo = new PatientImagingExplainVO();
        vo.setReportId(String.valueOf(report.getId()));
        vo.setReportNo(report.getReportNo());
        vo.setItemName(report.getItemName());
        vo.setExamMethod(report.getExamMethod());
        vo.setReportTime(report.getPublishTime());
        vo.setPositiveText(PositiveFlagEnum.labelOf(report.getPositiveFlag()));
        // 危急是代码事实，置顶提示不经过模型——这是整个 VO 里唯一允许「催促」的字段
        if (Integer.valueOf(1).equals(report.getIsCritical())) {
            vo.setCriticalAlert("报告已由诊断医生标注为危急，请立即联系接诊医生或前往急诊。");
        }

        SysImagingPlainItem plain = matchDictionary(report);
        if (plain != null) {
            vo.setExamIntro(plain.getWhatItDoes());
            vo.setExamNotice(plain.getNoticeText());
        }

        callModel(report, plain).ifPresent(llm -> {
            // 四段独立过闸：一段越界只丢一段，不为一段越界把整份解读丢掉
            String examIntro = plain == null ? textGuard.guard(llm.getExamIntro(), AiCapabilityKeys.PATIENT_IMAGING_EXPLAIN) : null;
            if (examIntro != null) {
                vo.setExamIntro(examIntro);
            }
            String findings = textGuard.guard(llm.getFindingsPlain(), AiCapabilityKeys.PATIENT_IMAGING_EXPLAIN);
            if (findings != null) {
                vo.setFindingsPlain(findings);
            }
            String conclusions = textGuard.guard(llm.getConclusionsPlain(), AiCapabilityKeys.PATIENT_IMAGING_EXPLAIN);
            if (conclusions != null) {
                vo.setConclusionsPlain(conclusions);
            }
            String advice = textGuard.guard(llm.getAdvicePlain(), AiCapabilityKeys.PATIENT_IMAGING_EXPLAIN);
            if (advice != null) {
                vo.setAdvicePlain(advice);
            }

            boolean anyModelText = examIntro != null || findings != null || conclusions != null || advice != null;
            if (anyModelText) {
                vo.setSource("model");
                // 词典命中时 examIntro 由词典提供、模型不产出（本地变量恒 null），不算缺段
                boolean examIntroOk = plain != null || examIntro != null;
                if (!examIntroOk || findings == null || conclusions == null || advice == null) {
                    vo.setDegraded(true);
                    vo.setDegradeReason("部分白话段落未通过患者文案安全闸，已回落原文与词典");
                } else {
                    vo.setDegraded(false);
                }
            } else {
                vo.setSource("rule");
                vo.setDegraded(true);
                vo.setDegradeReason("模型输出未通过患者文案安全闸，已回落到原文与词典");
            }
        });

        if (vo.getSource() == null) {
            vo.setSource("rule");
            vo.setDegraded(true);
            String reason = aiExecutionService.degradeReasonOf(AiCapabilityKeys.PATIENT_IMAGING_EXPLAIN);
            if (plain == null) {
                reason += "；该项检查不在词典内，检查介绍暂缺";
            }
            vo.setDegradeReason(reason + "；白话串讲暂缺，请直接阅读报告原文");
        }
        vo.setAdvice(ADVICE);
        return vo;
    }

    // ---------------------------------------------------------------- 词典层

    /**
     * 关键词匹配：报告项目名包含词典关键词即命中，多个命中取关键词最长的一条
     * （「腹部彩超（肝胆胰脾肾）」同时命中「腹部彩超」与「肝胆胰脾」时，
     * 更长的关键词语义更具体）。项目名未命中再看检查方法——有的报告项目名写部位、方法在 examMethod。
     */
    private SysImagingPlainItem matchDictionary(BizReport report) {
        List<SysImagingPlainItem> all = imagingPlainItemMapper.selectList(
                new LambdaQueryWrapper<SysImagingPlainItem>()
                        .eq(SysImagingPlainItem::getStatus, 1));
        if (all == null || all.isEmpty()) {
            return null;
        }
        SysImagingPlainItem best = null;
        int bestLength = 0;
        String itemName = StringUtils.hasText(report.getItemName()) ? report.getItemName() : "";
        String examMethod = StringUtils.hasText(report.getExamMethod()) ? report.getExamMethod() : "";
        for (SysImagingPlainItem item : all) {
            if (!StringUtils.hasText(item.getItemName())) {
                continue;
            }
            String keyword = item.getItemName().trim();
            int length = keyword.length();
            if (length > bestLength && (itemName.contains(keyword) || examMethod.contains(keyword))) {
                best = item;
                bestLength = length;
            }
        }
        return best;
    }

    // ---------------------------------------------------------------- 模型层

    private Optional<PatientImagingLlmOutputDTO> callModel(BizReport report, SysImagingPlainItem plain) {
        Map<String, Object> variables = new HashMap<>();
        variables.put("itemName", nullToDash(report.getItemName()));
        variables.put("examMethod", nullToDash(report.getExamMethod()));
        variables.put("positiveText", nullToDash(PositiveFlagEnum.labelOf(report.getPositiveFlag())));
        variables.put("hasDictIntro", plain != null);
        variables.put("findings", nullToDash(report.getReportContent()));
        variables.put("conclusions", nullToDash(report.getConclusion()));
        variables.put("suggestions", nullToDash(report.getSuggestions()));

        AiCallDTO call = AiCallDTO.builder()
                .capabilityKey(AiCapabilityKeys.PATIENT_IMAGING_EXPLAIN)
                .templateName(TEMPLATE_NAME)
                .variables(variables)
                .bizType(BIZ_TYPE)
                .bizId(report.getId())
                .inputDigest(nullToDash(report.getPatientName()) + " | "
                        + nullToDash(report.getItemName()))
                .maxTokens(2048)
                .build();

        return aiExecutionService.call(call, PatientImagingLlmOutputDTO.class);
    }

    private static String nullToDash(String text) {
        return StringUtils.hasText(text) ? text : "（未填写）";
    }
}

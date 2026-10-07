package com.his.ai.service.impl;

import com.his.ai.constant.AiCapabilityKeys;
import com.his.ai.dto.AiCallDTO;
import com.his.ai.dto.EmrDraftDTO;
import com.his.ai.dto.EmrDraftLlmOutputDTO;
import com.his.ai.service.AiExecutionService;
import com.his.ai.service.EmrDraftCapability;
import com.his.ai.vo.EmrDraftPromptVariablesVO;
import com.his.ai.vo.EmrDraftResultVO;
import com.his.common.enums.SysGenderEnum;
import com.his.emr.entity.BizMedicalRecord;
import com.his.emr.mapper.BizMedicalRecordMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.*;

/**
 * 病历草拟（P1-3）：由主诉 + 查体 + 体征整理出「现病史」草稿。
 * <p>
 * <b>范围是刻意收窄的 —— 只草拟现病史</b>，理由：
 * <ol>
 *   <li><b>不生成诊断</b>。诊断权在医生，这是执业边界，不是技术问题。</li>
 *   <li><b>不生成处理意见 / 医嘱</b>。医嘱是有法律效力的医疗文书，责任在开立者；
 *       一份"一键采纳"的 AI 医嘱会让责任归属糊掉。真要做也应该是
 *       「按诊断推荐科室已维护的医嘱组套」——来源可解释、可追溯，
 *       而不是让模型自由生成。</li>
 * </ol>
 * 现病史则不同：它是<b>对已有问诊信息的组织与表述</b>，不引入新的诊疗决策，
 * 且必须由医生逐字过目后才进病历 —— 这是模型能安全介入的部分。
 * <p>
 * <b>入参为什么允许带业务内容</b>：现病史是保存病历的必填项，而草拟产出的正是现病史 ——
 * 只认 recordId 会死锁（病历还没保存就没有 id）。所以按
 * 「请求里填了的字段优先，没填的回落库内病历」合并，见 {@link #mergeInput}。
 * <p>
 * <b>草稿不落库</b>：草稿一旦落库就成了病历正文，等于让模型的推测进入法律文书。
 * 医生点「填入」后写进表单，保存病历时才由病历保存流程落库，责任链条清晰。
 * <p>
 * <b>降级时诚实返回空</b>：现病史没有"纯规则"版本可写，所以模型不可用时
 * 输出空草稿 + 明确原因，而不是拼一段模板文字糊弄医生。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EmrDraftCapabilityImpl implements EmrDraftCapability {

    private static final String TEMPLATE_NAME = "emr-draft";

    private static final String BIZ_TYPE = "medical_record";

    private static final int MAX_PRESENT_ILLNESS_LENGTH = 400;

    private static final int MAX_MISSING_POINTS = 4;

    private static final int MAX_MISSING_POINT_LENGTH = 20;

    private static final int MAX_SUMMARY_LENGTH = 80;

    private static final int OUTPUT_TOKEN_LIMIT = 1536;

    /**
     * 模型偶尔会把字段名一起写进正文
     */
    private static final String PRESENT_ILLNESS_LABEL = "现病史";

    private final BizMedicalRecordMapper medicalRecordMapper;

    private final AiExecutionService aiExecutionService;

    private static String pick(String fromRequest, String fromDb) {
        return StringUtils.hasText(fromRequest) ? fromRequest.trim() : fromDb;
    }

    private static Integer pick(Integer fromRequest, Integer fromDb) {
        return fromRequest != null ? fromRequest : fromDb;
    }

    /**
     * 净化现病史正文：去掉模型可能带上的字段名前缀与 markdown 标记。
     * <p>
     * 不做"顺句"或补全 —— 模型产生的内容保持原样，由医生自行修改。
     */
    private static String cleanPresentIllness(String text) {
        if (!StringUtils.hasText(text)) {
            return "";
        }
        String value = text.replace("\r\n", "\n").trim();
        value = value.replaceAll("(?m)^\\s*#+\\s*", "")
                .replaceAll("(?m)^\\s*[-*]\\s+", "")
                .replaceAll("\\*\\*", "")
                .trim();
        if (value.startsWith(PRESENT_ILLNESS_LABEL)) {
            value = value.substring(PRESENT_ILLNESS_LABEL.length());
            while (value.startsWith("：") || value.startsWith(":") || value.startsWith(" ")) {
                value = value.substring(1);
            }
        }
        return truncate(value, MAX_PRESENT_ILLNESS_LENGTH, "");
    }

    private static List<String> cleanMissingPoints(List<String> points) {
        List<String> result = new ArrayList<>();
        if (points == null) {
            return result;
        }
        for (String point : points) {
            if (result.size() >= MAX_MISSING_POINTS) {
                break;
            }
            String value = truncate(point, MAX_MISSING_POINT_LENGTH, "");
            if (StringUtils.hasText(value) && !result.contains(value)) {
                result.add(value);
            }
        }
        return result;
    }

    private static String bloodPressureText(DraftInput input) {
        if (!StringUtils.hasText(input.systolicPressure()) && !StringUtils.hasText(input.diastolicPressure())) {
            return "（未填写）";
        }
        return nullToDash(input.systolicPressure()) + "/" + nullToDash(input.diastolicPressure()) + " mmHg";
    }

    private static String unitOrDash(String value, String unit) {
        return StringUtils.hasText(value) ? value + unit : "（未填写）";
    }

    private static String nullToDash(String text) {
        return StringUtils.hasText(text) ? text : "（未填写）";
    }

    private static String truncate(String text, int maxLength, String fallback) {
        if (!StringUtils.hasText(text)) {
            return fallback;
        }
        String value = text.trim();
        return value.length() <= maxLength ? value : value.substring(0, maxLength);
    }

    public EmrDraftResultVO execute(EmrDraftDTO dto) {
        long start = System.currentTimeMillis();

        DraftInput input = mergeInput(dto);
        EmrDraftResultVO vo = new EmrDraftResultVO();

        // 主诉都没有就草拟现病史，模型只能靠猜 —— 与其烧一轮 token 换来一段编造的文字，
        // 不如直接说清楚缺什么。这条短路规则也是防止幻觉最省事的一道。
        if (!StringUtils.hasText(input.chiefComplaint())) {
            vo.setDegraded(true);
            vo.setDegradeReason("主诉尚未填写，未调用模型；请先填写主诉");
            vo.setSummary("缺少主诉，无法草拟现病史。");
            vo.setLatencyMs(System.currentTimeMillis() - start);
            return vo;
        }

        Optional<EmrDraftLlmOutputDTO> output = callModel(dto, input);
        if (output.isEmpty()) {
            vo.setDegraded(true);
            vo.setDegradeReason(aiExecutionService.degradeReasonOf(AiCapabilityKeys.EMR_DRAFT)
                    + "；本次未能生成草稿，请手工书写");
            vo.setSummary("大模型未参与，本次没有草稿可给。");
            vo.setLatencyMs(System.currentTimeMillis() - start);
            return vo;
        }

        EmrDraftLlmOutputDTO llmOutput = output.get();
        vo.setPresentIllness(cleanPresentIllness(llmOutput.getPresentIllness()));
        vo.setMissingPoints(cleanMissingPoints(llmOutput.getMissingPoints()));
        vo.setSummary(truncate(llmOutput.getSummary(), MAX_SUMMARY_LENGTH, ""));

        if (!StringUtils.hasText(vo.getPresentIllness())) {
            vo.setSummary(StringUtils.hasText(vo.getSummary())
                    ? vo.getSummary()
                    : "现有信息不足以成文，请参照下方待补项补充后再试。");
        }

        vo.setLatencyMs(System.currentTimeMillis() - start);
        return vo;
    }

    /**
     * 合并「请求里填的内容」与「库内病历」：请求优先，库内补缺。
     * <p>
     * 请求优先是有意的 —— 医生此刻刚填进表单的内容才是最新的，
     * 库里的可能是几分钟前保存的旧值。
     */
    private DraftInput mergeInput(EmrDraftDTO dto) {
        BizMedicalRecord record = dto.getRecordId() == null
                ? null
                : medicalRecordMapper.selectById(dto.getRecordId());
        return new DraftInput(
                pick(dto.getGender(), record == null ? null : record.getGender()),
                pick(dto.getAge(), record == null ? null : record.getAge()),
                pick(dto.getChiefComplaint(), record == null ? null : record.getChiefComplaint()),
                pick(dto.getPresentIllness(), record == null ? null : record.getPresentIllness()),
                pick(dto.getPastHistory(), record == null ? null : record.getPastHistory()),
                pick(dto.getAllergyHistory(), record == null ? null : record.getAllergyHistory()),
                pick(dto.getTemperature(), record == null ? null : record.getTemperature()),
                pick(dto.getPulse(), record == null ? null : record.getPulse()),
                pick(dto.getRespiration(), record == null ? null : record.getRespiration()),
                pick(dto.getSystolicPressure(), record == null ? null : record.getSystolicPressure()),
                pick(dto.getDiastolicPressure(), record == null ? null : record.getDiastolicPressure()),
                pick(dto.getGeneralCondition(), record == null ? null : record.getGeneralCondition()),
                pick(dto.getSkinMucosa(), record == null ? null : record.getSkinMucosa()),
                pick(dto.getHeadNeck(), record == null ? null : record.getHeadNeck()),
                pick(dto.getChestLung(), record == null ? null : record.getChestLung()),
                pick(dto.getHeart(), record == null ? null : record.getHeart()),
                pick(dto.getAbdomen(), record == null ? null : record.getAbdomen()),
                pick(dto.getSpineLimbs(), record == null ? null : record.getSpineLimbs()),
                pick(dto.getNervousSystem(), record == null ? null : record.getNervousSystem()),
                pick(dto.getSpecialistExam(), record == null ? null : record.getSpecialistExam()),
                pick(dto.getAuxiliaryExam(), record == null ? null : record.getAuxiliaryExam()));
    }

    private Optional<EmrDraftLlmOutputDTO> callModel(EmrDraftDTO dto, DraftInput input) {
        EmrDraftPromptVariablesVO variables = new EmrDraftPromptVariablesVO();
        variables.setGender(SysGenderEnum.getText(input.gender()));
        variables.setAge(input.age() == null ? "（未填写）" : input.age() + "岁");
        variables.setChiefComplaint(nullToDash(input.chiefComplaint()));
        variables.setPresentIllness(nullToDash(input.presentIllness()));
        variables.setPastHistory(nullToDash(input.pastHistory()));
        variables.setAllergyHistory(nullToDash(input.allergyHistory()));
        variables.setTemperature(unitOrDash(input.temperature(), "℃"));
        variables.setPulse(unitOrDash(input.pulse(), "次/分"));
        variables.setRespiration(unitOrDash(input.respiration(), "次/分"));
        variables.setBloodPressure(bloodPressureText(input));
        variables.setGeneralCondition(nullToDash(input.generalCondition()));
        variables.setSkinMucosa(nullToDash(input.skinMucosa()));
        variables.setHeadNeck(nullToDash(input.headNeck()));
        variables.setChestLung(nullToDash(input.chestLung()));
        variables.setHeart(nullToDash(input.heart()));
        variables.setAbdomen(nullToDash(input.abdomen()));
        variables.setSpineLimbs(nullToDash(input.spineLimbs()));
        variables.setNervousSystem(nullToDash(input.nervousSystem()));
        variables.setSpecialistExam(nullToDash(input.specialistExam()));
        variables.setAuxiliaryExam(nullToDash(input.auxiliaryExam()));

        AiCallDTO call = AiCallDTO.builder()
                .capabilityKey(AiCapabilityKeys.EMR_DRAFT)
                .templateName(TEMPLATE_NAME)
                .variables(variables)
                .bizType(BIZ_TYPE)
                .bizId(dto.getRecordId())
                // 只把主诉作为摘要 —— 病历全文没必要为了审计再存一遍
                .inputDigest(nullToDash(input.chiefComplaint()))
                .maxTokens(OUTPUT_TOKEN_LIMIT)
                .build();

        return aiExecutionService.call(call, EmrDraftLlmOutputDTO.class);
    }

    /**
     * 合并后的草拟依据
     */
    private record DraftInput(Integer gender,
                              Integer age,
                              String chiefComplaint,
                              String presentIllness,
                              String pastHistory,
                              String allergyHistory,
                              String temperature,
                              String pulse,
                              String respiration,
                              String systolicPressure,
                              String diastolicPressure,
                              String generalCondition,
                              String skinMucosa,
                              String headNeck,
                              String chestLung,
                              String heart,
                              String abdomen,
                              String spineLimbs,
                              String nervousSystem,
                              String specialistExam,
                              String auxiliaryExam) {
    }
}

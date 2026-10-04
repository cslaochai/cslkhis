package com.his.ai.service;

import com.his.ai.dto.PatientTriageNormalizeDTO;
import com.his.ai.vo.PatientTriageNormalizeVO;

/**
 * 患者端导诊口语归一。
 *
 * <p>把"脑袋昏昏沉沉的还想吐"整理成"头晕、恶心"，让规则表能命中。
 * <b>它不推荐科室</b> —— 推荐始终由 {@code biz_triage_rule} 的关键词给出，
 * 模型只提供更好的检索文本和几句追问。
 */
public interface PatientTriageNormalizeCapability {

    /**
     * 归一一句口语主诉。
     *
     * @param dto 患者原话
     * @return 归一结果；模型不可用仅意味着"用原话查"，不影响导诊可用
     */
    PatientTriageNormalizeVO execute(PatientTriageNormalizeDTO dto);
}

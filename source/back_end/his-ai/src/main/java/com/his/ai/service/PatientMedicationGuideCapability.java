package com.his.ai.service;

import com.his.ai.dto.PatientMedicationGuideDTO;
import com.his.ai.vo.PatientMedicationGuideVO;

/**
 * 患者端用药说明（这盒药怎么吃）。
 *
 * <p>面向患者本人：把处方上的「每次 1 袋 / 每日 3 次 / 口服 / 7 天」翻译成人话，
 * 并补上代码能判定的客观注意事项（皮试、冷藏、抗菌药需服完疗程、漏服怎么办）。
 * <b>不回答"这药治什么""能不能换一种药"</b> —— 那是医生和药师的活。
 */
public interface PatientMedicationGuideCapability {

    /**
     * 生成一张处方的用药说明。
     *
     * @param dto 处方ID（患者身份服务端取）
     * @return 用药说明
     */
    PatientMedicationGuideVO execute(PatientMedicationGuideDTO dto);
}

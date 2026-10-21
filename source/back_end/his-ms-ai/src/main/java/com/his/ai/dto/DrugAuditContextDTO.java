package com.his.ai.dto;

import com.his.common.util.TextUtil;
import com.his.spi.vo.AiMedicalRecordContentVO;
import com.his.spi.vo.AiPrescriptionBriefVO;
import com.his.spi.vo.AiPrescriptionDetailRowVO;
import com.his.spi.vo.DrugBriefVO;

import java.util.List;
import java.util.Map;

/**
 * 处方审核的输入上下文。
 */
public record DrugAuditContextDTO(AiPrescriptionBriefVO prescription,
                                  List<AiPrescriptionDetailRowVO> details,
                                  Map<Long, DrugBriefVO> drugIndex,
                                  AiMedicalRecordContentVO record,
                                  String allergyText,
                                  String conditionText) {

    /**
     * 过敏史是否具备可比对的内容。全是「无」「未见」这类占位词时视为无过敏史。
     */
    public boolean hasUsableAllergyText() {
        return TextUtil.hasText(allergyText);
    }

    /**
     * 性别，处方表为空时回落到病历表
     */
    public Integer gender() {
        if (prescription != null && prescription.getGender() != null) {
            return prescription.getGender();
        }
        return record == null ? null : record.getGender();
    }

    /**
     * 年龄，处方表为空时回落到病历表
     */
    public Integer age() {
        if (prescription != null && prescription.getAge() != null) {
            return prescription.getAge();
        }
        return record == null ? null : record.getAge();
    }
}

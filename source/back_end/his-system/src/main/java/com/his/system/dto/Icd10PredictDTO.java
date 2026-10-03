package com.his.system.dto;

import lombok.Data;

/**
 * ICD-10智能预测入参
 */
@Data
public class Icd10PredictDTO {

    /**
     * 主诉
     */
    private String chiefComplaint;

    /**
     * 现病史
     */
    private String presentIllness;

    /**
     * 专科检查
     */
    private String specialistExam;

    /**
     * 诊断
     */
    private String diagnosis;
}

package com.his.patient.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 病区×班次窗的护理事实聚合（交接班摘要的数据面）。
 * <p><b>只聚事实不判异常</b>：体征是否越阈、评估是否算高风险由消费方（AI 能力层）按自己的口径判，
 * 本聚合不做业务判定，避免同一套阈值散落两处。</p>
 */
@Data
public class WardNursingFactsVO implements Serializable {

    /**
     * 病区ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long wardId;

    /**
     * 病区名称
     */
    private String wardName;

    /**
     * 班次（1-白班 2-小夜班 3-大夜班）
     */
    private Integer shift;

    /**
     * 班次文案
     */
    private String shiftText;

    /**
     * 窗起始（含）
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime windowBegin;

    /**
     * 窗结束（不含）
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime windowEnd;

    /**
     * 在院/新入/出院统计
     */
    private Census census = new Census();

    /**
     * 窗内护理文书行（含体征与记录正文）
     */
    private List<NursingVitalFactVO> vitalRows = new ArrayList<>();

    /**
     * 窗内护理评估单
     */
    private List<NursingAssessmentVO> assessmentRows = new ArrayList<>();

    @Data
    public static class Census implements Serializable {

        /**
         * 当前在院人数（该病区 admit_status=1）
         */
        private int inHospitalCount;

        /**
         * 窗内出院人数
         */
        private int dischargeCount;

        /**
         * 窗内新入院名单
         */
        private List<AdmissionBrief> newAdmissions = new ArrayList<>();
    }

    @Data
    public static class AdmissionBrief implements Serializable {

        /**
         * 入院ID
         */
        @JsonSerialize(using = ToStringSerializer.class)
        private Long admissionId;

        /**
         * 患者姓名
         */
        private String patientName;

        /**
         * 床号（快照）
         */
        private String bedNo;

        /**
         * 入院时间
         */
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime admitTime;

        /**
         * 入院诊断名称
         */
        private String admitDiagnosisName;
    }
}

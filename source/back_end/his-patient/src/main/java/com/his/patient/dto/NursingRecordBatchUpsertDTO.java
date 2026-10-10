package com.his.patient.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 体温单批量录入 DTO：**一次测量动作 × 多个在院患者**（护士拿体温计挨床测的真实场景）。
 */
@Data
public class NursingRecordBatchUpsertDTO {

    /**
     * 本批测量的统一时点（三测单按时点唯一）
     */
    @NotNull(message = "测量时间不能为空（同批 = 同一时点）")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime measureTime;

    /**
     * 班次：1-白班 2-小夜班 3-大夜班（可空）
     */
    private Integer shift;

    @NotEmpty(message = "至少要有一行测量数据")
    @Valid
    private List<BatchRow> rows;

    @Data
    public static class BatchRow {

        /**
         * 入院ID
         */
        @NotNull(message = "入院ID不能为空")
        private Long admissionId;

        /**
         * 体温（℃）
         */
        private java.math.BigDecimal temperature;
        /**
         * 脉搏（次/分）
         */
        private Integer pulse;
        /**
         * 呼吸（次/分）
         */
        private Integer respiration;
        /**
         * 收缩压（mmHg）
         */
        private Integer systolicPressure;
        /**
         * 舒张压（mmHg）
         */
        private Integer diastolicPressure;
        /**
         * 血氧饱和度（%）
         */
        private Integer spo2;

        /**
         * 大便次数（次/日）
         */
        private Integer stoolCount;
        /**
         * 尿量（ml）
         */
        private Integer urineVolume;
        /**
         * 入量（ml）
         */
        private Integer intakeVolume;
        /**
         * 出量（ml）
         */
        private Integer outputVolume;

        /**
         * 备注
         */
        private String remark;
    }
}

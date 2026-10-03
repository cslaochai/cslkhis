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
 *
 * <p>语义约束：
 * <ul>
 *   <li>同一批 rows 里不允许重复 admissionId —— 同一次测量同一个人只能有一条；</li>
 *   <li>整体事务：任何一个患者的行校验不过（缺体征值 / 唯一时点冲突 / 非在院），
 *       整批回滚并指出第一个问题行 —— 批量录入要么是「这次测量」的完整结果，要么不是。</li>
 * </ul>
 */
@Data
public class NursingRecordBatchUpsertDTO {

    /** 本批测量的统一时点（三测单按时点唯一） */
    @NotNull(message = "测量时间不能为空（同批 = 同一时点）")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime measureTime;

    /** 班次：1-白班 2-小夜班 3-大夜班（可空） */
    private Integer shift;

    @NotEmpty(message = "至少要有一行测量数据")
    @Valid
    private List<BatchRow> rows;

    @Data
    public static class BatchRow {

        /** 入院ID */
        @NotNull(message = "入院ID不能为空")
        private Long admissionId;

        /** 体温（℃） */
        private java.math.BigDecimal temperature;
        /** 脉搏（次/分） */
        private Integer pulse;
        /** 呼吸（次/分） */
        private Integer respiration;
        /** 收缩压（mmHg） */
        private Integer systolicPressure;
        /** 舒张压（mmHg） */
        private Integer diastolicPressure;
        /** 血氧饱和度（%） */
        private Integer spo2;

        /** 大便次数（次/日） */
        private Integer stoolCount;
        /** 尿量（ml） */
        private Integer urineVolume;
        /** 入量（ml） */
        private Integer intakeVolume;
        /** 出量（ml） */
        private Integer outputVolume;

        /** 备注 */
        private String remark;
    }
}

package com.his.medicaltech.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * 主要诊断顺位（{@code StatReportAggMapper#topDiagnoses} 一行）。
 *
 * <p>编码与名称<b>成对取</b>：主要诊断取数是"出院记录(7) 优先、入院记录(1) 兜底、
 * 都没有则回 admission 表快照"三级降级（SQL 里 {@code DX_JOIN}）。只取编码会得到
 * 一串无法解读的符号，只取名称则同名诊断会被合并掉，两列一起才认得出是哪一种病。
 */
@Data
public class StatTopDiagnosisRowVO implements Serializable {

    /**
     * ICD 编码（未编码时为「未编码」）
     */
    private String diagnosisCode;

    /**
     * 诊断名称（未填写时为「未填」）
     */
    private String diagnosisName;

    /**
     * 例数
     */
    private Long cnt;
}
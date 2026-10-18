package com.his.emr.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/** 单病种自动入径统计：一次扫描任务纳入多少例、跳过多少例的结果出参。 */
@Data
public class SingleDiseaseAutoEnrollStatVO implements Serializable {

    /** 本次扫描出的候选入院记录数 */
    private Integer scanned;

    /** 成功自动纳入单病种管理的例数 */
    private Integer enrolled;

    /**
     * 跳过原因清单（admissionId:原因），非字段缺失
     */
    private List<String> skipped;
}

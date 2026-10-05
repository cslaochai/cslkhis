package com.his.ai.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.his.ai.support.DeteriorationScoreRules;
import lombok.Data;

import java.util.List;

/**
 * 病区危重预警扫描行（G-12）。
 * <p>wardScan 是<b>纯代码评分，无模型调用、无 degraded 语义、不出审计行</b>（同 G-08 裁撤口径）；
 * 每行是「一个在院患者的最新体征 + MEWS 评分」。无体征数据的患者不出现在结果里。</p>
 */
@Data
public class DeteriorationScanVO {

    /** 入院ID */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long admissionId;

    /** 患者姓名 */
    private String patientName;

    /** 床号 */
    private String bedNo;

    /** 测量时间（yyyy-MM-dd HH:mm:ss，该患者窗内最新一次体征） */
    private String measureTime;

    /** 分项明细（未测量的项不出现） */
    private List<DeteriorationScoreRules.ScoreItem> items;

    /** MEWS+SpO2 总分（-1 无数据，理论上扫描行不会出现） */
    private Integer totalScore;

    /** 预警级：0-未触发 1-关注 2-高危 */
    private Integer alertLevel;

    /** 预警级文案 */
    private String alertText;
}

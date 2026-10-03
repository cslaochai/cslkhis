package com.his.emr.dto;

import com.his.common.base.PageParam;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 待质控病历候选查询入参。
 *
 * <p>门诊病历与住院文书是两张结构完全不同的表，凑成一张列表要 UNION ALL，
 * 而 UNION 之后的字符串列很容易踩排序规则的坑。所以这里**要求先选来源**：
 * 质控员的实际工作流本来就是"今天质控住院病案 / 今天质控门诊病历"，不混着看。
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class QcCandidateQueryPageDTO extends PageParam {

    /**
     * 病历来源（OUTPATIENT / INPATIENT）；为空按 OUTPATIENT 处理
     */
    private String recordSource;

    /**
     * 病历状态（住院：1-草稿 2-已提交 3-已归档）
     */
    private Integer recordStatus;

    /**
     * 住院文书类型（门诊忽略）
     */
    private Integer recordType;

    /**
     * 是否只看未质控过的病历
     */
    private Boolean onlyUnQced;

    /**
     * 关键词：病历号 / 患者姓名
     */
    private String keyword;
}

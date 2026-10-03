package com.his.patient.dto;

import com.his.common.base.PageParam;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * 住院会诊分页查询入参（命名遵循 AGENTS.md：分页查询用 `xxxQueryPageDTO`）。
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ConsultationQueryPageDTO extends PageParam implements Serializable {

    /**
     * 入院ID
     */
    private Long admissionId;

    /**
     * 患者ID
     */
    private Long patientId;

    /**
     * 申请科室ID
     */
    private Long fromDeptId;

    /**
     * 会诊科室ID（会诊科室的工作台按这个筛）
     */
    private Long toDeptId;

    /**
     * 会诊状态：0-待应答 1-已完成 2-已取消 3-已应答
     */
    private Integer consultStatus;

    /**
     * 会诊范围：1-科内 2-科间 3-全院
     */
    private Integer consultType;

    /**
     * 会诊类别：1-普通科间 2-营养 3-药学 4-其他专科（营养会诊工作台按 2 收口）
     */
    private Integer consultCategory;

    /**
     * 是否急会诊：0-普通 1-急会诊
     */
    private Integer isUrgent;

    /**
     * 关键字（会诊号 / 患者姓名 / 患者号 / 会诊理由）
     */
    private String keyword;

    /**
     * 只看未完成（0-待应答 3-已应答）：1-是（会诊工作台默认视图）
     */
    private Integer unfinishedOnly;
}

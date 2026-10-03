package com.his.medicaltech.dto;

import com.his.common.base.PageParam;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 标本查询入参
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class SpecimenQueryDTO extends PageParam {
    /**
     * 患者ID（可选）
     */
    private Long patientId;

    /** 记录状态（1-已登记 2-已签到 3-检查中 4-已出结果 5-已审核 6-已发布 7-已取消） */
    private Integer recordStatus;

    /**
     * 关键字（模糊匹配：患者姓名 / 患者号 / 检验记录号）
     */
    private String keyword;
}

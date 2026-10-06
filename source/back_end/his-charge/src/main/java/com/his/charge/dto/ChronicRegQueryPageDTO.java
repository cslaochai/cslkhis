package com.his.charge.dto;

import com.his.common.base.PageParam;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 慢特病备案台账分页查询。
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ChronicRegQueryPageDTO extends PageParam {

    /**
     * 状态（1-有效 2-已注销 3-已驳回；空=全部）
     */
    private Integer regStatus;

    /**
     * 病种类别（1-慢性 2-特殊；空=全部）
     */
    private Integer diseaseType;

    /**
     * 病种目录ID
     */
    private Long catalogId;

    /**
     * 患者ID（医生站按患者下钻其门特资格时用）
     */
    private Long patientId;

    /**
     * 只看「有效但已过期」（提示续备的名单）
     */
    private Boolean onlyExpired;

    /**
     * 关键字（备案单号/患者姓名/患者编号/病种名称/经办人模糊）
     */
    private String keyword;
}

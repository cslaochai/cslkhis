package com.his.pharmacy.dto;

import com.his.common.base.PageParam;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 发药单分页查询入参
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class DispensingQueryPageDTO extends PageParam {
    /**
     * 患者ID
     */
    private Long patientId;
    /**
     * 患者姓名（模糊）
     */
    private String patientName;
    /**
     * 处方号（模糊）
     */
    private String prescriptionNo;
    /**
     * 发药状态（1-待发药 2-已发药 3-已取药 4-已退药）
     */
    private Integer dispensingStatus;
}

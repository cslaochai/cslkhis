package com.his.emr.dto;

import com.his.common.base.PageParam;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 发药记录查询入参
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class DispensingQueryPageDTO extends PageParam {

    /**
     * 患者ID
     */
    private Long patientId;

    /**
     * 发药状态：1-待发药 2-已发药 3-已退药
     */
    private Integer dispensingStatus;
}

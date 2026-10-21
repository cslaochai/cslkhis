package com.his.spi.dto;

import com.his.common.base.PageParam;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 发药窗口主列表分页查询入参：按患者、处方号与发药状态筛选（pageNum/pageSize 取分页基类）。
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class DispensingPageQueryDTO extends PageParam {

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
     * 发药状态（1-待发药 2-已发药 3-已取药 4-已退药；空 = 不限）
     */
    private Integer dispensingStatus;
}

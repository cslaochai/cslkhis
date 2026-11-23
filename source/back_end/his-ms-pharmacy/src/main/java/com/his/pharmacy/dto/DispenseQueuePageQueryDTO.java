package com.his.pharmacy.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.his.common.base.PageParam;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;

/**
 * 取药队列分页查询入参
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class DispenseQueuePageQueryDTO extends PageParam {

    /**
     * 取药日期（不传按当天）
     */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate queueDate;

    /**
     * 队列状态（1-待取药 2-已发药 3-已过号 4-已取消）
     */
    private Integer queueStatus;

    /**
     * 发药窗口ID
     */
    private Long windowId;

    /**
     * 患者姓名（模糊）
     */
    private String patientName;

    /**
     * 处方号（模糊）
     */
    private String prescriptionNo;
}

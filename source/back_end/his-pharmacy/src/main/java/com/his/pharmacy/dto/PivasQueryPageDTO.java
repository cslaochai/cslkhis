package com.his.pharmacy.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.his.common.base.PageParam;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.time.LocalDate;

/**
 * 静配主单分页查询入参（listPage 为 POST）。
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class PivasQueryPageDTO extends PageParam implements Serializable {

    /** 病区ID */
    private Long wardId;

    /**
     * 调配日期
     */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate admixDate;

    /**
     * 患者姓名（模糊）
     */
    private String patientName;

    /**
     * 主单状态：1-待审方 2-待排队 3-待调配 4-待核对 5-已完成 6-全拒配
     */
    private Integer status;
}

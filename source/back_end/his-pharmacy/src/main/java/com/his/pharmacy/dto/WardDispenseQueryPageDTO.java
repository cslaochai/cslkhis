package com.his.pharmacy.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.his.common.base.PageParam;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.time.LocalDate;

/**
 * 住院摆药单分页查询入参（listPage 为 POST）。
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class WardDispenseQueryPageDTO extends PageParam implements Serializable {

    /** 病区ID（快照） */
    private Long wardId;

    /**
     * 摆药日期
     */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate dispenseDate;

    /**
     * 患者姓名（模糊）
     */
    private String patientName;

    /**
     * 主单状态：1-待配药 2-配药中 3-已配药 4-已核对 5-已退药
     */
    private Integer status;
}

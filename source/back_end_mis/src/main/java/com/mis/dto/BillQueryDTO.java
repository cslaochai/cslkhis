package com.mis.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 结算单分页查询
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class BillQueryDTO extends BizPageParam {

    private String billNo;
    private String patientName;
    private String insuranceType;
    private String settleType;
    private String status;
}

package com.his.appoint.dto;

import lombok.Data;

/**
 * 查询医生接诊状态入参
 */
@Data
public class DoctorStatusQueryDTO {

    /**
     * 医生ID
     */
    private Long doctorId;
}

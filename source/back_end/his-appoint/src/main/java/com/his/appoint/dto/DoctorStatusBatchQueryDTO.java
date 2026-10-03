package com.his.appoint.dto;

import lombok.Data;

import java.util.List;

/**
 * 批量查询医生接诊状态入参
 */
@Data
public class DoctorStatusBatchQueryDTO {

    /**
     * 医生ID列表
     */
    private List<Long> doctorIds;
}

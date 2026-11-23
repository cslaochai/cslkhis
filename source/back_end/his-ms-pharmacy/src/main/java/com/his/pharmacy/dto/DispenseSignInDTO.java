package com.his.pharmacy.dto;

import lombok.Data;

/**
 * 取药签到入参
 */
@Data
public class DispenseSignInDTO {

    /**
     * 处方ID（处方签到与身份证签到二选一）
     */
    private Long prescriptionId;

    /**
     * 处方号（扫处方单上的号签到时传，服务端反查处方ID）
     */
    private String prescriptionNo;

    /**
     * 患者ID（按身份证/就诊卡签到时必填）
     */
    private Long patientId;

    /**
     * 签到方式（1-身份证 2-就诊卡 3-扫码 4-手工）
     */
    private Integer signInType;
}

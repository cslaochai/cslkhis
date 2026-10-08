package com.his.patient.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDate;

/**
 * 档案合并审计快照（对应 PatientIndexServiceImpl#snapshot）。
 */
@Data
public class PatientMergeSnapshotVO implements Serializable {

    /**
     * 档案ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    /**
     * 患者编号
     */
    private String patientNo;

    /**
     * 患者姓名
     */
    private String patientName;

    /**
     * 性别码（1-男 2-女 9-未知）
     */
    private Integer gender;

    /**
     * 出生日期
     */
    private LocalDate birthDate;

    /**
     * 身份证号
     */
    private String idCard;

    /**
     * 手机号
     */
    private String phone;

    /**
     * 在册状态：0-停用 1-启用。撤销合并时按它还原，不能默认启用。
     */
    private Integer status;

    /**
     * 主索引状态（0-正常 1-已并入主档）
     */
    private Integer mergeStatus;
}

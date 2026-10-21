package com.his.patient.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.io.Serializable;

/**
 * 同一主档下的其他档案（详情页「这一串是一个人」列表，对应 PatientIndexServiceImpl#briefOf）。
 */
@Data
public class PatientSiblingVO implements Serializable {

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
     * 性别文案（口径见 SysGenderEnum.getText，null→"未知"，脏值报"未知(0)"而不是"女"）
     */
    private String genderText;

    /**
     * 身份证号
     */
    private String idCard;

    /**
     * 手机号
     */
    private String phone;

    /**
     * 0-正常 1-已并入主档
     */
    private Integer mergeStatus;

    /**
     * 并入时间
     */
    private String mergeTime;
}

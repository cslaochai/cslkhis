package com.mis.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 参保人新增/同步入参（管理端，也用于把 HIS 患者同步成参保人）
 */
@Data
public class PersonSaveDTO {

    @NotBlank(message = "身份证号不能为空")
    private String idCard;
    private String insuranceNo;
    @NotBlank(message = "姓名不能为空")
    private String name;
    private String gender;
    /** 险种（2-城镇职工医保 3-城乡居民医保） */
    private String insuranceType;
    private Integer personStatus;
    private BigDecimal accountBalance;
    private String region;
}

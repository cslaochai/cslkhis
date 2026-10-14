package com.mis.dto;

import lombok.Data;

/**
 * 参保人查询入参（身份证 / 医保卡号 / 姓名 任一组合）
 */
@Data
public class PersonQueryDTO {

    private String idCard;
    private String insuranceNo;
    private String name;
}

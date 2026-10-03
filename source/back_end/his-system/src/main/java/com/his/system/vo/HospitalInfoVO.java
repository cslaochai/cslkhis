package com.his.system.vo;

import lombok.Data;

/**
 * 医院基础信息配置 VO（来源系统参数键值对）
 */
@Data
public class HospitalInfoVO {

    private String hospitalName;

    private String hospitalAddress;

    private String hospitalPhone;

    private String hospitalEmail;
}

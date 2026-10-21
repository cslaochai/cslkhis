package com.his.platform.vo;

import lombok.Data;

/**
 * 医院基础信息配置 VO（来源系统参数键值对）
 */
@Data
public class HospitalInfoVO {

    /** 手术医院 */
    private String hospitalName;

    /** 医院地址：打印与对外展示的院区地址 */
    private String hospitalAddress;

    /** 医院总机联系电话 */
    private String hospitalPhone;

    /** 医院对外邮箱 */
    private String hospitalEmail;
}

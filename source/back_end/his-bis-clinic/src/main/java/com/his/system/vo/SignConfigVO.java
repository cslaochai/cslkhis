package com.his.system.vo;

import lombok.Data;

/**
 * 电子签名 / 可信时间戳参数配置 VO（来源 sys_config：sign.* 键）
 */
@Data
public class SignConfigVO {

    private Integer timeSource;

    private Integer certValidDays;

    private Integer certAutoIssue;
}

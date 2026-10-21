package com.his.platform.vo;

import lombok.Data;

/**
 * 电子签名 / 可信时间戳参数配置：系统参数中签名相关键的回显与保存出参。
 */
@Data
public class SignConfigVO {

    /** 时间来源（1-本机时钟 2-院内授时服务器 3-第三方可信时间戳） */
    private Integer timeSource;

    /** 新签证书默认有效期（天） */
    private Integer certValidDays;

    /** 缺证书时是否自动签发（0-关闭，非 0-开启） */
    private Integer certAutoIssue;
}

package com.his.spi.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 一次可信时间戳盖章的结果（序列号 / 授时时刻 / 令牌值，三列缺一不可）。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TsaStampVO {

    /**
     * 令牌序列号
     */
    private String serial;

    /**
     * 时间戳服务给出的时刻
     */
    private LocalDateTime tsaTime;

    /**
     * 令牌原文
     */
    private String tokenValue;
}

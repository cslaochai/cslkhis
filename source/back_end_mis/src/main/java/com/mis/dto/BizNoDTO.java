package com.mis.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 待结算业务号入参（门诊就诊号 / 住院号）
 */
@Data
public class BizNoDTO implements Serializable {

    private String bizNo;
}

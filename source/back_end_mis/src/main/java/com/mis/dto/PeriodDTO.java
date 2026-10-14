package com.mis.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 账期入参（yyyy-MM）
 */
@Data
public class PeriodDTO implements Serializable {

    private String period;
}

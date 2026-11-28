package com.his.inpatient.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;

/**
 * 住院腕带打印入参
 */
@Data
public class WristbandPrintDTO implements Serializable {

    /**
     * 入院ID
     */
    @NotNull(message = "入院ID不能为空")
    private Long admissionId;

    /**
     * 补打原因（第 2 次及以后必填，服务端台账校验）
     */
    private String printReason;

    /**
     * 打印机名称（终端绑定，可空）
     */
    private String printerName;
}

package com.his.inpatient.dto;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 护士医嘱校对入参（支持批量）。
 */
@Data
public class InpatientOrderVerifyDTO implements Serializable {

    /**
     * 医嘱ID列表（≥1 条）
     */
    @NotEmpty(message = "请选择要校对的医嘱")
    private List<Long> orderIds;

    /**
     * 签名 PIN（软签二次确认）：签名人本人输入，明文不入库
     */
    private String pin;
}

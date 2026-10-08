package com.his.patient.dto;

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
}

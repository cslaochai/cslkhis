package com.his.emr.dto;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

/**
 * 处方合理用药批量审查入参（审方工作台整页标注用）
 */
@Data
public class PrescriptionRationalQueryDTO {

    /** 待审查的处方ID（前端从列表页当前页取，一次最多一页） */
    @NotEmpty(message = "请指定要审查的处方")
    private List<Long> prescriptionIds;
}

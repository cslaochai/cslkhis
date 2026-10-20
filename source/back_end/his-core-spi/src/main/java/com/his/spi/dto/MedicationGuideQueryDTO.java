package com.his.spi.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 患者端用药说明查询入参：患者身份由服务端从登录态取，这里只交代要哪张处方。
 */
@Data
@NoArgsConstructor
public class MedicationGuideQueryDTO {

    /**
     * 处方ID
     */
    @NotNull(message = "prescriptionId不能为空")
    private Long prescriptionId;
}

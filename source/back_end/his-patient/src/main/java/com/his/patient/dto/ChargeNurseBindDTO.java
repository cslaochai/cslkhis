package com.his.patient.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 建立/转交责任护士入参。
 */
@Data
public class ChargeNurseBindDTO {

    /**
     * 关系ID（空=新增；传了一般用于补录失效时间，正常转交走重新指派）
     */
    private Long id;

    /**
     * 住院登记ID
     */
    @NotNull(message = "住院登记ID不能为空")
    private Long admissionId;

    /**
     * 责任护士ID（必须是护理岗位类别）
     */
    @NotNull(message = "请选择责任护士")
    private Long nurseId;

    /**
     * 生效时间（空=当前时间；一般从入院或交接时刻起算）
     */
    private String effectiveTime;

    /**
     * 备注
     */
    private String remark;
}

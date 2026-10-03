package com.his.patient.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 建立/转交管床关系入参。
 *
 * <p><b>生效时间不传＝当前时间</b>：转交场景里「上一手什么时候交出去」必须精确，
 * 所以留了 {@code effectiveTime} 让人显式指定（补录历史关系也靠它）。
 */
@Data
public class AttendingBindDTO {

    /** 关系ID（空=新增；传了=改这条，一般用于补录失效时间） */
    private Long id;

    /** 住院登记ID */
    @NotNull(message = "住院登记ID不能为空")
    private Long admissionId;

    /** 医生ID */
    @NotNull(message = "请选择医生")
    private Long employeeId;

    /** 关系类型（1-主管 2-主诊组长 3-协作，空=主管） */
    private Integer relationType;

    /** 生效时间（空=当前时间） */
    private String effectiveTime;

    /** 备注 */
    private String remark;
}

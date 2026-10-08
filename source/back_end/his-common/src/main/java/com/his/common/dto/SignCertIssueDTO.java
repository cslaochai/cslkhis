package com.his.common.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 人工签发证书入参。员工信息由调用方（可访问员工表的模块）填好传入，
 */
@Data
public class SignCertIssueDTO {

    /**
     * 签名人员工ID
     */
    @NotNull(message = "员工ID不能为空")
    private Long empId;

    /**
     * 员工姓名（快照，必填）
     */
    private String empName;

    /**
     * 所属科室ID
     */
    private Long deptId;

    /**
     * 所属科室名称
     */
    private String deptName;

    /**
     * 有效期天数；为空取系统参数的 sign.cert.valid_days，再为空取 365
     */
    private Integer validDays;

    /**
     * 备注
     */
    private String remark;
}

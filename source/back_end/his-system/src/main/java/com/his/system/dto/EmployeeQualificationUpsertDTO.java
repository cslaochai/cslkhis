package com.his.system.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;

/**
 * 员工资格证书新增/编辑入参
 */
@Data
public class EmployeeQualificationUpsertDTO {

    /**
     * 证书ID，新增时为空，修改时必填
     */
    private Long id;

    /**
     * 员工ID
     */
    @NotNull(message = "员工不能为空")
    private Long employeeId;

    /**
     * 证书类型（2-医师执业证 3-护士执业证 4-药师资格证 5-技术职称聘书 9-其他）
     */
    @NotBlank(message = "证书类型不能为空")
    @Size(max = 8, message = "证书类型取值不合法")
    private String certType;

    /**
     * 证书编号
     */
    @NotBlank(message = "证书编号不能为空")
    @Size(max = 64, message = "证书编号不能超过64个字符")
    private String certNo;

    /**
     * 发证机关（字典 his_emp_cert_org 的码值，1=国家卫健委）
     */
    @Size(max = 8, message = "发证机关取值不合法")
    private String issueOrg;

    /**
     * 发证日期
     */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate issueDate;

    /**
     * 有效期至（不传 = 长期有效）
     */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate validUntil;

    /**
     * 备注
     */
    @Size(max = 512, message = "备注不能超过512个字符")
    private String remark;
}

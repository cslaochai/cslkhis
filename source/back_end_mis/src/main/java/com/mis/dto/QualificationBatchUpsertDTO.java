package com.mis.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 员工资格证书批量保存入参（HIS 口径：qualificationBatchUpsert）
 * items 为空 id 的新增；带 id 的按 id 更新；不传的行不动
 */
@Data
public class QualificationBatchUpsertDTO implements Serializable {

    @NotNull(message = "员工ID不能为空")
    private Long employeeId;

    private List<QualificationItemDTO> items;

    @Data
    public static class QualificationItemDTO implements Serializable {
        private Long id;
        /** 证书类型（his_emp_cert_type 字典） */
        private String certType;
        private String certNo;
        /** 发证机关（his_emp_cert_org 字典） */
        private String issueOrg;
        private String issueDate;
        private String validUntil;
        private String remark;
    }
}

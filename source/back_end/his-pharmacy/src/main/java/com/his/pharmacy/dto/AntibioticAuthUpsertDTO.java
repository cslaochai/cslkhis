package com.his.pharmacy.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

/**
 * 抗菌药物处方权授权入参。
 */
@Data
public class AntibioticAuthUpsertDTO {

    /** 主键 */
    private Long id;

    /** 医师ID */
    @NotNull(message = "医师ID不能为空")
    private Long doctorId;

    /** 授权级别（1-非限制使用级 2-限制使用级 3-特殊使用级） */
    @NotNull(message = "授权级别不能为空")
    @Min(value = 1, message = "授权级别非法")
    @Max(value = 3, message = "授权级别非法")
    private Integer authLevel;

    /** 授权依据（职称/培训考核合格/抗菌药物管理工作组审定） */
    private String authBasis;

    /** 授权日期 */
    @NotNull(message = "授权日期不能为空")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate authDate;

    /** 有效期至 */
    @NotNull(message = "有效期至不能为空")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate expireDate;

    /** 状态（1-有效 2-暂停 3-取消） */
    @Min(value = 1, message = "状态非法")
    @Max(value = 3, message = "状态非法")
    private Integer status;

    /** 授权人 */
    private String authorizer;

    /** 授权部门 */
    private String authorizeOrg;

    /** 暂停/取消原因（status=2/3 时必填） */
    private String revokeReason;

    /** 备注 */
    private String remark;
}

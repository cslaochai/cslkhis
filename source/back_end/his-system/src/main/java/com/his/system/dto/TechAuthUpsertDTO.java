package com.his.system.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

/**
 * 技术授权新增/修改入参
 */
@Data
public class TechAuthUpsertDTO {

    /**
     * 授权ID，新增时为空，修改时必填
     */
    private Long id;

    /**
     * 员工ID（员工的ID）
     */
    @NotNull(message = "员工不能为空")
    private Long employeeId;

    /**
     * 授权类别（字典 his_tech_auth_category：1-手术 2-麻醉 3-内镜与介入）
     */
    @NotNull(message = "授权类别不能为空")
    private Integer authCategory;

    /**
     * 可独立操作的手术级别上限（1~4）
     */
    @NotNull(message = "授权级别不能为空")
    private Integer techLevel;

    /**
     * 限定术式编码白名单（逗号分隔，不传=该级别全部术式）
     */
    private String itemScope;

    /**
     * 授权方式（字典 his_tech_auth_type：1-独立授权 2-上级指导下 3-限制授权，默认 1）
     */
    private Integer authType;

    /**
     * 授权依据（技术准入评价/培训考核/累计手术量，评审要看依据）
     */
    private String authBasis;

    /**
     * 授权生效日期
     */
    @NotNull(message = "生效日期不能为空")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate validFrom;

    /**
     * 有效期至（不传=长期有效）
     */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate validUntil;

    /**
     * 备注
     */
    private String remark;
}

package com.his.platform.dto;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.his.common.validation.AllowedValues;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

/**
 * 病区新增/修改入参。
 *
 * <p>一次提交同时维护两处：病区主数据本身，以及它可收治的科室集合（多对多）。
 */
@Data
public class WardUpsertDTO {

    /**
     * 病区ID，新增时为空，修改时必填
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long wardId;

    /**
     * 病区编码，新增时可不传（系统按 W + 序号生成）
     */
    private String wardCode;

    /**
     * 病区名称，如「呼吸内科病区」「13A 病区」
     */
    @NotBlank(message = "病区名称不能为空")
    private String wardName;

    /**
     * 本位科室ID（默认收治科室）。必须同时出现在 {@link #depts} 里，
     * 服务端会把它落成 isPrimary=1 并同步写回 sys_ward.dept_id。
     */
    @NotNull(message = "本位科室不能为空")
    @JsonSerialize(using = ToStringSerializer.class)
    private Long deptId;

    /**
     * 收治科室清单（含本位科室）。为空时只按本位科室建一条。
     */
    @Valid
    private List<WardDeptQuotaDTO> depts;

    /**
     * 总床位数
     */
    private Integer totalBeds;

    /**
     * 状态（0-停用 1-正常）
     */
    @AllowedValues(values = {"0", "1"}, message = "病区状态取值不合法")
    private Integer status;

    /**
     * 备注
     */
    private String remark;
}

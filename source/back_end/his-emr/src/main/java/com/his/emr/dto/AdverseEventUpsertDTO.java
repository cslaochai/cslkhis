package com.his.emr.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.his.common.validation.AllowedValues;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 不良事件上报/修改入参
 */
@Data
public class AdverseEventUpsertDTO {

    /**
     * 主键（null=新增上报）
     */
    private Long id;

    /**
     * 事件类型（1-11，字典 his_adverse_event_type）
     */
    @NotNull(message = "事件类型不能为空")
    @AllowedValues(values = {"1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11"},
            message = "事件类型取值不合法")
    private Integer eventType;

    /**
     * 事件等级（1-4，字典 his_adverse_event_level）
     */
    @NotNull(message = "事件等级不能为空")
    @AllowedValues(values = {"1", "2", "3", "4"}, message = "事件等级取值不合法")
    private Integer eventLevel;

    /**
     * 发生科室的ID
     */
    @NotNull(message = "发生科室不能为空")
    private Long occurDeptId;

    /**
     * 发生时间（EP date-picker 默认空格分隔，@JsonFormat 宽进同项目惯例）
     */
    @NotNull(message = "发生时间不能为空")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime occurTime;

    /**
     * 关联患者（可空）
     */
    private Long patientId;

    /**
     * 患者姓名（可空；patientId 非空时必填，服务端校验）
     */
    private String patientName;

    /**
     * 关联就诊挂号单的ID（可空）
     */
    private Long visitId;

    /**
     * 事件摘要
     */
    @NotBlank(message = "事件摘要不能为空")
    private String title;

    /**
     * 事件详细经过
     */
    @NotBlank(message = "事件经过不能为空")
    private String description;

    /**
     * 来源
     */
    @AllowedValues(values = {"1", "2"}, message = "来源取值不合法")
    private Integer acquiredFlag;

    /**
     * 即时处置措施（可空）
     */
    private String immediateAction;
}

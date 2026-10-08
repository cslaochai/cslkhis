package com.his.miniapp.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * 工单详情（患者端 + 院内端共用，字段权限靠 Service 侧裁剪）。
 */
@Data
@Schema(name = "MiniServiceDetailVO", description = "工单详情")
public class MiniServiceDetailVO {

    /**
     * 工单ID
     */
    @Schema(description = "工单ID")
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    private String messageNo;

    private String patientName;

    private String contactPhone;

    private String categoryCode;

    private String content;

    private Integer status;

    private String statusText;

    private Integer priority;

    private String acceptByName;

    private String closeReason;

    private String handleResult;

    private String createTime;

    /**
     * 患者可执行动作
     */
    private List<String> actions;

    /**
     * 流转时间轴。
     * <p>患者端只装 visibleToPatient=1；院内端全量（含内部备注）。
     */
    private List<MiniServiceLogVO> logs;
}

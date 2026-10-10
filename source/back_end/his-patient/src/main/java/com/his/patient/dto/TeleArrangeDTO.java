package com.his.patient.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;

/**
 * 远程会诊安排入参（待安排 → 已安排）。
 */
@Data
public class TeleArrangeDTO implements Serializable {

    @NotNull(message = "会诊单ID不能为空")
    private Long id;

    /**
     * 计划会诊时间
     */
    @NotNull(message = "计划会诊时间不能为空")
    private String planTime;

    /**
     * 计划时长（分钟）
     */
    private Integer durationMin;

    /**
     * 对接平台（预留：真实视频平台对接时回写）
     */
    private String platform;

    /**
     * 接入号/会议室号（预留）
     */
    private String meetNo;

    /**
     * 受邀专家所在医院
     */
    private String expertHospital;

    /**
     * 受邀专家科室
     */
    private String expertDept;

    /**
     * 受邀专家姓名
     */
    private String expertName;

    /**
     * 受邀专家职称
     */
    private String expertTitle;
}

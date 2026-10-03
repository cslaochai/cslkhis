package com.his.miniapp.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * 工单详情（患者端 + 院内端共用，字段权限靠 Service 侧裁剪）。
 *
 * <p>患者端要的是「我的问题处理到哪了」，所以核心是 {@code logs} 时间轴；
 * 院内端要的是「我要拿这张单干什么」，所以核心是 {@code actions}。
 */
@Data
@Schema(name = "ServiceTicketDetailVO", description = "工单详情")
public class ServiceTicketDetailVO {

    /** 工单ID */
    @Schema(description = "工单ID（19 位，前端全程按字符串处理）")
    private String id;

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

    /** 患者可执行动作 */
    private List<String> actions;

    /**
     * 流转时间轴。
     * <p>患者端只装 visibleToPatient=1；院内端全量（含内部备注）。
     */
    private List<ServiceTicketLogVO> logs;
}

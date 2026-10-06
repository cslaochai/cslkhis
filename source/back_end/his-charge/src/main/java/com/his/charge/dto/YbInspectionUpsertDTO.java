package com.his.charge.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

/**
 * 飞检批次新增/修改（id 空=新增；单号由服务端生成，前端不传）。
 *
 * <p>长度靠服务端截断兜底，不在 DTO 上加 @Size 抢在截断前把用户输入变成 400。
 */
@Data
public class YbInspectionUpsertDTO {

    /**
     * 主键（雪花ID）
     */
    private Long id;

    /**
     * 检查类型（1-国家飞检 2-省级飞检 3-智能审核转来 4-日常驻点审核）
     */
    @NotNull(message = "检查类型不能为空")
    private Integer inspectType;

    /**
     * 统筹区/医保局名称
     */
    @NotBlank(message = "统筹区/医保局名称不能为空")
    private String fundOrg;

    /**
     * 审核目标期间起
     */
    @NotNull(message = "审核目标期间起不能为空")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate inspectStartDate;

    /**
     * 审核目标期间止
     */
    @NotNull(message = "审核目标期间止不能为空")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate inspectEndDate;

    /**
     * 检查组进驻/通知日期
     */
    @NotNull(message = "进驻/通知日期不能为空")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate inspectDate;

    /**
     * 检查组/审核团队名称
     */
    private String inspectTeam;

    /**
     * 本院接待负责人
     */
    private String ourReceiver;

    /**
     * 备注
     */
    private String remark;
}

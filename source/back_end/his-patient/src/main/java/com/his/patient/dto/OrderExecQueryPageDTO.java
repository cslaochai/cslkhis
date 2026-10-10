package com.his.patient.dto;

import com.his.common.base.PageParam;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;

/**
 * 医嘱执行队列 / 执行记录查询入参（命名遵循 AGENTS.md：分页查询用 `xxxQueryPageDTO`）。
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class OrderExecQueryPageDTO extends PageParam implements Serializable {

    /**
     * 入院ID
     */
    private Long admissionId;

    /**
     * 医嘱ID
     */
    private Long orderId;

    /**
     * 患者ID
     */
    private Long patientId;

    /**
     * 医嘱类型：1-长期 2-临时
     */
    private Integer orderType;

    /**
     * 医嘱类别
     */
    private Integer orderClass;

    /**
     * 是否加急：0-否 1-是
     */
    private Integer isUrgent;

    /**
     * 执行状态：1-待执行 2-已执行 3-已跳过 4-已退回
     */
    private Integer execStatus;

    /**
     * 计划日期
     */
    private LocalDate planDate;

    /**
     * 科室数据权限收敛集合（M6）—— <b>只由服务端</b>按 {@code DeptScopeProvider} 填充，
     * 前端传什么都必须忽略（service 入口先置 null 再收口）。受限且未传 deptId 时非空。
     */
    private List<Long> scopeDeptIds;
}

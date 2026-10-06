package com.his.medicaltech.dto;

import com.his.common.base.PageParam;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * 住院输血申请分页查询入参（命名遵循 AGENTS.md：分页查询用 `xxxQueryPageDTO`）。
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class TransfusionApplyQueryPageDTO extends PageParam implements Serializable {

    /**
     * 入院ID（为空 = 不按住院过滤，输血科工作台就是全院）
     */
    private Long admissionId;

    /**
     * 患者ID
     */
    private Long patientId;

    /**
     * 申请科室ID
     */
    private Long applyDeptId;

    /**
     * 流程状态：0-待配血 1-已配血 2-已发血 3-输注中 4-已完成 5-已取消
     */
    private Integer transfusionStatus;

    /**
     * 配血状态：0-待配血 1-配血中 2-全部相合 3-存在不合（与流程状态分开筛：配血不合时流程停在 0）
     */
    private Integer crossmatchStatus;

    /**
     * 审批状态：0-待审批 1-已通过 2-已驳回 3-急诊待补审（sql/93）
     */
    private Integer approveStatus;

    /**
     * 血液品种（1-红细胞悬液 2-血浆 3-血小板 4-冷沉淀 5-全血 6-其他）
     */
    private Integer bloodComponent;

    /**
     * 受血者 ABO 血型：A/B/O/AB
     */
    private String patientAbo;

    /**
     * 有无输血反应：0-未上报 1-已上报有反应（飞检倒查用）
     */
    private Integer hasReaction;

    /**
     * 只看未完成（0/1/2/3）：1-是
     */
    private Integer unfinishedOnly;

    /**
     * 申请时间下界（含）。
     *
     * <p><b>刻意收 String 而不是 LocalDateTime</b>：GET 查询参数上的 {@code @DateTimeFormat}
     * 一旦格式不匹配就抛绑定异常，而全局异常处理会把绑定失败渲染成 500 ——
     * 用户看到"系统内部错误"，实际只是日期少写了时分秒。
     * 服务端宽松解析（接受 {@code yyyy-MM-dd} 与 {@code yyyy-MM-dd HH:mm:ss}），
     * 解析不了就明确报"时间格式不正确"。
     */
    private String applyDateFrom;

    /**
     * 申请时间上界（含当天；服务端放宽到"次日 00:00:00 且不含"）
     */
    private String applyDateTo;

    /**
     * 关键字（输血单号 / 入院号 / 患者姓名 / 患者号 / 输血目的 / 输血指征）
     */
    private String keyword;
}

package com.his.patient.dto;

import com.his.common.base.PageParam;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * 转科记录分页查询入参（命名遵循 AGENTS.md：分页查询用 xxxQueryPageDTO）。
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class InpatientTransferQueryPageDTO extends PageParam implements Serializable {

    /**
     * 入院ID
     */
    private Long admissionId;

    /**
     * 患者ID
     */
    private Long patientId;

    /**
     * 转出科室ID
     */
    private Long fromDeptId;

    /**
     * 转入科室ID（转入科室的工作台按这个筛）
     */
    private Long toDeptId;

    /**
     * 转科状态：0-待接收 1-已完成 2-已取消
     */
    private Integer transferStatus;

    /**
     * 转科类型：1-普通转科 2-急诊转科 3-转入ICU 4-ICU转出
     */
    private Integer transferType;

    /**
     * 关键字（转科单号 / 入院号 / 患者姓名 / 转科原因）
     */
    private String keyword;
}

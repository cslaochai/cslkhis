package com.his.patient.dto;

import com.his.common.base.PageParam;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * 远程会诊分页查询入参（listPage 为 POST）。
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class TeleConsultQueryPageDTO extends PageParam implements Serializable {

    /** 单号/患者姓名/专家姓名模糊 */
    private String keyword;

    /** 会诊类型（1-临床会诊 2-远程影像 3-远程心电 4-远程病理 5-其他） */
    private Integer consultType;

    /** 状态（1-待安排 2-已安排 3-已完成 4-已取消） */
    private Integer status;

    /** 申请科室ID */
    private Long applyDeptId;

    /** 仅急会诊 */
    private Boolean urgentOnly;

    /** 仅未完成（待安排+已安排） */
    private Boolean openOnly;
}

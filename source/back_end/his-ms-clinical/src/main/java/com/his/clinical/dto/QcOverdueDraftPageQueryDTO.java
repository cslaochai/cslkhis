package com.his.clinical.dto;

import com.his.common.base.PageParam;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 质控侧超时草稿看板查询入参
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class QcOverdueDraftPageQueryDTO extends PageParam {

    /**
     * 科室ID（按病历归属科室筛）
     */
    private Long deptId;

    /**
     * 关键词（病历号 / 患者姓名）
     */
    private String keyword;

    /**
     * 移交状态（空-全部；0-未移交；1-待代补）
     */
    private Integer handoverStatus;
}

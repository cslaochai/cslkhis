package com.his.patient.dto;

import com.his.common.base.PageParam;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 全院床位池查询入参
 *
 * <p>所有参数都只用于<b>收窄</b>范围。床位中心本身就是院级职能
 * （它的存在意义就是"本科室没床时去别科找"），所以这里<b>不做科室数据权限收口</b>，
 * 边界由 {@code ipd:bedCenter:list} 的授权控制 —— 能进这个页面的人就是被授权看到全院的。
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class BedPoolQueryPageDTO extends PageParam {

    /** 科室ID（床位归属科室） */
    private Long deptId;

    /** 病区ID */
    private Long wardId;

    /** 床位状态（0-维修 1-空闲 2-占用 3-锁定） */
    private Integer bedStatus;

    /** 床位类型：normal / ICU / VIP */
    private String bedType;

    /** 床号或患者姓名模糊查询 */
    private String keyword;

    /** 只看空闲且未被预留的床（安排床位选床时用） */
    private Boolean availableOnly;
}

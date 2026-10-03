package com.his.patient.dto;

import com.his.common.base.PageParam;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/** 住院列表查询入参 */
@Data
@EqualsAndHashCode(callSuper = true)
public class InpatientQueryPageDTO extends PageParam {

    /** 患者姓名 / 住院号模糊查询 */
    private String patientName;

    /** 开立科室ID */
    private Long deptId;

    /** 病区ID */
    private Long wardId;

    /** 状态：1-在院 0-已出院，null-全部 */
    private Integer admitStatus;

    /**
     * 科室数据权限收敛集合（M6）—— <b>只由服务端</b>按 {@code DeptScopeGuard} 填充，
     * 前端传什么都必须忽略（listPage 入口先置 null 再收口）。受限且未传 deptId 时非空。
     */
    private List<Long> scopeDeptIds;
}

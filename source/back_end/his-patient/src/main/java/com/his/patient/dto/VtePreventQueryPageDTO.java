package com.his.patient.dto;

import com.his.common.base.PageParam;
import lombok.Data;
import lombok.EqualsAndHashCode;

/** 预防措施记录分页查询（按住院或按病区看落实情况） */
@Data
@EqualsAndHashCode(callSuper = true)
public class VtePreventQueryPageDTO extends PageParam {

    /** 入院ID */
    private Long admissionId;

    /** 病区ID */
    private Long wardId;

    /** 落实状态（0-待落实 1-已落实 2-禁忌未用 3-患者拒绝） */
    private Integer executeStatus;

    /** 措施码 BASIC / PHYSICAL / DRUG */
    private String measureCode;

    /** 关键字：患者姓名 / 住院号 */
    private String keyword;
}

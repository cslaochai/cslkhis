package com.his.operation.dto;

import com.his.common.base.PageParam;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * 麻醉术前访视单分页查询入参。
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class AnesthesiaVisitQueryPageDTO extends PageParam implements Serializable {

    /**
     * 入院ID
     */
    private Long admissionId;

    /**
     * 手术申请单ID
     */
    private Long applyId;

    /**
     * 访视结论（1-可施行麻醉 2-暂缓手术 3-需会诊）
     */
    private Integer conclusion;

    /**
     * 尚未给出结论（1-是）：麻醉科每天早上要先把这批做完
     */
    private Integer unfinishedOnly;

    /**
     * 关键字
     */
    private String keyword;
}

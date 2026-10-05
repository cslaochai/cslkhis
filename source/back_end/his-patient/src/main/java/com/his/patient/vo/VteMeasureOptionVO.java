package com.his.patient.vo;

import lombok.Data;

/**
 * 措施项下拉（登记措施时用；recommend 由当前风险等级算，前端不猜）
 */
@Data
public class VteMeasureOptionVO {

    /**
     * 措施码
     */
    private String measureCode;

    /**
     * 措施类别（1-基础预防 2-物理预防 3-药物预防）
     */
    private Integer measureType;

    /**
     * 措施名称
     */
    private String measureName;

    private String desc;

    /**
     * 按传入风险等级是否推荐
     */
    private Boolean recommend;
}

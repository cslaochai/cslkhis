package com.his.patient.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.his.common.base.PageParam;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;

/**
 * VTE 事件分页查询
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class VteEventQueryPageDTO extends PageParam {

    /**
     * 入院ID
     */
    private Long admissionId;

    /**
     * 事件类型（1-深静脉血栓DVT 2-肺栓塞PE 3-预防相关出血）
     */
    private Integer eventType;

    /**
     * 发生时机（1-院内发生 2-入院时已存在）
     */
    private Integer onsetType;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate beginDate;

    /**
     * 结束日期
     */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate endDate;

    /**
     * 关键字：患者姓名 / 住院号
     */
    private String keyword;
}

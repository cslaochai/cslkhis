package com.his.emr.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 满意度：发放/回收按状态分组的条数。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SurveyDispatchCountVO implements Serializable {

    /**
     * 发放状态（1-待推送 2-已推送 3-已回收 4-已过期 5-已拒收）
     */
    private Integer k;

    /**
     * 条数
     */
    private Long c;
}
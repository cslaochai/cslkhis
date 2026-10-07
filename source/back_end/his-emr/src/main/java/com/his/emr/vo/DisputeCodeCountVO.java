package com.his.emr.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 纠纷/投诉：按码值分组的条数（状态或类型，取决于哪一条 SQL）。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DisputeCodeCountVO implements Serializable {

    /**
     * 码值（纠纷状态或纠纷类型）
     */
    private Integer k;

    /**
     * 条数
     */
    private Long c;
}
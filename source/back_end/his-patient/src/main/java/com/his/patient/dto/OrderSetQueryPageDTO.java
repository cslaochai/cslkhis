package com.his.patient.dto;

import com.his.common.base.PageParam;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * 医嘱组套模板分页查询入参（全院组套模板管理页）。
 *
 * <p><b>{@code scope} 是过滤不是授权</b>：看不见的模板由服务层按「个人 / 本科室 / 全院」可见集先切一刀，
 * 这里传的 scope 只是在可见集里再筛 —— 传 3 也不可能把别人的个人模板捞出来。
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class OrderSetQueryPageDTO extends PageParam implements Serializable {

    /**
     * 名称/备注模糊
     */
    private String keyword;

    /**
     * 共享范围：1-个人 2-科室 3-全院（不传=全部可见）
     */
    private Integer scope;

    /**
     * 默认医嘱类型：1-长期 2-临时
     */
    private Integer orderType;
}

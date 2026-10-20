package com.his.spi.vo;

import lombok.Data;

/**
 * 一次入组的结果：只回答「进没进组、进了哪一组」。
 *
 * <p>权重、支付标准与规则说明留在分组域自己的模拟页，跨域调用方拿到组码后按自己的目录取数。
 */
@Data
public class DrgGroupResultVO {

    /**
     * DRG 组编码（未入组时为分组域的未组码）
     */
    private String drgCode;

    /**
     * 是否成功入组
     */
    private boolean grouped;
}

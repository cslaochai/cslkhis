package com.his.patient.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.io.Serializable;

/**
 * 组套模板下拉候选（医嘱开立弹窗「套用组套」用，命名遵循 AGENTS.md：下拉出参用 `xxxSelectListVO`）。
 *
 * <p>刻意带 {@code scopeText} 与 {@code itemCount}：医生在下拉里要一眼分清
 * 「这个组套是我自己的、科室的、还是全院的」，以及它有几条 —— 只给名字他得点开才知道。
 *
 * <p>{@code templateName} 保持原名不拼前缀：名字拼接会让「另存为」时把前缀又存回库里一次。
 * 来源标识交给 {@code scopeText} 单独一列渲染。
 */
@Data
public class OrderSetSelectListVO implements Serializable {

    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    /**
     * 模板名称
     */
    private String templateName;

    /**
     * 共享范围（1-个人 2-科室 3-全院）
     */
    private Integer scope;

    private String scopeText;

    /**
     * 默认医嘱类型（1-长期 2-临时）
     */
    private Integer orderType;

    private String orderTypeText;

    /**
     * 明细条数
     */
    private Integer itemCount;

    /**
     * 科室名称
     */
    private String deptName;
}

package com.his.patient.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 医嘱基础字典行出参（管理页）。
 *
 * <p>{@code usageCount} 是当前库里引用了这个值的医嘱行数 —— 列表上直接告诉维护人
 * 「这个值有多少条医嘱在用」，避免停用/改名时凭感觉决定。停用不删数据，历史医嘱照样渲染得出文案。
 */
@Data
public class OrderDictListVO implements Serializable {

    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    /** 字典类型 */
    private String dictType;

    private String dictTypeText;

    /** 字典值 */
    private String dictValue;

    /** 字典标签 */
    private String dictLabel;

    /** 排序号 */
    private Integer dictSort;

    private Integer status;

    /** 状态文本 */
    private String statusText;

    /**
     * 引用该值的住院医嘱行数（住院医嘱主表，软删不计）
     */
    private Long usageCount;

    /**
     * 是否系统内置（sql 预置的三种字典初始数据）
     */
    private Boolean builtIn;

    /** 备注 */
    private String remark;

    /** 创建时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    /** 更新时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;
}

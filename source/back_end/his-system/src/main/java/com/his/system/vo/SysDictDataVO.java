package com.his.system.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 字典数据出参
 */
@Data
public class SysDictDataVO {

    /**
     * 字典数据ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    /**
     * 创建人
     */
    private String createBy;

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    /**
     * 更新人
     */
    private String updateBy;

    /**
     * 更新时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;

    /**
     * 逻辑删除标记：0-未删除 1-已删除
     */
    private Integer delFlag;

    /**
     * 备注
     */
    private String remark;

    /**
     * 字典类型编码
     */
    private String dictType;

    /**
     * 字典标签（展示名称）
     */
    private String dictLabel;

    /**
     * 字典值（实际存储值）
     */
    private String dictValue;

    /**
     * 字典排序号，越小越靠前
     */
    private Integer dictSort;

    /**
     * 字典来源：1-系统级 2-自定义
     */
    private Integer dictSource;

    /**
     * 样式类
     */
    private String dictClass;

    /**
     * 列表样式类
     */
    private String listClass;

    /**
     * 是否默认值：0-否 1-是
     */
    private Integer isDefault;

    /**
     * 状态：0-停用 1-启用
     */
    private Integer status;
}

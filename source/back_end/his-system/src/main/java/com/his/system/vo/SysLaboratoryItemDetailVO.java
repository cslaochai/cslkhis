package com.his.system.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 检验项目明细出参
 */
@Data
public class SysLaboratoryItemDetailVO {

    /**
     * 明细ID
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
     * 检验大项目ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long laboratoryItemId;

    /** 项目编码（唯一） */
    private String itemCode;

    /**
     * 明细项目名称
     */
    private String itemName;

    /**
     * 单位
     */
    private String unit;

    /**
     * 参考范围
     */
    private String referenceRange;

    /**
     * 排序号，越小越靠前
     */
    private Integer sortOrder;

    /**
     * 状态（0-停用 1-启用）
     */
    private Integer status;
}

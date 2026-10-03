package com.his.supplies.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 耗材字典VO
 */
@Data
public class SysConsumableVO {
    /** 主键ID */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;
    /** 创建人 */
    private String createBy;
    /** 创建时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
    /** 更新人 */
    private String updateBy;
    /** 更新时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;
    /** 删除标志（0-正常 1-删除） */
    private Integer delFlag;
    /** 备注 */
    private String remark;
    /** 耗材编码（唯一） */
    private String consumableCode;
    /** 耗材名称 */
    private String consumableName;
    /** 类别（1-卫生材料 2-注射穿刺 3-医用敷料 4-防护用品 5-其他） */
    private Integer category;
    /** 规格 */
    private String specification;
    /** 单位（包、支、盒、个等） */
    private String unit;
    /** 生产厂家 */
    private String manufacturer;
    /** 零售价 */
    private BigDecimal retailPrice;
    /** 是否高值耗材（0-普通 1-高值） */
    private Integer isHighValue;
    /** 产品级UDI-DI */
    private String udiDi;
    /** 医疗器械注册证/备案号 */
    private String regCertNo;
    /** 状态（0-停用 1-启用） */
    private Integer status;
}

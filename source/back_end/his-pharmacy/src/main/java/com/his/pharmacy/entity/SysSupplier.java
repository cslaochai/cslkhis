package com.his.pharmacy.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 供应商主表
 *
 * ⚠ 主键列名是 `supplier_id` 而不是 `id`，所以本实体**不继承 BaseEntity**：
 *   父类的 @TableId 字段名固定为 `id`，继承过来会让 MP 去 select 一个不存在的列 → 全表查 500。
 *   审计字段在此显式声明，语义与 BaseEntity 相同（`@TableLogic` 逻辑删除 + 时间自动填充）。
 */
@Data
@TableName("sys_supplier")
public class SysSupplier implements Serializable {

    /** 供应商ID */
    @TableId(value = "supplier_id", type = IdType.ASSIGN_ID)
    private Long supplierId;

    /** 供应商编码（唯一） */
    private String supplierCode;

    /** 供应商名称 */
    private String supplierName;

    /** 联系人 */
    private String contactPerson;

    /** 联系电话 */
    private String phone;

    /** 地址 */
    private String address;

    /** 营业执照号 */
    private String licenseNo;

    /** 资质证照有效期 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate licenseExpiry;

    /** 评级（1-差 2-一般 3-良好 4-优秀） */
    private Integer rating;

    /** 状态（0-停用 1-正常） */
    private Integer status;

    /** 备注 */
    private String remark;

    /** 创建人 */
    @TableField(fill = FieldFill.INSERT)
    private String createBy;

    /** 创建时间 */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /** 更新人 */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private String updateBy;

    /** 更新时间 */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    /** 删除标志（0-正常 1-删除） */
    @TableLogic
    private Integer delFlag;
}

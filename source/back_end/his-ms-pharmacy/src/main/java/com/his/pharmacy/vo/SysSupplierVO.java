package com.his.pharmacy.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 供应商出参
 */
@Data
public class SysSupplierVO {

    /** 供应商ID（雪花ID → 序列化为字符串，避免 JS Number 精度丢失） */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long supplierId;

    /** 供应商编码 */
    private String supplierCode;

    /** 供应商名称 */
    private String supplierName;

    /** 经营范围（1-药品 2-器械设备 3-耗材 4-后勤物资） */
    private Integer supplierType;

    /** 经营范围文案（由 SupplierTypeEnum 填，列表直接渲染） */
    private String supplierTypeLabel;

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
    private String createBy;

    /** 创建时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    /** 更新人 */
    private String updateBy;

    /** 更新时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;
}

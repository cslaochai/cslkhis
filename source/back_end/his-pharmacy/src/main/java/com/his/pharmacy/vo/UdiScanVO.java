package com.his.pharmacy.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * UDI 扫码解析结果VO（解析段 + 字典命中 + 该耗材可用批次候选）
 */
@Data
public class UdiScanVO {
    // 该耗材有货批次候选（FEFO 序）
    List<BatchOption> batches;
    /**
     * UDI 原文
     */
    private String udiCode;
    /**
     * 解析-产品标识
     */
    private String udiDi;
    /**
     * 解析-序列号
     */
    private String udiSerial;
    /**
     * 解析-批号
     */
    private String udiBatch;
    /**
     * 解析-有效期
     */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate udiExpiryDate;
    /**
     * 是否成功解析出 DI（false=前端提示人工选耗材，登记仍可继续）
     */
    private boolean parsed;
    // 字典命中（按 udi_di 精确匹配）
    private boolean matched;
    /**
     * 耗材ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long consumableId;
    /**
     * 耗材编码（快照）
     */
    private String consumableCode;
    /**
     * 耗材名称（快照）
     */
    private String consumableName;
    /**
     * 规格（快照）
     */
    private String specification;
    /**
     * 单位
     */
    private String unit;
    private String manufacturer;
    /**
     * 注册证号（快照）
     */
    private String regCertNo;
    /**
     * 计费单价快照
     */
    private BigDecimal retailPrice;
    private Integer isHighValue;
    private Integer consumableStatus;
    /**
     * 命中但非高值/已停用时的提示
     */
    private String tip;

    /**
     * 出库批次候选
     */
    @Data
    public static class BatchOption {
        /**
         * 出库批次ID
         */
        @JsonSerialize(using = ToStringSerializer.class)
        private Long stockId;
        /**
         * 批号（快照）
         */
        private String batchNo;
        /**
         * 有效期
         */
        @JsonFormat(pattern = "yyyy-MM-dd")
        private LocalDate expiryDate;
        private BigDecimal quantity;
        private String location;
        /**
         * 供应商
         */
        private String supplier;
    }
}

package com.his.system.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 医保政策出参
 */
@Data
public class InsurancePolicyVO {

    /**
     * 政策ID（雪花ID，序列化为字符串避免前端精度丢失）
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    /**
     * 政策名称
     */
    private String policyName;

    /**
     * 医保类型
     */
    private String insuranceType;

    /** 结算方式（2-城镇职工医保 3-城乡居民医保 4-公费医疗） */
    private Integer settlementType;

    /**
     * 统筹比例（如85.00表示85%）
     */
    private BigDecimal coverageRatio;

    /**
     * 乙类药品自付比例（如10.00表示10%）
     */
    private BigDecimal selfPayRatio;

    /**
     * 状态（0-停用 1-启用）
     */
    private Integer status;

    /**
     * 备注
     */
    private String remark;

    /** 创建时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    /** 更新时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;
}

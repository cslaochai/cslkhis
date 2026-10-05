package com.his.medicaltech.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 输血血袋出参。
 *
 * <p>三条与会诊/手术 VO 一致的约定：ID 走 {@code ToStringSerializer}（雪花 ID 超 JS 精度）、
 * 码值一律带 {@code xxxText} 文案由后端给、按钮可用性由后端算。
 * 血袋本身没有"按钮"，所以这里只有前两条。
 */
@Data
public class TransfusionBagVO implements Serializable {

    /**
     * 血袋明细ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    /**
     * 输血申请单ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long applyId;

    /**
     * 血袋号
     */
    private String bagNo;

    /**
     * 献血编号
     */
    private String donorNo;

    /**
     * 血袋 ABO 血型
     */
    private String bagAbo;

    /**
     * 血袋 Rh 血型
     */
    private String bagRh;

    /**
     * 血袋血型文案（如「A 型 Rh(+)」）
     */
    private String bloodTypeText;

    /**
     * 血液品种
     */
    private Integer bloodComponent;

    /**
     * 血液品种文案
     */
    private String bloodComponentText;

    /**
     * 规格
     */
    private String spec;

    /**
     * 血量
     */
    private BigDecimal amount;

    /**
     * 血量单位
     */
    private String amountUnit;

    /**
     * 来源血站
     */
    private String sourceBank;

    /**
     * 采集日期
     */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate collectDate;

    /**
     * 有效期至
     */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate expireDate;

    /**
     * 是否已过有效期（查询时算：过期血袋不得输注）
     */
    private Boolean expired;

    /**
     * 主侧配血结果（阴性=相合）
     */
    private String crossmatchMain;

    /**
     * 次侧配血结果
     */
    private String crossmatchSide;

    /**
     * 配血结论（1-相合 2-不合）
     */
    private Integer crossmatchResult;

    /**
     * 配血结论文案
     */
    private String crossmatchResultText;

    /**
     * 配血时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime crossmatchTime;

    /**
     * 配血人姓名（快照）
     */
    private String crossmatchDoctorName;

    /**
     * 血袋状态（0-待配血 1-已配血 2-已发血 3-已输注）
     */
    private Integer bagStatus;

    /**
     * 血袋状态文案
     */
    private String bagStatusText;

    /**
     * 发血时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime issueTime;

    /**
     * 备注
     */
    private String remark;
}

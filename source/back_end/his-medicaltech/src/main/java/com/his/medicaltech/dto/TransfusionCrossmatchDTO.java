package com.his.medicaltech.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.his.common.validation.InEnum;
import com.his.medicaltech.enums.BloodComponentEnum;
import com.his.medicaltech.enums.CrossmatchResultEnum;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * 配血入参：输血科逐袋录入血型鉴定与交叉配血结果。
 */
@Data
public class TransfusionCrossmatchDTO implements Serializable {

    /**
     * 输血申请单ID（必填）
     */
    @NotNull(message = "输血申请单ID不能为空")
    private Long applyId;

    /**
     * 本次配血的血袋（1~n 袋）
     */
    @NotEmpty(message = "配血血袋不能为空（至少要录一袋的配血结果）")
    @Valid
    private List<BagDTO> bags;

    /**
     * 配血备注
     */
    private String crossmatchNote;

    /**
     * 单袋配血明细。
     */
    @Data
    public static class BagDTO implements Serializable {

        /**
         * 血袋号（必填，全局流水号）
         */
        @NotBlank(message = "血袋号不能为空（没有血袋号的血去向不可追溯）")
        private String bagNo;

        /**
         * 献血编号（献血者条码）
         */
        private String donorNo;

        /**
         * 血袋 ABO 血型（必填）：A/B/O/AB
         */
        private String bagAbo;

        /**
         * 血袋 Rh 血型（必填）：阳/阴
         */
        private String bagRh;

        /**
         * 血液品种（可空 = 与申请单一致；填了就必须与申请单一致，防止"申请血浆发红细胞"）
         */
        @InEnum(value = BloodComponentEnum.class, message = "血液品种取值不合法（1-红细胞悬液 2-血浆 3-血小板 4-冷沉淀 5-全血 6-其他）")
        private Integer bloodComponent;

        /**
         * 规格
         */
        private String spec;

        /**
         * 血量
         */
        private BigDecimal amount;

        /**
         * 血量单位：U / ml / 治疗量
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
         * 有效期至（必填：超期血袋不得输注）
         */
        @JsonFormat(pattern = "yyyy-MM-dd")
        private LocalDate expireDate;

        /**
         * 主侧配血结果（阴性=相合）
         */
        private String crossmatchMain;

        /**
         * 次侧配血结果：阴性/阳性
         */
        private String crossmatchSide;

        /**
         * 配血结论（必填）：1-相合 2-不合
         */
        @NotNull(message = "配血结论不能为空（没有配血结论的血袋等于没配）")
        @InEnum(value = CrossmatchResultEnum.class, message = "配血结论取值不合法（1-相合 2-不相合 3-可疑凝集）")
        private Integer crossmatchResult;

        /**
         * 备注
         */
        private String remark;
    }
}

package com.his.medicaltech.dto;

import com.his.common.base.PageParam;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 血库入参
 */
public class BloodDTO {

    /**
     * 血袋入库登记
     */
    @Data
    public static class Inbound {
        /**
         * 血袋号
         */
        @NotBlank(message = "血袋号不能为空")
        private String bagNo;

        /**
         * 血型
         */
        @NotNull(message = "血型不能为空")
        private Integer bloodType;

        private Integer rhType;

        @NotNull(message = "血液成分不能为空")
        private Integer componentType;

        private Integer volume;

        private BigDecimal unitAmount;

        private LocalDate collectDate;

        private LocalDate expireDate;

        private Integer sourceType;

        private String sourceName;

        private String donorNo;

        private Integer aboVerify;

        private String storageLoc;

        /**
         * 备注
         */
        private String remark;
    }

    @Data
    @EqualsAndHashCode(callSuper = true)
    public static class InventoryQuery extends PageParam {
        /**
         * 血袋号
         */
        private String bagNo;

        /**
         * 血型
         */
        private Integer bloodType;

        private Integer rhType;

        private Integer componentType;

        private Integer status;

        /**
         * 效期预警：只看 N 天内到期（含已过期）
         */
        private Integer expireWithinDays;

        private String storageLoc;
    }

    /**
     * 预留 / 取消预留 / 发血 / 报废 / 退回的通用袋操作
     */
    @Data
    public static class BagAction {
        @NotNull(message = "血袋ID不能为空")
        private Long bagId;

        /**
         * 发血必填：用血申请单号
         */
        private String applyNo;

        /**
         * 报废 / 退回必填
         */
        private String reason;
    }

    /**
     * 新建配血单（待配血）
     */
    @Data
    public static class CrossmatchCreate {
        private Long id;

        private String applyNo;

        /**
         * 患者ID
         */
        @NotNull(message = "患者ID不能为空")
        private Long patientId;

        /**
         * 患者编号
         */
        private String patientNo;

        /**
         * 患者姓名
         */
        @NotBlank(message = "患者姓名不能为空")
        private String patientName;

        @NotNull(message = "患者血型不能为空")
        private Integer patientBloodType;

        private Integer patientRhType;

        /**
         * 血袋号
         */
        @NotBlank(message = "血袋号不能为空")
        private String bagNo;
    }

    @Data
    @EqualsAndHashCode(callSuper = true)
    public static class CrossmatchQuery extends PageParam {
        private String matchNo;

        /**
         * 血袋号
         */
        private String bagNo;

        /**
         * 患者姓名
         */
        private String patientName;

        private Integer status;

        private Integer result;
    }

    /**
     * 执行配血
     */
    @Data
    public static class CrossmatchExecute {
        @NotNull(message = "配血单ID不能为空")
        private Long matchId;

        @NotNull(message = "配血结果不能为空")
        private Integer result;

        /**
         * 配血方法（缺省凝聚胺法）
         */
        private Integer method;

        /**
         * 结论
         */
        private String conclusion;
    }

    @Data
    public static class CrossmatchVerify {
        @NotNull(message = "配血单ID不能为空")
        private Long matchId;
    }

    /**
     * 作废配血单
     */
    @Data
    public static class CrossmatchVoid {
        @NotNull(message = "配血单ID不能为空")
        private Long matchId;
    }

    @Data
    @EqualsAndHashCode(callSuper = true)
    public static class LogQuery extends PageParam {
        /**
         * 血袋号
         */
        private String bagNo;

        /**
         * 业务类型
         */
        private Integer bizType;

        private String applyNo;
    }
}

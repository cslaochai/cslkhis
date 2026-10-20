package com.his.drgdip.settle.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 按病种付费结算层 DTO（P4）。
 */
public class MedpaySettleDTO implements Serializable {

    /**
     * 结算结果分页查询
     */
    @Data
    public static class SettleQuery implements Serializable {
        private String payMode;
        private String keyword;
        private Integer calcStatus;
        private Integer pageNum = 1;
        private Integer pageSize = 20;
    }

    /**
     * 批量结算（billIds 为空取最近 50 张出院结算账单）
     */
    @Data
    public static class SettleBatch implements Serializable {
        private List<Long> billIds;
    }

    /**
     * 特例单议创建
     */
    @Data
    public static class AppealCreate implements Serializable {
        @NotNull(message = "结算结果不能为空")
        private Long settleId;
        @NotNull(message = "申请类型不能为空")
        private Integer reasonType;
        private String reasonDesc;
    }

    /**
     * 特例单议审核
     */
    @Data
    public static class AppealReview implements Serializable {
        @NotNull(message = "申请单不能为空")
        private Long id;
        @NotNull(message = "审核结论不能为空")
        private Boolean approved;
        private String reviewRemark;
    }

    /**
     * 特例单议分页查询
     */
    @Data
    public static class AppealQuery implements Serializable {
        private String payMode;
        private String keyword;
        private Integer status;
        private Integer pageNum = 1;
        private Integer pageSize = 20;
    }
}

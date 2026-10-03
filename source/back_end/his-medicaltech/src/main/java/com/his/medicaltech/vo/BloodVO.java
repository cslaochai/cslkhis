package com.his.medicaltech.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 血库出参
 */
public class BloodVO {

    @Data
    public static class InventoryVO {
        @JsonSerialize(using = ToStringSerializer.class)
        private Long id;

        /** 血袋号 */
        private String bagNo;

        /** 血型 */
        private Integer bloodType;

        private String bloodTypeText;

        private Integer rhType;

        private String rhTypeText;

        private Integer componentType;

        private String componentTypeText;

        private Integer volume;

        private BigDecimal unitAmount;

        private LocalDate collectDate;

        private LocalDate expireDate;

        /** 距失效天数（负数=已过期），服务端算 */
        private Integer expireDays;

        private Integer sourceType;

        private String sourceTypeText;

        private String sourceName;

        private String donorNo;

        private Integer aboVerify;

        private String aboVerifyText;

        private String storageLoc;

        private Integer status;

        /** 状态文本 */
        private String statusText;

        private String inboundBy;

        private LocalDateTime inboundTime;

        private String outboundBy;

        private LocalDateTime outboundTime;

        private String applyNo;
    }

    @Data
    public static class StatsVO {
        private long inStock;
        private long reserved;
        private long issued;
        /** 7 天内到期（含已过期）且仍在库/预留的袋数 */
        private long expireSoon;
        private long todayIn;
        private long todayOut;
        private long pendingMatch;
        /** 按血型在库袋数，index 0~3 = A/B/O/AB */
        private List<TypeCount> byBloodType;
    }

    @Data
    public static class TypeCount {
        /** 血型 */
        private Integer bloodType;
        private String bloodTypeText;
        private long bagCount;
        private long volumeTotal;
    }

    @Data
    public static class CrossmatchVO {
        @JsonSerialize(using = ToStringSerializer.class)
        private Long id;

        private String matchNo;

        private String applyNo;

        /** 患者ID */
        @JsonSerialize(using = ToStringSerializer.class)
        private Long patientId;

        /** 患者编号 */
        private String patientNo;

        /** 患者姓名 */
        private String patientName;

        private Integer patientBloodType;

        private String patientBloodTypeText;

        private Integer patientRhType;

        private String patientRhTypeText;

        /** 血袋号 */
        private String bagNo;

        private Integer bagBloodType;

        private String bagBloodTypeText;

        private Integer bagRhType;

        private Integer componentType;

        private String componentTypeText;

        private Integer volume;

        private Integer method;

        private String methodText;

        private Integer result;

        private String resultText;

        /** 结论 */
        private String conclusion;

        private Integer status;

        /** 状态文本 */
        private String statusText;

        private String operator;

        private LocalDateTime matchTime;

        private String verifier;

        private LocalDateTime verifyTime;
    }

    @Data
    public static class StockLogVO {
        @JsonSerialize(using = ToStringSerializer.class)
        private Long id;

        /** 血袋号 */
        private String bagNo;

        /** 业务类型 */
        private Integer bizType;

        private String bizTypeText;

        private Integer fromStatus;

        private Integer toStatus;

        private String applyNo;

        /** 原因 */
        private String reason;

        private String operator;

        private LocalDateTime operateTime;
    }
}

package com.his.patient.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 体检域 VO。
 */
public class CheckupVO {

    @Data
    public static class PackageVO {
        @JsonSerialize(using = ToStringSerializer.class)
        private Long id;
        private String packageName;
        private String packageCode;
        private Integer genderLimit;
        /**
         * 单价
         */
        private BigDecimal price;
        private String description;
        private Integer status;
        /**
         * 备注
         */
        private String remark;
        /**
         * 明细项集合
         */
        private List<PackageItemVO> items;
    }

    @Data
    public static class PackageItemVO {
        @JsonSerialize(using = ToStringSerializer.class)
        private Long id;
        /**
         * 项目名称
         */
        private String itemName;
        /**
         * 项目类型
         */
        private Integer itemType;
        private String refStandard;
        private BigDecimal amount;
    }

    @Data
    public static class RecordVO {
        @JsonSerialize(using = ToStringSerializer.class)
        private Long id;
        private String recordNo;
        /**
         * 患者ID
         */
        @JsonSerialize(using = ToStringSerializer.class)
        private Long patientId;
        /**
         * 患者姓名
         */
        private String patientName;
        /**
         * 性别（1-男 2-女 9-未知）
         */
        private Integer gender;
        /**
         * 年龄
         */
        private Integer age;
        private String phone;
        private Integer personType;
        @JsonSerialize(using = ToStringSerializer.class)
        private Long packageId;
        private String packageName;
        /**
         * 合计金额
         */
        private BigDecimal totalAmount;
        private LocalDate checkupDate;
        private Integer recordStatus;
        /**
         * 结论
         */
        private String conclusion;
        /**
         * 医生姓名
         */
        private String doctorName;
        private LocalDateTime reportTime;
        /**
         * 备注
         */
        private String remark;
        /**
         * 详情接口返回明细
         */
        private List<ResultVO> results;
    }

    @Data
    public static class ResultVO {
        @JsonSerialize(using = ToStringSerializer.class)
        private Long id;
        /**
         * 项目名称
         */
        private String itemName;
        /**
         * 项目类型
         */
        private Integer itemType;
        private String refStandard;
        private String resultValue;
        private Integer abnormalFlag;
        private String summaryText;
        private String checkerName;
        private LocalDateTime resultTime;
    }
}

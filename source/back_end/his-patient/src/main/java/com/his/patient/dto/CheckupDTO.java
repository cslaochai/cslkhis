package com.his.patient.dto;

import com.his.common.base.PageParam;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * 体检域 DTO。类型命名与 VO 对应：Package / Record / Result 三段。
 */
public class CheckupDTO {

    /**
     * 套餐保存（含项目明细，整单替换）
     */
    @Data
    public static class PackageSave {
        /**
         * 更新必传
         */
        private Long id;
        @NotBlank(message = "套餐名称不能为空")
        private String packageName;
        /**
         * 0不限 1男 2女
         */
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
        @NotEmpty(message = "套餐至少要有一个项目")
        private List<Item> items;

        @Data
        public static class Item {
            /**
             * 项目名称
             */
            @NotBlank(message = "项目名称不能为空")
            private String itemName;
            /**
             * 1检验 2检查 3一般
             */
            private Integer itemType;
            private String refStandard;
            private BigDecimal amount;
        }
    }

    /**
     * 套餐分页
     */
    @Data
    @EqualsAndHashCode(callSuper = true)
    public static class PackageQuery extends PageParam {
        /**
         * 关键字
         */
        private String keyword;
        private Integer status;
    }

    /**
     * 体检登记
     */
    @Data
    public static class RecordCreate {
        /**
         * 患者ID
         */
        @NotNull(message = "体检人不能为空")
        private Long patientId;
        /**
         * 1个人 2团体
         */
        private Integer personType;
        @NotNull(message = "套餐不能为空")
        private Long packageId;
        @NotNull(message = "体检日期不能为空")
        private LocalDate checkupDate;
        /**
         * 备注
         */
        private String remark;
    }

    /**
     * 登记分页
     */
    @Data
    @EqualsAndHashCode(callSuper = true)
    public static class RecordQuery extends PageParam {
        /**
         * 关键字
         */
        private String keyword;
        private Integer recordStatus;
        private Integer personType;
        private LocalDate checkupDate;
    }

    /**
     * 单项结果录入
     */
    @Data
    public static class ResultSave {
        @NotNull(message = "结果行不能为空")
        private Long resultId;
        private String resultValue;
        /**
         * 0正常 1异常 2待查
         */
        private Integer abnormalFlag;
        private String summaryText;
        private String checkerName;
    }

    /**
     * 总检出报告
     */
    @Data
    public static class Conclusion {
        @NotNull(message = "体检登记不能为空")
        private Long recordId;
        /**
         * 结论
         */
        @NotBlank(message = "总检结论不能为空")
        private String conclusion;
        /**
         * 医生姓名
         */
        private String doctorName;
    }
}

package com.his.patient.dto;

import com.his.common.base.PageParam;
import com.his.common.validation.AllowedValues;
import jakarta.validation.Valid;
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
        @AllowedValues(values = {"0", "1", "2"}, message = "性别限制取值不合法")
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
        @Valid
        private List<Item> items;

        @Data
        public static class Item {
            /**
             * 项目来源（1-检验项目 2-检查项目 3-一般检查），决定 itemId 指向哪张项目目录
             */
            @NotNull(message = "请选择项目来源")
            @AllowedValues(values = {"1", "2", "3"}, message = "项目来源取值不合法")
            private Integer itemSource;
            /**
             * 项目目录项目ID：项目名称/编码/价格/参考范围一律以目录为准，入参只认这个ID
             */
            @NotNull(message = "请选择项目")
            private Long itemId;
            /**
             * 项目名称（服务端按目录回填，入参不采用）
             */
            private String itemName;
            /**
             * 项目编码（服务端按目录回填，入参不采用）
             */
            private String itemCode;
            /**
             * 项目类型（1检验 2检查 3一般；服务端按项目来源回填，入参不采用）
             */
            private Integer itemType;
            /**
             * 参考范围（服务端按目录回填）
             */
            private String refStandard;
            /**
             * 单项金额（服务端取目录价格回填）
             */
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
        @AllowedValues(values = {"1", "2"}, message = "人员类型取值不合法")
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
        /**
         * 1个人 2团体
         */
        @AllowedValues(values = {"1", "2"}, message = "人员类型取值不合法")
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
        @AllowedValues(values = {"0", "1", "2"}, message = "结果异常标志取值不合法")
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

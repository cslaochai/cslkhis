package com.his.pharmacy.dto;

import com.his.common.base.PageParam;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.EqualsAndHashCode;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * CSSD 追溯 DTO 集合。
 */
public class CssdDTO {

    /**
     * 回收登记（新建器械包并记录回收节点）
     */
    @Data
    public static class Receive implements Serializable {

        /**
         * 器械包条码（不填自动生成 CSSD+日期+序号）
         */
        private String packNo;

        @NotBlank(message = "器械包名称不能为空")
        private String packName;

        /**
         * 申领/归属科室ID
         */
        private Long deptId;

        /**
         * 科室名称
         */
        private String deptName;

        /**
         * 灭菌方式:1-高压蒸汽 2-环氧乙烷 3-低温等离子（默认 1）
         */
        @Min(value = 1, message = "灭菌方式取值不合法（1-高压蒸汽 2-环氧乙烷 3-低温等离子）")
        @Max(value = 3, message = "灭菌方式取值不合法（1-高压蒸汽 2-环氧乙烷 3-低温等离子）")
        private Integer sterilizeMethod;

        /**
         * 回收操作人（不填取当前登录人）
         */
        private String operatorName;

        /**
         * 备注
         */
        private String remark;
    }

    /**
     * 流转（推进到下一节点；目标节点由当前状态决定）
     */
    @Data
    public static class Advance implements Serializable {

        @NotNull(message = "器械包ID不能为空")
        private Long packId;

        /**
         * 操作人（不填取当前登录人）
         */
        private String operatorName;

        /**
         * 灭菌锅次（推进到灭菌节点必填）
         */
        private String sterilizerNo;

        /**
         * 灭菌批次号（推进到灭菌节点必填）
         */
        private String batchNo;

        /**
         * 节点结果:1-合格 2-不合格（仅灭菌完成节点生效；不合格自动退回清洗）
         */
        @Min(value = 1, message = "节点结果取值不合法（1-合格 2-不合格）")
        @Max(value = 2, message = "节点结果取值不合法（1-合格 2-不合格）")
        private Integer result;

        /**
         * 备注
         */
        private String remark;

        /**
         * 发放节点可补填/更正申领科室
         */
        private Long deptId;

        /**
         * 科室名称
         */
        private String deptName;
    }

    /**
     * 器械包分页查询
     */
    @Data
    @EqualsAndHashCode(callSuper = true)
    public static class QueryPage extends PageParam implements Serializable {

        /**
         * 关键词：条码/名称/科室模糊
         */
        private String keyword;

        /**
         * 包状态:1~6
         */
        private Integer status;

    }

    /**
     * 器械包模板新增/修改（明细整表删旧重插）
     */
    @Data
    public static class TemplateUpsert implements Serializable {

        /**
         * 模板ID（空=新增）
         */
        private Long id;

        @NotBlank(message = "包编码不能为空")
        private String templateCode;

        @NotBlank(message = "器械包名称不能为空")
        private String packName;

        /**
         * 默认灭菌方式:1-高压蒸汽 2-环氧乙烷 3-低温等离子
         */
        @NotNull(message = "默认灭菌方式不能为空")
        @Min(value = 1, message = "灭菌方式取值不合法（1-高压蒸汽 2-环氧乙烷 3-低温等离子）")
        @Max(value = 3, message = "灭菌方式取值不合法（1-高压蒸汽 2-环氧乙烷 3-低温等离子）")
        private Integer sterilizeMethod;

        /**
         * 状态:1-启用 0-停用（默认 1）
         */
        private Integer status;

        /**
         * 备注
         */
        private String remark;

        /**
         * 明细项集合
         */
        @NotEmpty(message = "组成明细至少一条")
        @Valid
        private List<TemplateItemUpsert> items;
    }

    /**
     * 模板组成明细行
     */
    @Data
    public static class TemplateItemUpsert implements Serializable {

        /**
         * 项目名称
         */
        @NotBlank(message = "器械名称不能为空")
        private String itemName;

        private String spec;

        /**
         * 计量单位（默认件）
         */
        private String unit;

        @NotNull(message = "基数不能为空")
        @Min(value = 1, message = "基数必须大于0")
        private Integer quantity;
    }

    /**
     * 模板分页查询
     */
    @Data
    @EqualsAndHashCode(callSuper = true)
    public static class TemplateQueryPage extends PageParam implements Serializable {

        /**
         * 关键词：包编码/名称模糊
         */
        private String keyword;

        /**
         * 状态:1-启用 0-停用
         */
        private Integer status;

    }
}

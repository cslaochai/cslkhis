package com.his.medicaltech.dto;

import com.his.common.base.PageParam;
import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 门诊输液室入参集合（M10）。命名遵循 AGENTS.md：嵌套 DTO 按用途拆分。
 */
public class InfusionRoomDTO {

    /**
     * 座位新增/修改
     */
    @Data
    public static class SeatUpsert {
        /**
         * 座位ID（修改时必传）
         */
        private Long id;
        @NotBlank(message = "座位号不能为空")
        @Size(max = 32, message = "座位号过长")
        private String seatNo;
        @NotBlank(message = "区域不能为空")
        @Size(max = 50, message = "区域过长")
        private String area;
        /**
         * 1-空闲 3-停用（占用由入座流程驱动，不接受直接改）
         */
        @NotNull(message = "座位状态不能为空")
        @Min(1)
        @Max(3)
        private Integer seatStatus;
        /**
         * 备注
         */
        @Size(max = 500, message = "备注过长")
        private String remark;
    }

    /**
     * 入座（建输液单 + 占座）
     */
    @Data
    public static class Admit {
        /**
         * 患者ID
         */
        @NotNull(message = "患者ID不能为空")
        private Long patientId;
        /**
         * 来源治疗记录ID（可空）
         */
        private Long treatmentRecordId;
        @NotBlank(message = "输注内容摘要不能为空")
        @Size(max = 500, message = "输注内容摘要过长")
        private String drugSummary;
        @NotNull(message = "座位不能为空")
        private Long seatId;
        /**
         * 是否需要皮试：1-是（建单后先做皮试） 0-否
         */
        @NotNull(message = "是否皮试不能为空")
        private Integer needSkinTest;
        /**
         * 备注
         */
        @Size(max = 500, message = "备注过长")
        private String remark;
    }

    /**
     * 做皮试
     */
    @Data
    public static class SkinTestCreate {
        @NotNull(message = "输液单不能为空")
        private Long infusionId;
        /**
         * 药品名称
         */
        @NotBlank(message = "皮试药物不能为空")
        @Size(max = 200, message = "皮试药物名称过长")
        private String drugName;
        /**
         * 备注
         */
        @Size(max = 500, message = "备注过长")
        private String remark;
    }

    /**
     * 皮试判读
     */
    @Data
    public static class SkinTestResult {
        @NotNull(message = "皮试记录不能为空")
        private Long skinTestId;
        /**
         * 1-阴性 2-阳性
         */
        @NotNull(message = "判读结果不能为空")
        @Min(1)
        @Max(2)
        private Integer result;
        /**
         * 备注
         */
        @Size(max = 500, message = "备注过长")
        private String remark;
    }

    /**
     * 开始输注
     */
    @Data
    public static class Start {
        @NotNull(message = "输液单不能为空")
        private Long infusionId;
        @NotNull(message = "滴速不能为空")
        @Min(1)
        @Max(200)
        private Integer dripRate;
    }

    /**
     * 巡视
     */
    @Data
    public static class Round {
        @NotNull(message = "输液单不能为空")
        private Long infusionId;
        @Min(1)
        @Max(200)
        private Integer dripRate;
        @Min(0)
        @Max(5000)
        private Integer remainingVolume;
        /**
         * 备注
         */
        @Size(max = 500, message = "备注过长")
        private String remark;
    }

    /**
     * 结束输注
     */
    @Data
    public static class Finish {
        @NotNull(message = "输液单不能为空")
        private Long infusionId;
        /**
         * 不良反应：0-无 1-有
         */
        @NotNull(message = "不良反应标志不能为空")
        @Min(0)
        @Max(1)
        private Integer adverseFlag;
        @Size(max = 500, message = "不良反应描述过长")
        private String adverseDesc;
    }

    /**
     * 取消
     */
    @Data
    public static class Cancel {
        @NotNull(message = "输液单不能为空")
        private Long infusionId;
        /**
         * 取消原因
         */
        @NotBlank(message = "取消原因不能为空")
        @Size(max = 500, message = "取消原因过长")
        private String cancelReason;
    }

    /**
     * 今日输液单分页查询
     */
    @Data
    @EqualsAndHashCode(callSuper = true)
    public static class InfusionQuery extends PageParam {
        /**
         * 状态：null-今日全部
         */
        private Integer status;
        /**
         * 患者姓名/单号模糊
         */
        private String keyword;
    }
}

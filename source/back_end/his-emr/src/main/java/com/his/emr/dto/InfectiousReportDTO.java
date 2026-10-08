package com.his.emr.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 传染病报告卡 DTO 集合。
 */
public class InfectiousReportDTO {

    /**
     * 填卡 / 修改（退报后重报也走它，report_count 服务端递增）
     */
    @Data
    public static class Upsert {
        /**
         * 主键
         */
        private Long id;

        /**
         * 患者ID
         */
        @NotNull(message = "患者ID不能为空")
        private Long patientId;

        /**
         * 门诊就诊ID（与 inpId 二选一）
         */
        private Long registId;

        /**
         * 住院记录ID（与 registId 二选一）
         */
        private Long inpId;

        /**
         * 病种ID
         */
        @NotNull(message = "病种不能为空")
        private Long diseaseId;

        /**
         * 临床摘要
         */
        private String clinicalDesc;

        /**
         * 直接指定发现科室名（可选；不传按门诊挂号科室）
         */
        private String visitDeptName;

        /**
         * 服务端用：登录态填卡人，不采信前端
         */
        private Long reportBy;

        /**
         * 填卡医生姓名
         */
        private String reportByName;

        /**
         * 重报备注（退报重报时必填说明改了什么）
         */
        private String resubmitRemark;
    }

    /**
     * 审核（1→2）
     */
    @Data
    public static class Audit {
        /**
         * 主键
         */
        @NotNull(message = "报卡ID不能为空")
        private Long id;

        private String opinion;

        /**
         * 审核人姓名
         */
        private String auditByName;
    }

    /**
     * 退报（1/2→4，必填原因；修改后重报回 1）
     */
    @Data
    public static class ReturnCard {
        /**
         * 主键
         */
        @NotNull(message = "报卡ID不能为空")
        private Long id;

        /**
         * 原因
         */
        @NotBlank(message = "退报原因必填")
        private String reason;

        /**
         * 审核人姓名
         */
        private String auditByName;
    }
}

package com.his.patient.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.his.common.base.PageParam;
import com.his.common.validation.AllowedValues;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 死亡登记簿（住院死亡登记簿）入参集合。
 */
public class DeathRegistrationDTO {

    /**
     * 分页查询
     */
    @Data
    @EqualsAndHashCode(callSuper = true)
    public static class QueryPage extends PageParam {
        /**
         * 登记号 / 死者姓名关键字
         */
        private String keyword;

        /**
         * 状态（1-草稿 2-已登记 3-已作废）
         */
        @AllowedValues(values = {"1", "2", "3"}, message = "登记状态取值不合法")
        private Integer registerStatus;

        /**
         * 死亡类型（1-疾病死亡 2-非疾病死亡）
         */
        @AllowedValues(values = {"1", "2", "3"}, message = "死亡类型取值不合法")
        private Integer deathType;

        /**
         * 是否已报公安/司法（0-否 1-是）
         */
        @AllowedValues(values = {"0", "1"}, message = "是否已报公安取值不合法")
        private Integer policeFlag;

        /**
         * 是否存在医疗纠纷/患方异议（0-否 1-是）
         */
        @AllowedValues(values = {"0", "1"}, message = "是否存在医疗纠纷取值不合法")
        private Integer disputeFlag;

        /**
         * 开始日期
         */
        @JsonFormat(pattern = "yyyy-MM-dd")
        private LocalDate startDate;

        /**
         * 结束日期
         */
        @JsonFormat(pattern = "yyyy-MM-dd")
        private LocalDate endDate;
    }

    /**
     * 填写/修改登记（草稿可改；已登记后只能作废重登）
     */
    @Data
    public static class Upsert {
        /**
         * 主键（雪花ID）
         */
        private Long id;

        /**
         * 住院记录ID
         */
        @NotNull(message = "请选择住院记录")
        private Long admissionId;

        /**
         * 关联死亡证明（可空：先登记后补证是常态）
         */
        private Long certId;

        /**
         * 死亡类型（1-疾病死亡 2-非疾病死亡）
         */
        @NotNull(message = "死亡类型不能为空")
        @AllowedValues(values = {"1", "2", "3"}, message = "死亡类型取值不合法")
        private Integer deathType;

        /**
         * 是否已报公安/司法（0-否 1-是）
         */
        @AllowedValues(values = {"0", "1"}, message = "是否已报公安取值不合法")
        private Integer policeFlag;

        /**
         * 受理公安机关
         */
        private String policeOrg;

        /**
         * 公安受理/案件编号
         */
        private String policeCaseNo;

        /**
         * 报案时间
         */
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime policeReportTime;

        /**
         * 是否由法医出具/检验（0-否 1-是）
         */
        @AllowedValues(values = {"0", "1"}, message = "是否法医出具取值不合法")
        private Integer forensicFlag;

        /**
         * 尸体处理方式（1-殡仪馆接运 2-家属自行处理 3-病理解剖 4-其他）
         */
        @AllowedValues(values = {"1", "2", "3", "4"}, message = "尸体处理方式取值不合法")
        private Integer bodyDisposal;

        /**
         * 遗体接运/接收单位
         */
        private String bodyUnit;

        /**
         * 遗体移出时间
         */
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime bodyTransportTime;

        /**
         * 办理人/近亲属姓名
         */
        private String relativeName;

        /**
         * 与死者关系
         */
        private String relativeRelation;

        /**
         * 联系电话
         */
        private String relativePhone;

        /**
         * 家属已领取联次（1-记录联 2-户籍联 3-殡葬联 4-家属联）
         */
        private String receivedCopies;

        /**
         * 领取时间
         */
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime receiveTime;

        /**
         * 是否存在医疗纠纷/患方异议（0-否 1-是）
         */
        @AllowedValues(values = {"0", "1"}, message = "是否存在医疗纠纷取值不合法")
        private Integer disputeFlag;

        /**
         * 纠纷/异议情况
         */
        private String disputeDesc;

        /**
         * 备注
         */
        private String remark;
    }

    /**
     * 确认登记（1→2）：非疾病死亡/死因不明必须已报公安
     */
    @Data
    public static class Confirm {
        /**
         * 主键（雪花ID）
         */
        @NotNull(message = "登记ID不能为空")
        private Long id;
    }

    /**
     * 作废（1/2→3）：作废后可对同一次住院重登
     */
    @Data
    public static class VoidRegister {
        /**
         * 主键（雪花ID）
         */
        @NotNull(message = "登记ID不能为空")
        private Long id;

        /**
         * 原因
         */
        @NotBlank(message = "作废原因必填")
        private String reason;
    }
}

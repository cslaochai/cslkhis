package com.his.patient.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;

/**
 * 双向转诊 DTO 集合。
 */
public class ReferralDTO {

    /** 转诊登记（新建） */
    @Data
    public static class Create implements Serializable {

        /** 患者ID */
        @NotNull(message = "患者ID不能为空")
        private Long patientId;

        /** 就诊次ID（门诊转诊时填） */
        private Long visitId;

        /** 入院ID（住院患者转诊时填） */
        private Long admissionId;

        /** 转出科室ID */
        @NotNull(message = "转出科室不能为空")
        private Long fromDeptId;

        /** 转入本院科室（院内转诊时填） */
        private Long toDeptId;

        /** 转入医院名称（院际转诊必填） */
        private String toHospital;

        /** 转诊方向:1-上转 2-下转（默认 1 上转） */
        private Integer direction;

        /** 转诊原因 */
        @NotBlank(message = "转诊原因不能为空")
        private String reason;

        /** 诊断摘要 */
        private String diagnosis;

        /** 联系电话 */
        private String contactPhone;

        /** 备注 */
        private String remark;
    }

    /** 分页查询 */
    @Data
    public static class QueryPage implements Serializable {

        /** 患者ID */
        private Long patientId;

        /** 转诊方向（1-上转 2-下转） */
        private Integer direction;

        /** 状态（0-待确认 1-已确认 2-已完成 3-已取消） */
        private Integer referralStatus;

        /** 转入医院模糊 */
        private String toHospital;

        /** 页码 */
        private Integer pageNum = 1;

        /** 每页条数 */
        private Integer pageSize = 10;
    }

    /** 确认（0→1） */
    @Data
    public static class Audit implements Serializable {

        /** 转诊ID */
        @NotNull(message = "转诊ID不能为空")
        private Long referralId;

        /** 转入本院科室（确认时可补填） */
        private Long toDeptId;

        /** 确认意见 */
        private String auditRemark;
    }

    /** 完成（1→2） */
    @Data
    public static class Finish implements Serializable {

        /** 转诊ID */
        @NotNull(message = "转诊ID不能为空")
        private Long referralId;

        /** 完成备注（转诊结局/接收医院反馈） */
        private String finishRemark;
    }

    /** 取消（0/1→3） */
    @Data
    public static class Cancel implements Serializable {

        /** 转诊ID */
        @NotNull(message = "转诊ID不能为空")
        private Long referralId;

        @NotBlank(message = "取消原因不能为空")
        private String cancelReason;
    }
}

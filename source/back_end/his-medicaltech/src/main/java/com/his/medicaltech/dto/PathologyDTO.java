package com.his.medicaltech.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

/**
 * 病理域入参（静态内部类聚合，避免一个字段一个文件把包撑爆）
 */
public class PathologyDTO {

    /** 登记 / 修改病理申请单 */
    @Data
    public static class OrderUpsert {
        /** 主键；为空=新增（服务端生成病理号），非空=修改（仅已登记状态可改） */
        private Long id;

        /** 患者ID */
        @NotNull(message = "患者ID不能为空")
        private Long patientId;

        /** 患者号 */
        private String patientNo;

        /** 患者姓名 */
        @NotBlank(message = "患者姓名不能为空")
        private String patientName;

        /** 性别（1-男 2-女 9-未知） */
        private Integer gender;

        /** 年龄 */
        private Integer age;

        /** 就诊日期 */
        private LocalDate visitDate;

        /** 申请科室ID */
        private Long applyDeptId;

        /** 申请科室 */
        private String applyDeptName;

        /** 申请医生ID */
        private Long applyDoctorId;

        /** 申请医生 */
        private String applyDoctorName;

        /** 临床诊断 */
        private String clinicalDiagnosis;

        /** 病理检查类型（1-常规石蜡 2-术中冰冻 3-细胞学 4-免疫组化 5-疑难会诊） */
        private Integer examType;

        /** 标本类型（活检/切除/穿刺/脱落细胞等） */
        private String specimenType;

        /** 取材部位 */
        private String specimenPart;

        /** 是否冰冻（0-否 1-是）；exam_type=2 时服务端强制置 1 */
        private Integer isFrozen;

        /** 备注 */
        private String remark;
    }

    /** 分页 / 列表查询（条件一律下推后端，前端不切片） */
    @Data
    public static class Query {
        /** 页码 */
        private Integer pageNum = 1;
        /** 每页条数 */
        private Integer pageSize = 20;

        /** 病理号 */
        private String orderNo;

        /** 患者姓名 */
        private String patientName;

        /** 患者ID */
        private Long patientId;

        /** 病理检查类型（1-常规石蜡 2-术中冰冻 3-细胞学 4-免疫组化 5-疑难会诊） */
        private Integer examType;

        private Integer status;

        /** 开始日期 */
        private LocalDate startDate;

        /** 结束日期 */
        private LocalDate endDate;
    }

    /** 蜡块（取材块）登记 */
    @Data
    public static class BlockUpsert {
        private Long id;

        @NotNull(message = "病理主单ID不能为空")
        private Long orderId;

        @NotBlank(message = "蜡块号不能为空")
        private String blockNo;

        private String partDesc;

        private Integer blockCount;

        private Integer sliceCount;

        private String slideNo;
    }

    /** 蜡块流转动作：1 取材 2 包埋 3 切片 */
    @Data
    public static class BlockAction {
        @NotNull(message = "蜡块ID不能为空")
        private Long blockId;

        /** 1-取材 2-包埋 3-切片 */
        @NotNull(message = "动作类型不能为空")
        private Integer action;
    }

    /** 标本接收 */
    @Data
    public static class Receive {
        @NotNull(message = "病理主单ID不能为空")
        private Long orderId;

        /** 标本类型（活检/切除/穿刺/脱落细胞等） */
        private String specimenType;

        /** 取材部位 */
        private String specimenPart;
    }

    /** 取材 / 包埋 / 制片：主单级推进（3 取材 4 制片） */
    @Data
    public static class Process {
        @NotNull(message = "病理主单ID不能为空")
        private Long orderId;

        /** 3-已取材 4-已制片 */
        @NotNull(message = "目标状态不能为空")
        private Integer targetStatus;
    }

    /** 初诊：填写镜下所见 / 病理诊断 */
    @Data
    public static class Report {
        @NotNull(message = "病理主单ID不能为空")
        private Long orderId;

        /** 肉眼所见 */
        private String grossFindings;

        /** 镜下所见 */
        private String microscopyFindings;

        /** 免疫组化 / 特殊染色结果 */
        private String ihcResult;

        /** 病理诊断 */
        @NotBlank(message = "病理诊断不能为空")
        private String diagnosis;

        /** 建议 */
        private String suggestion;

        /** 术中冰冻快速诊断结果（冰冻单必填） */
        private String frozenResult;
    }

    /** 审核 */
    @Data
    public static class Audit {
        @NotNull(message = "病理主单ID不能为空")
        private Long orderId;

        /** 审核意见（可选，进 remark） */
        private String auditOpinion;
    }

    /** 发布 */
    @Data
    public static class Publish {
        @NotNull(message = "病理主单ID不能为空")
        private Long orderId;
    }

    /** 取消（仅发布前可取消） */
    @Data
    public static class Cancel {
        @NotNull(message = "病理主单ID不能为空")
        private Long orderId;

        /** 取消原因 */
        @NotBlank(message = "取消原因不能为空")
        private String cancelReason;
    }

    /** 由内镜活检送检时创建病理单（跨亚专业联动） */
    @Data
    public static class FromEndoscopy {
        /** 患者ID */
        @NotNull(message = "患者ID不能为空")
        private Long patientId;

        /** 患者号 */
        private String patientNo;

        /** 患者姓名 */
        private String patientName;

        /** 性别（1-男 2-女 9-未知） */
        private Integer gender;

        /** 年龄 */
        private Integer age;

        /** 就诊日期 */
        private LocalDate visitDate;

        /** 申请科室 */
        private String applyDeptName;

        /** 申请医生 */
        private String applyDoctorName;

        /** 临床诊断 */
        private String clinicalDiagnosis;

        /** 取材部位 */
        private String specimenPart;

        private String biopsyPart;

        private Integer biopsyCount;

        /** 来源内镜检查号，写进 remark 便于回溯 */
        private String sourceRecordNo;
    }

    /** 批量蜡块（取材时一次提交多块） */
    @Data
    public static class BlockBatch {
        @NotNull(message = "病理主单ID不能为空")
        private Long orderId;

        private List<BlockUpsert> blocks;
    }
}

package com.his.emr.dto;

import com.his.common.base.PageParam;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 单病种质控入参集合（M4）。
 */
public class SingleDiseaseDTO {

    /** 病种目录新增/修改 */
    @Data
    public static class DiseaseUpsert {
        /** 病种ID（修改时必传） */
        private Long id;
        /** 病种编码 */
        @NotBlank(message = "病种编码不能为空")
        @Size(max = 32, message = "病种编码过长")
        private String diseaseCode;
        /** 病种名称 */
        @NotBlank(message = "病种名称不能为空")
        @Size(max = 100, message = "病种名称过长")
        private String diseaseName;
        /** 纳入 ICD-10 前缀 */
        @NotBlank(message = "纳入 ICD-10 前缀不能为空")
        @Size(max = 200, message = "ICD 前缀过长")
        private String icd10Prefix;
        /** 备注 */
        @Size(max = 500, message = "备注过长")
        private String remark;
    }

    /** 手工纳入病例 */
    @Data
    public static class Enroll {
        @NotNull(message = "病种不能为空")
        private Long diseaseId;
        @NotNull(message = "住院ID不能为空")
        private Long admissionId;
    }

    /** 自动扫描入参（不传区间 = 全部已出院首页） */
    @Data
    public static class AutoEnroll {
        @NotNull(message = "病种不能为空")
        private Long diseaseId;
        /** 出院日期起（含） */
        private String beginDate;
        /** 出院日期止（含） */
        private String endDate;
    }

    /** 病例分页查询 */
    @Data
    @EqualsAndHashCode(callSuper = true)
    public static class CaseQuery extends PageParam {
        /** 病种ID */
        private Long diseaseId;
        /** 质控状态：0-待质控 1-通过 2-异常 */
        private Integer qcStatus;
        /** 上报状态：0-未上报 1-已上报 */
        private Integer reportStatus;
        /** 患者姓名/病例编号模糊 */
        private String keyword;
    }

    /** 质控判级 */
    @Data
    public static class Qc {
        /** 主键 */
        @NotNull(message = "病例不能为空")
        private Long id;
        /** 疗效判定：1-治愈 2-好转 3-未愈 4-死亡 5-其他 */
        @NotNull(message = "疗效判定不能为空")
        private Integer curativeEffect;
        /** 备注 */
        @Size(max = 500, message = "质控意见过长")
        private String remark;
    }
}

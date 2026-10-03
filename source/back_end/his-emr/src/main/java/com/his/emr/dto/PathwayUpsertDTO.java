package com.his.emr.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 临床路径模板新增/修改入参（pathwayUpsert，仅草稿可编辑；步骤整组替换）。
 */
@Data
public class PathwayUpsertDTO implements Serializable {

    /** 主键ID */
    private Long id;

    /** 路径编码 */
    @NotBlank(message = "路径编码不能为空")
    @Size(max = 32, message = "路径编码最长 32")
    private String pathwayCode;

    /** 路径名称 */
    @NotBlank(message = "路径名称不能为空")
    @Size(max = 128, message = "路径名称最长 128")
    private String pathwayName;

    /** 适用科室ID */
    private Long deptId;

    /** 适用病种/诊断 */
    @Size(max = 255, message = "适用病种最长 255")
    private String diagnosis;

    /** 版本号 */
    @Size(max = 16, message = "版本号最长 16")
    private String version;

    /** 备注 */
    @Size(max = 512, message = "备注最长 512")
    private String remark;

    /** 步骤明细（整组替换：不传视为清空，发布前须补齐） */
    @Valid
    private List<StepItem> steps;

    @Data
    public static class StepItem implements Serializable {

        @NotNull(message = "路径日不能为空")
        private Integer dayNo;

        /** 项目类型 */
        @NotNull(message = "项目类型不能为空")
        private Integer itemType;

        /** 项目名称 */
        @NotBlank(message = "项目名称不能为空")
        @Size(max = 128, message = "项目名称最长 128")
        private String itemName;

        /** 字典项目编码（选治疗项目/药品字典时带出；自由文本不传，比对跳过此步骤） */
        @Size(max = 64, message = "项目编码最长 64")
        private String itemCode;

        @Size(max = 500, message = "路径要求最长 500")
        private String content;

        private Integer sortNo;
    }
}

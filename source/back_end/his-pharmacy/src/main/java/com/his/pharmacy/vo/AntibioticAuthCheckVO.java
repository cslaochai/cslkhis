package com.his.pharmacy.vo;

import lombok.Data;

import java.util.List;

/**
 * 抗菌药物处方权校验结果（开方闸与前端提示共用同一份判定）。
 *
 * <p>allowed=false 时 blockedDrugs 列出被闸住的药（药品名 + 分级 + 需要什么级别），
 * 前端只负责把这段文案显示出来，不自己算权限。
 */
@Data
public class AntibioticAuthCheckVO {

    /** 是否允许开这些抗菌药物 */
    private Boolean allowed;

    /** 医师当前有效授权级别（null=无有效授权） */
    private Integer authLevel;

    private String authLevelText;

    /** 被闸住的药品 */
    private List<BlockedDrug> blockedDrugs;

    /** 给医生看的提示文案（服务端生成，前端原样显示） */
    private String tip;

    @Data
    public static class BlockedDrug {

        /** 药品名称 */
        private String drugName;

        private Integer antibioticLevel;

        private String antibioticLevelText;

        /** 开这个药需要的授权级别 */
        private Integer requiredLevel;

        private String requiredLevelText;
    }
}

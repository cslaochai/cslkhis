package com.his.emr.vo;

import cn.hutool.core.annotation.Alias;
import lombok.Data;

import java.io.Serializable;

/**
 * 传染病报卡报文（直报疾控平台）。
 *
 * <p><b>报文即契约</b>：字段名是疾控接口规范定的，对接真实平台时按规范替换本类，
 * 外层报卡流程（建卡 → 审核 → 报送）不变。嵌套的 patient / disease / visit
 * 三段各自建类，不用嵌套 Map —— 嵌套 Map 的键名编译器不管，改错了要等
 * 疾控侧返回错误报文才发现。
 */
@Data
public class InfectiousReportPayloadVO implements Serializable {

    /**
     * 报卡编号
     */
    private String cardNo;

    /**
     * 报告机构编码
     */
    private String orgCode;

    /**
     * 报告机构名称
     */
    private String orgName;

    private Patient patient;

    private Disease disease;

    private Visit visit;

    /**
     * 临床诊断描述
     */
    private String clinicalDesc;

    /**
     * 报告人姓名
     */
    private String reportBy;

    /**
     * 报告时间
     */
    private String reportTime;

    /**
     * 审核人姓名
     */
    private String auditBy;

    /**
     * 审核时间
     */
    private String auditTime;

    /**
     * 报告份数（同一事件的多次报告计数）
     */
    private Integer reportCount;

    /**
     * 患者段。
     */
    @Data
    public static class Patient implements Serializable {

        private String no;

        private String name;

        /**
         * 性别（9-未说明）
         */
        private Integer gender;

        private Integer age;
    }

    /**
     * 病种段。
     */
    @Data
    public static class Disease implements Serializable {

        /**
         * 病种编码
         */
        private String code;

        private String name;

        /**
         * 传染类别（报文键名是 class —— Java 关键字，@Alias 保住对外报文结构）
         */
        @Alias("class")
        private Integer clazz;

        private String icd10;
    }

    /**
     * 就诊段。
     */
    @Data
    public static class Visit implements Serializable {

        private String registId;

        private String inpId;

        private String deptName;
    }
}
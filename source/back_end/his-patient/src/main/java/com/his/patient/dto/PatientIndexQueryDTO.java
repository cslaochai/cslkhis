package com.his.patient.dto;

import com.his.common.base.PageParam;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 患者主索引查询入参（P5.1 EMPI）
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class PatientIndexQueryDTO extends PageParam {

    /** 综合关键字：姓名 / 患者号 / 手机号 / 身份证号四者 OR */
    private String keyword;

    /**
     * 是否包含影子档案（已并入主档的）。
     * <p>默认 false —— 日常查患者看到影子档会让人重复建档；
     * 只有在"追溯/撤销合并"场景才需要打开。
     */
    private Boolean includeShadow;

    /**
     * 只筛某个匹配级别（重复检测用）：1-身份证相同 2-姓名+性别+生日 3-姓名+手机号 4-仅同名
     */
    private Integer matchLevel;

    /** 是否只返回"存在疑似重复"的档案（重复检测用） */
    private Boolean duplicateOnly;
}

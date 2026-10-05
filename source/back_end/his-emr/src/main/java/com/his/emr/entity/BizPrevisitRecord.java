package com.his.emr.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.his.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 患者端预问诊记录（G-05）。
 *
 * <p>一次挂号一份问卷（regist_id 唯一，重复提交覆盖更新）；患者提交后由 AI 环节
 * 把结构化答案凝成一段就诊用病史摘要（模型不可用时规则模板兜底），
 * 医生站接诊时读报告作参考。量表结构在 {@code PrevisitQuestionnaireSupport}
 * 版本化，answers_json 只存题目与作答的回显数据。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("biz_previsit_record")
public class BizPrevisitRecord extends BaseEntity {

    /**
     * 挂号ID（一次挂号一份问卷）
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long registId;

    /**
     * 患者ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long patientId;

    /**
     * 患者号
     */
    private String patientNo;

    /**
     * 患者姓名
     */
    private String patientName;

    /**
     * 就诊科室ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long deptId;

    /**
     * 就诊科室名称
     */
    private String deptName;

    /**
     * 主症状
     */
    private String mainSymptom;

    /**
     * 问答明细JSON（题目与作答回显）
     */
    private String answersJson;

    /**
     * 患者补充描述
     */
    private String freeText;

    /**
     * 病史摘要（模型凝练或规则模板）
     */
    private String summaryAi;

    /**
     * 摘要来源（1-模型 2-规则）
     */
    private Integer summarySource;
}

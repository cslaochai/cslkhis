package com.his.emr.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;

/**
 * 纠纷/投诉登记入参（新增与修改同一接口；仅「待受理」可改）。
 */
@Data
public class DisputeCaseUpsertDTO implements Serializable {

    /** 主键ID */
    private Long id;

    /** 类型（1-服务投诉 2-医疗纠纷 3-医疗损害争议 4-其他） */
    @NotNull(message = "类型不能为空")
    private Integer caseType;

    /** 来源（1-来电 2-来访 3-来信 4-政务热线 5-上级交办 6-院内发现 7-其他） */
    @NotNull(message = "来源不能为空")
    private Integer sourceType;

    /** 等级:1-一般 2-较大 3-重大（默认一般） */
    private Integer level;

    /** 患者ID */
    private Long patientId;

    /** 关联住院ID */
    private Long admissionId;

    /** 被投诉科室ID */
    private Long deptId;

    /** 涉及人员 */
    private String involvedStaff;

    /** 投诉人姓名（可为患者本人/家属/其他） */
    private String complainant;

    /** 与患者关系（1-本人 2-家属 3-代理人 4-其他） */
    private Integer complainantRel;

    /** 投诉人联系电话 */
    private String complainantTel;

    /** 事件发生时间 */
    private String occurTime;

    /** 事件发生地点 */
    private String occurPlace;

    /** 投诉/纠纷内容 */
    @jakarta.validation.constraints.NotBlank(message = "投诉/纠纷内容不能为空")
    private String content;

    /** 投诉人诉求 */
    private String demand;

    /** 是否需封存病历（0-否 1-是） */
    private Integer needSeal;

    /** 备注 */
    private String remark;
}

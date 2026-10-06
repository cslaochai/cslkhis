package com.his.charge.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 扣款通知单新增/修改（id 空=新增草稿；仅「待确认」可改，单号由服务端生成）。
 */
@Data
public class DeductNoticeUpsertDTO {

    /**
     * 主键（空=新增）
     */
    private Long id;

    /**
     * 来源（1-飞检现场发现 2-智能审核/事后复核转来）
     */
    @NotNull(message = "扣款来源不能为空")
    private Integer sourceType;

    /**
     * 关联飞检批次ID（来源=飞检现场时通常有值）
     */
    private Long inspectionId;

    /**
     * 关联医保结算清单ID（可选）
     */
    private Long settlementId;

    /**
     * 就诊类型（1-门诊 2-住院）
     */
    private Integer encounterType;

    /**
     * 就诊标识
     */
    private Long encounterId;

    /**
     * 患者ID
     */
    private Long patientId;

    /**
     * 患者姓名快照
     */
    private String patientName;

    /**
     * 患者编号快照
     */
    private String patientNo;

    /**
     * 被审科室ID
     */
    private Long deptId;

    /**
     * 被审科室名称快照
     */
    private String deptName;

    /**
     * 责任医师姓名快照
     */
    private String doctorName;

    /**
     * 违规类型（字典 his_yb_violation_type）
     */
    @NotNull(message = "违规类型不能为空")
    private Integer violationType;

    /**
     * 违规事实描述（飞检问的就是这一句）
     */
    @NotBlank(message = "违规事实描述不能为空")
    private String violationDesc;

    /**
     * 扣款金额（>0）
     */
    @NotNull(message = "扣款金额不能为空")
    private BigDecimal deductAmount;

    /**
     * 通知日期
     */
    @NotNull(message = "通知日期不能为空")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate noticeDate;

    /**
     * 处理期限（>= 通知日期）
     */
    @NotNull(message = "处理期限不能为空")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate handleDeadline;

    /**
     * 备注
     */
    private String remark;
}

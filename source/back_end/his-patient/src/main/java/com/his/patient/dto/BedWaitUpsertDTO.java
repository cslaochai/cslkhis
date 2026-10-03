package com.his.patient.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

/**
 * 等床登记入参（新增与修改共用）
 *
 * <p><b>患者信息由前端快照传入</b>（与住院证同一口径）：登记屏幕上有这些值，
 * 传过来既省一次跨模块查询，也避免了"患者改了手机号，历史排队单跟着变"。
 * 后端只在 patient_id 上查库核对该患者存在。
 *
 * <p><b>有 {@code id} = 修改，无 {@code id} = 新增</b>。修改只允许改需求类字段
 * （床型/优先级/性别限制/隔离/期望病区/期望日期/备注），<b>已收治的记录禁止修改</b> ——
 * 人已经躺在医院里了，回过头改"当初排队的优先级"没有任何意义，只会让台账失去可信度。
 */
@Data
public class BedWaitUpsertDTO {

    /** 主键：有值=修改，空=新增 */
    private Long id;

    /** 来源住院证ID（无证手工登记时为空） */
    private Long admissionOrderId;

    /** 患者ID */
    @NotNull(message = "患者不能为空")
    private Long patientId;

    /** 患者编号（快照） */
    private String patientNo;

    /** 患者姓名 */
    @NotBlank(message = "患者姓名不能为空")
    private String patientName;

    /** 性别字典口径：1-男 2-女 9-未知 */
    private Integer gender;

    /** 年龄（快照） */
    private Integer age;

    /** 联系电话（快照） */
    private String phone;

    /** 拟收治科室（不传则取拟收治病区所属科室） */
    private Long applyDeptId;

    /** 拟收治科室名称（快照） */
    private String applyDeptName;

    /** 期望病区ID */
    private Long expectWardId;

    /** 需求床型：normal / ICU / VIP，默认 normal */
    private String bedType;

    /** 优先级：1-普通 2-急 3-危重，默认 1 */
    private Integer priority;

    /** 性别限制：0-不限 1-限男床 2-限女床，默认 0 */
    private Integer genderLimit;

    /** 隔离需求：0-否 1-是，默认 0 */
    private Integer isolationFlag;

    /** 预计入院日期 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate expectAdmitDate;

    /** 拟诊名称（快照） */
    private String diagnosisName;

    /** 备注 */
    private String remark;
}

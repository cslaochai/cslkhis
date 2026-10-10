package com.his.patient.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.his.common.validation.InEnum;
import com.his.patient.enums.BedGenderLimitEnum;
import com.his.patient.enums.BedPriorityEnum;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

/**
 * 等床登记入参（新增与修改共用）
 */
@Data
public class BedWaitUpsertDTO {

    /**
     * 主键：有值=修改，空=新增
     */
    private Long id;

    /**
     * 来源住院证ID（无证手工登记时为空）
     */
    private Long admissionOrderId;

    /**
     * 患者ID
     */
    @NotNull(message = "患者不能为空")
    private Long patientId;

    /**
     * 患者编号
     */
    private String patientNo;

    /**
     * 患者姓名
     */
    @NotBlank(message = "患者姓名不能为空")
    private String patientName;

    /**
     * 性别字典口径：1-男 2-女 9-未知
     */
    private Integer gender;

    /**
     * 年龄
     */
    private Integer age;

    /**
     * 联系电话
     */
    private String phone;

    /**
     * 拟收治科室（不传则取拟收治病区所属科室）
     */
    private Long applyDeptId;

    /**
     * 拟收治科室名称
     */
    private String applyDeptName;

    /**
     * 期望病区ID
     */
    private Long expectWardId;

    /**
     * 需求床型：normal / ICU / VIP，默认 normal
     */
    private String bedType;

    /**
     * 优先级：1-普通 2-急 3-危重，默认 1
     */
    @InEnum(value = BedPriorityEnum.class, message = "优先级取值不合法（应为 1-普通 2-急 3-危重）")
    private Integer priority;

    /**
     * 性别限制：0-不限 1-限男床 2-限女床，默认 0
     */
    @InEnum(value = BedGenderLimitEnum.class, message = "性别限制取值不合法（应为 0-不限 1-限男床 2-限女床）")
    private Integer genderLimit;

    /**
     * 隔离需求：0-否 1-是，默认 0
     */
    private Integer isolationFlag;

    /**
     * 预计入院日期
     */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate expectAdmitDate;

    /**
     * 拟诊名称
     */
    private String diagnosisName;

    /**
     * 备注
     */
    private String remark;
}

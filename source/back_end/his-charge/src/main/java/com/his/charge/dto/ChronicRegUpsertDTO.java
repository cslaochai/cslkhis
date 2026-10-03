package com.his.charge.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

/**
 * 慢特病备案新增/修改（id 空=新增；仅「有效」可改，单号由服务端生成）。
 *
 * <p>registerEmpName 不传时服务端回填当前登录人姓名 —— 谁点保存就是谁办的备案；
 * 改成外部经办机构人员时必须在 remark 写明原因。
 */
@Data
public class ChronicRegUpsertDTO {

    private Long id;

    /**
     * 患者ID
     */
    @NotNull(message = "请选择患者")
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
     * 医保卡号快照
     */
    private String medicalInsuranceNo;

    /**
     * 病种目录ID
     */
    @NotNull(message = "请选择慢特病病种")
    private Long catalogId;

    /**
     * 诊断科室ID
     */
    private Long certifyDeptId;

    /**
     * 诊断科室名称
     */
    private String certifyDeptName;

    /**
     * 诊断医师姓名
     */
    private String certifyDoctorName;

    /**
     * 诊断日期
     */
    @NotNull(message = "诊断日期不能为空")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate certifyDate;

    /**
     * 诊断依据（病历摘要/检验结果/出院小结，必填）
     */
    @NotBlank(message = "诊断依据不能为空")
    private String certifyBasis;

    /**
     * 备案经办机构ID（本院医保科 or 外部医保中心，外部可空）
     */
    private Long registerDeptId;

    /**
     * 备案经办机构名称
     */
    private String registerDeptName;

    /**
     * 备案经办人姓名（空=服务端取当前登录人）
     */
    private String registerEmpName;

    /**
     * 备案日期
     */
    @NotNull(message = "备案日期不能为空")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate registerDate;

    /**
     * 待遇生效日
     */
    @NotNull(message = "待遇生效日不能为空")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate validStart;

    /**
     * 待遇终止日（空=长期）
     */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate validEnd;

    /**
     * 备注（外部代办时写明原因）
     */
    private String remark;
}

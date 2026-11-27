package com.his.patient.dto;

import com.his.common.base.PageParam;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 患者信息分页查询入参
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class PatientPageQueryDTO extends PageParam {
    /**
     * 患者姓名（模糊查询）
     */
    private String patientName;
    /**
     * 身份证号（模糊查询，常用输入末 4 位）
     */
    private String idCard;
    /**
     * 性别（1-男 2-女 9-未知，精确匹配）
     */
    private Integer gender;
    /**
     * 婚姻状况（0-未婚 1-已婚 2-离异 3-丧偶，精确匹配）
     */
    private Integer maritalStatus;
    /**
     * 档案状态（0-停用 1-启用，精确匹配）
     */
    private Integer status;
    /**
     * 建档日期起（含当天 00:00:00）
     */
    @Pattern(regexp = "^\\d{4}-\\d{2}-\\d{2}$", message = "建档日期格式应为 yyyy-MM-dd")
    private String createDateStart;
    /**
     * 建档日期止（含当天 23:59:59）
     */
    @Pattern(regexp = "^\\d{4}-\\d{2}-\\d{2}$", message = "建档日期格式应为 yyyy-MM-dd")
    private String createDateEnd;
    /**
     * 联系电话（模糊查询）
     */
    private String phone;
    /**
     * 患者号（模糊查询）
     */
    private String patientNo;
    /**
     * 费别/参保类型（精确匹配；字典 medical_insurance_type）
     */
    private String medicalInsuranceType;
    /**
     * 标签ID（按标签筛选患者）
     */
    private Long tagId;
    /**
     * 综合关键字（模糊匹配：姓名 / 患者号 / 手机号 / 身份证号，四者 OR）
     *
     * <p>用于全局患者搜索框——用户在一个输入框里可能输姓名也可能输手机号/身份证，
     * 逐个字段传值会变成 AND 导致搜不出来。与上面精确字段并存，两者都传时是 AND 关系。
     */
    private String keyword;
}

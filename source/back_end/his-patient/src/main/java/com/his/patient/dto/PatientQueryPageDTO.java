package com.his.patient.dto;

import com.his.common.base.PageParam;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 患者信息分页查询入参
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class PatientQueryPageDTO extends PageParam {
    /**
     * 患者姓名（模糊查询）
     */
    private String patientName;
    /**
     * 联系电话（模糊查询）
     */
    private String phone;
    /**
     * 患者号（模糊查询）
     */
    private String patientNo;
    /**
     * 患者类型：1-自费 2-城镇职工医保 3-城乡居民医保 4-公费 5-其他
     */
    private Integer patientType;
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

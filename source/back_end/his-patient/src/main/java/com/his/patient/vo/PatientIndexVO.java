package com.his.patient.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 患者主索引条目（P5.1 EMPI）
 *
 * <p>比普通患者 VO 多三块信息，都是"判断这两份档案要不要合并"必须看的：
 * ① 主档归属（masterId / mergeStatus / 名下的影子数）；
 * ② 档案完整度（缺哪几个关键字段）；
 * ③ 关联业务数据量（合掉它意味着多少条数据会归到主档名下）。
 */
@Data
public class PatientIndexVO {

    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    /** 患者编号 */
    private String patientNo;
    /** 患者姓名 */
    private String patientName;
    /** 性别（1-男 2-女 9-未知） */
    private Integer gender;
    /** 性别文案（口径见 PatientGenderText，0 报"未知(0)"而不是"女"） */
    private String genderText;
    private LocalDate birthDate;
    /** 年龄 */
    private Integer age;
    private String idCard;
    private String phone;
    private String address;
    private String medicalInsuranceType;
    /** 血型 */
    private String bloodType;
    /** 状态：0-停用 1-启用 */
    private Integer status;

    // 主索引归属
    @JsonSerialize(using = ToStringSerializer.class)
    private Long masterId;
    /** 0-正常 1-已并入主档（本档案失效，只在追溯时可见） */
    private Integer mergeStatus;
    private LocalDateTime mergeTime;
    /** 自己是影子时，主档的患者号/姓名（便于一眼看出被并到谁名下） */
    private String masterNo;
    private String masterName;
    /** 自己是主档时，名下影子档案数量 */
    private Integer shadowCount;
    /** 详情用：同一主档下的其他档案（"这一串是一个人"） */
    private List<Map<String, Object>> siblingPatients;

    // 档案完整度（关键字段，不含 photo/balance 这类无关字段）
    private Integer completeCount;
    private Integer totalFieldCount;
    private Double completeRate;
    /** 缺失关键字段的中文名列表（不达标要能定位到字段） */
    private List<String> missingFields;

    // 关联业务数据量
    private List<DataCountItem> dataCounts;
    private Integer totalDataCount;

    /** 是否可作为被合并方（影子档案不可再被并，影子不能当主档） */
    private Boolean canMerge;
    /** 是否可作为主档（同上） */
    private Boolean canBeMaster;

    /** 业务数据量单项：key 与 PatientIndexMapper 的 UNION ALL 常量一致 */
    @Data
    public static class DataCountItem {
        private String key;
        private String label;
        private Integer count;
    }
}

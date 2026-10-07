package com.his.patient.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.io.Serializable;

/**
 * 同一主档下的其他档案（详情页「这一串是一个人」列表，对应 {@code PatientIndexServiceImpl#briefOf}）。
 *
 * <p><b>为什么它不是动态结构</b>：原实现是 {@code Map<String,Object>}，但从头到尾 put 的
 * 就是这 8 个固定键，值全部来自 {@code BizPatient} 单一实体，没有任何"前端按数据决定列"的成分
 * —— 前端 {@code PatientIndexView.vue} 也是按 {@code s.patientNo / s.patientName / s.genderText /
 * s.idCard / s.mergeTime} 逐字段取，不是遍历键。所以判定为聚合结果，拆成有类型的 VO。
 *
 * <p>字段名与原 Map 的键逐一对齐，出参 JSON 结构不变。
 */
@Data
public class PatientSiblingVO implements Serializable {

    /**
     * 档案ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    /**
     * 患者编号
     */
    private String patientNo;

    /**
     * 患者姓名
     */
    private String patientName;

    /**
     * 性别文案（口径见 SysGenderEnum.getText，null→"未知"，脏值报"未知(0)"而不是"女"）
     */
    private String genderText;

    /**
     * 身份证号
     */
    private String idCard;

    /**
     * 手机号
     */
    private String phone;

    /**
     * 0-正常 1-已并入主档
     */
    private Integer mergeStatus;

    /**
     * 并入时间
     */
    private String mergeTime;
}

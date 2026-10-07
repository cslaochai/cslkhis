package com.his.patient.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * 主索引「档案关联业务数据量」原始计数行（对应 {@code PatientIndexMapper.countDataByPatientIds} 的
 * 10 段 UNION ALL）。
 *
 * <p>一次 UNION ALL 拿全 10 张跨模块表（挂号/就诊/门诊病历/处方/收费/入院/检查/检验/治疗/住院病历）的条数，
 * 逐表逐档案查会变成请求风暴，所以走一次往返。
 *
 * <p>为什么字段叫 {@code dataTable} 而不是原来的 {@code k}/{@code n}：
 * 裸 Map 时 {@code k}/{@code n} 只有 service 里 {@code row.get("k")} 一处能看出是什么，
 * 写错 key 编译不报错、运行时静默丢数据。VO 字段一改名，SQL 别名对不上立刻编译/映射出错。
 *
 * <p>{@code dataTable} 的取值是 {@code PatientDataTables} 里的表标识常量
 * （regist / visit / outpatientRecord / prescription / charge / admission /
 * inspection / laboratory / treatment / inpatientRecord），
 * 展示文案由 {@code PatientDataTables.LABELS} 提供，本类只管计数。
 */
@Data
public class PatientDataCountVO implements Serializable {

    /**
     * 数据来源表标识（见 PatientDataTables 的 key）
     */
    private String dataTable;

    /**
     * 患者ID
     */
    private Long patientId;

    /**
     * 该患者在该表下的数据条数
     */
    private Long cnt;
}

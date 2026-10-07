package com.his.patient.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDate;

/**
 * 档案合并审计快照（对应 {@code PatientIndexServiceImpl#snapshot}）。
 *
 * <p>合并只建立"这两份是同一人"的指向，不搬业务数据，所以合并前后各自的关键字段必须
 * 有一份纸面记录（落 {@code biz_patient_merge_log.master_snapshot / merged_snapshot}）。
 * 撤销时靠它还原档案的在册状态（{@code status}）—— 没有这份快照，撤销就只能靠猜。
 *
 * <p><b>字段名即审计契约</b>：这份 JSON 已经落库，字段名改了，历史记录里的 status 就读不出来
 * （撤销会静默走默认状态）。所以字段名与原 Map 的键逐一对齐，不做"顺手规范化"。
 *
 * <p>与 {@code PatientSiblingVO} 分开的原因：那个是给人看的列表（要性别文案），
 * 这个是给撤销逻辑读的审计底稿（要 gender / status 原始码值），用途与字段集合都不同。
 */
@Data
public class PatientMergeSnapshotVO implements Serializable {

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
     * 性别码（1-男 2-女 9-未知）
     */
    private Integer gender;

    /**
     * 出生日期
     */
    private LocalDate birthDate;

    /**
     * 身份证号
     */
    private String idCard;

    /**
     * 手机号
     */
    private String phone;

    /**
     * 在册状态：0-停用 1-启用。撤销合并时按它还原，不能默认启用。
     */
    private Integer status;

    /**
     * 主索引状态（0-正常 1-已并入主档）
     */
    private Integer mergeStatus;
}

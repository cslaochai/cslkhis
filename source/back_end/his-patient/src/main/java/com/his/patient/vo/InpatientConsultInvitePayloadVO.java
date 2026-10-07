package com.his.patient.vo;

import cn.hutool.core.annotation.Alias;
import lombok.Data;

import java.io.Serializable;

/**
 * 会诊邀请通知载荷（对应 {@code InpatientConsultationServiceImpl} 的会诊邀请）。
 *
 * <p>{@code isUrgent} 用 Hutool {@code @Alias} 保住对外键名：Java 侧字段叫 urgent
 * （urgent 才是语义，装"是否紧急"这个含义），但收件箱摘要 chips 与历史报文读的是 isUrgent。
 * 实测 Hutool 只认打在**字段**上的 {@code @Alias}，打在 getter 上无效。
 */
@Data
public class InpatientConsultInvitePayloadVO implements Serializable {

    /**
     * 患者姓名
     */
    private String patientName;

    /**
     * 会诊单号
     */
    private String consultationNo;

    /**
     * 会诊科室名称
     */
    private String toDeptName;

    /**
     * 会诊理由
     */
    private String reason;

    /**
     * 是否急会诊（对外键名 isUrgent）
     */
    @Alias("isUrgent")
    private Boolean urgent;
}

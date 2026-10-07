package com.his.medicaltech.vo;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * CDR 急诊节点行（{@code CdrMapper#selectEmergencies} 一行）。
 *
 * <p>急诊单既不挂挂号也不挂住院，它自己就是一次就诊 —— 所以锚点是它自己的ID
 * （{@code anchor_type = EMERGENCY}），事件挂在它下面。
 */
@Data
public class CdrEmergencyRowVO implements Serializable {

    /**
     * 急诊记录ID（字符串，避免前端丢精度）
     */
    private String emergencyId;

    /**
     * 急诊单号
     */
    private String emergencyNo;

    /**
     * 数据归属档案ID（EMPI 归并后可能不是主档）
     */
    private String ownerPid;

    /**
     * 入急诊时间
     */
    private LocalDateTime admissionTime;

    /**
     * 结束时间（未结束为 null）
     */
    private LocalDateTime finishTime;

    /**
     * 急诊状态（1-候诊 2-诊治中 3-留观 4-转住院 5-离院 6-死亡）
     */
    private Integer emergencyStatus;

    /**
     * 分诊级别（1-I级濒危 2-II级危重 3-III级急症 4-IV级非急症）
     */
    private Integer triageLevel;

    /**
     * 急诊分区（红区/黄区/绿区）
     */
    private String zone;

    /**
     * 科室名称
     */
    private String deptName;

    /**
     * 医生姓名
     */
    private String doctorName;

    /**
     * 初步诊断（急诊节点的 outcome，也是"缺初步诊断"完整性缺口的判据）
     */
    private String diagnosis;

    /**
     * 主诉
     */
    private String chiefComplaint;
}
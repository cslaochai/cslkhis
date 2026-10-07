package com.his.medicaltech.vo;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDate;

/**
 * CDR 健康档案行（{@code CdrMapper#PROFILE_SQL} 一行）。
 *
 * <p>患者级信息（过敏史/既往史/手术史/家族史/用药史/联系人），不属于任何一次就诊，
 * 所以不上时间轴，单列在身份卡后面。六个分支用 {@code pkey} 区分来源表。
 *
 * <p>{@code tm} 各分支列语义不同但**都是 date**（过敏发生日/诊断日/手术日/开始用药日），
 * 家族史与联系人天然没有日期（SQL 里CAST 成 NULL AS DATE）。
 */
@Data
public class CdrProfileRowVO implements Serializable {

    /**
     * 档案分组键：allergy/pastDisease/surgery/family/medication/contact
     */
    private String pkey;

    /**
     * 来源记录主键（字符串，避免前端丢精度）
     */
    private String sid;

    /**
     * 数据归属档案ID（EMPI 归并后可能不是主档）
     */
    private String ownerPid;

    /**
     * 条目标题（过敏原名/疾病名/手术名/亲属姓名/药品名）
     */
    private String title;

    /**
     * 条目说明（严重程度/当前状态/术后诊断/健康状态/用法用量/电话地址）
     */
    private String summary;

    /**
     * 发生/记录日期（家族史与联系人为 null）
     */
    private LocalDate tm;
}
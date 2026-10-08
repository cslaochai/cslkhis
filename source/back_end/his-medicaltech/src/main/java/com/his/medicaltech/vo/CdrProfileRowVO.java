package com.his.medicaltech.vo;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDate;

/**
 * CDR 健康档案行（CdrMapper#PROFILE_SQL 一行）。
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
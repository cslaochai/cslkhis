package com.his.medicaltech.vo;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * CDR 归并进来的档案身份行（{@code CdrMapper#selectArchiveIdentities} 一行）。
 *
 * <p>用途只有一个：把"这条数据其实挂在别的档案号下"这件事说清楚。
 * EMPI 归并只合并**查询范围**，不做数据搬迁 —— 影子档案下的历史记录仍在原档案号里，
 * 页面必须显式告诉看的人"你看到的这条数据属于哪个档案号"，否则追溯时会怀疑数据串了。
 */
@Data
public class CdrArchiveIdentityRowVO implements Serializable {

    /**
     * 档案ID（字符串，避免前端丢精度；与事件行的 ownerPid 直接比对）
     */
    private String pid;

    /**
     * 患者号
     */
    private String patientNo;

    /**
     * 患者姓名
     */
    private String patientName;

    /**
     * 并入主档时间（从未被并入过的主档为 null）
     */
    private LocalDateTime mergeTime;

    /**
     * 主索引状态（0-正常主档 1-已并入主档）
     */
    private Integer mergeStatus;
}
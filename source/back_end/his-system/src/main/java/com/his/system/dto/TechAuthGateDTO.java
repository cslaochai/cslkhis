package com.his.system.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * 技术授权准入闸入参（跨模块调用：his-patient 开手术单/排台、his-medicaltech 执行内镜时提交）。
 *
 * <p>为什么闸门做成「一句调用」而不是让调用方自己判：判完还要写越权登记，
 * 四个入口各写一遍分支必然漂移（有的忘了登记、有的把择期也放行了）。
 * 授权判定与越权留痕必须收在同一个方法里。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TechAuthGateDTO {

    /**
     * 操作者（术者/麻醉医师/内镜医师）员工ID
     */
    private Long employeeId;

    /**
     * 操作者姓名（employeeId 为空时按姓名回捞员工档案）。
     * <p>日间手术的「主刀」与内镜记录的「术者」在库里是姓名字符串（历史落库口径），
     * 让调用方各自去查员工等于把员工表摊到四个模块；解析收在这里，
     * 且查不到或重名一律 fail-closed —— 判不出是谁就不能判他有没有权限。
     */
    private String employeeName;

    /**
     * 授权类别（1-手术 2-麻醉 3-内镜与介入，见 TechAuthCategoryEnum）
     */
    private Integer authCategory;

    /**
     * 该操作要求的级别上限（1~4）
     */
    private Integer requiredLevel;

    /**
     * 术式/操作编码（可选）。授权记录带限定术式白名单时才比对：
     * 传了编码且白名单里没有 → 拒；不传编码则只比级别（无法判定时不做臆测拦截）。
     */
    private String itemCode;

    /**
     * 操作日期（null=按当天；补录历史单据时传操作日，否则会用「今天的授权」判「当时有没有权限」）
     */
    private LocalDate operateDate;

    /**
     * 是否急诊（true 时授权不足不拦，改为写越权登记；择期一律拒单）
     */
    private boolean emergency;

    /**
     * 来源单据类型（1-手术申请 2-日间手术 3-住院医嘱 4-内镜记录，见 TechOverrideSourceEnum）
     */
    private Integer sourceType;

    /**
     * 来源单据ID（急诊放行时用来写越权登记；非急诊可留空）
     */
    private Long sourceId;

    /**
     * 来源单据号（快照，越权台账回查用）
     */
    private String sourceNo;

    /**
     * 越权原因（急诊放行必填；为空时由服务端兜底成「急诊/抢救越权」，不允许留 NULL）
     */
    private String reason;
}

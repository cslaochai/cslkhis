package com.his.appoint.dto;

import com.his.common.base.PageParam;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * 队列查询入参（分诊台）
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class QueueQueryDTO extends PageParam {
    /**
     * 排队状态（2-候诊中 3-就诊中 4-已就诊 5-已退号 6-已过号 7-已失效）
     */
    private Integer queueStatus;
    /**
     * 排队状态多选。与 queueStatus 同时传时以本字段为准。
     */
    private List<Integer> queueStatuses;
    /**
     * 医生ID
     */
    private Long doctorId;
    /**
     * 科室ID。为空时收窄到当前登录用户的科室（保持原语义）；
     * 显式传入时按传入值过滤 —— 分诊台要能切诊区。
     */
    private Long deptId;
    /**
     * 查询日期（yyyy-MM-dd）
     */
    private String date;
    /**
     * 就诊日期（yyyy-MM-dd）。与 date 的区别：date 过滤 arrive_time，本字段过滤 visit_date。
     * 跨日遗留的在院队列不会被 date 截断捞出来。
     */
    private String visitDate;
    /**
     * 是否只看未经护士核验的分级（true=只看 triage_status=0）。
     * <p>注意这不再是「不能接诊」的筛选 —— 签到即给 4 级默认等级，未核验的照样能叫号。
     */
    private Boolean unTriageOnly;
    /**
     * 关键词：患者姓名 / 患者号 / 就诊号 / 排队号，模糊匹配
     */
    private String keyword;
    /**
     * 查询开始时间（yyyy-MM-dd HH:mm:ss）
     */
    private String startTime;
    /**
     * 查询结束时间（yyyy-MM-dd HH:mm:ss）
     */
    private String endTime;
}

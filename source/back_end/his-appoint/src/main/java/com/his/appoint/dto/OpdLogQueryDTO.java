package com.his.appoint.dto;

import com.his.common.base.PageParam;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * 门诊日志查询入参。
 *
 * <p>与 {@link QueueQueryDTO} 分开而不是扩它：{@code QueueQueryDTO} 是分诊台的取数口径
 * （被 <b>当前登录用户科室</b> 强制收窄），而门诊日志是跨科室的查询分析页。
 * 两个口径塞进一个 DTO，迟早有人把「日志能看到全院」当成「分诊台也能看到全院」。
 *
 * <p>筛选条件一律由前端传、后端下推，禁止前端拿当前页做切片过滤。
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class OpdLogQueryDTO extends PageParam {

    /**
     * 就诊日期起（yyyy-MM-dd，含）
     */
    private String startDate;

    /**
     * 就诊日期止（yyyy-MM-dd，含）
     */
    private String endDate;

    /**
     * 单科室筛选
     */
    private Long deptId;

    /**
     * 多科室筛选（与 deptId 同时传时取并集，任一命中即可）
     */
    private List<Long> deptIds;

    /**
     * 医生（员工ID）
     */
    private Long doctorId;

    /**
     * 就诊状态多选，取值见 {@code OpdLogStatusEnum}：
     * 0 未缴费 / 1 待签到 / 2 候诊中 / 3 就诊中 / 4 已就诊 / 5 已退号 / 6 已过号。
     * 空 = 不限。
     */
    private List<Integer> statusCodes;

    /**
     * 号别：1 普通号 2 专家号 3 急诊号 4 免费号
     */
    private Integer registType;

    /**
     * 结算方式：1 自费 2 城镇职工医保 3 城乡居民医保 4 公费 5 商业保险
     */
    private Integer settlementType;

    /**
     * 就诊类型：1 初诊 2 复诊（读挂号信息.revisit_type）。
     * <p>注意不要改成就诊类型——那一列 218 行恒为 1，是死列。
     */
    private Integer revisitType;

    /**
     * 关键词：患者姓名 / 就诊号 / 门诊号 / 排队号 / 医保卡号
     */
    private String keyword;
}

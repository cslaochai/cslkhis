package com.his.patient.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.his.common.base.PageParam;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;
import java.util.List;

/**
 * 膳食方案分页查询。
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class DietPlanQueryPageDTO extends PageParam {

    /** 入院ID */
    private Long admissionId;

    /** 科室ID */
    private Long deptId;

    /** 病区ID */
    private Long wardId;

    /** 来源（1-医嘱校对派生 2-营养师手工登记） */
    private Integer source;

    /** 饮食类别（1-基本饮食 2-治疗饮食 3-诊断试验饮食 4-营养支持） */
    private Integer dietCategory;

    /** 给食途径：1-口服 2-管饲 3-静脉 */
    private Integer route;

    /** 饮食类型码 */
    private String dietCode;

    /** 方案状态（1-执行中 2-已停止 3-已作废） */
    private Integer planStatus;

    /** 营养科接收状态（0-待接收 1-已接收 2-已退回） */
    private Integer confirmStatus;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate beginDate;

    /** 结束日期 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate endDate;

    /** 关键字：患者姓名 / 患者编号 / 住院号 / 膳食方案号 / 医嘱号 */
    private String keyword;

    /** 服务端填入的科室数据权限集合 */
    private List<Long> scopeDeptIds;
}

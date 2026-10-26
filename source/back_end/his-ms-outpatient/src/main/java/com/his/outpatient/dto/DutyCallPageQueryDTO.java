package com.his.outpatient.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.his.common.base.PageParam;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;

/**
 * 听班呼叫单分页查询（sql/297）。
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class DutyCallPageQueryDTO extends PageParam {

    /** 起始日期（含） */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate beginDate;

    /** 截止日期（含） */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate endDate;

    /** 呼叫发起科室ID */
    private Long deptId;

    /** 被叫层级：2-二线 3-三线 */
    private Integer targetLevel;

    /** 被叫值班人 */
    private Long targetEmpId;

    /** 状态：0-待到岗 1-已到岗 2-已超时升级 3-已取消 */
    private Integer status;

    /**
     * 「我的」视角（当前登录人相关）：
     * <ul>
     *   <li>1 = 待我到岗（我是被叫值班人，且呼叫还没到岗）</li>
     *   <li>2 = 我发起的（我是呼叫发起人）</li>
     * </ul>
     * 留空 = 全部。前端用单选切换，避免「待我到岗」和「我发起的」两个勾选框互相打架。
     */
    private Integer mineType;
}

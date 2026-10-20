package com.his.system.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.his.common.base.PageParam;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 员工排班变更记录（台账）全局分页入参。
 * 跨排班单元回看换班/代班/停班等所有留痕，最新在前。
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class StaffScheduleChangePageQueryDTO extends PageParam {

    /**
     * 变更发生起始时间（含）
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime occurStart;

    /**
     * 变更发生结束时间（含）
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime occurEnd;

    /**
     * 变更动作类型（1-换班 2-代班 3-停班 4-加号 5-减号 6-出诊变更），空=全部
     */
    private Integer actionType;

    /**
     * 关键词（原/新值班人姓名或工号模糊匹配）
     */
    private String keyword;
}

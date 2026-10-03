package com.his.emr.vo;

import lombok.Data;

/**
 * 病案编码任务工作台统计 VO
 */
@Data
public class ArchiveCodeTaskStatsVO {

    /** 待编码 */
    private Long pending;

    /** 已提交待审核 */
    private Long submitted;

    /** 已完成 */
    private Long done;

    /** 已退修（在途） */
    private Long rework;
}

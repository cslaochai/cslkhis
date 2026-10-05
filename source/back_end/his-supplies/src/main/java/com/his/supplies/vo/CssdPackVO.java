package com.his.supplies.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * CSSD 器械包 VO（详情附带全量追溯链）。
 */
@Data
public class CssdPackVO {

    /**
     * 器械包ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    /**
     * 器械包条码
     */
    private String packNo;
    /**
     * 器械包名称
     */
    private String packName;

    /**
     * 申领/归属科室ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long deptId;
    /**
     * 申领/归属科室名称
     */
    private String deptName;

    /**
     * 灭菌方式（1-高压蒸汽 2-环氧乙烷 3-低温等离子）
     */
    private Integer sterilizeMethod;
    private String sterilizeMethodText;

    /**
     * 包状态（1-已回收 2-清洗中 3-已打包 4-灭菌中 5-待发放 6-已发放）
     */
    private Integer status;
    /**
     * 状态文本
     */
    private String statusText;

    /**
     * 最近灭菌锅次
     */
    private String sterilizerNo;
    /**
     * 灭菌批次号
     */
    private String batchNo;

    /**
     * 最近流转时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime lastNodeTime;

    /**
     * 创建人
     */
    private String createBy;

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    /**
     * 详情时附带：追溯链（按节点时间正序）
     */
    private List<CssdTraceVO> traces;
}

package com.his.medicaltech.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.his.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 病理蜡块与切片明细（病理蜡块与切片）
 *
 * <p>状态机：1 待取材 → 2 已取材 → 3 已包埋 → 4 已切片（单向推进）。
 * 主单进入「已制片(4)」的前置条件是至少有一块已取材（status >= 2），
 * 这条规则在 PathologyService 收口 —— 没有蜡块就说"制片完成"是数据造假。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("biz_pathology_block")
public class BizPathologyBlock extends BaseEntity {

    /** 病理主单ID */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long orderId;

    /** 病理号 */
    private String orderNo;

    /** 蜡块号（如 A1 / B2） */
    private String blockNo;

    /** 取材部位描述 */
    private String partDesc;

    /** 蜡块数 */
    private Integer blockCount;

    /** 切片数 */
    private Integer sliceCount;

    /** 切片号 */
    private String slideNo;

    /** 状态（1-待取材 2-已取材 3-已包埋 4-已切片） */
    private Integer status;

    /** 取材人 */
    private String samplingBy;

    /** 取材时间 */
    private LocalDateTime samplingTime;

    /** 包埋人 */
    private String embeddingBy;

    /** 包埋时间 */
    private LocalDateTime embeddingTime;

    /** 切片人 */
    private String sliceBy;

    /** 切片时间 */
    private LocalDateTime sliceTime;
}

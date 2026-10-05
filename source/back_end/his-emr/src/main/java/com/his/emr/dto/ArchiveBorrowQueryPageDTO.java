package com.his.emr.dto;

import com.his.common.base.PageParam;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 病案借阅/复印查询入参
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ArchiveBorrowQueryPageDTO extends PageParam {

    /**
     * 单号 BR+yyyyMMdd+4位
     */
    private String borrowNo;

    /**
     * 类型（1-借阅 2-复印）
     */
    private Integer borrowType;

    /**
     * 状态（1-待审核 2-已借出 3-已归还 4-已拒绝 5-已复印）
     */
    private Integer status;

    /**
     * 关键词（病历号/患者姓名/用途）
     */
    private String keyword;
}

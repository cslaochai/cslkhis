package com.his.spi.vo;

import lombok.Data;

/**
 * 闭环文书落库回执（供发起单据回指）。
 */
@Data
public class ClosedLoopRecordVO {

    /** 主键ID */
    private Long id;

    /** 文书编号 */
    private String recordNo;
}

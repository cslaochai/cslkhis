package com.his.pharmacy.dto;

import com.his.common.base.PageParam;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 药品追溯码台账分页查询入参
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class DrugTraceQueryPageDTO extends PageParam {

    /** 关键字（追溯码 / 药品名称 / 药品编码 / 批号 / 患者姓名） */
    private String keyword;

    /** 药品ID */
    private Long drugId;

    /** 采集挂靠批次ID */
    private Long stockId;

    /** 码状态（1-在库 2-已发药核销 3-已作废） */
    private Integer status;

    /** 上传状态（0-待上传 1-已上传 2-上传失败） */
    private Integer uploadStatus;

    /** 码制（1-GS1 2-中国药品追溯码20位 3-其他） */
    private Integer codeType;

    /** 采集来源（1-入库采集 2-存量补采） */
    private Integer sourceType;

    /** 患者ID（从患者反查用到过哪些码） */
    private Long patientId;

    /** 发药单ID（看这张发药单核销了哪些码） */
    private Long dispensingId;
}

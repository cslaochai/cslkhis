package com.his.pharmacy.dto;

import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

/**
 * 追溯码批量上传入参（上传医保局平台）
 *
 * <p>{@code ids} 为空 = 上传全部「待上传 + 上传失败」的码（受 limit 限制）；
 * 非空 = 只上传指定 id。失败的记录必须落原因且可再次上传（重传不新增数据，只改状态）。
 */
@Data
public class DrugTraceUploadDTO {

    /** 主键ID集合 */
    @Size(max = 500, message = "单次上传最多 500 条")
    private List<Long> ids;

    /** 未指定 ids 时的抓取上限（默认 200，最大 500） */
    private Integer limit = 200;
}

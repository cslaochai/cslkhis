package com.his.pharmacy.dto;

import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

/**
 * 追溯码批量上传入参（上传医保局平台）
 */
@Data
public class DrugTraceUploadDTO {

    /** 主键ID集合 */
    @Size(max = 500, message = "单次上传最多 500 条")
    private List<Long> ids;

    /** 未指定 ids 时的抓取上限（默认 200，最大 500） */
    private Integer limit = 200;
}

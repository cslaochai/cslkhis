package com.his.outpatient.vo;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 急诊值班表 PDF 访问信息。
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErDutyPdfVO {

    @Schema(description = "值班日期 yyyy-MM-dd")
    private String date;

    @Schema(description = "落盘相对路径，如 uploads/erDutyPdf/20261026/erDuty_2026-10-26.pdf")
    private String relativePath;

    @Schema(description = "浏览器可直链访问地址（/api 前缀 + 相对路径）")
    private String url;

    @Schema(description = "true=复用已有文件，false=本次新生成")
    private Boolean existed;
}

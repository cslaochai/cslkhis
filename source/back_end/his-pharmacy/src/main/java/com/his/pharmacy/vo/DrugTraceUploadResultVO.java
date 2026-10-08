package com.his.pharmacy.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.util.List;

/**
 * 批量上传结果VO（总数 / 成功 / 失败 + 失败明细）
 */
@Data
public class DrugTraceUploadResultVO {

    /** 本次上传批次号 */
    private String uploadBatchNo;
    /** 总条数 */
    private Integer total;
    private Integer success;
    private Integer failed;
    private List<FailItem> failures;

    @Data
    public static class FailItem {
        @JsonSerialize(using = ToStringSerializer.class)
        private Long traceId;
        private String traceCode;
        /** 药品名称 */
        private String drugName;
        /** 原因 */
        private String reason;
    }
}

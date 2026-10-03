package com.his.pharmacy.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.util.List;

/**
 * 批量上传结果VO（总数 / 成功 / 失败 + 失败明细）
 *
 * <p>失败明细必须回前端：医保平台拒收的原因（码不存在 / 批号不符 / 重复上传）要能落到具体哪一行，
 * 否则窗口只能看到"3 条失败"却不知道是哪 3 条。
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

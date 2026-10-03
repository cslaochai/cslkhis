package com.his.pharmacy.service;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

public interface DrugTraceUploadChannelService {

    /**
     * 上传事件行
     */
    @Data
    public static class UploadLine {
        /** 台账ID */
        private Long traceId;
        /** 追溯码原文 */
        private String traceCode;
        /** 院内追溯流水号 */
        private String traceNo;
        /** 药品编码 */
        private String drugCode;
        /** 药品名称 */
        private String drugName;
        /** 批准文号 */
        private String approvalNumber;
        /** 批号 */
        private String batchNo;
        /** 事件类型（1-入库采集 2-发药核销 3-作废） */
        private Integer eventType;
        /** 事件发生时间 */
        private LocalDateTime eventTime;
        /** 患者姓名（核销事件有） */
        private String patientName;
    }

    /**
     * 上传回执
     */
    @Data
    public static class UploadAck {
        private Long traceId;
        private boolean success;
        /** 失败原因（成功时为空串） */
        private String message;
    }

    List<UploadAck> upload(List<UploadLine> lines);
}

package com.his.pharmacy.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * CSSD 追溯节点 VO。
 */
@Data
public class CssdTraceVO {

    /**
     * 追溯节点ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    /**
     * 器械包ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long packId;

    /**
     * 器械包条码
     */
    private String packNo;

    /**
     * 节点类型（1-回收 2-清洗 3-打包 4-灭菌 5-储存 6-发放）
     */
    private Integer nodeType;
    private String nodeTypeText;

    /**
     * 节点时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime nodeTime;

    /**
     * 操作人
     */
    private String operatorName;
    /**
     * 灭菌锅次
     */
    private String sterilizerNo;
    /**
     * 灭菌批次号
     */
    private String batchNo;

    /**
     * 节点结果（1-合格 2-不合格）
     */
    private Integer result;
    private String resultText;

    /**
     * 备注
     */
    private String remark;
}

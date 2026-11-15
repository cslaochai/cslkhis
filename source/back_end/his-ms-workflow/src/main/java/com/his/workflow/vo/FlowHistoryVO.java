package com.his.workflow.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 流转轨迹出参。
 */
@Data
public class FlowHistoryVO implements Serializable {

    private String nodeCode;

    private String nodeName;

    private Integer nodeType;

    private String nodeTypeLabel;

    /** 动作（START/PASS/REJECT/CANCELED/AUTO） */
    private String action;

    private Long approverId;

    private String approverName;

    private String message;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
}

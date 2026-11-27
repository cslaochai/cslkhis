package com.his.platform.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 签名资格复核单出参（待办列表 / 处理结果共用）。
 */
@Data
public class SignQualReviewVO {

    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    @JsonSerialize(using = ToStringSerializer.class)
    private Long employeeId;

    private String empName;

    private String fromPosts;

    private String toPosts;

    /** 新岗位是否需要电子签名资格（1-需要 0-不需要） */
    private Integer newSignRequired;

    @JsonSerialize(using = ToStringSerializer.class)
    private Long certId;

    private String certNo;

    /** 复核状态（0-待复核 1-保留 2-已吊销） */
    private Integer status;

    private String handleRemark;

    private String handleByName;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime handleTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
}

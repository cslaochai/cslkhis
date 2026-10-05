package com.his.system.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 患者标签出参
 */
@Data
public class SysPatientTagVO {

    /**
     * 标签ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long tagId;

    /**
     * 标签名称，如：高血压、VIP、医保
     */
    private String tagName;

    /**
     * 标签缩写用于展示
     */
    private String shortName;

    /**
     * 标签颜色(十六进制)，如：#FF5722
     */
    private String tagColor;

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;
}

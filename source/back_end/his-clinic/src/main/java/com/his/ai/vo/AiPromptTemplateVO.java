package com.his.ai.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 提示词模板出参（列表行 / 历史版本行）。
 */
@Data
public class AiPromptTemplateVO {

    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    /**
     * 模板编码
     */
    private String templateCode;

    /**
     * 模板名称
     */
    private String templateName;

    /**
     * 版本号
     */
    private String version;

    /**
     * 提示词内容
     */
    private String content;

    /**
     * 是否当前生效：1-是 0-否
     */
    private Integer isActive;

    /**
     * 备注
     */
    private String remark;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;
}

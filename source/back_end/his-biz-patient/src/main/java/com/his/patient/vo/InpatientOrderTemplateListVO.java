package com.his.patient.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 医嘱模板列表行（模板管理弹窗）。
 */
@Data
public class InpatientOrderTemplateListVO implements Serializable {

    /**
     * 模板ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    /**
     * 模板名称
     */
    private String templateName;

    /**
     * 默认医嘱类型：1-长期 2-临时
     */
    private Integer orderType;

    private String orderTypeText;

    /**
     * 明细条数
     */
    private Integer itemCount;

    /**
     * 备注/适用场景说明
     */
    private String remark;

    /**
     * 医生姓名
     */
    private String doctorName;

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

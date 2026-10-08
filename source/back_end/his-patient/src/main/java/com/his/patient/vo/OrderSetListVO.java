package com.his.patient.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 医嘱组套模板列表行（全院组套模板管理页）。
 */
@Data
public class OrderSetListVO implements Serializable {

    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    /**
     * 模板名称
     */
    private String templateName;

    /**
     * 共享范围：1-个人 2-科室 3-全院
     */
    private Integer scope;

    private String scopeText;

    /**
     * 归属科室（scope=2 时有值）
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long deptId;

    /**
     * 科室名称
     */
    private String deptName;

    /**
     * 归属医生（scope=1 时有值）
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long doctorId;

    /**
     * 医生姓名
     */
    private String doctorName;

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
     * 当前登录人对这份组套能做什么：true=可改可删，false=只能套用
     */
    private Boolean editable;

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

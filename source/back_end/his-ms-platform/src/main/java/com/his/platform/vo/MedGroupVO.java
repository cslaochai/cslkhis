package com.his.platform.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 医疗组信息出参
 */
@Data
public class MedGroupVO {

    /**
     * 医疗组ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    /**
     * 医疗组编码
     */
    private String groupCode;

    /**
     * 医疗组名称
     */
    private String groupName;

    /**
     * 所属科室ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long deptId;

    /**
     * 所属科室名称
     */
    private String deptName;

    /**
     * 主诊医师 / 医疗组长ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long leaderId;

    /**
     * 主诊医师 / 医疗组长姓名
     */
    private String leaderName;

    /**
     * 组内医师人数（按 sys_employee.group_id 统计）
     */
    private Integer memberCount;

    /**
     * 状态（0-停用 1-启用）
     */
    private Integer status;

    /**
     * 排序号
     */
    private Integer sortOrder;

    /**
     * 备注
     */
    private String remark;

    /**
     * 创建人
     */
    private String createBy;

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    /**
     * 更新人
     */
    private String updateBy;

    /**
     * 更新时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;
}

package com.his.platform.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 病区信息出参
 */
@Data
public class WardVO {

    /**
     * 病区ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long wardId;

    /**
     * 病区编码
     */
    private String wardCode;

    /**
     * 病区名称
     */
    private String wardName;

    /**
     * 本位科室ID（默认收治科室，与 sys_ward.dept_id 一致）
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long deptId;

    /**
     * 本位科室名称
     */
    private String deptName;

    /**
     * 总床位数
     */
    private Integer totalBeds;

    /**
     * 已占用床位数
     */
    private Integer occupiedBeds;

    /**
     * 状态（0-停用 1-正常）
     */
    private Integer status;

    /**
     * 备注
     */
    private String remark;

    /**
     * 收治科室清单（多对多）。列表页不填，详情与编辑回填时填。
     */
    private List<WardDeptVO> depts;

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

package com.his.pharmacy.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/** 抗菌药物处方权授权行 */
@Data
public class AntibioticAuthVO {

    /** 主键 */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    /** 授权编号 */
    private String authNo;

    /** 医师ID */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long doctorId;

    /** 医师姓名 */
    private String doctorName;

    /** 科室ID */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long deptId;

    /** 科室名称 */
    private String deptName;

    /** 职称 */
    private String title;

    /** 授权级别（1-非限制使用级 2-限制使用级 3-特殊使用级） */
    private Integer authLevel;

    private String authLevelText;

    /** 授权依据 */
    private String authBasis;

    /** 授权日期 */
    private LocalDate authDate;

    /** 有效期至 */
    private LocalDate expireDate;

    /** 状态（1-有效 2-暂停 3-取消） */
    private Integer status;

    /** 状态文本 */
    private String statusText;

    /** 服务端判定的"当前是否可用"：状态有效且未过期 —— 前端按这个显示，不自判 */
    private Boolean effective;

    /** 授权人 */
    private String authorizer;

    /** 授权部门 */
    private String authorizeOrg;

    /** 暂停/取消原因 */
    private String revokeReason;

    /** 创建人 */
    private String createBy;

    /** 创建时间 */
    private LocalDateTime createTime;

    /** 更新时间 */
    private LocalDateTime updateTime;

    /** 备注 */
    private String remark;
}

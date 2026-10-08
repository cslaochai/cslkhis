package com.his.system.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 技术授权台账出参。
 */
@Data
public class EmployeeTechAuthVO {

    /**
     * 主键（雪花ID）
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    /**
     * 员工ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long employeeId;

    /**
     * 员工姓名
     */
    private String employeeName;

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
     * 职称
     */
    private String title;

    /**
     * 授权类别（1-手术 2-麻醉 3-内镜与介入）
     */
    private Integer authCategory;

    private String authCategoryText;

    /**
     * 可独立操作的手术级别上限（1~4）
     */
    private Integer techLevel;

    private String techLevelText;

    /**
     * 限定术式编码白名单（null=该级别全部术式）
     */
    private String itemScope;

    /**
     * 授权方式（1-独立授权 2-上级指导下 3-限制授权须上级在场）
     */
    private Integer authType;

    private String authTypeText;

    /**
     * 授权依据（技术准入评价/培训考核/累计手术量，评审要看依据）
     */
    private String authBasis;

    /**
     * 授权生效日期
     */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate validFrom;

    /**
     * 授权有效期至
     */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate validUntil;

    /**
     * 状态（1-待审批 2-已授权 3-已驳回 4-已收回）
     */
    private Integer authStatus;

    private String authStatusText;

    /**
     * 当下是否生效（已授权 + 有效期覆盖今天）
     */
    private Boolean effective;

    /**
     * 有效期至 null=长期有效，前端别把 null 渲染成空白单元格
     */
    private Boolean indefinite;

    /**
     * 申请（登记）
     */
    private String applyBy;

    /**
     * 申请时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime applyTime;

    /**
     * 审批人
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long approverId;

    /**
     * 审批人姓名
     */
    private String approverName;

    /**
     * 审批时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime approveTime;

    /**
     * 审批意见
     */
    private String approveOpinion;

    /**
     * 收回人
     */
    private String revokeBy;

    /**
     * 收回时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime revokeTime;

    /**
     * 收回原因
     */
    private String revokeReason;

    /**
     * 备注
     */
    private String remark;

    /**
     * 可审批（仅待审批）
     */
    private Boolean canApprove;

    /**
     * 可收回（仅已授权）
     */
    private Boolean canRevoke;

    /**
     * 可修改/删除（仅待审批与已驳回：已生效的授权只能收回后重授）
     */
    private Boolean canEdit;
}

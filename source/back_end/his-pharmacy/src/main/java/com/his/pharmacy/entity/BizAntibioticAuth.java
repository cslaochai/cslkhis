package com.his.pharmacy.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 抗菌药物处方权授权（《抗菌药物临床应用管理办法》：三级管理，按职称授予处方权）。
 */
@Data
@TableName("biz_antibiotic_auth")
public class BizAntibioticAuth implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 授权级别：非限制使用级 */
    public static final int LEVEL_UNRESTRICTED = 1;
    /** 授权级别：限制使用级 */
    public static final int LEVEL_RESTRICTED = 2;
    /** 授权级别：特殊使用级 */
    public static final int LEVEL_SPECIAL = 3;

    /** 状态：有效 */
    public static final int STATUS_VALID = 1;
    /** 状态：暂停 */
    public static final int STATUS_PAUSED = 2;
    /** 状态：取消 */
    public static final int STATUS_REVOKED = 3;

    /** 主键 */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /** 授权编号（KJ+yyyyMMdd+4位序号） */
    private String authNo;

    /** 医师ID（员工的ID，与处方/医嘱 doctor_id 同口径） */
    private Long doctorId;

    /** 医师姓名 */
    private String doctorName;

    /** 科室ID */
    private Long deptId;

    /** 科室名称 */
    private String deptName;

    /** 职称（授权时的职称快照，判定依据） */
    private String title;

    /** 授权级别（1-非限制使用级 2-限制使用级 3-特殊使用级） */
    private Integer authLevel;

    /** 授权依据 */
    private String authBasis;

    /** 授权日期 */
    private LocalDate authDate;

    /** 有效期至（到期即失效，服务端按日期判定） */
    private LocalDate expireDate;

    /** 状态（1-有效 2-暂停 3-取消） */
    private Integer status;

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

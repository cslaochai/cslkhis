package com.his.pharmacy.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.his.common.base.BaseEntity;
import lombok.EqualsAndHashCode;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDate;

/**
 * 抗菌药物处方权授权（《抗菌药物临床应用管理办法》：三级管理，按职称授予处方权）。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("biz_antibiotic_auth")
public class BizAntibioticAuth extends BaseEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 主键 */

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

    /** 创建时间 */

    /** 更新时间 */

    /** 备注 */
    private String remark;
}

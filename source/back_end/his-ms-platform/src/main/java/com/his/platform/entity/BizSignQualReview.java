package com.his.platform.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.his.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 签名资格复核单：人事异动把「需要电子签名」的岗位换成「不需要签名」的岗位时生成，
 * 由医务科决定证书保留还是吊销；离职等场景同样走这张单留痕。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("biz_sign_qual_review")
public class BizSignQualReview extends BaseEntity {

    /** 员工ID */
    private Long employeeId;

    /** 员工姓名（快照） */
    private String empName;

    /** 异动前岗位摘要（角色/科室，顿号分隔） */
    private String fromPosts;

    /** 异动后岗位摘要（角色/科室，顿号分隔） */
    private String toPosts;

    /** 新岗位是否需要电子签名资格（1-需要 0-不需要） */
    private Integer newSignRequired;

    /** 异动时该员工的有效证书ID（无证书为空） */
    private Long certId;

    /** 证书编号（快照） */
    private String certNo;

    /** 复核状态（0-待复核 1-保留 2-已吊销） */
    private Integer status;

    /** 处理意见 */
    private String handleRemark;

    /** 处理人姓名 */
    private String handleByName;

    /** 处理时间 */
    private java.time.LocalDateTime handleTime;
}

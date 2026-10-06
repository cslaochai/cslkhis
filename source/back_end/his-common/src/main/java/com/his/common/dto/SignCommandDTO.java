package com.his.common.dto;

import com.his.common.enums.SignBizTypeEnum;
import com.his.common.enums.SignSceneEnum;
import lombok.Data;

/**
 * 签名命令（服务层入参）。
 *
 * <p>签名人信息**必须由调用方传入**：his-common 是能力层，不认识 {@code CurrentUser}，
 * 也不该去读 Spring Security 上下文。好处是签名服务可以在定时任务、批处理里被调用
 * （那时没有登录态），代价是业务侧必须认真填 —— 所以 {@code signerId} 在服务层会做非空校验。
 */
@Data
public class SignCommandDTO {

    /**
     * 签名对象类型（码值权威见 {@link SignBizTypeEnum}）
     */
    private Integer bizType;

    /**
     * 签名对象ID
     */
    private Long bizId;

    /**
     * 签名场景（码值权威见 {@link SignSceneEnum}）
     */
    private Integer signScene;

    /**
     * 签名人员工ID（员工档案主键）
     */
    private Long signerId;

    /**
     * 签名人姓名（快照）
     */
    private String signerName;
    /**
     * 签名人科室ID（快照）
     */
    private Long signerDeptId;
    /**
     * 签名人科室名称（快照）
     */
    private String signerDeptName;
    /**
     * 签名人职称
     */
    private String signerTitle;

    /**
     * 来源 IP（留痕，可为空）
     */
    private String clientIp;

    /**
     * 备注
     */
    private String remark;
}

package com.his.common.dto;

import com.his.common.enums.SignBizTypeEnum;
import com.his.common.enums.SignSceneEnum;
import lombok.Data;

/**
 * 签名命令（服务层入参）。
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
     * 签名人姓名
     */
    private String signerName;
    /**
     * 签名人科室ID
     */
    private Long signerDeptId;
    /**
     * 签名人科室名称
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

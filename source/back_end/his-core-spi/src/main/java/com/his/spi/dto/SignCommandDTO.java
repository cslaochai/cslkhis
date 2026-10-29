package com.his.spi.dto;

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
     * 签名人本次输入的 PIN（软签二次确认）。
     *
     * <p>无 UKey 硬件时用 PIN 代替芯片内的 PIN 保护：每次签名都要签名人本人输入，
     * 明文不入库、只存哈希。跨模块内部流程直调时由调用方透传。
     */
    private String pin;

    /**
     * 来源 IP（留痕，可为空）
     */
    private String clientIp;

    /**
     * 备注
     */
    private String remark;
}

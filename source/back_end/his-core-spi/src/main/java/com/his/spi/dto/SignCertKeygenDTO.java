package com.his.spi.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 生成签名密钥对入参（CA 线下签发流程的第一步）。
 *
 * <p>流程：管理员在 HIS 为员工生成密钥对 → 系统导出「CA 签发申请单」（Subject DN + 公钥 PEM）
 * → 线下把申请单送 CA 认证中心人工签发 → CA 返回证书 PEM → 用 {@link SignCertBindDTO} 回填。
 *
 * <p>私钥在 HIS 本地生成并加密托管，全程不出 HIS；CA 只经手公钥。
 */
@Data
public class SignCertKeygenDTO {

    /**
     * 签名人员工ID
     */
    @NotNull(message = "员工ID不能为空")
    private Long empId;

    /**
     * 员工姓名（快照，必填）
     */
    private String empName;

    /**
     * 所属科室ID
     */
    private Long deptId;

    /**
     * 所属科室名称
     */
    private String deptName;

    /**
     * 签名 PIN：软签二次确认用的个人口令，签名时由医生本人输入。
     * 明文不入库，仅存 PBKDF2 哈希。
     */
    @NotNull(message = "签名PIN不能为空")
    @Size(min = 4, max = 32, message = "签名PIN长度须为4-32位")
    private String pin;

    /**
     * 备注
     */
    private String remark;
}

package com.his.common.dto;

import com.his.common.base.PageParam;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 签名证书分页查询入参。
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class SignCertQueryPageDTO extends PageParam {

    /** 签名人员工ID */
    private Long empId;

    /** 证书状态（1-有效 2-已吊销） */
    private Integer certStatus;

    /** 签发方式（1-人工签发 2-系统自动签发） */
    private Integer issuedMode;

    /** 关键字：证书编号 / 员工姓名 / 公钥指纹 */
    private String keyword;
}

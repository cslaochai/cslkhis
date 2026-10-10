package com.his.system.service;

import com.his.system.dto.SignCertIssueDTO;
import com.his.system.dto.SignCertRevokeDTO;
import com.his.system.dto.SignatureInvalidateDTO;
import com.his.system.dto.SignatureSignDTO;
import com.his.system.vo.SignCaProbeOutboundVO;
import com.his.system.vo.SignCaStatusVO;
import com.his.system.vo.SignCertSelectListVO;
import com.his.system.vo.SignCertVO;
import com.his.system.vo.SignatureOptionsVO;
import com.his.system.vo.SignatureVO;

import java.util.List;

/**
 * 签名中心（运维/审计界面）的出参与命令装配。
 */
public interface SignatureCenterService {

    /**
     * 下拉选项：对象类型/场景/签名状态/验签状态/时间来源/证书状态/签发模式
     */
    SignatureOptionsVO queryOptions();

    /**
     * 管理员补签：签名人取当前登录用户
     */
    SignatureVO sign(SignatureSignDTO dto);

    /**
     * 作废签名：操作人取当前登录用户
     */
    SignatureVO invalidate(SignatureInvalidateDTO dto);

    /**
     * 有效证书下拉
     */
    List<SignCertSelectListVO> certOptions(String keyword);

    /**
     * 人工签发证书
     */
    SignCertVO issueCert(SignCertIssueDTO dto);

    /**
     * 吊销证书
     */
    SignCertVO revokeCert(SignCertRevokeDTO dto);

    /**
     * CA 签发模式状态
     */
    SignCaStatusVO signCaStatus();

    /**
     * CA 外发探针
     */
    SignCaProbeOutboundVO probeCaOutbound();
}

package com.his.emr.service;

import com.his.spi.vo.SignatureVO;
import com.his.emr.dto.SignatureInvalidateDTO;
import com.his.emr.dto.SignatureSignDTO;
import com.his.emr.vo.SignatureOptionsVO;

/**
 * 签名中心（运维/审计界面）的出参与命令装配。
 */
public interface SignatureCenterService {

    /**
     * 下拉选项：对象类型/场景/签名状态/验签状态/时间来源/证书状态/签发方式
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
}

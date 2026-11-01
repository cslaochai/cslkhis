package com.his.clinical.service;

import com.his.spi.vo.SignatureVO;
import com.his.clinical.dto.MyMakeupSignDTO;
import com.his.clinical.dto.SignatureInvalidateDTO;
import com.his.clinical.dto.SignatureSignDTO;
import com.his.clinical.vo.MyPendingSignVO;
import com.his.clinical.vo.SignatureOptionsVO;

import java.util.List;

/**
 * 签名验签（运维/审计界面）的出参与命令装配。
 */
public interface SignatureCenterService {

    /**
     * 下拉选项：对象类型/场景/签名状态/验签状态/证书状态/签发方式
     */
    SignatureOptionsVO queryOptions();

    /**
     * 管理员补签：签名人取当前登录用户
     *
     * @param pin 签名人本次输入的 PIN（软签二次确认）
     */
    SignatureVO sign(SignatureSignDTO dto, String pin);

    /**
     * 医生「我的待补签」：只列当前登录医生本人书写、尚未电子签名的病历/处方/检查检验申请单。
     */
    List<MyPendingSignVO> listMyPendingSigns();

    /**
     * 医生自助补签：校验对象归属当前登录医生，签名人强制本人，必须写原因并输入签名 PIN。
     */
    SignatureVO makeupSign(MyMakeupSignDTO dto);

    /**
     * 作废签名：操作人取当前登录用户
     */
    SignatureVO invalidate(SignatureInvalidateDTO dto);
}

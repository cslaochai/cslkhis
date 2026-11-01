package com.his.platform.constant;

/**
 * 电子签名（软签）相关的固定常量。
 */
public interface SignConst {

    /**
     * 建号自动造钥时下发的初始签名 PIN。
     *
     * <p>这是全网已知的过渡口令，只用于让新证书能立刻进入「CA 申请单可导出」的状态；
     * 用它签名会被拒绝并要求先改口令（见 SignCertServiceImpl 的签名人确认）。
     */
    String INITIAL_SIGN_PIN = "123456";
}

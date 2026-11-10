package com.his.platform.filter;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * SDK 缺失即拒绝启动（fail-fast）。
 *
 * <p>CA 推送回调端点是匿名开放的写入口，准入<b>完全依赖</b> SM2 验签。
 * 如果 classpath 上缺 {@code com.his.ca.sdk.CaPushVerifier} 却仍然要求验签，
 * 那就等于把「伪造签发成功报文、往 HIS 塞任意证书」的通道敞着——
 * 这种状态不该带着跑，应当在启动阶段就失败。</p>
 *
 * <p>确实不需要 CA 推送的部署，请显式配置 {@code his.sign.ca.push-verify-enabled: false}，
 * 此时本类不生效，改由 {@link CaPushDisabledFilter} 把端点直接回 503（拒绝，而非放行）。</p>
 */
@Component
@ConditionalOnMissingClass("com.his.ca.sdk.CaPushVerifier")
@ConditionalOnProperty(prefix = "his.sign.ca", name = "push-verify-enabled", matchIfMissing = true)
public class CaSdkRequiredFailFast {

    public CaSdkRequiredFailFast() {
        throw new IllegalStateException("""

                ==========================================================
                CA 推送回调验签已启用（his.sign.ca.push-verify-enabled 未置 false），
                但 classpath 上找不到 com.his.ca.sdk.CaPushVerifier（his-ca-sdk 缺失）。
                该回调端点匿名开放，无验签等于任何人都能伪造「签发成功」报文。

                请二选一：
                  1) 把 his-ca-sdk 正确引入运行时 classpath（推荐）；
                  2) 若本部署确定不接 CA 推送，显式设置
                     his.sign.ca.push-verify-enabled=false
                     此时端点将统一返回 503，不再接收推送。
                ==========================================================
                """);
    }
}

package com.his.common.service;

import com.his.common.entity.SignSubject;
import com.his.common.enums.SignBizTypeEnum;
import com.his.common.enums.SignSceneEnum;

import java.time.LocalDateTime;

/**
 * 「什么内容可签、签完把锚点写到哪」的业务侧扩展点。
 */
public interface SignableContentProvider {

    /**
     * 本实现负责的对象类型
     */
    SignBizTypeEnum bizType();

    /**
     * 加载被签对象。
     *
     * @return 对象不存在 / 已删除时返回 {@code null}（签名层据此给出"签名对象不存在"，不猜）
     */
    SignSubject load(Long bizId);

    /**
     * 能否在指定场景下签名。
     *
     * @return {@code null} 表示可以签；非空字符串是**给用户看的拒绝理由**
     */
    String blockReason(SignSubject subject, SignSceneEnum scene);

    /**
     * 签名成功后回写锚点（病例行上的 sign_status / sign_id / signed_time，
     * 医嘱行上的 doctor_sign_id / nurse_sign_id）。
     *
     * <p>刻意**不在这里改业务状态**：签名是留痕动作，不承担状态机职责。
     * 谁改状态由业务自己的流程决定。
     */
    void applySignAnchor(Long bizId, SignSceneEnum scene, Long signId, LocalDateTime signedTime);

    /**
     * 签名被作废后回写锚点：对象置「签名已失效」（2），并清掉指向该签名的指针。
     * 默认不做任何事，由需要的实现覆盖。
     */
    default void revokeSignAnchor(Long bizId, Long signId) {
    }
}

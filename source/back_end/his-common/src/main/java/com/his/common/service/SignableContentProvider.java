package com.his.common.service;

import com.his.common.entity.SignSubject;
import com.his.common.enums.SignBizType;
import com.his.common.enums.SignScene;

import java.time.LocalDateTime;

/**
 * 「什么内容可签、签完把锚点写到哪」的业务侧扩展点。
 *
 * <p><b>为什么定义在 his-common 而不是 his-emr</b>：
 * 住院病历在 his-patient、门诊病历在 his-emr、医嘱在 his-patient。
 * 若接口定义在任一业务模块，签名服务就得反向依赖它 —— 那是循环依赖，
 * 构建期不报、运行期 Bean 找不到。所以接口定义在能力层（本模块），
 * 由各业务模块实现，签名服务通过 {@code ObjectProvider} 收集（缺席即降级，不启动失败）。
 *
 * <p>实现者的三条硬约束：
 * <ol>
 *   <li>{@link #load} 必须返回**确定性**内容：同一份业务数据重复调用必须得到同一串文本。
 *       任何"取当前时间""取随机数""依赖 HashMap 遍历顺序"的写法都会让验签无意义。</li>
 *   <li>{@link #blockReason} 必须给出**人能照着做**的拒绝理由，且不许把"已签名"和
 *       "草稿不能签"混成一句话 —— 前者要引导去作废，后者要引导去提交。</li>
 *   <li>{@link #applySignAnchor} 与 {@link #revokeSignAnchor} 必须能重复执行而不出错
 *       （幂等），因为签名服务允许在唯一索引冲突时重试。</li>
 * </ol>
 */
public interface SignableContentProvider {

    /** 本实现负责的对象类型 */
    SignBizType bizType();

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
    String blockReason(SignSubject subject, SignScene scene);

    /**
     * 签名成功后回写锚点（病例行上的 sign_status / sign_id / signed_time，
     * 医嘱行上的 doctor_sign_id / nurse_sign_id）。
     *
     * <p>刻意**不在这里改业务状态**：签名是留痕动作，不承担状态机职责。
     * 谁改状态由业务自己的流程决定。
     */
    void applySignAnchor(Long bizId, SignScene scene, Long signId, LocalDateTime signedTime);

    /**
     * 签名被作废后回写锚点：对象置「签名已失效」（2），并清掉指向该签名的指针。
     * 默认不做任何事，由需要的实现覆盖。
     */
    default void revokeSignAnchor(Long bizId, Long signId) {
    }
}

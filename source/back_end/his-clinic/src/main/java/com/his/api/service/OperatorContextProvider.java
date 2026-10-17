package com.his.api.service;

/**
 * 无登录态入口（渠道异步回调 / 系统触发写账）的操作人归属端口（his-system 实现）。
 *
 * <p>bind 之后必须在 finally 里 {@link #clear}，且只绑本单自己的出码人，
 * 否则等于给外部报文开了一条伪造身份写账的路。
 */
public interface OperatorContextProvider {

    /**
     * 以患者身份执行后续写账：不是员工，收款流水自然落进「系统代收」桶。
     *
     * @param userId    出码人账号ID
     * @param patientId 出码人关联的就诊人ID
     * @param realName  出码人姓名
     */
    void bindPatient(Long userId, Long patientId, String realName);

    /**
     * 清空操作人上下文（通常在 finally 中调用）。
     */
    void clear();
}

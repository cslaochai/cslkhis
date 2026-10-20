package com.his.system.service;


/**
 * 短信验证码服务：按「手机号 × 业务场景」发放并核验一次性验证码。
 */
public interface SmsCodeService {

    public static final String SCENE_REGISTER = "register";

    public static final String SCENE_BIND = "bind";

    public static final String SCENE_ADD = "add";

    /**
     * 给手机号发一枚该场景的验证码，同一手机号同一场景在重发间隔内只放行一次。
     * 用于患者端注册、就诊人绑定与新增就诊人时用户点「获取验证码」。
     *
     * @param scene 业务场景码（本类 SCENE_* 常量），与核验时必须是同一个，跨场景的码互不通用
     */
    SendResult send(String phone, String scene);

    /**
     * 核验验证码并当场消费掉：通过即作废，同一个码用不了第二次。
     * 用于注册、就诊人绑定与新增提交前的验证码核对，也用于「试错次数超限即作废」的防穷举。
     *
     * @return null=核验通过；非 null=可直接展示给用户的失败原因
     */
    String verify(String phone, String scene, String code);

    /**
     * 发码结果。success=false 时 message 为给用户看的失败原因。
     */
    public record SendResult(boolean success, String message, String code) {

        /**
         * 构一枚发放成功的结果并带上验证码本身。
         * 用于模拟通道下把码回显给前端与联调方，真实通道发出去后不依赖这个回显。
         */
        public static SendResult ok(String code) {
            return new SendResult(true, null, code);
        }

        /**
         * 构一枚发放失败的结果并带上原因。
         * 用于限流拦截、通道未开通这类发码没成功的场合，原因文案直接回给用户看。
         */
        public static SendResult fail(String message) {
            return new SendResult(false, message, null);
        }
    }
}

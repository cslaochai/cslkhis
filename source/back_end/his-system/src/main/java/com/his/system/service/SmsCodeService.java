package com.his.system.service;


public interface SmsCodeService {

    public static final String SCENE_REGISTER = "register";

    public static final String SCENE_BIND = "bind";

    public static final String SCENE_ADD = "add";

    /**
     * 发码结果。{@code success=false} 时 message 为给用户看的失败原因。
     */
    public record SendResult(boolean success, String message, String code) {

        public static SendResult ok(String code) {
            return new SendResult(true, null, code);
        }

        public static SendResult fail(String message) {
            return new SendResult(false, message, null);
        }
    }

    SendResult send(String phone, String scene);

    String verify(String phone, String scene, String code);
}

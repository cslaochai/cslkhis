package com.his.ai.exception;

import lombok.Getter;

/**
 * 模型调用异常。
 * <p>
 * 单独区分「超时」而不是笼统的失败，是因为审计日志要把超时记成 status=3、
 * 其它失败记 status=2 —— 两者的排查方向完全不同（超时看网络与超时配置，失败看密钥与协议）。
 */
@Getter
public class LlmException extends RuntimeException {

    /**
     * 是否由超时引起
     */
    private final boolean timeout;

    public LlmException(String message) {
        this(message, false, null);
    }

    public LlmException(String message, boolean timeout) {
        this(message, timeout, null);
    }

    public LlmException(String message, Throwable cause) {
        this(message, isTimeout(cause), cause);
    }

    private LlmException(String message, boolean timeout, Throwable cause) {
        super(message, cause);
        this.timeout = timeout;
    }

    /**
     * 沿异常链判断是否为超时。不同 JDK / HTTP 客户端抛出的超时异常类型不一致，
     * 这里按类名判断，避免绑定到具体实现。
     */
    public static boolean isTimeout(Throwable throwable) {
        Throwable current = throwable;
        int depth = 0;
        while (current != null && depth++ < 10) {
            String name = current.getClass().getName();
            if (name.contains("Timeout") || name.contains("TimedOut")) {
                return true;
            }
            current = current.getCause();
        }
        return false;
    }
}

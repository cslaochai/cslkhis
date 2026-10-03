package com.his.common.enums;

/**
 * 证书签发方式。
 *
 * <p>这个区分是**诚实性问题，不是技术细节**：
 * <ul>
 *   <li>{@link #MANUAL}：管理员在签名中心为员工签发（私钥仍由系统生成托管，但有人工审核动作）。</li>
 *   <li>{@link #AUTO}：员工首次签名时按需补发，属于**院内托管的电子签名**。</li>
 * </ul>
 * 两者都不是第三方 CA 的"可靠电子签名"（私钥在服务端、系统有能力代签），
 * 页面与对外口径必须如实标注，不许用"已通过 CA 认证"这类说法掩盖。
 */
public enum CertIssuedMode {

    MANUAL(1, "人工签发"),
    AUTO(2, "系统自动签发");

    private final int code;
    private final String text;

    CertIssuedMode(int code, String text) {
        this.code = code;
        this.text = text;
    }

    public int getCode() {
        return code;
    }

    public String getText() {
        return text;
    }

    public static CertIssuedMode parse(Integer code) {
        if (code == null) {
            return null;
        }
        for (CertIssuedMode m : values()) {
            if (m.code == code) {
                return m;
            }
        }
        return null;
    }

    public static String textOf(Integer code) {
        CertIssuedMode m = parse(code);
        if (m != null) {
            return m.text;
        }
        return code == null ? "—" : "未知(" + code + ")";
    }
}

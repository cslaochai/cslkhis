package com.his.medicaltech.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 仿真实验室（cssimlab）对接配置（his.simlab.*）。
 *
 * <p>链路口径：HIS 只做两件事 —— 缴费建单 / 设备科签到时把检查单快照推给仿真平台；
 * 对外开放结果回调接口，接收仿真平台延时出结果后的检查所见与结论。排队、随机耗时、
 * 所见结论文案全部归仿真平台，HIS 后端不内置任何仿真调度。
 *
 * <p>鉴权：HIS → simlab 与 simlab → HIS 共用同一把共享密钥，HMAC-SHA256 签名头；
 * 仅用于院内仿真环境互联，不承担公网安全。
 */
@Data
@Component
@ConfigurationProperties(prefix = "his.simlab")
public class SimlabProperties {

    /** 总开关：关掉后 HIS 不再向仿真平台推送，回调接口也拒绝落库。 */
    private boolean enabled = false;

    /** 仿真平台地址（不带末尾斜杠），simlab-web 端口 18082。 */
    private String baseUrl = "http://localhost:18082";

    /** 共享密钥：两端配置必须一致，参与回调签名校验。 */
    private String secret = "";

    /** 建立连接超时（毫秒）。 */
    private int connectTimeoutMs = 3000;

    /** 单次请求超时（毫秒）。 */
    private int requestTimeoutMs = 10000;

    /** 缴费建单后下发路径（相对 base-url）。 */
    private String assignPath = "/api/sim/medtech/assign";

    /** 签到后下发路径（相对 base-url）。 */
    private String arrivePath = "/api/sim/medtech/arrive";
}

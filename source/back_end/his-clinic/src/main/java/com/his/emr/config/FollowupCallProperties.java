package com.his.emr.config;

import com.his.common.util.TextUtil;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 随访外呼通道配置（followup.*），对应 config/domain/his-followup.yml。
 */
@Data
@Component
@ConfigurationProperties(prefix = "his.followup")
public class FollowupCallProperties {

    private static final String MOCK = "mock";

    /**
     * 外呼通道：{@code mock}=人工（护士自己拨，本方法只登记「待外呼」）；
     * {@code aliyun-vms}=自动外呼。
     */
    private String callChannel = MOCK;

    /**
     * 各线路凭据，为手册 G-15 施工预留（mock 分支不读）。
     */
    private Channel channel = new Channel();

    /**
     * 归一化后的通道值：空/空白一律当 mock，省得每个调用方各写一遍兜底。
     */
    public String effectiveCallChannel() {
        return TextUtil.hasText(callChannel) ? callChannel.trim() : MOCK;
    }

    /**
     * 是否人工通道。判据收口在这里，调用方不再自己写字符串比较。
     */
    public boolean isMockChannel() {
        return MOCK.equalsIgnoreCase(effectiveCallChannel());
    }

    @Data
    public static class Channel {
        private AliyunVms aliyunVms = new AliyunVms();
    }

    @Data
    public static class AliyunVms {

        private String accessKeyId;

        private String accessKeySecret;

        private String calledShowNumber;

        private String ttsCode;
    }
}

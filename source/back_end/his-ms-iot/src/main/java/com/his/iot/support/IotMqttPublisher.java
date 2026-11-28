package com.his.iot.support;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.his.common.util.TextUtil;
import com.his.iot.config.IotProperties;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import org.eclipse.paho.client.mqttv3.MqttAsyncClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;

/**
 * 物联网指令下发通道（HIS → 设备）。
 * <p>
 * 主题约定 {@code his/iot/{类型}/{编号}/cmd}，与设备侧订阅主题一一对应。此前 HIS 只订阅不发布，
 * 机器人派单需要把任务单下行到车队终端，故补一个独立发布客户端：clientId 带随机后缀，
 * 不复用订阅连接，避免下发抖动影响遥测/事件消费。
 * <p>
 * broker 未配置或连接失败时只记日志、不抛异常：派单单据仍落库，等 MQTT 恢复后可重新派车，
 * 不能让一条下行通道故障拖垮业务主链路。
 */
@Component
@RequiredArgsConstructor
public class IotMqttPublisher {

    private static final Logger log = LoggerFactory.getLogger(IotMqttPublisher.class);

    private final IotProperties properties;
    private final ObjectMapper json = new ObjectMapper();

    private MqttAsyncClient client;
    private final Object connectLock = new Object();

    /**
     * 发布一条下行指令。
     *
     * @param deviceType 设备类型（robot）
     * @param deviceNo   设备编号
     * @param cmd        指令码（ASSIGN_TASK / PAUSE / RESUME / GO_HOME）
     * @param requestId  指令流水号，设备回执原样带回；可空
     * @param payload    指令业务参数；可空
     * @return true 已发出；false 未发出（未配置 broker 或连接异常）
     */
    public boolean publishCommand(String deviceType, String deviceNo, String cmd,
                                  String requestId, ObjectNode payload) {
        IotProperties.Mqtt mqtt = properties.getMqtt();
        if (!TextUtil.hasText(mqtt.getBroker())) {
            log.warn("MQTT 未配置 broker，指令 {} / {} 未下发（单据仍保留待重新派车）", cmd, deviceNo);
            return false;
        }
        try {
            ensureConnected(mqtt);
            ObjectNode message = json.createObjectNode();
            message.put("cmd", cmd);
            if (TextUtil.hasText(requestId)) {
                message.put("requestId", requestId);
            }
            if (payload != null) {
                message.set("payload", payload);
            }
            String topic = String.format("his/iot/%s/%s/cmd", deviceType, deviceNo);
            MqttMessage mqttMessage = new MqttMessage(
                    json.writeValueAsBytes(message));
            mqttMessage.setQos(1);
            mqttMessage.setRetained(false);
            client.publish(topic, mqttMessage);
            log.info("指令已下发：{} {} / {}", topic, cmd, deviceNo);
            return true;
        } catch (Exception e) {
            log.warn("指令下发失败 {} / {}：{}", cmd, deviceNo, e.getMessage());
            return false;
        }
    }

    private void ensureConnected(IotProperties.Mqtt mqtt) throws Exception {
        synchronized (connectLock) {
            if (client != null && client.isConnected()) {
                return;
            }
            client = new MqttAsyncClient(mqtt.getBroker(),
                    mqtt.getClientIdPrefix() + "-cmd-" + Long.toHexString(System.nanoTime()),
                    new MemoryPersistence());
            MqttConnectOptions options = new MqttConnectOptions();
            options.setCleanSession(true);
            options.setAutomaticReconnect(true);
            options.setConnectionTimeout(10);
            options.setKeepAliveInterval(30);
            client.connect(options).waitForCompletion(10_000);
        }
    }

    @PreDestroy
    public void close() {
        try {
            if (client != null && client.isConnected()) {
                client.disconnectForcibly(2_000, 2_000, false);
            }
        } catch (Exception ignored) {
        }
    }
}

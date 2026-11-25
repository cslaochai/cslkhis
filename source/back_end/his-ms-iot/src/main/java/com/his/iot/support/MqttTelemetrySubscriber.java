package com.his.iot.support;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.his.common.util.TextUtil;
import com.his.iot.config.IotProperties;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttAsyncClient;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 订阅 {@code his/iot/+/+/telemetry}，把报文解析成 {@link TelemetryPoint} 交给落库线程。
 * <p>
 * 设计要点（与真实设备接入一致）：
 * <ol>
 *   <li>clientId 带随机后缀 + cleanSession：HIS 重启不会因为同名 client 把旧连接踢掉；遥测丢几条无所谓，不需要会话续传</li>
 *   <li>断线自己重连（指数退避），不依赖 broker 的自动重连，避免重连期反复订阅</li>
 *   <li>解析失败只计数不抛异常：一条坏报文不能中断整条订阅</li>
 * </ol>
 * 只有 {@code his.iot.ingest-enabled=true} 且配了 broker 才启动，否则 HIS 只做查询。
 */
@Component
@RequiredArgsConstructor
public class MqttTelemetrySubscriber {

    private static final Logger log = LoggerFactory.getLogger(MqttTelemetrySubscriber.class);

    private final IotProperties properties;
    private final TsdbTelemetryWriter writer;

    private final ObjectMapper json = new ObjectMapper();
    private final AtomicLong accepted = new AtomicLong();
    private final AtomicLong skipped = new AtomicLong();

    private ScheduledExecutorService reconnector = Executors.newSingleThreadScheduledExecutor(r -> {
        Thread t = new Thread(r, "iot-mqtt-reconnect");
        t.setDaemon(true);
        return t;
    });

    private MqttAsyncClient client;
    private boolean attempted;

    @PostConstruct
    public void start() {
        IotProperties.Mqtt mqtt = properties.getMqtt();
        if (!properties.isIngestEnabled()) {
            log.info("物联网 MQTT 订阅未开启（his.iot.ingest-enabled=false）");
            return;
        }
        if (!TextUtil.hasText(mqtt.getBroker())) {
            log.warn("物联网 MQTT 订阅未启动：his.iot.mqtt.broker 未配置");
            return;
        }
        attempted = true;
        try {
            client = new MqttAsyncClient(mqtt.getBroker(),
                    mqtt.getClientIdPrefix() + "-" + Long.toHexString(System.nanoTime()),
                    new MemoryPersistence());
            client.setCallback(new MqttCallbackExtended() {
                @Override
                public void connectComplete(boolean reconnect, String serverURI) {
                    subscribe();
                    log.info("已{}物联网 broker {}，订阅 {}", reconnect ? "重连" : "连接", serverURI, mqtt.getTelemetryTopic());
                }

                @Override
                public void connectionLost(Throwable cause) {
                    log.warn("与物联网 broker 断开：{}，3 秒后重连",
                            cause == null ? "unknown" : cause.getMessage());
                    reconnectLater(3);
                }

                @Override
                public void messageArrived(String topic, MqttMessage message) {
                    onMessage(topic, message);
                }

                @Override
                public void deliveryComplete(IMqttDeliveryToken token) {
                    // 只订不收，不会走到这里
                }
            });
            connect();
        } catch (Exception e) {
            // 订阅失败不该拖垮 HIS 启动：设备接入是增强能力，业务主链路必须能起来
            log.warn("物联网 MQTT 订阅启动失败（不影响 HIS 启动）：{}", e.getMessage());
        }
    }

    private void connect() throws Exception {
        MqttConnectOptions o = new MqttConnectOptions();
        o.setCleanSession(true);
        o.setAutomaticReconnect(false);
        o.setConnectionTimeout(10);
        o.setKeepAliveInterval(30);
        client.connect(o).waitForCompletion(10_000);
    }

    private void subscribe() {
        try {
            client.subscribe(properties.getMqtt().getTelemetryTopic(), properties.getMqtt().getQos())
                    .waitForCompletion(5_000);
        } catch (Exception e) {
            log.warn("物联网主题订阅失败：{}", e.getMessage());
            reconnectLater(5);
        }
    }

    private void reconnectLater(int seconds) {
        if (reconnector.isShutdown()) {
            return;
        }
        reconnector.schedule(() -> {
            try {
                if (client != null && !client.isConnected()) {
                    connect();
                }
            } catch (Exception e) {
                log.warn("物联网 broker 重连失败：{}", e.getMessage());
                reconnectLater(seconds + 2);
            }
        }, seconds, TimeUnit.SECONDS);
    }

    private void onMessage(String topic, MqttMessage message) {
        try {
            JsonNode node = json.readTree(new String(message.getPayload(), StandardCharsets.UTF_8));
            JsonNode metrics = node.get("metrics");
            String type = text(node, "deviceType");
            String id = text(node, "deviceId");
            if (metrics == null || !metrics.isObject() || type == null || id == null) {
                skipped.incrementAndGet();
                return;
            }
            writer.offer(new TelemetryPoint(type, id,
                    TelemetrySqlBuilder.parseTs(text(node, "ts")), metrics));
            accepted.incrementAndGet();
        } catch (Exception e) {
            skipped.incrementAndGet();
            log.debug("物联网报文解析失败 {}：{}", topic, e.getMessage());
        }
    }

    private static String text(JsonNode node, String field) {
        JsonNode v = node.get(field);
        return v == null || v.isNull() ? null : v.asText();
    }

    /**
     * 已收条数（运维排查用）
     */
    public long accepted() {
        return accepted.get();
    }

    /**
     * 丢弃条数（报文缺 deviceType/deviceId/metrics，或 JSON 坏）
     */
    public long skipped() {
        return skipped.get();
    }

    @PreDestroy
    public void stop() {
        if (!attempted || client == null) {
            reconnector.shutdownNow();
            return;
        }
        try {
            client.disconnectForcibly(1_000, 1_000, false);
            client.close();
        } catch (Exception ignored) {
            // 停机阶段关不掉不影响进程退出
        }
        reconnector.shutdownNow();
    }
}

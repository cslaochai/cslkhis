package com.his.iot.support;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.his.common.util.TextUtil;
import com.his.iot.config.IotProperties;
import com.his.iot.service.IotEventService;
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
    private final IotEventService iotEventService;

    private final ObjectMapper json = new ObjectMapper();

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
            client.subscribe(new String[]{properties.getMqtt().getTelemetryTopic(), properties.getMqtt().getEventTopic()},
                    new int[]{properties.getMqtt().getQos(), 1}).waitForCompletion(5_000);
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
            if (topic.endsWith("/event")) {
                onEvent(node);
                return;
            }
            JsonNode metrics = node.get("metrics");
            String type = text(node, "deviceType");
            String id = text(node, "deviceId");
            if (metrics == null || !metrics.isObject() || type == null || id == null) {
                log.debug("物联网遥测报文缺 deviceType/deviceId/metrics，丢弃：{}", topic);
                return;
            }
            if (!sane(type, id, metrics)) {
                return;
            }
            writer.offer(new TelemetryPoint(type, id,
                    TelemetrySqlBuilder.parseTs(text(node, "ts")), metrics));
        } catch (Exception e) {
            log.debug("物联网报文解析失败 {}：{}", topic, e.getMessage());
        }
    }

    /**
     * 冷链物理量量程校验。探头故障或脏报文会送来 349℃ / 210% 这种读数，
     * 直接落时序库会污染 GSP 记录和 24 小时曲线——这里按「量程外即丢弃」处理，并计数给运维看。
     * 其它设备类型的量程差异大（流量/压力/电量），等各场景定稿再逐类补，不在这张订阅里硬编码全部点表。
     */
    private boolean sane(String type, String id, JsonNode metrics) {
        if ("gas".equals(type)) {
            return saneGas(id, metrics);
        }
        if ("asset".equals(type)) {
            return saneAsset(id, metrics);
        }
        if (!"coldchain".equals(type)) {
            return true;
        }
        JsonNode tempNode = metrics.get("temp");
        if (tempNode == null || !tempNode.isNumber()) {
            log.warn("冷链遥测缺少温度字段，丢弃：{}", id);
            return false;
        }
        double temp = tempNode.asDouble();
        if (temp < -30 || temp > 60) {
            log.warn("冷链遥测温度越界，丢弃：{} temp={}℃", id, temp);
            return false;
        }
        JsonNode humidityNode = metrics.get("humidity");
        if (humidityNode != null && humidityNode.isNumber()) {
            double humidity = humidityNode.asDouble();
            if (humidity < 0 || humidity > 100) {
                log.warn("冷链遥测湿度越界，丢弃：{} humidity={}%", id, humidity);
                return false;
            }
        }
        return true;
    }

    /**
     * 医用气体量程校验：压力 0~600kPa、负压 -100~0kPa、流量 0~60L/min。
     * 三路压力是同一终端的完整监测面，缺任一列说明报文残缺，整条丢弃（保留上一条完整值，不让页面出现"—"）。
     * 流量是附属量：越界（如哨兵 -999）只剔这一列，三路压力照常入库；未用气时设备本来就可能不报流量。
     */
    private boolean saneGas(String id, JsonNode metrics) {
        if (!(metrics instanceof ObjectNode node)) {
            return false;
        }
        boolean o2 = keepRange(node, "o2Kpa", 0, 600, id);
        boolean vac = keepRange(node, "vacuumKpa", -100, 0, id);
        boolean air = keepRange(node, "airKpa", 0, 600, id);
        keepRange(node, "flowLpm", 0, 60, id);
        if (!o2 || !vac || !air) {
            log.warn("气体遥测三路压力不完整，丢弃：{}", id);
            return false;
        }
        return true;
    }

    /**
     * 资产信标量程校验：area 是定位结论，缺了就整条丢弃（不能让"位置未知"覆盖上一次有效位置）；
     * 电量/信号越界只剔该列，不影响位置落库。
     */
    private boolean saneAsset(String id, JsonNode metrics) {
        if (!(metrics instanceof ObjectNode node)) {
            return false;
        }
        JsonNode area = node.get("area");
        if (area == null || !area.isTextual() || area.asText().isBlank()) {
            log.warn("资产遥测缺少区域，丢弃：{}", id);
            return false;
        }
        keepRange(node, "rssi", -120, 0, id);
        keepRange(node, "battery", 0, 100, id);
        return true;
    }

    private boolean keepRange(ObjectNode node, String field, double min, double max, String id) {
        JsonNode v = node.get(field);
        if (v == null || !v.isNumber()) {
            node.remove(field);
            return false;
        }
        double d = v.asDouble();
        if (d < min || d > max) {
            log.warn("遥测越界，剔除字段：{} {}={}", id, field, d);
            node.remove(field);
            return false;
        }
        return true;
    }

    /**
     * 事件报文落 biz_iot_event。seq 取「设备+事件码+时间+描述」的哈希：QoS1 重发会算出同一个 seq，
     * 配合表上 (device_no, seq, ts) 唯一键天然去重，不用在内存里记已见的报文。
     */
    private void onEvent(JsonNode node) {
        String id = text(node, "deviceId");
        String type = text(node, "deviceType");
        String code = text(node, "event");
        if (id == null || code == null) {
            log.debug("物联网事件报文缺 deviceId/event，丢弃");
            return;
        }
        String tsText = text(node, "ts");
        String detail = text(node, "detail");
        JsonNode data = node.get("data");
        String dataJson = null;
        if (data != null && !data.isNull()) {
            try {
                dataJson = json.writeValueAsString(data);
            } catch (Exception ignored) {
                // 序列化失败只丢明细，不影响事件本身落库
            }
        }
        Long seq = Integer.toUnsignedLong((id + '|' + code + '|' + tsText + '|' + detail).hashCode());
        iotEventService.saveDeviceEvent(id, type, code, intValue(node, "level"), detail, dataJson, seq,
                TelemetrySqlBuilder.parseTs(tsText));
    }

    private static Integer intValue(JsonNode node, String field) {
        JsonNode v = node.get(field);
        return v == null || v.isNull() ? null : v.asInt();
    }

    private static String text(JsonNode node, String field) {
        JsonNode v = node.get(field);
        return v == null || v.isNull() ? null : v.asText();
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

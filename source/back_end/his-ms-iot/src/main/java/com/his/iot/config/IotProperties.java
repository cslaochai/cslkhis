package com.his.iot.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 物联网接入配置，绑定 application.yml 的 {@code his.iot.*}。
 * <p>
 * 「订阅 + 落库」与「查询」分开控制：{@link #ingestEnabled} 只决定 HIS 是否自己消费 MQTT 并写时序库，
 * 查询接口不受它影响——时序库里的数据可能由独立落库进程写入（联调期就是这么跑的），
 * HIS 只要连上 TDengine 就能查。
 */
@Data
@Component
@ConfigurationProperties(prefix = "his.iot")
public class IotProperties {

    /**
     * 是否启用 HIS 侧 MQTT 订阅与落库。默认关闭：联调期由独立进程 iot-telemetry-writer 写入，
     * 两边同时开会把同一条遥测重复写进时序库。
     */
    private boolean ingestEnabled = false;

    /**
     * 一批最多攒多少条再落库，防内存涨
     */
    private int maxBatchPerFlush = 5000;

    /**
     * 落库周期（毫秒）。设备 1 秒一条，1 秒一批足够，Grafana 曲线看不出差别
     */
    private long flushMs = 1000;

    /**
     * 单条 INSERT 最多带多少行
     */
    private int maxRowsPerInsert = 200;

    /**
     * 历史查询单次最多返回多少行（防止前端拉全量把接口拖死）
     */
    private int maxHistoryRows = 5000;

    private Mqtt mqtt = new Mqtt();

    private Tsdb tsdb = new Tsdb();

    @Data
    public static class Mqtt {
        /**
         * broker 地址，如 tcp://192.168.88.132:1883
         */
        private String broker = "";
        /**
         * clientId 前缀，实际为「前缀-随机后缀」，避免多实例被 broker 踢下线
         */
        private String clientIdPrefix = "his-ms-iot";
        /**
         * 遥测主题：his/iot/{设备类型}/{设备编号}/telemetry
         */
        private String telemetryTopic = "his/iot/+/+/telemetry";
        /**
         * QoS。0 = 最多一次，遥测允许丢点换吞吐；需要「至少一次」改 1
         */
        private int qos = 0;
    }

    @Data
    public static class Tsdb {
        /**
         * JDBC-RS 连接串，库名写在路径里；REST 无状态，拼 SQL 时仍带库前缀
         */
        private String url = "jdbc:TAOS-RS://127.0.0.1:6041/iot_raw?timezone=Asia/Shanghai";
        /**
         * 账号：查询用只读 his_reader；订阅落库时必须是可写的 his_writer
         */
        private String user = "";
        private String password = "";
        /**
         * 库名，SQL 里一律带 {@code iot_raw.} 前缀
         */
        private String database = "iot_raw";
        /**
         * 设备档案还没落 MySQL 前，标签 dept_id 统一写 0
         */
        private long defaultDeptId = 0L;
    }
}

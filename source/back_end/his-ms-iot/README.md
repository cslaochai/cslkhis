# his-ms-iot —— 物联网设备接入模块

设备遥测接入 HIS 的最小闭环：**设备 → MQTT(EMQX) → 本模块 → TDengine → 页面/Grafana**。
不新增进程、不新增前端，HIS 启动即带上网关能力。

## 为什么放 HIS 里，而不是独立落库进程

独立进程（`E:\work_source\his_cslk\iot-telemetry-writer`）只是联调期用来隔离验证的临时件。
生产形态是：设备厂商网关 ↔ 集成平台 ↔ HIS，HIS 侧必须能**自己收数、自己查数**，否则
「设备在线吗、这条告警谁处理了」永远要跨系统对账。

## 目录

| 包 | 职责 |
|---|---|
| `config/IotProperties` | 绑定 `his.iot.*`；`ingest-enabled` 只控制「是否由 HIS 订阅并落库」 |
| `support/DeviceTypeSchema` | 11 类设备 → 超级表 + 列映射 + 中文名/单位，全链路唯一的协议翻译表 |
| `support/MqttTelemetrySubscriber` | paho 异步订阅 `his/iot/+/+/telemetry`，断线指数退避重连，解析失败只计数 |
| `support/TsdbTelemetryWriter` | 队列 + 定时批量 `INSERT ... USING ... TAGS`，20 万条上限保护 |
| `support/TsdbQuerySupport` | TDengine JDBC-RS 查询，一次一条 SQL，无连接池（REST 无状态） |
| `service/IotTelemetryService` | `latestList`（每类设备一条 `LAST_ROW + PARTITION BY`）/ `historyList` |
| `controller/IotTelemetryController` | `POST /iot/telemetry/latestList`、`POST /iot/telemetry/historyList` |

## 配置（`application-dev.yml` 的 `his.iot`）

```yaml
his:
  iot:
    ingest-enabled: false          # true 才会订阅 MQTT 并写时序库
    mqtt:
      broker: tcp://192.168.88.132:1883
    tsdb:
      url: jdbc:TAOS-RS://192.168.88.132:6041/iot_raw?timezone=Asia/Shanghai
      user: his_reader             # 只查用 his_reader；开启落库必须换 his_writer
      password: HisReader@2026
```

## 接口

最新值（不传 deviceType 返回全部 11 类）：

```json
POST /api/iot/telemetry/latestList
{ "deviceType": "infusion" }
```

历史值（时间必须 `yyyy-MM-dd HH:mm:ss`，limit ≤ 5000）：

```json
POST /api/iot/telemetry/historyList
{ "deviceType": "infusion", "deviceNo": "PUMP-01",
  "startTime": "2026-11-25 10:00:00", "endTime": "2026-11-25 11:00:00", "limit": 500 }
```

两个接口都要登录（`isAuthenticated()`），权限码 `iot:telemetry:view` 尚未铺底。

## 开关互斥（重要）

132 上的 `his-iot-writer` 容器与本模块的订阅**不能同时开**：同一条遥测会被写两遍。
切到 HIS 自收数：先 `docker stop his-iot-writer`，再把 `ingest-enabled` 置 true、
`tsdb.user` 换成 `his_writer`。

## 还没做的事

- `biz_iot_device` / `biz_iot_event` / `biz_iot_cmd_log` 三张 MySQL 表未建（迁移见
  `docs/03-增量SQL变更/355-*.sql`），本模块目前不依赖它们，设备清单直接从 TDengine 标签读。
- 前端页面未做；当前可视化走 Grafana 看板 `his-iot-overview`。
- 指令下行（`his/iot/{type}/{id}/cmd`）未实现，只收上行。

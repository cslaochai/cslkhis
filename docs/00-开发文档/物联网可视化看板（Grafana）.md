# 物联网可视化看板（Grafana）

链路：设备模拟器 → EMQX(1883) → `his-iot-writer`(容器) → TDengine(6041) → Grafana(3001)。
本文只讲"看"，写入侧见 `物联网设备数据存储设计.md` 与 `iot-telemetry-writer` 工程。

## 一、访问与账号

| 项 | 值 |
|---|---|
| 地址 | `http://192.168.88.132:3001` |
| 看板 | `http://192.168.88.132:3001/d/his-iot-overview` |
| Grafana 账号 | `admin` / `HisGrafana@2026`（已用 CLI 重置过，原 admin/admin 已失效） |
| 数据库账号 | 数据源用 **只读** `his_reader` / `HisReader@2026` |

**端口为什么是 3001**：132 的 `3000` 被 Gitea 占着；Grafana 是这台机既有的 `/opt/cicd/grafana`（v11.3.0，systemd 服务 `grafana`），
没有另起一套。早前在 `/opt/grafana` 装了一份副本，已验证功能后删除，避免两套实例抢端口。

## 二、数据源配置（踩过的坑都在这）

- 数据源 uid：`tdengine_iot`，类型 `tdengine-datasource`（插件 v3.7.1，装在 `/opt/cicd/grafana/data/plugins`）
- 连接：`http://127.0.0.1:6041`，走 taosAdapter REST，**不用 6030**
- 账号字段的位置很讲究，配错就是 401 或插件 panic：

| 字段 | 放哪 | 说明 |
|---|---|---|
| `url` | `jsonData.url` + `secureJsonData.url` | 两处都要 |
| 用户名 | **数据源顶层 `user`** | 不是 jsonData 里的 user |
| 密码 | `secureJsonData.password` | 明文写进 provisioning，Grafana 自己加密入库 |

两个反面教材（都实测过）：

1. **`jsonData.basicAuth` 写成字符串 → 插件直接 panic**：
   `interface conversion: interface {} is string, not bool`。Grafana SDK 把 `basicAuth` 当布尔开关解析（TLS/basic 那套
   http settings），塞 base64 进去必崩，查询 500。
2. **只用 `secureJsonData.basicAuth` → 401**：这是前端代理路由的老约定，后端插件读的是
   `user` + 解密后的 `password`。

改完 provisioning 一定要 `systemctl restart grafana`；另外 **Grafana 的 provisioning 是"合并"而不是"覆盖"**：
早先写脏的 `jsonData` 会一直留在 `grafana.db` 的 `data_source` 表里。现象是"文件明明改了，健康检查还是 401/500"。
彻底修法：停服务 → 删掉 `data_source` 里该 uid 的行（或用 API 删）→ 启动，让它按 yaml 重建。

## 三、看板内容

`HIS 物联网设备总览（MQTT → TDengine）`，12 个面板，10 秒刷新，默认看最近 30 分钟：

| 面板 | 设备 | 指标 |
|---|---|---|
| 输液泵滴速 | PUMP-01/02/03 | `rate` |
| 输液余量 | PUMP-01/02 | `remaining_ml` |
| 冷藏箱温度 | FRIDGE-01/02/03 | `temp` |
| 冷藏箱湿度 | FRIDGE-01/02 | `humidity` |
| 环境温湿度 | ENV-01 | `temp` / `humidity` |
| 医用气体压力 | GAS-01 | `o2_kpa` / `vacuum_kpa` / `air_kpa` |
| 病床心率/呼吸 | BED-01 | `heart_rate` / `resp_rate` |
| 透析血流量 | HD-01 | `blood_flow_ml_min` / `dialysate_temp_c` |
| 药品柜温湿度 | CAB-01 | `temp` / `humidity` |
| 婴儿防盗电量 | BABY-01 | `battery` / `rssi` |
| 紫外线灯管 | UV-01 | `lamp_hours` / `filter_pct` |
| 机器人 | RBT-01 | `battery` / `odometer_m` |

查询模板（面板里就这么写，`$from`/`$to` 由插件按时间范围展开）：

```sql
SELECT ts, rate AS PUMP_01
FROM iot_raw.st_infusion
WHERE ts >= $from AND ts <= $to AND device_no = 'PUMP-01'
```

实测（`/api/ds/query` 走 Grafana 代理，时间范围 10 分钟）：输液泵 598 点、冷藏箱 119 点，时间戳与本地时区一致，
说明 `$from/$to` 展开和时区都对。注意 TDengine 会把列别名转小写，图例显示成 `pump_01`，不影响使用。

需要聚合时（例如 6 小时以上）再加窗口：

```sql
SELECT _wstart AS ts, avg(rate) AS rate
FROM iot_raw.st_infusion
WHERE ts >= $from AND ts <= $to AND device_no = 'PUMP-01'
INTERVAL(1m)
```

## 四、文件位置（都在 132 上）

| 文件 | 作用 |
|---|---|
| `/opt/cicd/grafana/conf/provisioning/datasources/his-iot.yaml` | 数据源定义 |
| `/opt/cicd/grafana/conf/provisioning/dashboards/his-iot.yaml` | 看板 provider（folder=HIS IoT） |
| `/opt/cicd/grafana/provisioning-dashboards/his-iot/his-iot.json` | 看板 JSON（12 面板） |
| `/opt/cicd/grafana/data/plugins/tdengine-datasource` | TDengine 插件 |

服务：`systemctl status grafana` / `systemctl restart grafana`，开机自启已启用（原本就是 enabled）。

## 五、边界

- Grafana 是**运维视角**的看板：给开发、设备科看趋势，不是给护士用的业务页面。护士站/大屏最终由 HIS 页面渲染
  （前端调 `/iot/...` 接口），Grafana 不嵌入业务系统。
- 只读账号 `his_reader` 只授了 `iot_raw` / `iot_agg` 的读权限；写权限在 `his_writer`（容器 `his-iot-writer` 用）。
- 手动改看板 UI 会被 provisioning 覆盖（`allowUiUpdates: true` 时改动保存在库里，但文件更新会覆盖同名 uid）。要固化就改 JSON 文件。

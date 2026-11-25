# 物联网时序库（TDengine）部署与接入

> 对应建表脚本：`docs/03-增量SQL变更/356-物联网时序库（TDengine）建库建表.sql`
> 存储总设计：`docs/00-开发文档/物联网设备数据存储设计.md`
> 实施时间：2026-11-24，B 档（关系库 + 时序库）

## 一、部署形态

| 项 | 值 |
|---|---|
| 主机 | 192.168.88.132（与 EMQX 同机） |
| 容器 | `his-tsdb-tdengine`，镜像 `tdengine/tdengine:3.3.6.13` |
| compose | `/opt/tsdb/docker-compose.yml` |
| 端口 | 6030 原生连接 / **6041 REST（HIS 走这个）** / 6060 taosKeeper 监控 |
| 数据卷 | `tdengine_data`（/var/lib/taos）、`tdengine_log`（/var/log/taos） |
| 自启 | `restart: unless-stopped` + `docker.service enabled`，随虚拟机启动 |
| 资源 | `mem_limit: 4g`；实测空库常驻约 160 MB、CPU 3%，磁盘占用按压缩后计 |
| 健康检查 | 容器内 `curl -u root:$TSDB_PASS -d 'SELECT 1' 6041`，返回 `"code":0` 即健康 |

镜像来源：Docker Hub 在这台机器上不可达（DNS 污染），统一从 `docker.1panel.live` 拉取后本地 tag：

```bash
docker pull docker.1panel.live/tdengine/tdengine:3.3.6.13
docker tag  docker.1panel.live/tdengine/tdengine:3.3.6.13 tdengine/tdengine:3.3.6.13
```

**账号**：root 已从默认 `taosdata` 改为非默认密码，密码只存在部署机 compose 的 `TSDB_PASS` 环境变量里，**不写入仓库**。生产环境应另建 `his_writer` / `his_reader` 两个账号并按库授权，不给业务用 root。

```sql
CREATE USER his_writer PASS '<单独口令>';
GRANT WRITE ON iot_raw.* TO his_writer;
GRANT WRITE ON iot_agg.* TO his_writer;
```

## 二、库与保留

| 库 | 存什么 | KEEP | 文件组 |
|---|---|---|---|
| `iot_raw` | 设备原始点位（秒级/分钟级） | 90 天 | 10 天 |
| `iot_agg` | 5 分钟聚合 | 730 天 | 30 天 |

KEEP 是**库级**参数，到期由 TDengine 自动清理，不需要写清理任务；要延长保留直接 `ALTER DATABASE iot_raw KEEP 180`。

## 三、表结构

**一类设备一张超级表**，一台设备由 `USING ... TAGS(...)` 自动建子表，子表名 = 设备编号小写、`-` 换 `_`（`PUMP-01` → `pump_01`）。查询永远写超级表 + 标签过滤，不写子表名。

| 超级表 | 对应设备 | 列 |
|---|---|---|
| `iot_raw.st_coldchain` | 药品冷藏 | temp, humidity, door, power |
| `iot_raw.st_infusion` | 输液泵 | rate, remaining_ml, battery, occluded |
| `iot_raw.st_asset` | 定位信标 | area, rssi, battery |
| `iot_raw.st_env` | 手术室/ICU 环境 | temp, humidity, pressure_pa, door, air_velocity |
| `iot_raw.st_gas` | 医用气体 | o2_kpa, vacuum_kpa, air_kpa, flow_lpm |
| `iot_raw.st_bed` | 智能床垫 | in_bed, heart_rate, resp_rate, turns, body_movement |
| `iot_raw.st_dialysis` | 透析机 | blood_flow_ml_min, dialysate_temp_c, conductivity_ms_cm, tmp_mmhg, elapsed_min, remaining_min, ultrafiltration_ml |
| `iot_raw.st_cabinet` | 智能药柜 | temp, humidity, door, stock_pct |
| `iot_raw.st_baby` | 婴儿腕带 | area, rssi, battery, tamper |
| `iot_raw.st_uv` | 空气消毒机 | running, lamp_hours, filter_pct, occupied |
| `iot_raw.st_robot` | 送药机器人 | task_state, location, battery, load_kg, odometer_m |
| `iot_agg.st_metric_5m` | 通用聚合 | temp, rate, battery, sample_count, metrics(NCHAR JSON) |

所有超级表标签统一为 `device_no / device_type / dept_id`，与 MQTT 主题 `his/iot/{type}/{id}/...` 和设备档案一一对应。

### 字段映射（模拟器 JSON → 列）

| 设备 | JSON 字段 | 列名 | 类型/转换 |
|---|---|---|---|
| coldchain | temp / humidity | temp / humidity | DOUBLE |
| coldchain | door `OPEN/CLOSED` | door | BOOL（OPEN=true） |
| coldchain | power `NORMAL/LOW` | power | BOOL（NORMAL=true） |
| infusion | rate / remainingMl / battery / occluded | rate / remaining_ml / battery / occluded | DOUBLE / DOUBLE / INT / BOOL |
| env | pressurePa / airVelocity | pressure_pa / air_velocity | DOUBLE |
| gas | o2Kpa / vacuumKpa / airKpa / flowLpm | o2_kpa / vacuum_kpa / air_kpa / flow_lpm | DOUBLE |
| bed | inBed / heartRate / respRate / bodyMovement | in_bed / heart_rate / resp_rate / body_movement | BOOL / INT / INT / DOUBLE |
| dialysis | 各 `*MlMin/*TempC/*MsCm/*MmHg/*Min` | 对应 snake_case | INT / DOUBLE |
| cabinet | stockPct | stock_pct | INT |
| baby | tamper | tamper | BOOL |
| uv | running / lampHours / filterPct / occupied | running / lamp_hours / filter_pct / occupied | BOOL / DOUBLE / INT / BOOL |
| robot | state / loadKg / odometerM | task_state / load_kg / odometer_m | NCHAR(32) / DOUBLE / DOUBLE |

**子表列定义建表时就固定**：新增指标要么加到对应超级表（`ALTER STABLE ADD COLUMN`），要么进聚合表的 `metrics` 字段，不能像 MySQL 那样随手塞 JSON。

## 四、HIS 侧接入方式

用 **JDBC-RS（纯 HTTP，走 6041）**，不要用原生 6030：RS 驱动没有本地动态库依赖，容器化部署最省事。

```xml
<dependency>
  <groupId>com.taosdata.jdbc</groupId>
  <artifactId>taos-jdbcdriver</artifactId>
  <version>3.3.6</version>
</dependency>
```

```java
// jdbc:TAOS-RS://host:6041/db?user=...&password=...
String url = "jdbc:TAOS-RS://192.168.88.132:6041/iot_raw";
```

写入用一条 SQL 多行 VALUES，按设备批量、按 200~500 行一批：

```sql
INSERT INTO iot_raw.pump_01 USING iot_raw.st_infusion TAGS('PUMP-01','infusion',100)
  VALUES(1763990000000, 120.0, 180.5, 88, false)
        (1763990001000, 120.5, 179.2, 88, false);
```

要点：

1. **时间戳用毫秒 long**，不要传格式化字符串；`ts` 列声明为 `TIMESTAMP` + 库 `PRECISION 'ms'`。
2. 子表第一次写入即自动创建，不需要预建 63 张表。
3. 标签值写错（比如 `device_type` 拼错）会建出错误的子表，接入层必须用设备档案校验后再写。
4. 批量失败要整批重试；TDengine 对**完全相同时间戳**的重复写入是覆盖语义，天然幂等，不需要再做去重表。

## 五、常用查询

```sql
-- 某台设备最近 24 小时温度曲线（原始库，秒级）
SELECT ts, temp FROM iot_raw.st_coldchain
WHERE device_no = 'FRIDGE-01' AND ts > NOW - 24h;

-- 全病区输液泵当前状态（取每台最后一条）
SELECT LAST_ROW(rate, remaining_ml, battery), device_no
FROM iot_raw.st_infusion WHERE ts > NOW - 5m PARTITION BY device_no;

-- 5 分钟均值（由 iot_agg 直接查，不在查询时算）
SELECT _wstart, AVG(temp) FROM iot_agg.st_metric_5m
WHERE device_type = 'coldchain' AND ts > NOW - 7d PARTITION BY device_no INTERVAL(1h);

-- 跨设备汇总：全院当前告警设备数（标签过滤）
SELECT COUNT(*) FROM iot_raw.st_coldchain WHERE ts > NOW - 5m AND temp > 8;
```

前端"当前值/火花线"仍然走 Redis，不查时序库；时序库只承担**历史曲线和趋势**。

## 六、运维

| 操作 | 命令 |
|---|---|
| 查看状态 | `docker ps --filter name=his-tsdb` |
| 查看库表 | `curl -u root:$PASS -d 'SHOW iot_raw.STABLES' http://192.168.88.132:6041/rest/sql` |
| 备份 | `docker exec his-tsdb-tdengine taosdump -u root -p -o /var/lib/taos/dump`（容器内执行后拷出） |
| 扩保留期 | `ALTER DATABASE iot_raw KEEP 180;` |
| 加列 | `ALTER STABLE iot_raw.st_infusion ADD COLUMN air_alarm BOOL;`（只对之后的数据生效） |
| 重启 | `cd /opt/tsdb && docker compose restart` |

磁盘余量：`/` 497 GB、已用 63 GB；按 63 台 17 条/秒、压缩 10:1 估算，`iot_raw` 90 天约 1~2 GB，`iot_agg` 两年约 1 GB，不构成压力。

## 七、与 A 档的差异

| 项 | A 档（纯 MySQL） | B 档（当前） |
|---|---|---|
| 原始遥测 | 不落库，只 Redis | 落 `iot_raw`，保留 90 天 |
| 5 分钟聚合 | MySQL `biz_iot_metric_5m` | `iot_agg.st_metric_5m`，**MySQL 那张表停用** |
| 设备档案/事件/指令审计 | MySQL | 不变，仍在 MySQL（355） |
| 秒级历史曲线 | 查不到 | 可查（90 天内） |
| 新增设备类型 | 改 JSON | 建新超级表（一类一张），业务代码只加映射 |

## 八、用 IDEA / DBeaver 连接（已实测）

结论：**能连**。用 JDBC-RS 驱动，不需要装 TDengine 客户端。

驱动包已备好（Windows 本机）：`E:\work_source\his_cslk\tools\jdbc\lib`，16 个 jar
（`taos-jdbcdriver-3.6.3.jar` + httpclient / jackson / guava 等依赖，缺一个都会 `NoClassDefFoundError`）。

IDEA（**Ultimate 才有 Database 面板**，Community 没有）：

1. `View → Tool Windows → Database` → `+` → `Driver`
2. Name 填 `TDengine`
3. Driver Files 选 **上面整个 lib 目录的全部 jar**
4. Class 填 `com.taosdata.jdbc.rs.RestfulDriver`
5. URL template 填 `jdbc:TAOS-RS://{host}:{port}/{database}`
6. 数据源里填 `192.168.88.132` / `6041` / `iot_raw`，用户 `root`，密码见部署机 `TSDB_PASS`
7. `Test Connection` 返回 `TDengine 3.3.6.13`

实测（用同一套 jar 跑 `TaosProbe`）：

```
connected = TDengine 3.3.6.13
server_version = 3.3.6.13
stable_count = 11
```

DBeaver 同理：新建驱动 → 加这批 jar → 类名和 URL 同上；DBeaver 还能直接从 Maven 拉依赖，不用手动凑 jar。

两个已知限制：

- IDEA 不认识 TDengine 方言，**语法高亮和补全会很弱**，但查询窗口能正常执行 `SELECT / SHOW / INSERT`；表结构树对超级表的展示也不完整。
- 不要用 6030 原生连接：Windows 端需要本地 `taos.dll`，装客户端才通；6041 纯 HTTP 才是零依赖方案。

生产环境不要用 root 连，按第一节建 `his_writer` / `his_reader` 两个账号。

### 8.1 想"可视化"就别指望 IDEA

IDEA 装完只等于一个 SQL 窗口：结果能出表格，但 `DatabaseMetaData.getSchemas()` 返回 0 条，IDE 按 schema 分组，
于是 `information_schema`、`log` 库和 11 张超级表、子表全糊在同一层，没有 ER 图、没有补全。实测 `getTables("iot_raw",...)`
能返回 14 个对象、`st_infusion` 能返回 8 个字段——数据是有的，是 IDE 的呈现方式不对。

三种更合适的看法：

| 用途 | 工具 | 入口 |
|---|---|---|
| 浏览库表结构、临时跑 SQL | DBeaver（自定义驱动，同 §8 的 jar） | 免注册；表树同样受 metadata 限制，但结果网格、导出、列过滤可用 |
| ~~官方 Web 控制台 taosExplorer~~ | **已关闭，不要再用** | 6060 端口进的是「TDengine 管理系统」，必须手机号 + 短信验证码注册，且要求 explorer 能出外网；院内网环境不适用。已在 compose 设 `TAOS_DISABLE_EXPLORER: "1"` 并删掉 6060 端口映射 |
| 看遥测曲线、做设备大屏 | **Grafana + TDengine 数据源插件** | 已落地在 132 既有实例 `http://192.168.88.132:3001`（3000 被 Gitea 占用），用只读 `his_reader` 连 6041，详见《物联网可视化看板（Grafana）》 |
| 核对表结构、导出 DDL | 容器内 `taos` CLI | `docker exec -it his-tsdb-tdengine taos -s "DESCRIBE iot_raw.st_infusion"` |



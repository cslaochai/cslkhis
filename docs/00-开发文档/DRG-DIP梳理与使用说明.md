# DRG / DIP 梳理与使用说明

> 模块：`his-medicaltech`（DRG 分组模拟器）；前端：`views/drg/DrgSimView.vue`
> 适用库：`192.168.88.132:3306/hn_biz_his`
> 最近更新：2026-10-11（补多版本隔离骨架 + 2.0 数据决策待定）

---

## 1. 现状一览（做到哪了）

| 能力 | 状态 | 说明 |
|------|------|------|
| **DRG 3.0 分组模拟** | ✅ 完整可用 | 目录 5 表 + 入组引擎 + 前端模拟页，按 CHS-DRG 3.0 官方方案跑通 |
| **DRG 2.0 多版本隔离** | 🟡 骨架就绪，数据未灌 | 唯一键已含 `version`、引擎按版本分桶装载、前端版本下拉已加；但官方 2.0 数据**尚未入库**（原因见 §5） |
| **DIP（按病种分值付费）** | ❌ 未开工 | 无表、无引擎、无页面；CHS-DRG 与 DIP 是两套并行体系，DIP 不在本模拟器范围内 |

**一句话结论**：目前能用的只有 DRG 3.0 模拟。切到 2.0 会显示「官方数据未接入，判为未入组（QY）」。

---

## 2. 表结构（DRG 共 6 张表）

### 2.1 分组方案目录（官方下发，重灌型，5 张）

| 表 | 作用 | 关键列 | 唯一键（已含 version） |
|----|------|--------|------------------------|
| `sys_drg_mdc` | 主要诊断大类 MDC（先期分组） | `mdc_code` `mdc_name` `mdc_rule`（规则原文） `sort_no` | `(mdc_code, version, del_flag)` |
| `sys_drg_adrg` | 核心疾病诊断相关组 ADRG | `adrg_code` `adrg_name` `adrg_rule` `mdc_code`（归属） | `(adrg_code, version, del_flag)` |
| `sys_drg_group` | DRG 细分组（最终付费组） | `drg_code` `drg_name` `drg_rule` `adrg_code` `mdc_code` `weight` `pay_standard` | `(drg_code, version, del_flag)` |
| `sys_drg_set` | 规则引用的 ICD 码集合（集合编号→精确码） | `set_code` `icd_code` `set_type`（OP/DI/EX） | `(set_code, icd_code, version, del_flag)` |
| `sys_drg_ccmcc` | 并发症合并症 CC/MCC 目录 | `icd_code` `cc_level`（MCC/CC/NONE） `excl_group`（排除组） | `(icd_code, version, del_flag)` |

> 每张方案表都有 `version varchar(32)`、`source`、`status` 列。3.0 的存量数据 `version` 默认 `'3.0'`。
> 规则原文（`*_rule` 列）存的是 **3.0 两跳 DSL**（见 §3），不是自然语言。

### 2.2 模拟结果（本院产生，按版本隔离，1 张）

`biz_drg_sim_result` — 每条病案首页**每个版本**一条结果（`summary_id + version` 唯一）：

| 列 | 含义 |
|----|------|
| `summary_id` | 病案首页 ID（`biz_inpatient_summary`） |
| `version` | 分组方案版本（3.0 / 2.0） |
| `drg_code` `drg_name` `mdc_code` | 入组结果 |
| `weight` `pay_standard` `actual_amount` `profit_amount` | 权重 / 支付标准 / 实际费用 / 盈亏（标准未下发则为 `null`，页面显示 —） |
| `sim_status` | 1=已入组 2=未入组（QY） |
| `rule_note` | 命中的规则说明 |

唯一键：`(summary_id, version)`。

---

## 3. 分组引擎怎么跑（两跳模型 / 两步法）

CHS-DRG 3.0 是「**先期分组 → 细分组**」两级，规则用 **两跳 DSL**：

1. **MDC 层（先期分组）**：主诊断 ICD 决定进哪个 MDC。规则形如 `ZYZD in DI_B00`（主诊断属于集合 B00 里的码）。
2. **ADRG 层**：在 MDC 内，主诊断 + 手术操作决定进哪个 ADRG。规则形如 `ZYSS in OP_AA1` 或 `{ZYSS, QTSS} in OP2_AC1`。
3. **DRG 细分组层**：ADRG 内再按 CC/MCC 并发症、性别年龄、手术数量细分。规则形如 `length[OP_IC2 ∩ {ZYSS,QTSS}] >= 2`（交集里手术 ≥ 2 个）。

**「两跳」含义**：规则里写的是**集合编号**（如 `DI_B00`、`OP_AA1`），引擎先到 `sys_drg_set` 把集合编号展开成精确 ICD 码列表，再拿病例的实际码去比——这就是两跳。所以 `sys_drg_set` 是引擎的「字典」，没有它规则就无法落地。

`DrgRuleParser` 只认 3.0 这种 DSL（4 种形状：`in` / `not in` / 数值比较 / `length[∩]>=n` / 常量 `1`）。
解析失败的行会被丢弃并打警告，**绝不当兜底档**（避免整组病例错入一个没读懂的组）。

---

## 4. 怎么用（前端 + 接口）

### 4.1 前端页面
- 菜单：`报表统计` 下 `DRG 模拟`（菜单 907）。
- 顶部「分组方案版本」下拉：默认 `CHS-DRG 3.0`；切到 `2.0` 会弹出黄标签提示数据未接入。
- 四个区块：
  1. **汇总卡**：已模拟病案 / 已入组·未入组 / 结余合计 / 超支合计（按当前版本统计）。
  2. **病案首页列表**（最近 50 条）：点「模拟」弹窗，主诊断编码缺失可在弹窗补录 ICD 再跑；「批量模拟」跑全部（编码缺失行跳过并计数）。
  3. **结果列表**：分页查询，关键字（患者/DRG）、入组状态过滤，带「版本」列。
  4. **DRG 细分组目录**：当前版本的组表（权重/支付标准未下发显示 —）。

### 4.2 后端接口（`/api/medicaltech/drg/**`）
| 接口 | 作用 | 版本参数 |
|------|------|----------|
| `POST /simulate` | 单条模拟 | `version`（缺省 3.0） |
| `POST /simulate/batch` | 批量模拟 | `version` |
| `GET /result/page` | 结果分页 | `version` 过滤 |
| `GET /summary/list` | 首页列表 + 汇总统计 | `version` |
| `GET /group/list` | 细分组目录 | `version` 必填 |

---

## 5. 2.0 数据为什么没灌（关键事实 + 决策项）

**事实**：国家医保局下发的 **CHS-DRG 2.0 官方包是一份 1834 页 PDF**，不是 3.0 那种可解析的 Excel/结构化文件。
它的规则是 **一跳自然语言 + 码段文本**（例如「主要诊断在 A00–B99 范围内」「手术操作包含 XX 术式」），
**与 3.0 的两跳 DSL 不兼容**——`DrgRuleParser` 读不了这种文本。

**风险**：如果硬把 PDF 规则「翻译」成 DSL 自动灌库，极可能出现「看起来灌进去了、实际上分组是错的」这种
**看似合理但实则错** 的结果，比不灌更危险（会误导结算判断）。

**所以 2.0 数据加载卡在这里，需要你拍板走哪条路：**

| 方案 | 做法 | 代价 / 风险 |
|------|------|-------------|
| **A. 等官方结构化数据** | 等统筹区医保局下发 2.0 的 Excel/数据库版目录（含 `sys_drg_*` 5 表 + 两跳 DSL 化规则），直接灌 | 最稳妥；但下发时间不可控，且 2.0 规则本身就不是两跳 DSL，仍需人工或半自动转写 |
| **B. 手工转写 2.0 规则** | 由懂 DRG 分组的人把 PDF 一跳规则逐条转成两跳 DSL 入库 | 工作量大、需专业知识、易错（**建议只让真懂分组的人做，且必须抽样回算验证**） |
| **C. 先不灌 2.0** | 维持现状：3.0 可用，2.0 下拉占位但判 QY | 零风险，等数据到位再说 |

**当前默认是 C**：骨架已留好 `version` 通道，哪天拿到可用的 2.0 结构化数据，灌进 5 张表（带 `version='2.0'`）即可，引擎和前端不用再改。

---

## 6. 多版本隔离设计（Route A 落点）

你选的「A：多版本共存 + 切换」已落地，改动点：

1. **唯一键加 `version`**：`sys_drg_mdc / adrg / set / group` 和 `biz_drg_sim_result` 的唯一键都纳入 `version`，两版码值同名也不撞键。
   - 全新库：跑 `01-初始化DDL/04`（已同步）；
   - 已部署库：跑 `03-增量SQL变更/235-DRG多版本隔离（唯一键加version+结果表加version列）.sql`。
2. **缓存按版本分桶**：`DrgSchemeCache` 从单份方案改为 `Map<version, DrgScheme>`，首次用到某版本惰性装载；`DEFAULT_VERSION = "3.0"`，存量行为不变。
3. **引擎入参带版本**：`DrgGrouper.group(facts, version)` 取对应版本快照；某版本目录为空则判未入组并提示版本号。
4. **前端版本下拉**：`DrgSimView.vue` 顶部下拉（3.0/2.0），切版本重拉列表/结果/组表；结果表新增「版本」列。
5. **统计按版本隔离**：汇总卡的计数只算当前版本，不跨版本累加。

> 验证状态：后端 `his-medicaltech` 已 `mvn -o compile` 通过；运行时联调（3.0 照旧、2.0 判 QY）需在能连库的机器启动后端 + 前端走一遍。

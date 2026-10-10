# cslk-HIS · 三甲医院信息化系统（Hospital Information System）

一套面向三甲医院业务闭环的全栈 HIS 系统：模块化单体后端 + Vue3 管理端 +
微信患者端小程序，覆盖预约挂号与全院排班（人力出勤）、门诊医嘱与电子病历、手术麻醉与日间手术、检验检查（LIS/PACS）、药房药库、收费结算与医保、急诊分诊等临床域，以及患者端互联网服务（智能导诊、预问诊、报告解读、随访、在线客服）和
AI 辅助（ICD-10 编码、处方审核、病历质控/草拟、检验/报告白话解读、运营问数、知识库 RAG 等 18 项能力）。

## 技术栈

| 层   | 技术                                                                                                                                                    |
|-----|-------------------------------------------------------------------------------------------------------------------------------------------------------|
| 后端  | Java 21 · Spring Boot 3.2.5 · MyBatis-Plus 3.5.6 · MySQL 8 · Redis · JWT (jjwt) · Knife4j 4.4.0 (OpenAPI) · Hutool · OpenPDF · ZXing · 微信支付 / 支付宝 SDK |
| 管理端 | Vue 3.5 · Vite 8 · Element Plus · Tailwind CSS 4 · Pinia · Vue Router · Axios                                                                         |
| 患者端 | 微信小程序（原生）                                                                                                                                             |
| 部署  | Docker Compose（后端 + Nginx 管理端）                                                                                                                        |
| 数据库 | 全量 DDL 快照（`docs/01-初始化DDL/`，23 个领域、302 张表）+ 铺底数据（`docs/02-铺底基础数据/`，93 张表 + 汇总导入脚本）+ 增量脚本（`docs/03-增量SQL变更/`，200+ 编号）                                  |

## 目录结构

```
cslk/
├── source/
│   ├── back_end/              # Maven 多模块后端（模块化单体）
│   │   ├── his-common/        #   底座：公共实体、电子签名/时间戳(TSA)、工具
│   │   ├── his-system/        #   系统管理：用户/角色/菜单/字典、认证鉴权
│   │   ├── his-patient/       #   患者主索引、健康档案
│   │   ├── his-pharmacy/      #   药房药库、摆药发药
│   │   ├── his-appoint/       #   预约挂号、全院排班与出勤、叫号、急诊
│   │   ├── his-operation/     #   手术申请/排班/安全核查、麻醉、复苏(PACU)、日间手术
│   │   ├── his-medicaltech/   #   检验检查（LIS/PACS 报告）
│   │   ├── his-emr/           #   电子病历、门诊医嘱、病历质控
│   │   ├── his-charge/        #   收费记账、结算、医保（DRG/DIP 分组引擎 + 合规控费）
│   │   ├── his-ai/            #   AI 能力（18 项能力 + 知识库 RAG；位于业务域之上，业务模块不得依赖它）
│   │   ├── his-miniapp/       #   患者端小程序 BFF 聚合层
│   │   ├── his-web/           #   启动层（唯一 Spring Boot 入口，:8080，context-path /api）
│   │   └── sql/               #   增量脚本（按编号递增执行）
│   ├── front/                 # Vue3 管理端（pnpm dev → :3000）
│   └── miniapp/               # 微信患者端小程序
├── docs/
│   ├── 00-开发文档/           # 需求/设计说明书（docx）+ 测试账号与凭据
│   ├── 01-初始化DDL/          # 全库 DDL 快照（23 个领域、302 张表，按 01~23 编号）
│   ├── 02-铺底基础数据/       # 铺底数据 SQL（93 张 A/B 类表 + 00_import_all.sql 汇总导入）
│   ├── 03-增量SQL变更/        # 增量脚本（200+ 编号：排班、AI、患者端服务等新域演进）
│   └── 10-ER关系图/           # E-R 反向建模：index.html 连线图查看器、relationships.csv、分域 Mermaid
├── docker/                    # Docker Compose 部署（后端 :8009 / 管理端 :3100）
└── AGENTS.md                  # 开发规范（前后端契约、命名、鉴权铁律、凭据分流等）
```

## 快速开始

### 前置要求

- JDK 21+、Maven 3.9+
- MySQL 8.0+、Redis
- Node.js 20+、pnpm

### 1. 初始化数据库

1. 先执行 `docs/01-初始化DDL/` 下 `01~23` 编号的领域 DDL（全库建表快照，无 DROP、无数据）。
2. 再执行 `docs/02-铺底基础数据/` 的铺底数据（`00_import_all.sql` 汇总导入，或按需单表执行）。
3. 最后按编号顺序执行 `docs/03-增量SQL变更/` 下的增量脚本（排班、AI、患者端服务等新域的结构与菜单授权演进）。

### 2. 启动后端

配置分三层：`application.yml`（环境无关公共配置）、`config/domain/his-*.yml`（领域行为片段，dev/pro 共用、零密钥）、
`application-{dev,pro}.yml`（环境差异）。**密钥一律走 `HIS_*` 环境变量**（数据库、Redis、JWT、签名主密钥、AI 密钥，参照
`docker/.env.example`）；`application-dev.yml` 为本地个人配置，不入库。

```bash
cd source/back_end
mvn clean package -DskipTests
java -jar his-web/target/his-web-1.0.0.jar
# 或 IDE 直接运行 com.his.HisApplication，服务监听 :8080
```

接口文档（Knife4j）：`http://localhost:8080/api/doc.html`

### 3. 启动管理端

```bash
cd source/front
pnpm install
pnpm dev        # 默认 http://localhost:3000
```

dev server 将 `/api` 代理到 `http://localhost:8080`；需要指向其他实例（如另跑一个 jar 做验收）时，用
`VITE_API_TARGET=http://localhost:8082 pnpm dev`，不改配置。

### 4. 患者端小程序

用微信开发者工具导入 `source/miniapp/`，修改 `app.js` 中的 `globalData.baseUrl`（默认指向局域网后端）为你的后端服务地址。

### 5. Docker 部署（可选）

```bash
cp docker/.env.example docker/.env   # 复制后填真值（.env 不入库）
docker compose -f docker/docker-compose.yml up -d --build
# 后端 http://localhost:8009/api  ·  管理端 http://localhost:3100
```

## 架构要点

- **模块化单体 + 单向分层**：`his-common` 底座 → 业务域（system/patient/pharmacy/appoint/operation/medicaltech/emr/charge）→
  `his-ai`（横切 AI 能力，业务模块**禁止**依赖它，由它反向读业务数据）→ `his-miniapp`（患者端 BFF）→ `his-web` 启动层。跨模块只依赖对方
  Service，禁止直连 Mapper。
- **接口契约**：RESTful + 统一响应体 `{code, message, data}`；入参 `xxxDTO`（带校验注解）、出参
  `listVO/detailVO/SelectListVO`；主键 ID 序列化为字符串避免前端精度丢失。
- **鉴权**：JWT 登录态 + `sys_menu` 驱动的菜单/按钮级权限（前端 `v-perm` 指令 + 后端方法级 `@PreAuthorize`）。
- **电子签名**：内置 TSA 时间戳网关（`his-common` 的 `TsaService`/`SignCryptoUtil`），支撑病历/处方签名合规。
- **AI 纪律铁律**：能力白名单调用（`capabilityKey` 只来自常量）；事实层代码算、模型只解释/产出候选；模型不可用时能力必须降级可用且降级可见；所有模型调用经
  `AiExecutionService` 唯一入口落审计。详见 [AI能力施工手册](docs/AI能力施工手册.md)。
- **凭据分流**：代码/配置入库零密钥，环境凭据只走环境变量（`HIS_*`）或 `docker/.env`；详见 [AGENTS.md](AGENTS.md) 第 8 节。
- 详细开发规范见 [AGENTS.md](AGENTS.md)，业务规划与施工方案见 [docs/](docs/)。

## 医保支付分组（DRG/DIP）

医保支付按国家 DRG/DIP 付费改革落地，系统走 **「贯标字典 → 结构化单据 → 分组引擎 → 合规控费 → 医保平台对接」** 五层管线（图见
[面试架构图](docs/面试架构图.md) 图 3）。

- **分组方案（已接入国家 3.0 官方包）**：照官方的「两跳」模型建表 —— `sys_drg_mdc` / `sys_drg_adrg` / `sys_drg_group`
  三级目录逐行带**入组规则原文**，规则里的集合编号再由 `sys_drg_set` 展开成精确 ICD 码；CC/MCC 目录 `sys_drg_ccmcc`
  逐条挂排除组编号（排除关系由集合表达，不再按主诊断逐对展开）。建表与灌入见 `docs/03-增量SQL变更/233~235`。
- **分组引擎**（`his-medicaltech` 的 `DrgGrouper`）：逐层求值 MDC → ADRG → DRG 细分组，规则原文由 `DrgRuleParser` 编译成表达式，
  **精确码比较、不做前缀近似**；方案快照由 `DrgSchemeCache` 惰性装载。入参维度取自病案首页结构化明细（主诊断/主手术/其他诊断与其他手术、
  性别、年龄含单位、入院体重）。
- **贯标口径**：`sys_icd10` / `sys_icd9cm3` 用 `code_std` 标出医保版贯标码，官方集合引用的码已按贯标口径补齐字典。
- **诚实闸门**：主诊断落不进任何 MDC 时返回 QY 并写明卡在哪一层；合规 D 组（`GroupingRatioRule` D01/D02）在方案为空时判 NA
  并给出获取方案的建议，**绝不谎报分组、绝不静默判「正常」**。

**仍缺的部分（由统筹区医保局下发，不入库、不在代码内编造）**：

1. 病组**权重**与**支付标准**（国家方案包的六表只有编码/名称/规则/所属/排序，不含这两列）→ 下发后更新
   `sys_drg_group.weight` / `pay_standard`；未落地时 D01 费用倍率一律 NA，并写明「目录已接入、标准未下发」，不把「没标准」落成 0 元。
2. 医保局前置机/分组器对接 → 替换 `his-charge` 中 `InsuranceChannelServiceImpl` 的 M9 Mock 口子。

以上到位后，分组引擎与合规 D 组闸门**自动生效，无需再改代码**。

## 文档索引

- E-R 反向建模(docs/10-ER关系图) — 302 张表的关系模型；`docs/er/index.html` 可离线查看连线图
- [测试账号与凭据](docs/测试账号与凭据.md)
- 需求与设计文档（docx）：需求规格说明书、用户需求说明书、业务架构设计说明书、概要设计说明书、详细设计说明书（见 `docs/`）

## License

私有项目，保留所有权利。

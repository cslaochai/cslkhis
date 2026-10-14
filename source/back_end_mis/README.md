# back\_end\_mss 医保结算经办

独立的**医保结算经办平台**（模拟医保局 / 医保结算经办核心系统），与主 HIS（`source/back_end`）完全隔离：

独立 Maven 工程、独立数据库、独立端口、**不做任何登录鉴权**（接口全开放）。

HIS 作为定点机构，通过 HTTP 把结算报文发到本平台，模拟真实医保的「即时结算 → 日对账 → 月度申报 →

智能审核扣款 → 预留质保金 → 月度拨付 → 年终清算」全过程。

> 定位说明：这是
>
> **医保结算经办平台**
>
> （基金怎么申报、怎么核、怎么拨给医院），
> **不是医保基金监管系统**
>
> （后者核心是规则引擎、疑点稽核、飞检、违规追回）。
> HIS 里的 
>
> `ComplianceAudit / YbInspection / YbDeductNotice`
>
>  属于监管侧，本平台不涉及。

## 技术栈（与 back\_end 同构）



* Spring Boot 3.2.5 / Java 21 / Maven

* MyBatis-Plus 3.5.6 + MySQL 8

* 统一返回 `{code, message, data}`、全局异常处理、controller/service/mapper/entity/dto/vo 分层

* Knife4j（OpenAPI）接口文档

## 启动



* 端口：`8090`，context-path：`/mss`，Base URL：`http://localhost:8090/mss`

* 数据库：MySQL 同实例，独立库 `mss_insurance`，**首次启动自动建库建表**
  （`schema.sql` 全 `IF NOT EXISTS`、`data.sql` 全 `INSERT IGNORE`；`spring.sql.init.mode=always`，可重复启动）。
  启动后 `DataInitializer` 幂等播种参保人 + 经办演示数据（三大目录、项目对照、待结算业务、结算单、费用审核、异地备案、清算对账），日期以当天为基准生成，便于工作台/报表直接有数据。

* 默认连接：`192.168.88.132:3306`，账号 `xz_feng`；可用环境变量覆盖：

  `JINB_DB_URL` / `JINB_DB_USERNAME` / `JINB_DB_PASSWORD`

> 工程目录名仍为 `source/back_end_jinb`（历史遗留），但包名 `com.mss`、主类 `MssApplication`、
> artifactId / 产物 `mss-back-end`、context-path `/mss`、库名 `mss_insurance` 均为 mss。

```
cd source/back_end_jinb
mvn spring-boot:run
# 或
mvn -DskipTests package
java -jar target/mss-back-end.jar
```

接口文档：`http://localhost:8090/mss/doc.html`

## 配套经办前端（source/front_mss）

独立 Vue3 + Vite + Element Plus + Tailwind + Pinia 工程，模拟医保经办人员操作界面，端口 `3100`，
9 个菜单全部可见、**无登录守卫**。通过 vite 代理 `/mss` → `http://localhost:8090/mss` 调本后端，
已彻底去除 mock，全部走真实接口。

```
cd source/front_mss
npm install
npm run dev          # http://localhost:3100
```

菜单：经办工作台、门诊/住院医保结算、结算单管理、费用审核、三大目录与对照、异地就医备案、清算对账、统计报表。

## 接口清单（均无鉴权）



| 方法   | 路径                                     | 说明                              |
| ---- | -------------------------------------- | ------------------------------- |
| POST | `/insure/person/query`                 | 参保查询（身份证 / 医保卡号 / 姓名）           |
| GET  | `/insure/person/listPage`              | 参保人分页（keyword/pageNum/pageSize） |
| POST | `/insure/person/save`                  | 新增 / 同步参保人（按身份证幂等）              |
| POST | `/gateway/2304`                        | 结算信息上传（body 为 HIS 报盘原文 JSON）    |
| POST | `/gateway/2305`                        | 结算信息撤销（冲正）                      |
| GET  | `/gateway/dayBill?billDate=yyyy-MM-dd` | 拉取平台侧 “对方账”（日对账）                |
| GET  | `/admin/settlement/listPage`           | 平台结算单分页（month/status）           |
| POST | `/monthly/declare`                     | 月度申报，body `{"month":"yyyy-MM"}` |
| POST | `/monthly/audit`                       | 平台智能审核（拒付 / 扣款 + 质保金预留）         |
| POST | `/monthly/allocate`                    | 月度基金拨付                          |
| POST | `/monthly/yearSettle`                  | 年终清算（考核合格返还质保金）                 |
| GET  | `/monthly/result?month=yyyy-MM`        | 月度结果（应收 vs 实收，含扣款 / 拨付明细）       |
| GET  | `/monthly/listPage`                    | 月度结算单分页                         |

### 经办平台接口（前端 `front_mss` 使用，均无鉴权）

统一返回 `Result`；分页出参为 `{ list, total }`，列表入参 `page/size/keyword`（GET 对象绑定），
动作类为 POST + body DTO。金额为数字、id 为字符串。

| 模块 | 方法 | 路径 | 说明 |
| ---- | ---- | ---- | ---- |
| 工作台 | GET | `/workbench/summary` | 今日 KPI + 待办 + 最近结算单 |
| 工作台 | GET | `/workbench/stats` | 按险种/科室统计 + 近 10 日趋势 |
| 门诊结算 | GET | `/settle/outpatient/listPage` | 门诊待结算业务（bizType=1，前端字段 `visitNo`） |
| 门诊结算 | POST | `/settle/outpatient/preview` | 费用试算分解（body `{bizNo}`） |
| 门诊结算 | POST | `/settle/outpatient/confirm` | 确认结算并生成结算单（body `{bizNo}`） |
| 住院结算 | GET | `/settle/inpatient/listPage` | 住院待结算业务（bizType=2，前端字段 `admissionNo`） |
| 住院结算 | POST | `/settle/inpatient/preview` | 住院费用试算 |
| 住院结算 | POST | `/settle/inpatient/confirm` | 确认出院结算 |
| 结算单 | GET | `/bill/listPage` | 结算单分页（billNo/patientName/insuranceType/settleType/status） |
| 结算单 | GET | `/bill/getDetailById?id=` | 结算单明细（含费用明细） |
| 结算单 | POST | `/bill/reverse` | 退费冲正（body `{id}`） |
| 三大目录 | GET | `/catalog/listPage` | 目录分页（keyword/category/payClass） |
| 三大目录 | GET | `/catalog/stats` | 目录分类/支付类别统计 |
| 院内对照 | GET | `/mapping/listPage` | 院内项目对照分页（keyword/mapped 布尔） |
| 院内对照 | POST | `/mapping/map` | 建立对照（body `{id,insureCode,...}`） |
| 院内对照 | POST | `/mapping/unmap` | 解除对照（body `{id}`） |
| 院内对照 | POST | `/mapping/autoMatch` | 一键自动对照（返回 `matched`） |
| 费用审核 | GET | `/audit/listPage` | 待审核费用分页（keyword/payClass/status） |
| 费用审核 | POST | `/audit/pass` | 审核通过 |
| 费用审核 | POST | `/audit/reject` | 审核驳回 |
| 异地备案 | GET | `/filing/listPage` | 备案分页 |
| 异地备案 | POST | `/filing/upsert` | 新增/编辑备案 |
| 异地备案 | POST | `/filing/revoke` | 撤销备案（body `{id}`） |
| 清算对账 | GET | `/recon/listPage` | 月度对账账期分页 |
| 清算对账 | POST | `/recon/doReconcile` | 执行对账平账（body `{period}`） |

费用试算口径（`SettleEngine`，丙类全自费、甲类 0 先行自付、乙类 10% 先行自付；起付线
职工 300 / 居民 500 / 异地 800；报销比例职工 0.85 / 居民 0.70 / 异地 0.60；
职工个账 = min(自付×0.4, 1500)）由后端统一计算，前端只展示。



* 2304/2305 回执：HTTP 恒为 200，业务成败看 `data.infcode`（`0` 成功、`-1` 失败），

  与 HIS 原进程内 mock 前置机口径一致。

* 幂等：同一 `tradeNo` 重复上传回原回执；2305 对已撤销单幂等成功。

## 内置测试参保人（data.sql）



| 身份证                | 医保卡号      | 姓名 | 险种 | 个账余额    | 状态 |
| ------------------ | --------- | -- | -- | ------- | -- |
| 430102199001011234 | 430100001 | 张三 | 职工 | 3200.50 | 正常 |
| 430102198505056789 | 430100002 | 李四 | 居民 | 0       | 正常 |
| 430102197012121234 | 430100003 | 王五 | 职工 | 1500    | 正常 |
| 430121200006069999 | 430100004 | 赵六 | 居民 | 0       | 正常 |
| 43010219660606001X | 430100005 | 孙七 | 职工 | 80      | 停保 |

未登记 / 停保仅告警不阻断（保证上传链路可跑通）。

## 业务规则（`application.yml` 的 `jinb.*`）



* **平台侧即时核定**（2304 回执 `poolConfirm`，与院方申报值相互独立）：


  * 职工：门诊起付 0 / 报销 70%，住院起付 800 / 报销 85%

  * 居民：门诊起付 0 / 报销 55%，住院起付 1000 / 报销 70%

* **智能审核**：结算单号末位命中 `3` 或 `7` 判违规（模拟超限价 / 不合理诊疗），按 `refuse-ratio=1.00`

  全额拒付该笔统筹。

* **质量保证金**：审核后按净审核金额的 `reserve-ratio=0.05` 预留，年终清算考核合格返还。

* **月度状态机**：1 已申报 → 2 已审核 → 3 已拨付 → 4 已清算，只能顺序推进，重复操作报错。

* 单据号：上传回执 `YBZ+yyyyMMdd+6位序列`；月度拨付 `YBB+yyyyMM+01`；质保金返还 `YBN+yyyyMM+01`。

已验证金额链（3 笔，申报统筹 2100，其中 2 笔末位 3/7 违规）：

申报 2100 → 审核拒付 1400 → 净 700 → 预留质保金 35 → 当月拨付 665 → 年终返还 35 → 累计到账 700。

## 与 HIS（back\_end）联调

HIS 防腐层 `InsuranceChannelService` 保持不变，提供两个实现，按配置二选一：



* `InsuranceChannelServiceImpl`（本地 mock，默认）

* `RemoteInsuranceChannelServiceImpl`（HTTP 调本平台，新增）

配置在 `his-web/src/main/resources/config/domain/his-insurance.yml`：



```
his:
  insurance:
    channel:
      mode: remote                      # mock=不依赖外部；remote=调 back_end_mss
      base-url: http://localhost:8090/mss
      connect-timeout-ms: 5000
      read-timeout-ms: 15000
```



* 平台连不上时，2304/2305 落一条失败回执（台账可见、可重试），不抛断业务；日对账直接报错，

  避免把 “平台不可用” 误判成 “本地多出差笔”。

* 中文报文（患者姓名 / 诊断 / 科室）已按 `application/json; charset=UTF-8` 显式编码。

## 数据表（均 InnoDB /utf8mb4）

网关/月度链路：`mss_insured_person`（参保人）、`mss_settlement`（平台结算单）、`mss_message`（报文台账）、

`mss_monthly`（月度汇总）、`mss_monthly_deduct`（月度扣款明细）、`mss_allocation`（拨付单）。

经办平台（前端 `front_mss`）：`mss_catalog`（医保三大目录）、`mss_item_mapping`（院内项目对照）、

`mss_pending_settle`（待结算业务）、`mss_bill`（结算单）、`mss_fee_item`（费用明细，
`owner_type` 1=待结算 / 2=结算单）、`mss_fee_audit`（费用审核）、`mss_filing`（异地备案）、
`mss_recon`（月度清算对账）。
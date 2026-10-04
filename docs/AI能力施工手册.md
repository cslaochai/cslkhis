# AI 能力施工手册 v1

> 编制日期：2026-10-04　项目：`e:\work_source\his_cslk\cslk`　库：`hn_biz_his`
> 适用范围：`his-ai` 模块（模型接入、提示词、结构化输出、降级熔断、调用审计）及全部 AI 能力的接入、演进与验收。
> **本文件所有"现状"数字均为当日实测**（菜单/表结构/行数来自 `information_schema` 与 `COUNT(*)`，不是估算）。

---

## 0. 这份文件怎么用

- 第 1 节是**纪律铁律**（任何能力不许违反），第 2 节是**现在在哪**（实测水位），第 3 节是**断点总清单**，第 4 节是**新增能力施工 SOP**，第 5 节起是**分期施工计划**，第 8 节是**行业对标**（只校准方向，不构成立项）。
- 施工按 P0→P2 顺序推进；新需求先登记进第 3 节清单、排进对应分期，再动手。
- 每期能力有明确验收标准（跑哪个脚本、断言什么），没跑过不算完成。施工后把实测结果回填进第 7 节。
- 本文件取代原《AI 能力接入方案.md》（该文件从未落盘，`AiCapabilityKeys` / `RestClientLlmClientImpl` 的 javadoc 已改指本手册）。

---

## 1. 目标与纪律铁律

### 1.1 目标

让 HIS 的每个岗位在自己的工作站里获得**降级可用、全程审计、人类终审**的 AI 能力：

| 层 | 回答什么 | 已覆盖 |
|---|---|---|
| 诊中 · 临床 | 帮医生写、审、判 | 病历抽取/草拟/质控、处方审核、检验解读、急诊分诊、ICD 编码 |
| 患者端 | 用大白话解释既有事实 | 报告解读、费用解释、导诊归一 |
| 运营 · 管理 | 帮管理者问数、看健康 | **P0 本期补齐：运营问数**；健康检查、调用审计 |
| 知识 | 院内制度/须知问答 | 知识库 RAG（后端齐，前端待接） |

### 1.2 纪律铁律（违反任何一条 = 不予合入）

1. **能力白名单调用**：`capabilityKey` 必须来自 `AiCapabilityKeys` 常量，**绝不允许由模型输出决定调用哪个能力**。模型自主选择能力 = 从工作流退化成 Agent，可审计性与延迟预算同时失效。
2. **事实层代码算，模型只解释/产出候选**：码值判定、异常识别、危急值判断用代码与规则；模型负责把事实串成话或产出候选草稿。模型不可用时能力必须**降级可用**（降级 ≠ 失效：处方审核降级=放行，ICD 降级=关键词召回，质控降级=必填项硬规则）。
3. **临床动作不写库或必须人类终审**：分诊建议只升不降且不写库；病历草稿/抽取结果必须医生逐字段确认；AI 质控结论只是"提示人工复核"。
4. **降级必须可见**：`data.degraded === true` 时前端必须展示（医生有权知道建议是谁给的）；`degradeReason` 要说人话（"密钥未配置"而不是"AI 不可用"）。
5. **全程审计**：所有模型调用经 `AiExecutionService` 唯一入口，落 `sys_ai_call_log`（能力、状态、时延、token、输入/输出摘要脱敏）。任何绕过执行器直接调 `LlmClient` 的业务代码都是违规。
6. **隐私**：送模型的内容先过脱敏/摘要（`AiMaskUtils`）；患者端能力过 `PatientTextGuard`；审计只落 digest 不落原文。
7. **超时与预算**：每个能力独立 `timeout`；同步路径（医生在等）≤ 10s；超预算的设计必须改异步。
8. **患者端 vs 临床端分开建能力**：读者不同纪律不同（对比 `lab_interpret` 与 `patient_report_explain`），禁止共用提示词与输出契约。

### 1.3 工程约定

- 提示词模板放 `his-ai/src/main/resources/prompts/*.md`，文件头 `version: x.y.z`，`<!-- user -->` 分隔 system/user 段，变量 `{{key}}`；**调提示词不改 Java、不重新编译**。
- 模型输出一律结构化 JSON（`StructuredOutputParser` + `XxxLlmOutputDTO`），解析失败带错误自修正重试一次。
- 依赖方向：业务模块**不得**依赖 `his-ai`（his-ai 位于业务之上读数据）；跨模块取数只走对应模块的 service/mapper 依赖注入。
- 运营问数（P0）是唯一"模型产出 SQL"的能力，必须过 `OperationSqlGuard` 白名单闸门（见 §6.2），属于纪律 2 的受控例外。

---

## 2. 现状水位（2026-10-04 实测）

### 2.1 底座组件（全部在 `com.his.ai`）

| 组件 | 职责 |
|---|---|
| `LlmClient` / `RestClientLlmClientImpl` | OpenAI 兼容接入（DashScope compatible-mode，`deepseek-v4.1-flash`），失败/超时统一抛 `LlmException` |
| `AiExecutionService` | **所有模型调用唯一入口**：开关→熔断→渲染→调用→解析→自修正→审计→降级，异常不外抛，返回 `Optional.empty()` |
| `AiDegradeGuard` | 连续失败熔断（300s/5 次）、降级原因三段递进（配置→熔断→具体失败） |
| `PromptTemplate` | prompts/*.md 渲染 + 版本号进审计 |
| `StructuredOutputParser` | JSON 解析 + 契约校验 |
| `AiAuditService` / `AiAuditQueryService` | `sys_ai_call_log` 落库与分页查询 |
| `AiConfigProvider` | `ai.*` 配置快照 + 能力开关/超时/模型分流 |
| `AiHealthService` | `/ai/healthCheck` 配置快照（不产生费用） |

### 2.2 能力清单（12 个，key ↔ yml `ai.features.<key>`）

| key | 能力 | 触达端 | 降级语义 |
|---|---|---|---|
| `icd10` | ICD-10 智能编码（粗召回 50 → 模型精选） | 医生工作站 | 关键词规则召回 |
| `drug_audit` | 处方合理性审核（硬规则先行 + 模型补充） | 医生工作站/审方 | 放行不拦开方 |
| `emr_qc` | 病历内涵质控（必填项硬规则 + 内涵质检） | 质控员 | 仅硬规则结果 |
| `lab_interpret` | 检验结果解读（异常/趋势代码算，模型解释） | 检验工作站 | 规则结论照常返回 |
| `emergency_triage` | 急诊分诊建议（红旗征硬规则，只升不降，不写库） | 急诊 | 维持人工级别 |
| `emr_extract` | 病历自由文本结构化抽取（不写库，逐字段确认） | 医生工作站 | 标签逐字切分 |
| `emr_draft` | 病历现病史草拟（只草拟现病史） | 医生工作站 | 无草稿可给，如实说 |
| `patient_report_explain` | 患者端报告大白话解读（事实+词典，模型串话） | 患者端 | 朴素文案 |
| `patient_fee_explain` | 患者端费用解释（甲乙丙类拆分确定性计算） | 患者端 | 拆分表照常 |
| `patient_triage_normalize` | 导诊口语归一（口语→症状词，不推荐科室） | 患者端 | 原话直查规则表 |
| `knowledge_qa` | 知识库问答 RAG（向量召回 + 生成，只科普不判定） | 院内/患者端 | 返回召回原文片段 |
| `health_check` | 连通性自检 | 运维 | — |

### 2.3 数据面实测

- `sys_ai_call_log`：审计主表（能力/状态/时延/token/摘要），有改名同步留痕（`workspace/_apply.mjs`）。
- `sys_knowledge_doc` + `sys_knowledge_chunk`：RAG 存储；向量库 `InMemoryVectorStore`（重启重建），embedding 已切 `remote`（DashScope text-embedding-v3）。
- 模型密钥：`HIS_AI_API_KEY`（`application-local.yml`，同一把 key 兼 LLM 与 embedding）。

### 2.4 前端接线实测

- `src/api/ai.js` 已备 12 组函数；**`getAiAuditLogPage` 与知识库管理接口无任何视图消费**（grep 证据）。
- AI 能力散落在各业务页内嵌使用，无独立 AI 页面；`report:bi:list`（BI 驾驶舱，菜单 906，父级 1200 报表统计）是运营层唯一数据页。

---

## 3. 断点总清单

> 编号规则：`G` = gap。分期列标明在哪一期解决。

| 编号 | 现象 | 证据 | 影响 | 分期 |
|---|---|---|---|---|
| G-01 | `docs/AI能力接入方案.md` 被 2 处 javadoc 引用但**文件从未落盘**（悬空引用） | `AiCapabilityKeys.java:8`、`RestClientLlmClientImpl.java:35` | 新人按注释找文档找不到；架构口径无权威载体 | **P0**（本手册落盘 + 改引用） |
| G-02 | 能力全景的**运营·管理层 AI 空白**：管理者没有自然语言问数入口 | §1.1 表；报表层只有静态 BI（菜单 906/1201） | 院长/审计要数据只能翻报表或找信息科跑 SQL | **P0**（本期施工 `operation_qa`） |
| G-03 | AI 调用审计**有接口无页面**：`getAiAuditLogPage` 零视图消费 | grep 证据（仅 api/ai.js） | 出问题只能查库；管理员看不到模型调用健康度 | P1 |
| G-04 | 知识库问答**后端/存储齐但前端零接线**：无上传、无维护、无问答页 | `AiKnowledgeController` + `sql/217` vs 前端 grep | RAG 能力对用户不存在 | P1 |
| G-05 | **预问诊**缺失（患者挂号后、就诊前采集病史写回 EMR） | 无对应能力 | 医生站缺最省时的前置输入 | P2（依赖小程序启用） |
| G-06 | **智能随访**缺失（出院随访/慢病管理外呼文案） | 无对应能力 | 诊后管理靠人工电话 | P2 |
| G-07 | 医保智能审核目前是**纯规则闸门**（`insurance.compliance.*`），LLM 读病历证据未引入 | `application.yml` insurance 段 | 证据判定依赖人工，是否引入 LLM 属业务拍板 | 决策 |

---

## 4. 新增能力施工 SOP（8 步，按序执行）

1. **登记**：在第 3 节清单立项，明确触达端、降级语义、审计 bizType。
2. **能力常量**：`AiCapabilityKeys` 加 `public static final String XXX = "xxx";` + 纪律注释（做什么/不做什么/降级行为）。
3. **配置**：`application.yml` 的 `ai.features.<key>: true` 与 `ai.timeouts.<key>`（同步路径 ≤10000ms；说明超时理由，参照既有注释口径）。**key 必须与常量逐字一致**。
4. **提示词**：`prompts/<能力名>.md`，system 段写角色 + 禁区 + 输出 JSON 契约，user 段放事实数据 `{{vars}}`，头部 `version`。
5. **契约**：`XxxLlmOutputDTO`（模型输出）+ 能力入参 DTO（校验注解 `@NotBlank/@NotNull`，中⽂ message）+ `XxxResultVO`（含 `degraded`/`degradeReason`）。
6. **实现**：`service` 接口 + `service/impl` 实现。骨架照 `EmrQcCapabilityImpl`：事实层代码算 → `AiCallDTO`（bizType/bizId/inputDigest 摘要）→ `aiExecutionService.call(call, XxxLlmOutputDTO.class)` → `Optional.empty()` 时按能力语义降级并填 `degradeReasonOf(key)`。
7. **接口**：Controller 类级 + 每个方法级 `@PreAuthorize` 双标（权限码逐字取自 `sys_menu`，通用参照数据用 `isAuthenticated()`）；方法体只「调 service → Result」，禁止逻辑下放 Controller 或上提 Mapper。
8. **前端 + 验收**：`src/api/ai.js` 加函数（契约注释写明 degraded 语义）；页面渲染；跑 §4.1 验收清单，实测结果回填 §7。

### 4.1 能力级验收清单（机械判据）

- [ ] `mvn -o -DskipTests clean package` 成功（改包/加依赖后必须 clean package，不认增量编译）。
- [ ] 新端口启动 `Started HisApplication`，`GET /api/ai/healthCheck` 快照含新能力且 enabled。
- [ ] 登录脚本打新接口：断言 `code=200`、`data.degraded=false`、**真实业务字段非空**（禁止拿降级文案当 PASS）。
- [ ] `SELECT status, COUNT(*) FROM sys_ai_call_log WHERE capability_key='<key>' GROUP BY status` 有 1-success 行。
- [ ] 降级演练：`ai.features.<key>: false` 重启（或临时改配置刷新）再调 → `degraded=true`、`degradeReason` 说人话、**页面/接口不报错**；改回 true 再刷新。
- [ ] 前端 playwright-core + Edge 真浏览器：页面渲染真实接口数据、`degraded` 提示可见、控制台零报错。
- [ ] 权限码核对：前端引用的码 100% 能在 `sys_menu.menu_type=3` 找到；铺底 SQL 跑完回查 + 清 `his:perm:role:*` 缓存。

---

## 5. 分期施工计划

| 期 | 内容 | 断点 |
|---|---|---|
| **P0（本期）** | ① 手册落盘 + 修 G-01 悬空引用；② `operation_qa` AI 运营问数（NL2SQL 白名单闸门 + 问数页 + 菜单 sql/224） | G-01、G-02 |
| P1 | 「AI 管理台」一页收口：调用审计列表（G-03）+ 知识库维护/问答接线（G-04） | G-03、G-04 |
| P2 | 预问诊（G-05）、智能随访（G-06） | 依赖小程序/外呼通道启用 |
| 决策 | G-07 医保审核 LLM 证据判定 | 医保办拍板后进对应期 |

---

## 6. P0 详细设计：AI 运营问数（`operation_qa`）

### 6.1 定位与纪律

- 读者：院领导（role 18）、系统管理员（role 1）。回答"最近 7 天每天发药多少张处方/多少钱""本月各支付方式收款占比"这类**经营统计问题**。
- 模型只做一件事：把自然语言翻译成一条受控 SELECT。**执行、截断、呈现全部由代码完成**；返回体原样带回生成的 SQL（透明可查）。
- 明确不做：不答个体患者临床信息（白名单表里没有患者主档/病历内容列）；不写任何库；不做跨库/跨 schema 访问。

### 6.2 安全闸门 `OperationSqlGuard`（模型产 SQL 的十道闸）

| # | 规则 | 违规处置 |
|---|---|---|
| 1 | 去首尾空白与结尾 `;`，语句内部再出现 `;` 判多语句 | 拒绝 |
| 2 | 含 SQL 注释痕迹（`--` `/*` `#`） | 拒绝 |
| 3 | 必须以 SELECT 开头（大小写不敏感） | 拒绝 |
| 4 | 黑名单词（insert/update/delete/drop/alter/create/truncate/rename/merge/grant/revoke/call/set/use/lock/unlock/load/handler/prepare/execute/show/for update/outfile/dumpfile；**不含 desc/explain/replace** —— 三者的语句形态都以非 SELECT 开头被闸 3 拦截，而 ORDER BY DESC、REPLACE() 清洗函数是合法分析写法，进黑名单只会误杀） | 拒绝 |
| 5 | 禁止 schema 限定（`information_schema.` / `performance_schema.` / `mysql.`） | 拒绝 |
| 6 | `FROM`/`JOIN` 后的表名逐个提取，**必须 ⊆ 白名单 11 张表**（§6.3） | 拒绝 |
| 7 | 无 `LIMIT` 自动补 `LIMIT 200`；`LIMIT n>200` 压到 200 | 改写 |
| 8 | 执行时 `queryTimeout=10s`、只读连接语义（JdbcTemplate 查询方法天然只读） | 兜底 |
| 9 | 结果行 >100 只回显前 100，`truncated=true` 标注 | 改写 |
| 10 | 生成 SQL 与执行行数写审计（inputDigest=问题摘要，outputDigest=SQL 摘要） | 留痕 |

### 6.3 白名单表与码值口径（进提示词 + `OperationSchemaCatalog` 常量，两处同源）

| 表 | 用途 | 关键列/码值口径 |
|---|---|---|
| `sys_department` | 科室字典 | `dept_name`、`dept_type`、`is_open`、`status` |
| `sys_ward` | 病区与床位 | `ward_name`、`total_beds`、`occupied_beds`、`status(0-停用 1-正常)` |
| `biz_admission` | 住院 | `admit_status(0-已出院 1-在院)`、`nursing_level(1-特级…4-三级)`、`admit_time`、`discharge_time`、`ward_id`、`admit_dept_id` |
| `biz_visit` | 门诊就诊 | `visit_status(0-已取消 1-进行中 2-已完成)`、`start_time`、`total_amount` |
| `biz_fee_record` | 记账行（费用事实） | `amount`、`fee_status(1-待结算 2-已锁定 3-已结算 4-已红冲)`、`item_type(1-挂号费 2-西药 3-中成药 4-中药饮片 5-检查 6-检验 7-治疗 8-耗材)`、`catalog_type(0-自费…3-丙类)`、`encounter_type(1-门诊 2-住院)`、`book_time` |
| `biz_settlement_bill` | 结算账单 | `bill_status(1-待支付…4-已作废 5-已退费)`、`bill_type`、`settlement_mode(1-自费 2-医保)`、`total/discount/pool/account/self/payable/paid/refund_amount`、`bill_time` |
| `biz_payment_txn` | 收退款流水 | `direction(1-收款 2-退款)`、`amount` **收正退负**、`txn_status(1-成功 2-已冲正)`、`pay_method(1-现金…7-转账)`、`txn_time` |
| `biz_prescription` | 处方 | `prescription_status(1-草稿 2-已提交 3-已审核 4-已发药 5-已取消 6-已退药)`、`payment_status(0/1/2)`、`visit_date`、`total_amount`、`dept_name`、`doctor_name` |
| `biz_prescription_detail` | 处方用药明细 | `drug_name`、`quantity`、`amount`、`detail_status` |
| `biz_laboratory_record` | 检验 | `record_status(1-已登记…7-已发布 8-已取消)`、`visit_date`、`apply_dept_name`、`price` |
| `biz_operation_apply` | 手术 | `operation_status(0-待排期…3-已完成 4-已取消)`、`operation_level(1~4)`、`anesthesia_type(1~5)`、`is_emergency`、`apply_time` |

提示词内口径纪律：金额聚合默认排除红冲/作废/取消（`fee_status<>4`、`txn_status=1`、处方排除 5/6）；时间条件必须写，用户未说范围默认近 30 天；输出列一律中文别名。

### 6.4 接口契约

| 接口 | 方法 | 权限码 | 入参 | 出参 |
|---|---|---|---|---|
| `/ai/operationQa/ask` | POST | `ai:operationQa:ask`（页面 `ai:operationQa:list`） | `OperationQaAskDTO{question(@NotBlank @Size(200)), withSummary?}` | `OperationQaResultVO{question,title,sql,columns[{key,label}],rows,rowCount,truncated,elapsedMs,degraded,degradeReason,summary}` |
| `/ai/operationQa/schema` | GET | `ai:operationQa:list` | — | 白名单表清单 `List<OperationSchemaVO>`（前端"可查询的数据域"提示） |

`withSummary=true` 时二段调用（lite 模型）把结果表串成 ≤3 句结论；失败静默置空（表格照常返回）。

### 6.5 页面与菜单

- 页面 `views/ai/OperationQaView.vue`：问题输入 + 常用问题 chips + 结果表（动态列）+ 结论段 + degraded 警示条（`v-if` 必显）+ SQL 折叠面板（透明可查）+ 可查询数据域说明。查询按钮挂 `v-perm="'ai:operationQa:ask'"`。
- 路由：主布局 children 加 `{ path: 'ai-operation-qa', ... }`（与 menu.path `/ai-operation-qa` 一字不差）。
- 铺底 `source/back_end/sql/224-AI运营问数（菜单与授权）.sql`：菜单 2940（页面，父 1200 报表统计，sort 46）+ 2941（按钮）；授权 role 1 + 18；`sys_role_menu` id 派生 `7000+menu_id`；守卫 `NOT EXISTS (id OR menu_key OR permission)`；执行后回查 + 清 `his:perm:role:*` 缓存。

### 6.6 验收脚本（P0 专用）

1. 「最近7天每天的发药处方数量和金额」→ 200 且 `rows` 非空、`degraded=false`。
2. 「本月各支付方式的净收款金额」→ 200 且 `rows` 非空。
3. 「删除所有患者」→ 200 且 `degraded=true`（闸门拒绝，`degradeReason` 说明只允许 SELECT）。
4. 「查询所有患者的手机号和身份证号」→ 200 且 `degraded=true`（表不在白名单）。
5. 审计核对：`capability_key='operation_qa'` 有 success / degraded 行各若干。

---

## 7. 施工记录（回填区）

| 日期 | 期 | 内容 | 实测结果 |
|---|---|---|---|
| 2026-10-04 | P0 | 修 G-01：`AiCapabilityKeys` / `RestClientLlmClientImpl` 两处悬空文档引用改指本手册 | 完成 |
| 2026-10-04 | P0 | `operation_qa` 后端全量落地：prompts ×2（operation-qa / operation-qa-summary）、`OperationQaAskDTO` + 两个 LLM 输出 DTO、`OperationSchemaCatalog`（白名单 11 表单一事实源）、`OperationSqlGuard`（闸门 1~7，执行层 8~9 在能力实现内）、`OperationColumnVO` / `OperationRowVO` / `OperationSchemaVO` / `OperationQaResultVO`、`OperationQaCapability` + Impl、`AiOperationQaController`；`application.yml` 开关与超时已配 | `mvn -o -DskipTests clean package` 通过，fat jar 75MB 产出 |
| 2026-10-04 | P0 | 前端接线：`api/ai.js` 增 `askOperationQa` / `getOperationSchema`（契约注释含 degraded 必显、SQL 可查）；`views/ai/OperationQaView.vue`（chips + 动态列表格 + 结论段 + degraded 警示 + SQL 折叠 + 数据域说明，按钮挂 `v-perm`）；路由 `ai-operation-qa` | `vite build` 通过（7.59s） |
| 2026-10-04 | P0 | 菜单铺底 `sql/224-AI运营问数（菜单与授权）.sql`：菜单 2940/2941（父 1200，sort 46，icon DataAnalysis）、授权 role 1+18、role_menu id 7000+menu_id、三键守卫 + 7 项自检；执行脚本 `workspace/_sql224.mjs`、验收脚本 `workspace/_verify_operationqa.mjs`（§6.6 五条） | **SQL 未执行** —— dev 库 192.168.88.132 自 12:14 起无应用层响应（3306/6379 TCP 可建连但无数据返回，中间设备代答），属环境故障非脚本问题；库恢复后执行 sql/224 → 回查自检 → 清 `his:perm:role:*` 缓存 → 起包跑验收 |
| 2026-10-04 | P0 | 口径修正：闸门 4 黑名单移除 desc / explain / replace（语句形态被闸 3 拦截，词形在合法 SELECT 里真实存在——ORDER BY DESC、REPLACE() 函数，进黑名单必误杀）；§6.2 已同步 | 完成 |
| 2026-10-04 | P0 | 新增第 8 节行业对标：卫宁/讯飞/京东健康/江苏医保局公开动态 → 与 12 能力映射；4 项候选登记待拍板 | 完成 |

---

## 8. 行业对标：HIS 厂商 AI 落地方向（2026-10-04 调研）

> 依据当日检索的公开信息：卫宁健康（半年报交流会、WAIC/CHINC 发布动态）、讯飞医疗、温医大附一院×京东健康、江苏省医保局 DIP 监管答复、昆明延安医院智慧服务采购征集。
> **数字是厂商公开口径，不是本项目实测**；与 §2"实测水位"严格区分。本节只校准方向，候选能力拍板后才登记进第 3 节。

### 8.1 行业主线

1. **医生文书负担**：病历生成是唯一公认"已过实用门槛"的大模型场景（讯飞星火医疗 V3.5 病历生成/影像报告生成达实用门槛，累计 5.8 亿次辅助病历书写、12.3 亿次辅助诊断）。
2. **医保监管高压**：监管端已用 AI 查医院——江苏医保局在建"DIP 智能监管智能体"，AI 读电子病历识别"低码高编"并要求医院上传全量电子病历（13 个接口）。**医院端必须用 AI 自检自保，这是刚需而非加分项。**
3. **运营精细化 + 平台化**：卫宁 2026 上半年发布 8 款 AI 产品、客户开始为 AI 设独立预算（最受认可的两款是 MedMap 医案智图与 AI 重症系统）；采购形态从单点产品转向"AI 应用管理平台（统一管模型/智能体/知识库/提示词）+ 场景插件"（昆明延安医院征集口径）。

### 8.2 四层落地地图与本项目映射

| 层 | 行业主流场景（2026 实测状态） | 本项目对应 | 判定 |
|---|---|---|---|
| 诊前 | AI 预问诊（温医大附一院×京东卓医：预问诊报告直写 EMR，门诊 AI 累计 910 万人次）、精准预约（多轮对话匹配科室）、数字人陪诊 | G-05 预问诊（P2） | 行业最热场景之一；本项目列 P2 的约束是小程序/互联网医院启用，属外部依赖 |
| 诊中 | 病历生成、语音转写、CDSS 全科推理、危重预警（卫宁"鲲鹏/锐治"专科大模型、讯飞 AI 诊疗助理 2.0 多智能体，质控 Agent 事中拦截 60%+ 病历风险） | `emr_draft` / `emr_extract` / `drug_audit` / `emergency_triage` 已建；语音转写、危重预警未建 | 草拟/抽取"人终审"口径与行业一致；缺口见 §8.3 候选 |
| 诊后 | 随访外呼（卫宁 AI 家医随访助手"说完即录"）、专病全病程管理（讯飞 150+ 条专病路径覆盖约 80% 出院患者） | G-06 智能随访（P2） | 与既有分期一致 |
| 运营 | 运营问数（NL2SQL）、病案编码辅助、病历内涵质控、医保智能审核（规则库 + AI 证据判定）、护理交接班 | **`operation_qa`（P0 本期）**、`icd10`、`emr_qc` 已建；G-07 医保审核 LLM 证据判定待拍板 | 问数/编码/质控与行业主线完全对齐，无返工风险 |
| 底座 | 统一模型接入 + 能力开关 + 全程审计 + 降级（医院自建 AI 平台成采购标配） | his-ai 底座（执行器唯一入口/熔断/审计/提示词外置/结构化输出） | 架构形态一致 |

### 8.3 候选登记项（未立项，拍板后进第 3 节）

| 候选 | 行业参照 | 前置条件与约束 |
|---|---|---|
| 语音转写（问诊录音→现病史草稿） | 各厂商 Copilot 标配输入 | 需录音采集终端与院内 ASR 选型；产出走 `emr_draft` 同款"草稿 + 医生确认"契约 |
| 危重预警/病情恶化评分 | 卫宁"鲲鹏"重症、讯飞危重预警 | 依赖生命体征/护理数据完整性；先做事实层评分代码，模型只解释——纪律 2 |
| AI 护理交接班 | 卫宁与东莞一院四大落地应用之一 | 依赖护理记录结构化程度 |
| 影像报告解读 | 讯飞影像报告生成已达实用门槛 | **涉 NMPA 三类证**：只做"报告/指标解读"（同患者端纪律），不做诊断结论 |

### 8.4 对标结论

- 本项目 12 能力的选点落在行业"文书负担 + 监管自保 + 运营问数"三条主线上，**无方向性返工风险**；且"降级可用 + 人类终审 + 全程审计"三条纪律与行业共识（LLM 在编码/DRG 场景仍处验证期、人机协同是落地前提）一致。
- 差距集中在**输入侧**（语音/预问诊的数据采集）与**专科深度**（危重预警），均为外部依赖而非架构缺陷。
- 行业红线与 §1.2 纪律 2/3 相同：AI 不出诊断结论、临床动作人类终审；此口径不随厂商进度放宽。

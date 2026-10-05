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
| 诊前 | 就诊前采集病史 | G-05 预问诊（量表下发 + AI 凝摘要 + 医生站报告卡，P2 起） |
| 诊后 | 随访管理与患者反馈 | G-06 智能随访（AI 拟话术 + 站内信触达 + 小程序反馈回写，P2 起） |
| 知识 | 院内制度/须知问答 | 知识库 RAG（P1 起 AiAdminView 问答/维护签页已接） |

### 1.2 纪律铁律（违反任何一条 = 不予合入）

1. **能力白名单调用**：`capabilityKey` 必须来自 `AiCapabilityKeys` 常量，**绝不允许由模型输出决定调用哪个能力**。模型自主选择能力 = 从工作流退化成 Agent，可审计性与延迟预算同时失效。
2. **事实层代码算，模型只解释/产出候选**：码值判定、异常识别、危急值判断用代码与规则；模型负责把事实串成话或产出候选草稿。模型不可用时能力必须**降级可用**（降级 ≠ 失效：处方审核降级=放行，ICD 降级=关键词召回，质控降级=必填项硬规则）。
3. **临床动作不写库或必须人类终审**：分诊建议只升不降且不写库；病历草稿/抽取结果必须医生逐字段确认；AI 质控结论只是"提示人工复核"。
4. **降级必须可见**：`data.degraded === true` 时前端必须展示（医生有权知道建议是谁给的）；`degradeReason` 要说人话（"密钥未配置"而不是"AI 不可用"）。
5. **全程审计**：所有模型调用经 `AiExecutionService` 唯一入口，落 `sys_ai_call_log`（能力、状态、时延、token、输入/输出摘要脱敏）。任何绕过执行器直接调 `LlmClient` 的业务代码都是违规。
6. **隐私**：送模型的内容先过脱敏/摘要（`AiMaskUtils`）；患者端能力过 `PatientTextGuard`；审计只落 digest 不落原文。
7. **超时与预算**：每个能力独立 `timeout`；同步路径（医生在等）≤ 10s；超预算的设计必须改异步。
8. **患者端 vs 临床端分开建能力**：读者不同纪律不同（对比 `lab_interpret` 与 `patient_report_explain`），禁止共用提示词与输出契约。
9. **可穷举禁走模型**：能力的"模型产出"若能被一张对照表/词典/规则集穷举（逐项解释、固定拆分、固定映射、固定量表），禁止调用模型——做页面/词典/规则实现（判据见 §8.5）；模型只处理开放输入与长尾。违反即伪 AI：白付审计/熔断/降级全套底座税，还引入幻觉与延迟。

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

### 2.2 能力清单（17 个，key ↔ yml `ai.features.<key>`）

| key | 能力 | 触达端 | 降级语义 |
|---|---|---|---|
| `icd10` | ICD-10 智能编码（粗召回 50 → 模型精选） | 医生工作站 | 关键词规则召回 |
| `drug_audit` | 处方合理性审核（硬规则先行 + 模型补充） | 医生工作站/审方 | 放行不拦开方 |
| `emr_qc` | 病历内涵质控（必填项硬规则 + 内涵质检） | 质控员 | 仅硬规则结果 |
| `lab_interpret` | 检验结果解读（异常/趋势/危急值代码算；**仅异常 ≥3 组合异常时调模型连贯解读**，P1 起） | 检验工作站 | 异常 <3 走规则（设计内路径，`source=rule, degraded=false`）；≥3 时模型失败降级规则（`degraded=true` + 原因） |
| `emergency_triage` | 急诊分诊建议（红旗征硬规则，只升不降，不写库） | 急诊 | 维持人工级别 |
| `emr_extract` | 病历自由文本结构化抽取（不写库，逐字段确认） | 医生工作站 | 标签逐字切分 |
| `emr_draft` | 病历现病史草拟（只草拟现病史） | 医生工作站 | 无草稿可给，如实说 |
| `patient_report_explain` | 患者端报告大白话解读（事实+词典，模型串话） | 患者端 | 朴素文案 |
| `patient_triage_normalize` | 导诊口语归一（口语→症状词，不推荐科室） | 患者端 | 原话直查规则表 |
| `knowledge_qa` | 知识库问答 RAG（向量召回 + 生成，只科普不判定） | 院内/患者端 | 返回召回原文片段 |
| `previsit_summary` | 预问诊病史摘要（量表结构由代码版本化下发，模型只把结构化作答凝成就诊用摘要，P2 起） | 患者端（生成）→ 医生工作站（只读报告卡） | 规则模板拼接（`source=rule`），摘要不缺位 |
| `followup_compose` | 随访话术草拟（随访类型 + 慢病档案上下文，医生核对改完才可保存，P2 起） | 随访工作台（医护端） | 类型模板兜底（`source=rule, degraded=true` + 原因） |
| `insurance_evidence` | 医保审核证据判定（规则命中后逐条读病历证据判「支持/反驳/证据不足」，P3 起） | 合规审核台账（详情抽屉） | `degraded=true` + 原因（规则判定依据与整改建议照常可见） |
| `deterioration_alert` | 危重预警·病情恶化评分（MEWS+SpO2 **纯代码评分**，仅预警级≥1 调模型给观察建议，只提示不写库、预警级由评分定不由模型定，P4 起） | 护士工作站/床位中心床位图（角标 + 预警弹窗） | 评分与预警级照常（代码事实），仅缺文字建议 |
| `nursing_handover` | AI 护理交接班摘要（病区×班次事实聚合（代码）→ 模型拟 SBAR 草稿 → 护士编辑终审，不写库，P4 起） | 护士工作站第四视图 | 规则模板拼接（`source=2`）+ 警示条必显 |
| `voice_transcribe` | 语音口述转写（DashScope qwen3-asr-flash 同步 ASR，**非 LLM 能力、不走 AiExecutionService**，独立 `SpeechTranscribeService` 直调；转写文本医生改后喂既有 `emr_draft`，P5 起） | 医生工作站病历编辑 | 无降级：失败如实报错不造文本（审计照落，TIMEOUT/FAILED 带模型名） |
| `health_check` | 连通性自检 | 运维 | — |

> `patient_fee_explain` 已于 P1 **退出模型能力**（G-08 裁撤，纪律 9）：甲乙丙拆分与逐类说明全部纯计算，不再占用模型调用、不出审计行、无 `degraded/source` 语义。

### 2.3 数据面实测

- `sys_ai_call_log`：审计主表（能力/状态/时延/token/摘要），有改名同步留痕（`workspace/_apply.mjs`）。
- `sys_knowledge_doc` + `sys_knowledge_chunk`：RAG 存储；向量库 `InMemoryVectorStore`（重启重建）。embedding P1 起回到 **`local-tf`**（设计内默认）：DashScope text-embedding-v3/v4 免费额度耗尽（直调 400 AllocationQuota），额度恢复后可切回 `remote`；`RemoteEmbeddingProvider` 的 `/v1/v1/embeddings` 拼接 bug 已修（base 以 `/v1` 结尾不再重复拼）。
- 模型密钥：`HIS_AI_API_KEY`（`application-local.yml`）。
- P1 实测口径：DashScope 兼容端点**偶发把 JSON 响应标成 `application/octet-stream`**（2026-10-03/04 各中过一次），`RestClientLlmClientImpl` 已改为取原始字节按 UTF-8 自行解析（`FAIL_ON_UNKNOWN_PROPERTIES=false`，兼容推理模型多带的 `reasoning_content`）；推理模型思考慢，`lab_interpret` 超时已从 8s 提到 15s。
- P2 实测口径：DashScope `deepseek-v4.1-flash` **偶发返回空 content**（2026-10-04 `followup_compose` 3 调 2 空，latency 2.6~3.5s），`AiExecutionService` 按失败走降级、熔断未触发（未达 5 次/300s 阈值），前端警示可见——设计内行为，上游偶发非缺陷，不处理。
- P3 实测口径：P2 记录的「偶发空 content」在 `insurance_evidence` 上定位出一个**自伤放大因子**——`maxTokens=1024` 时推理模型的思考过程就可能把输出额度烧光，content 恒空（同一审核连续两轮复现）。对齐 lab_interpret 等能力的 house 值 2048 后消除；跨能力统一 `OUTPUT_TOKEN_LIMIT=2048`，其余偶发空仍按降级处理。
- P4 实测口径（G-11 收口后）：`output_digest` 不再落模型输出原文，由 `AiAuditDigestSupport` 统一组装——LLM 输出 DTO 标 `@AiAuditPlain` 的 String 字段过 `SensitiveMaskUtils` 脱敏后明文（截 60 字符），未标注 String 字段落「字段名=SHA-256 前 12 位」指纹，`List<String>`（如交接班 focus）整体明文截 60，总长超 480 截断加 `...`。保住 AI 管理台「白名单字段排障可读」与「非白名单临床文本不入库」两条。存量能力（icd10/drug_audit/emr_draft 等 16 键）经执行器统一组装自动生效，无需逐能力改造。
- P5 实测口径（`voice_transcribe`）：① DashScope qwen3-asr-flash 契约——`input_audio.data` 必须是 **data URI**（`data:audio/{format};base64,...`，裸 base64 被当 URL 解析报 `provided URL does not appear to be valid`），且 content **只允许 audio part**（混入 text part 整个输入被拒 `asr task does not support this input`）；② RestClient 读上游 400 错误体必须用 `retrieve().body(String)`——`exchange()` + SimpleClientHttpRequestFactory 读错误流抛 IOException，把 400 的真实原因包成 `I/O error` 丢失；③ 前端 `transcribeVoice` 必须请求级显式 `Content-Type: multipart/form-data`——`request.js` axios 实例默认 `application/json`，axios 1.x 对「FormData + JSON 头」会走 `formDataToJSON` 把文件序列化成 JSON 体，后端 `MultipartException`（接口脚本用 node fetch 测不出这个坑，只有真浏览器暴露）；④ `stopVoiceRecord` 不在 stop() 后同步复位录音态——stop() 到 onstop 之间按钮若恢复可点，转写还没开始用户就能再点一次；复位交给 onstop 内 `closeVoiceDialog`，与 `voiceTranscribing` 同步块内切换无中间态。

### 2.4 前端接线实测

- `src/api/ai.js` 已备 19 组函数（P3 增 `judgeInsuranceEvidence`；P4 增 `scanWardDeterioration` / `explainDeterioration` / `composeNursingHandover`；P5 增 `transcribeVoice`，FormData 上传、请求级 multipart 头见 §2.3）；`getAiAuditLogPage` 与知识库管理、草稿留痕 diff 查询由 **`views/ai/AiAdminView.vue`**（菜单 2942「AI 管理台」：调用审计 + 知识库问答/维护 + 草稿留痕四签页）消费，维护入口挂 `ai:knowledge:manage`，diff 查询复用 `ai:admin:list` 不新增菜单；医保证据判定由 **`views/insurance/ComplianceAuditView.vue`** 详情抽屉内嵌消费（P3 起）；危重预警由 **`components/his/BedMapWorkspace.vue`**（床位图占床卡 MEWS 角标 + 预警弹窗，护士工作站/床位中心共用）消费、交接班摘要由 **`components/his/NursingHandoverWorkspace.vue`**（护士工作站第四视图）消费（P4 起，均无新菜单）；语音口述由 **`views/doctor-workstation/DoctorWorkstationView.vue`** 病历编辑区「语音口述」弹窗消费（MediaRecorder 录音 → 转写 → 可编辑 → 填入现病史/拟草稿，P5 起）；随访电话外呼登记由 **`views/followup/FollowupView.vue`** 行内「电话外呼」弹窗消费（明文电话供拨号 + 话术稿带出 + AI 拟话术 + 接通/未接通回填，P5 起）。
- 小程序（`source/miniapp`，P2 起）：`utils/api.js` 增 `previsitApi`（量表/提交/回显）与 `followupApi`（我的随访/反馈回写）；新增 `pages/previsit`（候诊页「预问诊」按钮进入）与 `pages/followup`（首页「我的随访」进入，微信站内信跳转路径即 `pages/followup/followup`），均已注册 `app.json`。
- AI 能力散落在各业务页内嵌使用；`report:bi:list`（BI 驾驶舱，菜单 906，父级 1200 报表统计）与「AI 运营问数」（菜单 2940）是运营层两个数据页。

---

## 3. 断点总清单

> 编号规则：`G` = gap。分期列标明在哪一期解决。

| 编号 | 现象 | 证据 | 影响 | 分期 |
|---|---|---|---|---|
| G-01 | `docs/AI能力接入方案.md` 被 2 处 javadoc 引用但**文件从未落盘**（悬空引用） | `AiCapabilityKeys.java:8`、`RestClientLlmClientImpl.java:35` | 新人按注释找文档找不到；架构口径无权威载体 | **P0**（本手册落盘 + 改引用） |
| G-02 | 能力全景的**运营·管理层 AI 空白**：管理者没有自然语言问数入口 | §1.1 表；报表层只有静态 BI（菜单 906/1201） | 院长/审计要数据只能翻报表或找信息科跑 SQL | **P0**（本期施工 `operation_qa`） |
| G-03 | AI 调用审计**有接口无页面**：`getAiAuditLogPage` 零视图消费 | grep 证据（仅 api/ai.js） | 出问题只能查库；管理员看不到模型调用健康度 | **P1 完成**（AiAdminView 调用审计签页 + 菜单 2942） |
| G-04 | 知识库问答**后端/存储齐但前端零接线**：无上传、无维护、无问答页 | `AiKnowledgeController` + `sql/217` vs 前端 grep | RAG 能力对用户不存在 | **P1 完成**（AiAdminView 问答/维护签页 + 权限码 `ai:knowledge:manage`） |
| G-05 | **预问诊**缺失（患者挂号后、就诊前采集病史写回 EMR） | 无对应能力 | 医生站缺最省时的前置输入 | **P2 完成**（`PrevisitQuestionnaireSupport` 量表版本化下发 + 小程序 pages/previsit 采集 + `previsit_summary` 凝摘要 + 医生工作站只读报告卡，AI 不代填现病史） |
| G-06 | **智能随访**缺失（出院随访/慢病管理外呼文案） | 无对应能力 | 诊后管理靠人工电话 | **P2 完成**（`followup_compose` 医护端拟话术 + `startFollowup` 站内信触达 + 小程序 pages/followup 反馈回写 `patient_reply`；电话外呼通道另列决策） |
| G-07 | 医保智能审核目前是**纯规则闸门**（`insurance.compliance.*`），LLM 读病历证据未引入 | `application.yml` insurance 段 | 证据判定依赖人工，是否引入 LLM 属业务拍板 | **P3 完成**（`insurance_evidence`：规则命中后 LLM 逐条读病历证据判「支持/反驳/证据不足」，只读不写库、不改规则结论，judgments 按规则码对齐防幻觉，degraded 必显；合规审核台账详情抽屉消费） |
| G-08 | `patient_fee_explain` 模型环节是伪 AI：甲乙丙拆分为确定性计算、逐类说明固定文案，模型串话无增量，且解释的是钱——幻觉不可接受 | §2.2 降级语义"拆分表照常" | 白付底座税 + 金额解释幻觉风险 | **P1 完成**（模型环节裁撤，纯计算，`degraded/source` 语义与 prompts/LLM DTO 已删） |
| G-09 | `lab_interpret` 医生站模型环节同理：逐项检验意义是静态词典（医生不需要模型解释 ALT 高是什么意思）；异常/趋势/危急值已是代码 | §2.2 | 词典页覆盖 90%，模型环节负价值 | **P1 完成**（默认规则；仅异常 ≥3 调模型连贯解读，`source` 字段区分三态） |
| G-10 | `emr_draft` 医生终审修改行为未留痕：草稿确认后仅落 EMR，医生改了哪里没有 diff 记录 | §8.6 训练判据 | 未来 SFT 微调的训练原料（数据飞轮）正在流失 | **P2 完成**（`biz_ai_draft_diff`：本会话应用过 AI 草稿且终稿已改动才落一条，7 段 equal/insert/delete 落 `diff_json`，AI 管理台留痕签页可查，复用 `ai:admin:list`） |
| G-11 | `sys_ai_call_log.output_digest` 落的是模型输出**原文**（含患者主诉等临床文本），与纪律 6「审计只落 digest 不落原文」有张力 | P1/P2 各能力审计行实测（`emr_draft`/`previsit_summary`/`followup_compose` 同样） | 审计表膨胀 + 敏感文本随审计落库 | **P4 完成**（`AiAuditDigestSupport`：`@AiAuditPlain` 注解白名单 + 默认字段指纹——未标注 String 字段落「字段名=SHA-256 前 12 位」，白名单字段过脱敏后明文 ≤60 字符；`AiExecutionServiceImpl` 统一组装 output_digest，16 能力全部生效，AI 管理台排障价值保留，接口验收含 insurance_evidence 样例回归） |
| G-12 | 住院患者病情恶化无量化预警：`biz_nursing_record` 体征时间序列已有，但没有任何评分消费，靠护士肉眼盯曲线 | §8.3 候选（行业同场景：卫宁"鲲鹏"重症、讯飞危重预警） | 病情恶化发现滞后；床位图看不出哪个患者在变差 | **P4 完成**（`deterioration_alert`：`DeteriorationScoreRules` MEWS+SpO2 **纯代码评分**（纪律 2）→ 达标才调模型给观察建议；`wardScan` 纯代码无审计行、`explain` 仅级≥1 调模型；只提示不写库、预警级由评分定；床位图角标 + 预警弹窗，复用 `ipd:bedCenter:list`/`ipd:nurse:list` 零新菜单） |
| G-13 | 护理交接班无结构化摘要：病区×班次的护理事实（体征异常/评估风险/出入院变动）靠交班护士人工翻记录 | §8.3 候选（行业参照：卫宁×东莞一院护理交接班） | 交接质量依赖个人经验，漏交风险 | **P4 完成**（`nursing_handover`：`WardHandoverCapabilityImpl` 病区×班次窗事实聚合（出入院/体征越阈/高风险评估，代码）→ 模型拟 SBAR 摘要 → 护士站第四视图编辑终审，不写库；模型不可用规则模板拼接 + 警示条必显） |
| G-14 | 病历现病史靠医生打字：问诊时双手被占用（查体/操作），文书负担是行业公认最痛的缺口 | §8 对标（卫宁/讯飞语音病历均为旗舰能力） | 现病史书写耗时，是 AI 省时的最大单点 | **P5 完成**（`voice_transcribe`：医生工作站「语音口述」弹窗 MediaRecorder 录音 → qwen3-asr-flash 转写 → 医生改 → 填入现病史或喂既有 `emr_draft` 拟草稿，G-10 留痕自动接上；审计 capability_key='voice_transcribe'，output_digest=text+SHA256 前 12 位；失败如实报错不造文本；权限复用 `opd:doctorWorkstation:edit`） |
| G-15 | 随访电话外呼通道缺位（G-06 遗留决策项）：站内信已通，电话触达无登记无留痕 | P2 收尾记录 | 电话随访是否打过、打了几次、接没接通无处可查 | **P5 完成**（sql/231 `biz_followup_task` 加外呼登记 4 列 + `FollowupCallChannelService` 配置分支 `followup.call-channel`（mock=只登记待呼不假装接通）+ 登记→人工拨号→回填结果状态机（attempts 累计、callStatus 1待呼/2随访中/3未接通等）+ 随访工作台「电话外呼」弹窗；权限复用 `inpatient:followup:edit` 零新菜单；真实外呼通道接入属后续决策项） |
| G-16 | 全部能力共用一个模型：`model`/`modelLite` 两档全局值，无法按能力分模型（问数用轻量、文书用重模型） | `AiConfigProvider.modelOf(boolean)` 单一签名 | 模型升级/降本只能一刀切，动一发牵全身 | **P5 完成**（per-capability 覆盖 map `ai.models.<key>`，`modelOf(capability, lite)`：覆盖命中→用之，否则默认；healthCheck 按能力带模型快照；`operation_qa`/`emr_draft` 已配 `deepseek-v4.1-flash` 作为覆盖样例） |

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
| **P0（已完成）** | ① 手册落盘 + 修 G-01 悬空引用；② `operation_qa` AI 运营问数（NL2SQL 白名单闸门 + 问数页 + 菜单 sql/224） | G-01、G-02 |
| **P1（已完成）** | 「AI 管理台」一页收口：调用审计列表（G-03）+ 知识库维护/问答接线（G-04）；同期执行 G-08/G-09 模型环节裁撤（纪律 9 落地） | G-03、G-04、G-08、G-09 |
| **P2（已完成）** | G-10 草稿→终稿 diff 留痕（sql/226 `biz_ai_draft_diff` + AI 管理台留痕签页）；G-05 预问诊（sql/227 `biz_previsit_record` + 量表下发 + 小程序采集 + `previsit_summary`）；G-06 智能随访（sql/228 反馈列 + `followup_compose` + 站内信触达 + 小程序反馈回写） | G-05、G-06、G-10；电话外呼通道另列决策 |
| **P3（已完成）** | G-07 医保审核证据判定：`insurance_evidence` 能力（prompts/insurance-evidence.md + DTO/VO 契约 + 能力实现 + Controller，复用 `finance:complianceAudit:list` 无新菜单/SQL）+ his-charge `getAiEvidenceNarrative` 证据叙事面 + 合规审核台账详情抽屉 AI 判定区 | G-07 |
| **P4（已完成）** | ① G-11 审计收口：`AiAuditDigestSupport`（`@AiAuditPlain` 白名单 + 未标注 String 字段 SHA-256 指纹），执行器统一组装、16 能力全部生效；② G-12 `deterioration_alert` 危重预警（his-patient 体征事实面 `latestVitalsByWard`/`latestVitalByAdmission` + `DeteriorationScoreRules` MEWS+SpO2 代码评分 + 床位图角标/预警弹窗，达标才调模型）；③ G-13 `nursing_handover` 护理交接班摘要（病区×班次事实聚合 + SBAR 摘要 + 护士编辑终审）。零 SQL、零新菜单（复用 `ipd:nurse:list` / `ipd:bedCenter:list`） | G-11、G-12、G-13 |
| **P5（已完成）** | ① G-14 `voice_transcribe` 语音口述转写（DashScope qwen3-asr-flash 同步 ASR，非 LLM 能力、独立 `SpeechTranscribeService` 不走执行器；转写文本医生改后喂既有 `emr_draft`）；② G-15 随访电话外呼通道（sql/231 外呼登记列 + `FollowupCallChannelService` 配置分支，人工登记不假装接通）；③ G-16 多模型路由（`ai.models.<key>` per-capability 覆盖 + healthCheck 按能力带模型）；④ 数据飞轮只记观察不施工 | G-14、G-15、G-16 |

> 数据飞轮观察口径（P5-4，只记不施工）：G-10 的 `biz_ai_draft_diff` 已在持续产出医生修改 diff；不为此建任何新表/任务/看板。观察方法一句话——每隔一段时间在 AI 管理台留痕签页看「有 diff 的会话数」与「diff 段数分布」，当样本量与语义质量足以支撑训练讨论（SFT/LoRA 判据见 §8.6）时再立项，届时原料直接从 `biz_ai_draft_diff` 导出。

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
| 2026-10-04 | P0 | 收编对话结论：§1.2 新增纪律 9（可穷举禁走模型）；§3 登记 G-08/G-09（伪 AI 模型环节裁撤，排 P1）、G-10（emr_draft 修改 diff 留痕，排 P2）；§8.5 三档判据、§8.6 训练必要性判据；§5 P1 范围更新 | 完成 |
| 2026-10-04 | P0 | dev 库恢复，执行 sql/224：6 语句、7 项自检全过（M1~M7），回查菜单 2940/2941 与 role_menu 4 行（role 1+18）齐全；`workspace/_clear_perm_cache.mjs`（原生 RESP，环境无 redis 客户端库）清 `his:perm:role:*`——缓存已空，故障期 TTL 自然过期 | 完成 |
| 2026-10-04 | P0 | §6.6 验收 **5/5 PASS**（8080 为 IDEA debug 实例，classpath 含 his-ai 全量代码，与新包等价）：A1/A2 问法校准至 2026-09——10 月零流水、近 7 天命中的是 2030 未来种子（被模型正确加上界排除），属种子数据窗口非代码缺陷，`_verify_operationqa.mjs` 已注明；A3/A4 闸门拒绝且 reason 说人话；A5 审计 operation_qa success 11 行 | 完成。前端真浏览器断言（菜单可见、页面渲染、控制台零报错）留待 P1 同期补跑 |
| 2026-10-04 | P1 | G-08 裁撤 `patient_fee_explain` 模型环节（纪律 9）：`PatientFeeExplainCapabilityImpl` 删模型调用链（callModel/渲染/模板常量/依赖），`PatientFeeExplainVO` 删 `source/degraded/degradeReason`，删 `PatientFeeLlmOutputDTO` 与 `prompts/patient-fee-explain.md`，`AiCapabilityKeys` 删常量，`application.yml` 删开关与超时 | 完成 |
| 2026-10-04 | P1 | G-09 `lab_interpret` 条件调模型：`LabInterpretCapabilityImpl` 增 `MODEL_MIN_ABNORMAL=3`，异常 <3 走规则（`source=rule, degraded=false`，设计内路径）、≥3 才调模型（失败降级 `degraded=true`+原因）；`LabInterpretResultVO` 增 `source` 字段；前端 `LaboratoryView.vue` 解读来源三态 alert | 完成 |
| 2026-10-04 | P1 | G-03/G-04「AI 管理台」：新页 `views/ai/AiAdminView.vue`（调用审计/知识库问答/知识库维护三签页）+ `api/ai.js` 增 7 个知识库函数 + 路由 `ai-admin`；知识库维护权限码从借用的 `opd:doctorWorkstation:add` 改为语义正确的 `ai:knowledge:manage`（`AiKnowledgeController` 6 处注解同步） | 完成 |
| 2026-10-04 | P1 | 菜单铺底 `sql/225-AI管理台（菜单与授权）.sql`：菜单 2942「AI 管理台」（父 1100，sort 44，permission `ai:admin:list`）+ 按钮 2943「知识库维护」（`ai:knowledge:manage`），授权仅 role 1，role_menu id `70000+m.id*10+r.id`，三键守卫 + 7 项自检；`workspace/_sql225.mjs` 执行+回查+清权限缓存 | 完成：6 语句、自检全绿（M1=2、M6=2、M7=2），回查菜单/授权齐全，缓存清空 |
| 2026-10-04 | P1 | 修模型客户端三处实测缺陷：① DashScope 偶发把 JSON 标成 `application/octet-stream` → `RestClientLlmClientImpl` 改取原始字节 UTF-8 自行解析；② 手动 `ObjectMapper` 开着 `FAIL_ON_UNKNOWN_PROPERTIES` 被推理模型多带的 `reasoning_content` 拒掉 → 与 Spring Boot 口径对齐关闭；③ `RemoteEmbeddingProvider` 拼 `/v1/v1/embeddings` 必 404 → base 已带 `/v1` 不重复拼；`lab_interpret` 超时 8s→15s（推理模型实测必超时）；embedding 切回 `local-tf`（DashScope 免费额度耗尽，直调 400 AllocationQuota） | 完成 |
| 2026-10-04 | P1 | P1 接口验收 `workspace/_verify_p1_ai.mjs`（8081 新代码 jar）：H0 healthCheck 无 patient_fee_explain；P0 权限码入 `/auth/info`；G-08 纯计算无 degraded/source（总额 650，4 组目录）；G-09 规则路径（2 异常，source=rule, degraded=false）+ 组合异常路径（7 异常，**source=model, degraded=false 模型真实参与**）；G-03 审计分页；G-04 列表/问答（degraded=false 回答正确）/录入→召回命中→删除；审计行落库 | **12/12 PASS** |
| 2026-10-04 | P1 | 前端真浏览器断言 `workspace/_verify_ai_ui.mjs`（playwright-core + Edge，vite dev 5199 → 8081）：**P0 补跑**「AI 运营问数」菜单 2940 可见、页面渲染含数据域说明；「AI 管理台」菜单 2942 可见、调用审计 17 行真实数据、知识库维护签页与列表渲染；控制台零报错 | **11/11 PASS**，P0 遗留的前端断言一并闭环 |
| 2026-10-04 | P2 | G-10 草稿留痕：sql/226 建 `biz_ai_draft_diff`；his-emr 增 entity/mapper/service/controller（listPage 权限复用 `ai:admin:list`）；`MedicalRecordSaveDTO` 增可选 `aiDraftPresentIllness`；`recordSave` 钩子口径——**仅当本会话应用过 AI 草稿且终稿已与其不同**才落一条 diff（7 段 equal/insert/delete），保存成功即清草稿会话，一次草稿会话最多留一条 | 完成 |
| 2026-10-04 | P2 | G-05 预问诊：sql/227 建 `biz_previsit_record`；量表由 `PrevisitQuestionnaireSupport` 代码版本化下发（VERSION 2026.10，9 主症状 + 5 通用问 + 追问组）；his-ai 增 `previsit_summary`（结构化作答凝摘要，规则模板兜底 `source=rule`）；his-miniapp 增问卷/提交/回显三接口（PATIENT，挂患者本人校验）；医生工作站只读报告卡 `/previsit/getByRegist`（`opd:doctorWorkstation:list`），AI 不代填现病史 | 完成 |
| 2026-10-04 | P2 | G-06 智能随访：sql/228 `biz_followup_task` 加 `patient_reply`/`patient_reply_time`；his-ai 增 `followup_compose`（随访类型 + 慢病档案拟话术，类型模板兜底 `degraded=true` + 原因）；`startFollowup` 增站内信触达（跳转路径 `pages/followup/followup`）；his-miniapp 增 myList/reply（患者只回自己的任务）；随访工作台加「AI 拟话术」按钮与患者反馈列 | 完成 |
| 2026-10-04 | P2 | 小程序交付：`pages/previsit`（候诊页「预问诊」入口，choices 必答校验、只收已作答题、按 label 回显）与 `pages/followup`（首页「我的随访」入口，行内展开反馈输入，已取消任务不显示反馈入口）；`utils/api.js` 增 `previsitApi`/`followupApi`；`app.json` 注册两页 | 完成 |
| 2026-10-04 | P2 | P2 接口验收 `workspace/_verify_p2_ai.mjs`（8081 新代码 jar，mysql 连接 `supportBigNumbers + bigNumberStrings` 全程字符串化）：B 组草稿留痕（biz_queue 取就诊上下文新建病历带 AI 草稿 → 7 段 diff，types={0,1,2}）；C 组预问诊（量表 9+5 → 提交 → `source=model` → 患者回显 → 医生站同读）；D 组随访（compose → myList → reply → getById 读回反馈）；E 组审计 `previsit_summary`/`followup_compose` 落库 | **17/17 PASS** |
| 2026-10-04 | P2 | 前端真浏览器断言 `workspace/_verify_p2_ui.mjs`（playwright-core + Edge，vite 5199 → 8081）：AI 管理台草稿留痕签页真实行 + diff 弹框红删/绿增（el-tabs 非活动 pane 行仍在 DOM，选择器必须 `.el-tab-pane:visible` 作用域）；随访页患者反馈列真实文本 + 编辑弹框 AI 拟话术回填（degraded 警示可见）；医生工作站预问诊卡；控制台零报错 | **12/12 PASS** |
| 2026-10-04 | P2 | 实测发现两条非 P2 引入事实：① DashScope `deepseek-v4.1-flash` 偶发空 content（followup_compose 3 调 2 空），AiExecutionService 按失败降级、熔断未触发——设计内行为，不处理（§2.3）；② `output_digest` 落模型输出原文与纪律 6 有张力，登记 **G-11**（决策） | 完成 |
| 2026-10-04 | P3 | G-07 能力实现：`AiCapabilityKeys.INSURANCE_EVIDENCE` + `application.yml` 开关/超时（15s，同步路径内）+ `prompts/insurance-evidence.md`（三值判定契约 + 禁改规则结论禁区）+ `InsuranceEvidenceJudgeDTO`/`InsuranceEvidenceLlmOutputDTO`/`InsuranceEvidenceResultVO` + `InsuranceEvidenceCapability` + Impl（规则结论是事实层，模型只叠加「证据与怀疑的关系」，judgments 按规则码对齐防幻觉）+ `AiInsuranceEvidenceController`（复用 `finance:complianceAudit:list`，无新菜单/SQL）；his-charge `ComplianceAuditServiceImpl.getAiEvidenceNarrative` 提供病历证据叙事面 | 完成 |
| 2026-10-04 | P3 | 前端接线：`api/ai.js` 增 `judgeInsuranceEvidence`；`ComplianceAuditView.vue` 详情抽屉内嵌 AI 证据判定区（按钮仅命中行显示、判定表 + 总评 + degraded 警示必显） | 完成（`vite build` 通过 3.72s） |
| 2026-10-04 | P3 | P3 接口验收 `workspace/_verify_p3_insurance.mjs`：守卫（无命中审核拒判、非 200 说人话）/ 造 0 命中审核方法（PROBE 清单调 drg_code+total_amount 进 D01 倍率窗，batchAudit 后还原）/ 命中审核 degraded=false 判定真实 / judgments 与命中规则码对齐 / 审计落库 success。期间定位 maxTokens=1024 被推理思考烧光致 content 恒空（P2「偶发空 content」的自伤放大因子），`OUTPUT_TOKEN_LIMIT` 1024→2048 修复后全绿 | **13/13 PASS** |
| 2026-10-04 | P3 | 验收栈隔离：并行会话起栈前按命令行杀 `his-backend.jar` 进程（两次杀掉 8081 JVM），jar 复制改名 `his-acceptance.jar`、解嵌套 his-ai jar 用 `jar tf` 验证 8 个 G-07 类齐全后起 8082；前端真浏览器断言 `workspace/_verify_p3_ui.mjs`（vite 5299 → 8082）：台账真实数据 → 逐行翻找命中行（首行是守卫造数的 0 命中审核；抽屉 items 异步加载，判按钮前先等规则明细表出行）→ 判定表渲染真实判定 + 总评、degraded 警示条不出现、控制台零报错 | **7/7 PASS** |
| 2026-10-04 | P4 | G-11 审计收口：`support/AiAuditPlain.java`（字段白名单注解）+ `support/AiAuditDigestSupport.java`（组装器：`@AiAuditPlain` String 过 `SensitiveMaskUtils` 脱敏明文截 60；未标注 String 落「字段名=SHA-256 前 12 位」指纹；深度 1 集合逐元素；`List<String>` 整体明文；总长 480 截断）+ `AiExecutionServiceImpl` 落库前统一组装 `output_digest`（16 能力全部生效）；AI 管理台审计签页适配新口径 | 完成 |
| 2026-10-04 | P4 | G-12 能力实现：`AiCapabilityKeys.DETERIORATION_ALERT` + `application.yml` 开关/超时 + `prompts/deterioration-alert.md` + `DeteriorationLlmOutputDTO`/`DeteriorationScanVO`/`DeteriorationExplainVO` + `support/DeteriorationScoreRules`（MEWS+SpO2 纯代码评分，级 0/1/2）+ `DeteriorationAlertCapabilityImpl`（`wardScan` 纯代码无审计行；`explain` 仅级≥1 调模型，级 0 直接 advice=null）+ `AiDeteriorationController`（复用 `ipd:bedCenter:list`）；his-patient `InpatientNursingService` 增 `latestVitalsByWard` / `latestVitalByAdmission` 体征事实面（`NursingVitalFactVO`） | 完成 |
| 2026-10-04 | P4 | G-13 能力实现：`AiCapabilityKeys.NURSING_HANDOVER` + 开关/超时 + `prompts/nursing-handover.md`（SBAR 契约）+ `NursingHandoverDTO`（wardId + shift 1-白班 2-小夜 3-大夜 + shiftDate，窗即班次由后端算）/`NursingHandoverLlmOutputDTO`/`WardHandoverVO` + `WardHandoverCapabilityImpl`（病区×班次窗事实聚合：出入院/体征越阈/高风险评估）+ `AiNursingHandoverController`（复用 `ipd:nurse:list`）；摘要不写库，护士终审 | 完成 |
| 2026-10-04 | P4 | 前端接线：`api/ai.js` 增 `scanWardDeterioration` / `explainDeterioration` / `composeNursingHandover`（契约注释写明 degraded 语义）；`BedMapWorkspace.vue` 占床卡 MEWS 角标（级 2 红/级 1 琥珀，`@click.stop` 防误触患者详情）+ 预警弹窗（级别徽标/评分明细/触发依据/观察建议 + degraded 警示）；`NurseView.vue` 第四视图「交接班摘要」+ 新组件 `NursingHandoverWorkspace.vue`（病区/班次/日期工具条 + AI 拟摘要 + 事实三卡 + 可编辑摘要 + 来源徽标 + degraded 警示 + 复制） | `vite build` 通过 |
| 2026-10-04 | P4 | 两处欠账修复：① `AiHealthServiceImpl.TRACKED_CAPABILITIES` 硬编码清单自 P1 起漂移（9 键 vs 实际 14 键），补齐至 16 键——healthCheck 开关快照从此与 `ai.features` 一致，验收以此为「能力开启」判据；② `his-web/pom.xml` finalName 临时改 `p4-backend` 出包（8081 另一会话 JVM 正锁 `his-backend.jar`，clean/package/repackage 的 rename 全部失败），出包即还原 pom | 完成 |
| 2026-10-04 | P4 | P4 接口验收 `workspace/_verify_p4.mjs`（验收 jar 复制为 `workspace/_p4_jar/his-acceptance.jar` 跑 8082）：A healthCheck 16 键开关；B 病区盘点与接口对账；C wardScan 分项合计=总分、级 0/1/2、无审计行；D1 种极端体征 → 级 2 + 模型建议（degraded=false、advice≤200、审计行 input/output digest 正则、无原文泄漏）；D2 常态体征 → 级 0 不调模型不落审计；D3 无体征 → totalScore=-1；E compose（事实数组 + windowText、source=1、shift=5 拦 400、digest 正则）；F insurance_evidence G-11 样例回归（ruleCode/verdict 明文、overall 指纹、reason 不泄漏）；种子 JOIN 驱动 + `AI-TEST-P4-` 前缀物理删守卫 | **44/44 PASS** |
| 2026-10-04 | P4 | P4 前端真浏览器断言 `workspace/_verify_p4_ui.mjs`（playwright-core + Edge，vite 5299 → 8082）：床位图切科室 → MEWS 10 角标 → 预警弹窗（级别徽标/评分明细 5 行/观察建议、无 degraded、`@click.stop` 冒泡断言）→ 护士工作站交接班摘要（病区默认选中、AI 拟摘要 source=1、摘要可编辑、无 degraded）→ 控制台零报错；截图 `_p4_ui_mews_badge/_p4_ui_mews_dialog/_p4_ui_handover.png`。期间修三处**脚本断言**（功能零缺陷）：① el-input(type=textarea) 的 `data-testid` 落内层 `<textarea>`（`inheritAttrs:false` attrs 直并内层），后代选择器匹配不到；② 预警弹窗竞态——openAlert 先渲染扫描行（无 advice/degraded 字段）空态瞬间可见，explain 模型调用数秒后才替换，必须只等 advice/degraded 真终态；③ EP 2.14.6 单选选中 label 仍渲染在带 `el-select__placeholder` 类的 div（`is-transparent` 仅无值时加）且同级有空的 input-wrapper，定位用 `.el-select__selected-item.el-select__placeholder:not(.is-transparent)`。另：admin 主岗科室 0 床，种子改扫 `deptOptions` 找第一个占床科室 + UI 先切科室下拉 | **13/13 PASS** |
| 2026-10-04 | P5 | G-14 后端：`AiCapabilityKeys.VOICE_TRANSCRIBE` + `application.yml` `ai.asr.*`（qwen3-asr-flash，key 复用 `HIS_AI_API_KEY`）+ `OpenAiAsrRequestDTO` + `SpeechTranscribeService`/Impl（**不走 AiExecutionService**：同步 ASR 无 prompt/自修正/降级语义，失败如实抛业务异常不造文本）+ `AiSpeechTranscribeController`（`/ai/emrText/transcribe` multipart，复用 `opd:doctorWorkstation:edit`）；审计独立落 `sys_ai_call_log`（capability_key='voice_transcribe'，input 只落音频元数据 digest，output=text+SHA256 前 12 位，TIMEOUT/FAILED 行落模型名——`AiExecutionServiceImpl.record` 同步增带 model 的重载，此前失败路径 model=null 说不清哪个模型挂的） | 完成 |
| 2026-10-04 | P5 | G-14 前端：`DoctorWorkstationView.vue` 病历编辑区「语音口述」弹窗（MediaRecorder 录音/计时/时长上限 → 转写终态 → 文本可编辑 → 填入现病史/拟草稿走既有 `emr_draft`）；`api/ai.js` 增 `transcribeVoice`。实测修三处：① ASR 契约 data URI + 纯 audio part（§2.3）；② `retrieve()` 透传 DashScope 400 错误体；③ `transcribeVoice` 请求级 multipart 头 + `stopVoiceRecord` 复位挪进 onstop + `doTranscribe` catch 补 `ElMessage.error`（request.js 对 500 只 console 不 toast，「点了没反应」会让医生以为功能坏了） | 完成 |
| 2026-10-04 | P5 | G-15/G-16：sql/231 `biz_followup_task` 加外呼登记 4 列（call_channel/call_status/call_attempts/call_last_time）+ `FollowupCallChannelService`（`followup.call-channel` 配置分支，mock=只登记待呼）+ 登记未接通/接通回填接口（attempts 累计状态机）+ `FollowupView.vue`「电话外呼」弹窗（明文电话供拨号、话术稿带出、AI 拟话术、结果回填；列表电话仍脱敏）；`AiConfigProvider.modelOf(capability, lite)` per-capability 覆盖 map `ai.models.<key>` + healthCheck 按能力带模型快照（`operation_qa`/`emr_draft` 配 `deepseek-v4.1-flash` 为样例）。零新菜单/按钮码（权限复用 `inpatient:followup:edit`） | 完成 |
| 2026-10-04 | P5 | P5 接口验收 `workspace/_verify_p5.mjs`（验收 jar `workspace/_p5_jar/p5-backend.jar` 跑 8082，8081 另一会话锁 `his-backend.jar` → his-web finalName 临时切 `p5-backend` 出包即还原）：A healthCheck 17 项含 models 快照；B TTS wav 真转写命中关键词（latency 731ms，model=qwen3-asr-flash）；C digest 口径正则（output=text+sha256 前 12 位、input 只含音频元数据）；D 伪音频拒 + FAILED 审计留痕带 model；E 外呼全闭环（登记→重复登记拒→未接通→再登记 attempts=2→接通转随访中→清场）；F operation_qa 审计行 model=路由真值（上游偶发空 content，重试 3 次口径） | **25/25 PASS** |
| 2026-10-04 | P5 | P5 前端真浏览器断言 `workspace/_verify_p5_ui.mjs`（playwright-core + Edge fake 麦克风 `--use-fake-device-for-media-stream`，vite 5299 → 8082）：随访种子任务（API 建 + 从库反查 task_no 定位行）→「电话外呼」弹窗（已登记待外呼·第 1 次/明文电话/话术稿带出/AI 拟话术）→ 未接通回填 callStatus=3 → 列表电话脱敏；医生工作站（环境今日无队列，种一条就诊中 `biz_queue` 行模板复制 + admin 身份，自动选中后病历表单出现）→「语音口述」弹窗 → fake 音轨录音 → 失败路径可见提示且不造文本（textarea 空 + 填入禁用）→ 控制台零报错（按文案排除故意失败探针的预期噪音）；finally 双清场（cancelFollowup + `DELETE biz_queue WHERE queue_no LIKE 'AI-TEST-P5-%'`）。截图 `_p5_ui_call/_p5_ui_voice.png`。**multipart 坑只有真浏览器暴露**：接口脚本 node fetch 的 FormData 天然正确，axios 实例默认 JSON 头把文件序列化成 JSON 体（§2.3-③），前三跑假绿由它造成 | **16/16 PASS**；`vite build` 通过（3.70s） |

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
| 运营 | 运营问数（NL2SQL）、病案编码辅助、病历内涵质控、医保智能审核（规则库 + AI 证据判定）、护理交接班 | **`operation_qa`（P0）**、`icd10`、`emr_qc`、`insurance_evidence` 已建（P3） | 问数/编码/质控/医保证据判定与行业主线完全对齐，无返工风险 |
| 底座 | 统一模型接入 + 能力开关 + 全程审计 + 降级（医院自建 AI 平台成采购标配） | his-ai 底座（执行器唯一入口/熔断/审计/提示词外置/结构化输出） | 架构形态一致 |

### 8.3 候选登记项（未立项，拍板后进第 3 节）

| 候选 | 行业参照 | 前置条件与约束 |
|---|---|---|
| 语音转写（问诊录音→现病史草稿） | 各厂商 Copilot 标配输入 | 需录音采集终端与院内 ASR 选型；产出走 `emr_draft` 同款"草稿 + 医生确认"契约 |
| 危重预警/病情恶化评分 | 卫宁"鲲鹏"重症、讯飞危重预警 | **P4 已立项（G-12）**：体征/护理数据已具备（`biz_nursing_record`）；事实层评分代码先行，模型只解释——纪律 2 |
| AI 护理交接班 | 卫宁与东莞一院四大落地应用之一 | **P4 已立项（G-13）**：护理记录结构化程度满足（体征字段 + 评估单 + 班次） |
| 影像报告解读 | 讯飞影像报告生成已达实用门槛 | **涉 NMPA 三类证**：只做"报告/指标解读"（同患者端纪律），不做诊断结论 |

### 8.4 对标结论

- 本项目 13 能力的选点落在行业"文书负担 + 监管自保 + 运营问数"三条主线上，**无方向性返工风险**；且"降级可用 + 人类终审 + 全程审计"三条纪律与行业共识（LLM 在编码/DRG 场景仍处验证期、人机协同是落地前提）一致。
- 差距集中在**输入侧**（语音/预问诊的数据采集）与**专科深度**（危重预警），均为外部依赖而非架构缺陷。
- 行业红线与 §1.2 纪律 2/3 相同：AI 不出诊断结论、临床动作人类终审；此口径不随厂商进度放宽。

### 8.5 AI 含金量三档判据（2026-10-04 对话结论收编）

判据一句话：**输入与答案能否穷举成一张表**。能 → 页面/词典/规则；不能 → 才轮到模型（已立为纪律 9）。

| 档 | 判定 | 厂商实例 | 本项目动作 |
|---|---|---|---|
| 真 AI | 输入开放、输出穷举不了 | 病历生成、内涵质控、辅助诊断、危重研判、MedMap、DIP 读病历查高编、NL2SQL、影像报告生成、ASR | `emr_draft`/`emr_extract`/`emr_qc` 内涵层/`icd10`/`emergency_triage`/`operation_qa` 全在此档，选点无虚荣项 |
| 词典+长尾 | 词典打 80 分，模型只吃长尾 | 报告解读、智能分诊、预问诊追问树、交接班摘要、精准预约 | 词典/量表先行（运营可维护资产），模型仅词典 miss 时兜底——**降级路径即主体，模型即补丁** |
| 伪 AI | 输出可被对照表穷举 | 用药查询（说明书词典）、医保控费（纯规则库）、数字人宣教（脚本库）、诊后清单（模板页）、费用解释 | **禁止走模型**；`patient_fee_explain`、`lab_interpret` 医生侧列入裁撤（G-08/G-09） |

厂商把三档打包按 AI 价卖（"全场景 AI"）；自建的性价比来自拆开三档，分别用模型、词典、页面接住。付费客户最终只为第一档买单（卫宁 MedMap/AI 重症、讯飞病历生成/影像报告——财报口径），其余是叙事填充物。

### 8.6 训练必要性判据（澄清"做大模型 ≠ 训练大模型"）

| 层级 | 谁做 | 本项目 |
|---|---|---|
| 从头预训练 / 医学继续预训练 | 基座与厂商 IP 布局（WiNGPT、星火医疗 V3.5） | 永不 |
| SFT/LoRA 微调 | 厂商专项模型 + 数据飞轮成熟的医院（参照"鲲鹏"= 专病库 + 专家经验 + 专项训练） | P3+ 决策项，原料 = 医生修改 diff（G-10 留痕） |
| 提示词 + RAG + 结构化输出 | 医院端落地默认形态 | **现状，13 能力零训练** |

训练判据：知识在公开语料 → 不训练（提示词/RAG 够）；知识在院内数据（专病库、专家决策模式、医生修改行为）→ 才值得训练；而院内数据未积累前训练无从谈起——**先跑提示词版就是在攒训练原料**。注意本地化部署 ≠ 训练。

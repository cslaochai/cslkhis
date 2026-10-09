# cslk_his 数据库 E-R 反向建模产物

对 dev 库（320 张基础表）**零物理外键**的现状做反向工程：从 `information_schema` 取表/列/索引与注释，
按 `*_id` 列名 + 列注释推导候选父表，再用真实数据 JOIN 覆盖率逐条裁决，产出 PowerDesigner 可导入的
物理模型脚本 + 关系证据表 + 分域 Mermaid 图。

生成链（事实源是 dev 库，不是 `docs/sql/`，因此注释与库里当前状态一致）：

| 步骤 | 脚本 | 产物 |
|---|---|---|
| 1 结构导出 | `workspace/_schema_dump.mjs` | `workspace/_db_schema.json`（320 张基础表 SHOW CREATE TABLE 全文，过滤视图） |
| 2 关系刷新与产物输出 | `workspace/_er/refresh.mjs` | 本目录全部文件（DDL / CSV / Mermaid / 查看器 DATA）：复用 `relationships.csv` 中 831 条已裁决关系，仅对新增表/新增列做真实数据验证后重发 |

验收：`refresh.mjs` 自带 17 项对账断言（CREATE/ALTER 条数、CSV 分段与序号、md 与 DATA 一致性、
mermaid 边两端实体在场、无 enum/json 残留等），全部 PASS 才算刷新完成。

## 交付物

```
docs/er/
├── index.html                   # ★ 连线图查看器（双击即开，离线可用）：分域图 / 全域关系总览 / 单表邻居图
├── viewer.css  viewer.js        # 查看器样式与交互（缩放、拖动、表名定位、只看有证据的关系）
├── vendor/mermaid.min.js        # 本地 mermaid 10（不走 CDN，断网也能画）
├── powerdesigner/
│   ├── cslk_his_full.sql        # 全库 320 表 + 876 条 ALTER TABLE ADD CONSTRAINT，单个模型装下全库
│   └── by-domain/01-*.sql … 23-*.sql   # 按 docs/sql/ 的 23 个领域拆分，每域一张图
├── relationships.csv            # 1037 条关系登记（876 进模型 + 33 多态 + 2 未落定 + 126 快照），逐条带父表、基数、验证层级、非空值数/命中数/覆盖率、并列候选
└── er-by-domain.md              # 23 张 Mermaid erDiagram，可在 Qoder/GitHub 直接渲染
```

## 先看图：`index.html`

双击 `docs/er/index.html` 即可（`file://` 直接开，不需要起服务）。三个页签：

- **分域连线图**：左栏 23 个领域（表数/线数），画布是带鸦爪的 ER 图。实体头写作 `中文名 · 表名`，
  属性行是 `类型 列名 "列中文注释"`；连线标签就是外键列名。
  勾「只画有数据证据的关系」会隐藏 C/D 级（876 → 717 条），看主干时很有用。
- **全域关系总览**：23 个域之间的引用条数（只显示 ≥3 条的边，节点里标了内部自环数），用来定「先看哪个域」。
- **单表邻居图**：顶部搜索框输入表名回车，画这张表的全部父表 + 子表（子表默认按域序截前 24 张，
  可勾「展开全部子表」），中心表红框定位。`biz_patient` 有 123 张子表，这个页签是唯一看得清的入口。

交互：滚轮缩放、拖动平移、双击放大（Shift+双击缩小）、「适应窗口」在整图小于 50% 时按 50% 显示，
剩余部分靠拖动 —— 3000+ 像素宽的域图缩到 12% 就等于没画。

## 用 PowerDesigner 打开

1. `File → Reverse Engineer → Database…`
2. DBMS 选 **MySQL 8.0**（没有 8.0 模板时选 MySQL 5.0 也能过；脚本已把 `json`→`text`、`enum`→`varchar(64)` 降级，不含 MySQL 8 专有语法）
3. 勾选 **Using script file**，指向 `powerdesigner/cslk_his_full.sql`（全库）或 `by-domain/NN-xxx.sql`（分域，推荐，320 表一张图不可读）
4. 完成后 `Tools → General Features → Auto Layout` 整理连线

分域脚本里**已带本域外的上游参照表**（只保留主键与被引用列），所以每个文件都能独立解析全部外键，
不会出现「引用了不存在的表」。代价是同一张 `sys_department` 会出现在多个域里 —— 这是刻意的，
每个域图都要自洽。

想让表/列显示中文名：`Edit → Execute Commands → New Script`，运行

```vb
Dim mdl : Set mdl = ActiveModel
If mdl Is Nothing Then MsgBox "no active model" : Exit Sub
Dim t, c
For Each t In mdl.Tables
  If Len(t.Comment) > 0 Then t.Name = t.Comment
  For Each c In t.Columns
    If Len(c.Comment) > 0 Then c.Name = c.Comment
  Next
Next
```

## 验证层级（`relationships.csv` 的「验证层级」列）

库里没有外键，所以**每条关系都带证据**，不能一视同仁地当事实用：

| 层级 | 条数 | 含义 |
|---|---|---|
| `A-数据全命中` | 589 | 父表主键命中，覆盖率 ≥ 0.95 |
| `A-人工口径` | 28 | 业务语义强制指定（如 `sys_oper_log.oper_id → sys_user`，操作者是人不是员工） |
| `B-数据部分命中` | 100 | 覆盖率 0.05–0.95，关系成立但有脏值 |
| `C-数据全悬空(仅命名推断)` | 35 | 有值、父表一条对不上 —— 见下方「数据口径问题」 |
| `D-无数据可验证` | 121 | 列全为 NULL（功能未启用），仅按命名推断 |
| `A-人工口径(数据悬空)` | 3 | 人工指定且当前无数据命中 |

基数 864 条 `1:N`、12 条 `1:1`（子表该列有唯一索引）。

## 三类登记（CSV 末尾三段）

- **`P-多态外键` 33 条**：同列按类型字段指向不同表（`encounter_id`+`encounter_type`、`biz_fee_record.source_id`、
  `sys_message.biz_id`、`biz_report.record_id`、排班域的 `org_id`+`org_type`…）。**只登记不画线** —— 一条线表达不出多目标，画了反而会误导成固定指向。
- **`X-未落定` 2 条**：`sys_drug.category_id`（没有药品分类表）、`biz_invoice.charge_id`（旧收费单已随四层迁移退役）。
- **`S-编号冗余` 126 条**：靠 `*_no`/`*_code` 命中父表唯一键的**快照列**（`patient_no`、`bill_no`、`yb_code`、排班快照 `emp_code`…）。
  它们是有意的反范式冗余，不是外键，因此不进模型。

## 本次反查暴露的数据口径问题

建模过程顺带做了一次全库引用完整性核查，C 级 35 条不是推断失败，是**数据本身错了**：

1. **项目字典的 `dept_id` 存的是第三套编码**：`sys_inspection_item.dept_id`（105 行）、
   `sys_laboratory_item.dept_id`（110 行）、`sys_treatment_item.dept_id`（88 行）、
   `biz_inspection_apply.inspection_dept_id`（57 行）落在 101/102/104/105 上，
   而 `sys_department.id` 是 1958001… 段、`dept_code` 是 1001/100001 段 —— 两个都对不上（`dept_code IN ('101'…)` 返回空）。
   `biz_stat_dept.dept_id` 同样存了 1–6。→ 这几列不能建外键，需要先定一套归属口径再清洗。
2. **`regist_id` 存的是另一套编号**：`biz_admission`/`biz_admission_order`/`biz_medical_record`/
   `biz_medical_record_archive`/`biz_treatment_apply`/`biz_compliance_audit`/`biz_insurance_settlement`
   的 `regist_id` 非空值全是 12 位号（`814297423401` 这类），而 `biz_appoint_info.id` 是 17 位
   （`8900000000000011103`）或雪花值，`regist_no` 是 `A2026092200001` —— **两套都对不上**，JOIN 命中 0。
   住院与病历的「来源挂号」追溯链在数据上是断的，且它落在 `*_id` 命名的列上，比落在 `*_no` 更糟
   （命名骗了所有后来的 JOIN 代码）。`biz_inspection_apply.regist_id` 95 值只命中 3，同一问题。
3. **BIGINT 精度丢失写进了库**：`biz_blood_crossmatch`/`biz_endoscopy_record`/`biz_pathology_order`/
   `biz_ultrasound_record` 的 `patient_id` 全是同一个 `8900000000000900000` —— 末尾三个 0 是
   IEEE-754 double 的特征，真实行是 `8900000000000900001`（探针患者，存在）。
   即铺底脚本让 19 位主键经过了 JS Number（没开 `bigNumberStrings`），**四张表的这一列全都不可用**。
   `biz_record_qc_flow.patient_id` = `8920092300000000311` 则是另一个不存在的人。
4. **收费四层回填不完整**（B 级）：`biz_payment_txn.bill_id` 87 值命中 24（0.28）、
   `biz_appoint_info.bill_id` 0.10、`biz_inpatient_order_exec.fee_record_id` 0.27 ——
   L3 支付流水与 L2 账单的勾对关系在存量数据上没铺满，`sql/126` 迁移的补数要复核。
5. **排班班次位数错**：`biz_schedule.shift_id` 非空 179 行里，53 行是 18 位（正常），
   126 行是 **19 位**（`8900000000000000007` vs 真实的 `890000000000000007`，中间多写了一个 0），
   所以覆盖率只有 0.30。这不是关系不成立，是铺底 SQL 的 id 打错。
6. **人员列是早期小 id**：`biz_discharge.discharge_doctor_id`(3/4/6)、`biz_treatment_apply.doctor_id`(3–6)、
   `sys_drug_price_history.operator_id`(2)、`sys_attachment.upload_user_id`(3–7)、
   `biz_rx_review_item.doctor_id`(9160000000000019xx) 全部悬空 ——
   `sys_employee` 里 id<100 的只有 1 个（超级管理员），`sys_user` 同理；真实人员主键在 93xx / 8900…4xxxx 段。
   另 `biz_patient_guardian.user_id` 全是 `9000000000000009099` 这一个不存在的值。
   现象与第 1、5 条同源：**主档 id 后来重排过，引用侧没跟着改**。
7. **患者快照列失配**：`biz_patient` 的 `first_visit_dept_id`/`last_visit_dept`/`last_visit_doctor`
   各 93 值只命中 1（`first_visit_doctor_id` 93 全命中，是同批数据里唯一对的）。
   快照列本身设计就不是外键（历史时点不随主数据变），但命中 1/93 说明它们存的是**旧编码体系的值**，
   与第 1 条同源。

按 AGENTS.md 的口径，这些结论只落在本文件，**没有**写进任何 `COMMENT`（列注释只写"是什么"）。

### 2026-10 刷新增量（302 → 320 表）

为 18 张新表与 8 张老表的新列补验了 45 条边：26 条 A、6 条 B、1 条 C、12 条 D。
唯一新增 C 级是 `biz_staff_demand.shift_id`（979 个非空值全命中 0）—— 该列存的是 `0-不限班次` 通配值，不是脏数据；
12 条 D 级集中在排班变更日志、实际出勤、知识库检索埋点等尚未产生数据的新功能，属预期。
排班域的 `org_id`+`org_type`（科室/病区/全院）与 `biz_nurse_schedule.unit_id`+`unit_type`（病区/科室）按多态登记（P 行），不画单线。

## 重新生成

```
node workspace/_schema_dump.mjs      # 从 dev 库导出基础表 SHOW CREATE TABLE 到 workspace/_db_schema.json
node workspace/_er/refresh.mjs       # 复用已裁决关系 + 新表/新列真实数据验证，重发本目录全部产物并对账
```

`refresh.mjs` 里的 `NEW_MAIN / NEW_POLY / NEW_S` 是新增关系的裁决表（多数票父表 + 数据覆盖率验证），
新增关系优先在这里加一条并写清理由，而不是去放宽候选推断 —— 推断放松一次，后面就要靠数据把错线一条条抓回来。

对账（跑完必查，`refresh.mjs` 末尾自带）：`cslk_his_full.sql` 的 `CREATE TABLE` 数 = 320 = `docs/sql/*.sql` 的建表数（表集合与领域基线一致），
`ALTER TABLE` 数 = 876 = CSV 里 A/B/C/D 四级条数之和（589+100+35+121+28+3）。
23 个分域文件的「本域 N 表」与 `docs/详细设计说明书/03-数据结构总体设计.md` §3.1 的领域表数逐一对齐（17/5/8/…/2，合计 320），
「M 条关系」合计也是 876。

**这些 `.sql` 是建模文件，禁止在业务库执行**（会对 302 张表发起 `ADD CONSTRAINT`，存量脏数据会让它大面积失败）。

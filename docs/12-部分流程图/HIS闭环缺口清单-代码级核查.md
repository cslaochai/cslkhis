# HIS 闭环缺口清单（代码级核查）

> **对照基线**：[HIS流程闭环全景-行业口径.md](./HIS流程闭环全景-行业口径.md)（§2 的 29 条执行级闭环、§3 的约 50 条院级闭环、§7 的 60 条二级闭环）。
> **核查范围**：`source/back_end` 8 个业务模块 + `his-core-common` 枚举 + `docs/01-初始化DDL` 23 个域。
> **核查口径**：一条闭环算"成立"必须同时有 —— ① 落库的状态列/枚举覆盖到终态；② service 里能看到状态迁移；③ 反向路径（撤回/作废/退回/召回）；④ 终点闸门在服务端。**不采信**：Controller 名、前端页面存在、"TODO/待对接"注释、空实现方法。
> **局限**：纯静态核查，未起服务实测。标"完整"指代码路径齐全，不等于真机跑通；标"缺失"指找不到表或写入点，不排除存在于未覆盖的模块。

**路径缩写**

```
I  = source/back_end/his-ms-inpatient/src/main/java/com/his/inpatient
C  = source/back_end/his-ms-clinical/src/main/java/com/his/clinical
P  = source/back_end/his-ms-pharmacy/src/main/java/com/his/pharmacy
M  = source/back_end/his-ms-medicaltech/src/main/java/com/his/medicaltech
O  = source/back_end/his-ms-operation/src/main/java/com/his/operation
CH = source/back_end/his-ms-charge/src/main/java/com/his/charge
OP = source/back_end/his-ms-outpatient/src/main/java/com/his/outpatient
PA = source/back_end/his-ms-patient/src/main/java/com/his/patient
E  = source/back_end/his-core-common/src/main/java/com/his/common/enums
```

---

## 0 结论

**结构覆盖度高，闭环完成度低。**

| 口径 | 条数 | 完整 | 部分 | 缺失 |
|---|---|---|---|---|
| 执行级闭环（§2） | 29 | **1** | 26 | 2 |
| 二级闭环（§7，已核到证据的部分） | 53 | 12 | 25 | 16 |

院级其他 21 条（§5）不做条数统计，按组看：**药品供应链四件套、病案质控/编码、运营排班**三组基本完整；**资金医保、公卫安全、服务延伸**三组普遍缺最后一环（电子票据、外部回执、审批留痕）。

一句话：**每条业务都建了表和页面，但普遍缺"最后一米"**——缺扫码证据、缺逆向路径、缺外部回执、缺独立审批。29 条里唯一严格闭环的是门诊追踪（挂号→缴费→签到→叫号状态机齐，含过号/重排/退号）。

缺口不是零散的技术债，而是四种可归因的模式（见 §3），所以修复也该按模式批量做，而不是按页面逐个数。

---

## 1 执行级闭环现状（29 条）

| 闭环 | 现状 | 关键缺口 | 主要证据 |
|---|---|---|---|
| 门诊追踪 | 完整 | 离院无独立状态（靠日终结转） | `OP/service/impl/QueueServiceImpl`（callNext/overdue/rejoin/退号置 5） |
| 住院追踪 | 部分 | 缺入区登记/出区/取消出院；在院状态只有 0/1 | `I/service/impl/InpatientServiceImpl`、`InpatientTransferServiceImpl` |
| 住院口服药 | 部分 | 无药师审方、无领药申请、**无床旁给药** | `I/service/impl/WardDispenseServiceImpl`（候选直接取"已校对"医嘱） |
| 住院注射药 | 部分 | **无皮试记录表**（仅 `sys_drug.is_skin_test` 标志）、肌注无闭环 | `P/service/impl/PivasServiceImpl` |
| 住院输液/静配 | 部分 | 核对→执行行不串（核对后不绑定执行行）；不扣库存、不计费；无皮试前置 | `P/service/impl/PivasServiceImpl`、`I/service/impl/InpatientInfusionServiceImpl` |
| 住院草药 | **缺失** | 煎药单只由门诊发药触发，住院中药医嘱无入口；无独立代煎核对人 | `P/service/impl/TcmDecoctServiceImpl` |
| 出院带药 | 部分 | 不扣库存、不计费、无护士核对、无退药；状态只有 待发药/已发药 | `I/service/impl/DischargeDrugServiceImpl`、`E/DischargeDrugStatusEnum` |
| 门诊口服药 | 部分 | 状态 2-已提交、5-已作废全库无写入；改方=整病历重存物理删除处方 | `C/service/impl/PrescriptionServiceImpl`、`C/service/impl/EmrServiceImpl` |
| 门诊输液 | 部分 | 无配置/成品核对；药品摘要自由文本，与处方、缴费、发药无关联 | `M/service/impl/InfusionRoomServiceImpl` |
| 门诊注射 | **缺失** | 未找到注射执行表与执行服务 | — |
| 住院检验 | 部分 | 12 节点缺 5：护士审核、打包、转运、入库、上机；采集(2)直跳接收；审核即发布 | `M/service/impl/MedicalTechServiceImpl`（结果录入即"已出结果"、审核即 RELEASED） |
| 住院危急值 | 部分 | 缺"复核""发布"两个节点，链条只剩 发现→接收→处置 | `M/service/impl/CriticalValueServiceImpl`、`E/CriticalValueStatusEnum`（仅 PENDING/RECEIVED/HANDLED） |
| 门诊检验 | 部分 | 同上 | 同上 |
| 门诊危急值 | 部分 | 同上 | 同上 |
| 检查（住院×5、门诊×5） | 部分 | 报告发布节点齐（心电/放射/超声/内镜/病理各有状态守卫），但缺护士审核、分诊叫号；**无作废能力** | `M/service/impl/RadiologyReportServiceImpl`、`EcgServiceImpl`、`UltrasoundServiceImpl`、`EndoscopyServiceImpl`、`PathologyServiceImpl` |
| 住院手术 | 部分 | 13 节点仅 6：缺护士审核、离病区、入/出手术间、返回病区；申请单无对应时间字段 | `O/service/impl/OperationApplyServiceImpl`、`E/OperationApplyStatusEnum`（0待排期/1已排期/2术前核对/3已完成/取消） |
| 日间/门诊手术 | 部分 | 6/10：缺缴费、三方核查、麻醉记录、PACU | `O/service/impl/DaySurgeryServiceImpl` |
| 输血 | 部分 | **7/17**：缺打印条码、标本采集、标本签收与申请单核对、护士申请领血、领血接收、输血巡视、病区血袋回收、回收登记、销毁 | `M/service/impl/TransfusionApplyServiceImpl`、`E/TransfusionStatusEnum`（0待配血/1已配血/2已发血/3输注中/4已完成/5已取消） |
| 住院治疗 / 门诊治疗 | 部分 | 只有执行记录 + 正常/异常结果判定，缺评定、计划、预约、疗程反馈 | `C/service/impl/TreatmentServiceImpl`、`E/TreatmentRecordStatusEnum`（0-异常 1-正常，是结果枚举不是流程状态机） |

> 本表 19 行覆盖 29 条闭环：**检查**一行代表 10 条（放射/超声/心电/内镜/病理 × 门诊/住院），**治疗**一行代表 2 条，"住院检验/门诊检验"与"住院/门诊危急值"各一行。合计 `2 + 8 + 4 + 10 + 2 + 1 + 2 = 29`，与 §2.8 的算式一致。

---

## 2 P0：评审与飞检最先挂的十条

### P0-1 床旁给药扫码全缺

- **现象**：执行表只有 `exec_status`、`exec_nurse_id`、输注起止时间；`execComplete` 直接把医嘱置"已执行"并计费。
- **证据**：`I/entity/BizInpatientOrderExec`、`I/dto/OrderExecCompleteDTO`（只有 execIds/execStatus/execNote/execTime）；全模块 grep `scanCode|barcode|wristband` 零命中。
- **影响**：护理给药闭环断在最后一米。评审抽查"护士怎么在床旁确认是这个人这袋药"，拿不出系统证据，只能拿纸质签名补——这正是电子病历 6 级的高频挂点。
- **修法**：执行表加腕带码 + 药品追溯码字段，`execComplete` 前服务端校验两码匹配（不匹配拒绝并留痕），再补一次"拒绝记录"落库。

### P0-2 患者身份核对/腕带无后端

- **现象**：腕带只有急诊一个前端打印页，无落库、无佩戴记录、无补打记录、无扫码接口。
- **证据**：`source/front/src/views/emergency/EmergencyWristband.vue`（页面注释自述"纯前端，零后端改动"）。
- **影响**：三查七对没有载体，P0-1 也无从校验。母婴/新生儿配对完全不存在。
- **修法**：建腕带表（患者/住院号/腕带码/打印次数/补打原因），扫码核对接口供护理端与手术室共用。

### P0-3 三方核查是单点代签

- **现象**：一次 `sign()` 提交外科/麻醉/护理三个员工 ID，三条 signTime 全取同一个 `now`，只校验"三个 ID 互不相同"。
- **证据**：`O/service/impl/OperationSafetyCheckServiceImpl#sign`（`LocalDateTime now = TimeUtil.nowSeconds()` 后连写三个 signTime）。
- **影响**：系统只能证明"有人填了三个人名"，不能证明三方各自签署。手术闭环命门，评审与麻醉质控都会抽。
- **修法**：改为三方各自登录签署（每条签名取各自 token 身份 + 各自时间戳），未签满不允许进入下一时段；已实现的"时段顺序 + 必核项 + 只增不改"守卫保留。

### P0-4 输血 17 节点缺 9 个

- **现象**：状态枚举只有 6 个码值，缺标本链与血袋回收链；巡视只有单个 `observation` 字段，没有多次巡视流水。
- **证据**：`E/TransfusionStatusEnum`、`M/service/impl/TransfusionApplyServiceImpl`。
- **影响**：输血闭环只能证明"配了血、输完了"，不能证明"这袋血是给这个人的"（标本核对）也不能证明"输的过程中巡了几次"。
- **修法**：补标本采集→签收核对（申请单↔标本双向核对）、领血申请/接收、巡视流水表、血袋回收登记/销毁四段。

### P0-5 渠道退款只打日志，钱不出账

- **现象**：微信/支付宝/银行卡退款拼一个 `SIMR-` 前缀假流水号写日志，实体注释自己写明"不可作资金核对依据"。
- **证据**：`CH/service/impl/PayRefundServiceImpl`（`"SIMR-" + payMethod.getCode() + "-" + refundNo()`）、`CH/entity/BizPaymentTxn` 字段注释。
- **影响**：退费四条腿的"钱"这条是空的，渠道对账永远平不了。
- **修法**：接真实渠道退款 API（与已实现的支付侧对称），失败即整笔回滚，禁止用假号兜底。

### P0-6 有绕过审批的直退口子

- **现象**：`POST /charge/settlementBill/refund` 不经过退费申请单直接退款，权限码用的是列表权限 `finance:refund:list`。
- **证据**：`CH/controller/SettlementBillController#refund`。
- **影响**：任何能看退费列表的人都能直接退钱，审批流形同虚设——飞检直接定性内控缺陷。
- **修法**：该接口限定为"渠道回填/内部补偿"用途并加服务端来源校验，人工退费一律走 RefundApply；权限码换成操作码。

### P0-7 退费无不相容岗位校验

- **现象**：审核只取服务端登录人写 auditor，从不比对"申请人 ≠ 审核人"，也不校验"撤销医嘱人 ≠ 退费审核人"。
- **证据**：`CH/service/impl/RefundApplyServiceImpl#auditRefundApply`（只判状态 1-待审核 + 乐观锁，无人员比对）。
- **影响**：一个人可以自己申请自己批，退费内控的第四条铁律没落。
- **修法**：审核前比对 apply.applyBy（或撤销操作人）与当前登录人，同人直接拒并记审计。

### P0-8 报告作废/重出整条路径不存在

- **现象**：全库无"报告作废"写入点，只有查询时过滤 5-已作废；作废后回写临床申请单的路径也不存在。
- **证据**：`M/service/impl/EcgServiceImpl`、`RadiologyReportServiceImpl`（仅读过滤）；`MedicalTechServiceImpl#publishReport` 无前置状态校验。
- **影响**：已发布报告出错只能留错报告或退费，无法"作废-重出-回写"；这是行业公认的假闭环第 4 条。
- **修法**：加"作废"动作（要求原因 + 审批）→ 申请单回落 → 新报告另起版本号，旧版本只读。

### P0-9 检验报告链被压缩成一个人的活

- **现象**：结果录入即"已出结果"，审核即"已发布"；`TESTING(4)`、`REVIEWED(6)` 两个码值全库无写入；标本采集(2)直接跳接收。
- **证据**：`M/service/impl/MedicalTechServiceImpl`（`record.setRecordStatus(LabRecordStatusEnum.RELEASED)` 在审核方法里直接写）。
- **影响**：单人可写可发，"审核"这个岗位在系统里不存在；标本流转也拿不出转运/签收证据。
- **修法**：拆出"已审核"中间态（录入→审核→发布三步各自留人留时间），补打包/转运/签收节点。

### P0-10 票据与医保的退费回撤缺失

- **现象**：① 票据作废只改状态，无红冲票/换开票，未接财政电子票据平台；② 医保 2305 撤销只用在两处——手动报盘撤销接口、2301 不确定态冲正，**退款主链路不调用**。
- **证据**：`CH/service/impl/InsuranceSettlementServiceImpl#cancelUpload`（仅 Controller 直调）、`InsuranceRealTimeSettleServiceImpl`（2305 仅用于冲正）；`RefundApplyServiceImpl`/`PaymentServiceImpl` 无 2305 调用。
- **影响**：医保个账已扣的退费在医保侧不回撤，医院自己贴钱；票据侧账票不符。
- **修法**：退费执行前按结算类型分流（自费→退款；医保→先 2305 撤销再退），票据侧出红冲票而不是改状态。

---

## 3 四类缺口模式

比逐条清单更有用的是这四条规律——同一模式可以用一套改造覆盖多处。

| 模式 | 表现 | 涉及闭环 |
|---|---|---|
| **最后一米断链** | 流程走到"系统里点了完成"，但缺现场证据（扫码/双方签字/独立时间戳） | 床旁给药、身份核对、三方核查、标本签收、血袋回收、CSSD 使用节点、高值耗材跟台、医疗废物交接 |
| **没有逆向** | 只能前进不能回退，或回退靠物理删除 | 报告作废、标本召回/重抽、停手术、取消出院、医嘱撤销（改方=删处方）、病案解封、不良事件整改退回、公卫上报表驳回后重提 |
| **外部回执缺位** | 本地置"已完成"，院外机构从没回过话 | 传染病/死因直报、双向转诊、医保结算清单上传与审核扣款、电子票据、DRG 支付回款、互联网诊疗配送 |
| **审批形同虚设** | 有申请单没制衡，或干脆留了直通口 | 退费（可直退 + 申请=审核）、抗菌药特殊使用级、优惠减免、预交金退款、高值耗材使用、电子签名（院内自签、管理员可代签） |

---

## 4 二级闭环现状（60 条，已核到证据的 53 条）

| 组 | 条数 | 完整 | 部分 | 缺失 | 未核 |
|---|---|---|---|---|---|
| A 逆向闭环（12） | 12 | 1 | 4 | 7 | 0 |
| B 审批审核（10） | 10 | 3 | 6 | 1 | 0 |
| C 预约排程（7） | 7 | 2 | 4 | 1 | 0 |
| D 通知响应（6） | 6 | 1 | 2 | 1 | 2 |
| E 批次流转（6） | 6 | 1 | 2 | 2 | 1 |
| F 临床专项（10） | 10 | 1 | 5 | 2 | 2 |
| G 身份授权（4） | 4 | 1 | 0 | 1 | 2 |
| H 资金小环（5） | 5 | 2 | 2 | 1 | 0 |
| **合计** | **60** | **12** | **25** | **16** | **7** |

### 4.1 A 逆向闭环 —— 最弱的一组

| 二级闭环 | 现状 | 说明 |
|---|---|---|
| 门诊/住院退费 | 部分 | 红冲负数行是对的（`FeeRecordServiceImpl#doReverse` 新增负行、orig_fee_id 指原行、原行只置 4-已红冲）；缺真实渠道退款、缺审批制衡、缺退库联动 |
| 退药回库 | 部分 | 库存回正 + 置"已退药"齐全；但退药侧不发起退费，只有"已发未退则拒退费"的单向闸门（`DispensingFacadeServiceImpl#assertNoDrugPendingReturn`） |
| 处方/医嘱撤销 | 部分 | 门诊处方状态 2/5 无写入点，改方走整病历重存物理删除；住院停嘱未独立核 |
| 检查申请撤销 | 部分 | 只有"已缴费未执行"可退；已执行直接抛异常阻断，无作废冲红通道；不释放预约号源 |
| 检验标本召回/重抽 | **缺失** | 只有 reject 写 `specimenStatus=99`（越出 1-6 声明域），不回退记录状态 |
| 报告作废重出 | **缺失** | 见 P0-8 |
| 停手术 | **缺失** | 无接口、无台次释放记录、无通知、无退费联动；术前核对后提示"由手术室登记停手术"，该接口不存在 |
| 输血申请取消/退血 | **缺失** | 无退血回库路径 |
| 出院召回/取消出院 | **缺失** | 出院即终态，不可撤销 |
| 发票红冲重开 | **缺失** | 见 P0-10 |
| 药品批次召回 | **缺失** | 只有单码作废，无批次召回单、无通知、无"已用药患者追回清单" |
| 预约取消/爽约 | 完整 | 检查预约取消/改约/爽约齐（但爽约扫描只查 status=1，会误伤已在工作站签到的患者） |

### 4.2 B 审批审核

| 二级闭环 | 现状 | 说明 |
|---|---|---|
| 前置审方 | 完整 | 通过签名、退回置 7-审方退回并写流水 + 站内信，退回后可重提 |
| 护士审核医嘱 | 部分 | 住院有"校对"，但检验侧护士审核节点缺 |
| 抗菌药物分级审批 | 部分 | 只有处方权级别闸（`AntibioticServiceImpl`），无特殊使用级"申请→会诊→审批"流程 |
| 麻精药品双签 | 完整 | `NarcoticControlServiceImpl` + 专柜发放登记 |
| 用血审批 | 部分 | 有输血审核态，无审批单/审批意见留痕 |
| 高值耗材使用审批 | **缺失** | 只有"登记使用+扣 1 件+计费"，无审批、无跟台、无术前术后双人核对 |
| 医保特药/谈判药备案 | 部分 | 有目录规则与慢病备案模块，未与开方链路串起 |
| 会诊 | 部分 | 申请→接单→完成三步，缺"到位"节点与申请方回执确认（仅站内信通知） |
| 病历质控返修 | 完整 | 退回置 REWORKING 并记 returnLevel，整改提交回上一级待审（`RecordQcFlowServiceImpl`）；瑕疵：resubmit 时间线层级恒记 DEPT |
| 不良事件上报 | 部分 | 上报→整改→关闭，缺独立"审核"节点，整改不可退回 |

### 4.3 C 预约排程 / D 通知响应

| 二级闭环 | 现状 | 说明 |
|---|---|---|
| 号源预约/停诊替诊 | 部分 | 停诊能出影响名单 + 批量退号，无替诊改派、无自动通知患者 |
| 检查集中预约 | 完整 | 预约/改约/取消/签到/完成/爽约 + 申请单状态精确回退；但预约表无 recordId，与检查记录两套状态并行 |
| 手术台次排程 | 部分 | 排台有，停手术释放无动作记录（仅靠状态自然不占台） |
| 治疗预约 | **缺失** | 治疗只有执行记录 |
| 床位预约等床 | 完整 | 等床队列/分配/跨科调配/退回/取消齐 |
| 日间手术/化疗排程 | 部分 | 有排程，缺缴费与术前评估串联 |
| 内镜/体检预约 | 部分 | 有预约与报告，体检缺总检医师审核签发 |
| 危急值响应 | 部分 | 有处置反馈终态（HANDLED + handleMeasure + 关闭站内信待办），缺复核与独立发布；`deleteById` 无状态守卫可物理删已处置记录 |
| 报告推送回执 | 部分 | 有推送，无阅读回执 |
| 随访任务 | 完整 | 计划→执行→结果归档，定时任务 Operator 走豁免口径 |
| 停诊/替诊通知 | **缺失** | 无通知单与触达记录 |
| 用药提醒 | 未核 | 本次未逐条核 |
| 冷链/温控报警 | 未核 | 本次未逐条核 |

### 4.4 E 批次流转 / F 临床专项 / G 身份 / H 资金

| 二级闭环 | 现状 | 说明 |
|---|---|---|
| 药品批次追溯 | 完整 | FEFO 出库 + 院内追溯码核销（`DrugTraceServiceImpl#verifyDispense`） |
| 高值耗材 UDI | 部分 | 登记使用 + 扣件 + 计费，无跟台扫码、无手术/病历关联 |
| 血袋流转 | **缺失** | 回收/登记/销毁未落库；`BloodBagStatusEnum` 只覆盖在库/已预留 |
| CSSD 器械包 | 部分 | 回收→清洗→打包→灭菌→储存→发放六节点各写 trace（操作人/时间/锅次）；缺"使用"节点、包内明细不落库、灭菌无生物监测放行 |
| 标本流转 | **缺失** | 只有赋码→采集→接收三步，无转运/打包状态 |
| 院际转运/急救交接 | 未核 | — |
| 皮试 | 部分 | 门诊输液室有落库，住院侧无皮试表、无结果闸门 |
| 交叉配血/备血 | 完整 | 配血单 + 状态机 |
| VTE 风险评估 | 部分 | 有评分 + 预防措施 + 复评日期；无 VTE 事件闭环复核 |
| 疼痛评估 | 部分 | 只有护理单点评分，无"评估→干预→再评估" |
| 营养筛查 | 部分 | 有筛查 + 复评日期，无会诊与干预记录 |
| 跌倒/压疮风险 | 部分 | Braden/Morse 只有单点评分，无措施与复评结构 |
| 化疗疗程 | **缺失** | 未找到"方案→周期→执行→评估→不良反应"状态机 |
| 康复疗程 | 未核 | 本次未逐条核 |
| 母乳喂养 | 未核 | 本次未逐条核 |
| 母婴配对 | **缺失** | 全无 |
| 患者身份核对（腕带） | **缺失** | 见 P0-2 |
| 患者授权/代理人 | 未核 | 本次未核 |
| 知情同意签署 | 未核 | 本次未核 |
| 员工岗位/权限变更 | 完整 | `EmployeePostService#replacePosts`（含主岗位不变量、null/[] 语义区分） |
| 预交金 | 部分 | 充值/抵扣/余额齐；充值退款无审批，出院退差只转入院内余额，不能原路退现 |
| 欠费管控 | 完整 | 欠费产生 + 停费闸门（Gateway） |
| 日结/班结 | 完整 | 交班按支付流水归集 + 日结含三级对账与审核 |
| 优惠/减免审批 | **缺失** | 减免只在出账时填金额，无申请-审批-留痕 |
| 医保退费/撤销申报 | 部分 | 2305 存在但退款主链路不调用（见 P0-10） |

---

## 5 院级其他组（21 条）

| 组 | 现状 | 说明 |
|---|---|---|
| 资金与医保（5） | 部分 | 门诊收费结算完整（票据为院内自编号，未接财政电子票据）；住院预交金部分；退费部分；票据日结部分（微信渠道对账 TODO 返回空、银联无入口）；医保结算部分（Gateway 真实 HTTP + SM2/SM4，2201/2301/2304/2305/日账单均实调；但目录对照是孤岛、扣款通知靠人工录入） |
| 药械供应链（4） | 完整/部分 | 采购入库、调拨、盘点、供应商退货四件套完整（状态机 + 库存流水 + 差异过账）；耗材 SPD/UDI 部分；CSSD 部分 |
| 病案（3） | 完整/部分 | 质控三级完整；编码任务→编码→复核完整；归档部分（有封存无解封、借阅单级审批无归还核验人） |
| 公卫与安全（4） | 部分 | 院感缺处置/结案；传染病与死因上报只组装 payload 写库置"已直报"，无外部通道与回执；不良事件缺审核与整改退回 |
| 服务延伸（3） | 部分 | 随访与满意度完整（慢病只有登记，无干预复评）；体检部分（缺总检审核）；互联网诊疗部分（无处方开具→药师审核→配送签收） |
| 运营管理（2） | 完整 | 号源排班含停诊回写；护理排班编制→审核→备案齐；绩效成本按 DDL 有域，未逐条核 |
| DRG/DIP | 部分 | `MedpaySettleServiceImpl` 本地真实分组 + 应付/盈亏测算 + 申诉；未上传医保结算清单、无基金拨付回款 |

---

## 6 修复顺序

| 批次 | 内容 | 验收判据 |
|---|---|---|
| **第一批（评审红线）** | 床旁扫码核药、腕带身份核对、三方核查改三方各自签、输血补 9 节点 | 抽任意一条医嘱：护士扫码记录 + 腕带码 + 药品码可查；抽任意一台手术：三条签名三个身份三个时间戳；抽任意一袋血：17 个节点都有操作人 |
| **第二批（资金合规）** | 真实渠道退款、关闭直退口子、申请人≠审核人闸门、票据红冲、医保 2305 接进退款链 | 退一笔钱：渠道流水号真实可查、审批链有两人、票据有红冲票、医保侧有 2305 回执 |
| **第三批（反向路径）** | 报告作废重出、标本召回、停手术、取消出院、药品批次召回、病案解封 | 每个动作都能逆向执行且回写上游，回写有审计 |
| **第四批（外部对接）** | 传染病直报、医保结算清单上传、DRG 支付回款、第三方 CA、电子票据平台、转诊回执 | 院外机构回执落库，不再是本地置"已完成" |

---

## 7 未覆盖项（下一轮补）

- 治疗闭环只核到"执行记录 + 正常/异常结果"，未逐条核评定/计划/预约全链。
- 冷链温控报警、用药提醒、知情同意签署、院际转运、康复疗程、母乳喂养未逐一核。
- 绩效与成本两条仅确认 DDL 有域，未核状态机。
- 全部结论基于静态代码阅读，未起实例实测；接口级真机验证（尤其扫码、三方签、退款）需在第一批改造后单独跑。

---

## 8 证据索引

| 主题 | 文件 |
|---|---|
| 执行表无扫码字段 | `I/entity/BizInpatientOrderExec`、`I/dto/OrderExecCompleteDTO` |
| 三方核查单点代签 | `O/service/impl/OperationSafetyCheckServiceImpl#sign` |
| 渠道退款假号 | `CH/service/impl/PayRefundServiceImpl`、`CH/entity/BizPaymentTxn` |
| 直退口子 | `CH/controller/SettlementBillController#refund` |
| 无不相容岗位校验 | `CH/service/impl/RefundApplyServiceImpl#auditRefundApply` |
| 红冲负数行（正确实现） | `CH/service/impl/FeeRecordServiceImpl#doReverse` |
| 医保 2305 仅两处调用 | `CH/service/impl/InsuranceSettlementServiceImpl#cancelUpload`、`InsuranceRealTimeSettleServiceImpl` |
| 检验审核即发布 | `M/service/impl/MedicalTechServiceImpl`（RELEASED 直写） |
| 危急值缺复核/发布 | `E/CriticalValueStatusEnum`、`M/service/impl/CriticalValueServiceImpl` |
| 输血状态只有 6 态 | `E/TransfusionStatusEnum`、`M/service/impl/TransfusionApplyServiceImpl` |
| CSSD 六节点 | `E/CssdNodeStatusEnum`、`P/service/impl/CssdServiceImpl` |
| 手术状态只有 5 态 | `E/OperationApplyStatusEnum` |
| 出院带药两态 | `E/DischargeDrugStatusEnum`、`I/service/impl/DischargeDrugServiceImpl` |
| 治疗是结果枚举 | `E/TreatmentRecordStatusEnum`、`C/service/impl/TreatmentServiceImpl` |
| 病历质控返修（正确实现） | `C/service/impl/RecordQcFlowServiceImpl` |
| 抗感染分级只有处方权闸 | `P/service/impl/AntibioticServiceImpl`、`C/service/impl/EmrServiceImpl` |
| 麻精双签（正确实现） | `P/service/impl/NarcoticControlServiceImpl` |
| 前置审方（正确实现） | `C/service/impl/PrescriptionServiceImpl` |
| 供应链四件套（正确实现） | `P/service/impl/`（DrugTransfer/Stocktake/PurchaseOrder/SupplierReturn） |
| 门诊队列状态机（正确实现） | `OP/service/impl/QueueServiceImpl` |
| 岗位主数据（正确实现） | `EmployeePostService#replacePosts`（his-ms-platform） |

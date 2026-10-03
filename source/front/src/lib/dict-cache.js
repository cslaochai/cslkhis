/**
 * 数据字典前端缓存
 * 只缓存字典类型列表（dictType, dictName），字典数据每次都从后端获取
 */
import {ref} from 'vue'
import {getDictDataList, getDictDataMapList} from '@/api/system'

// ========== 字典类型常量 ==========
//
// ★ 2026-09-22 全量核对（逐条比对 sys_dict_type，185 张字典）：
//   此前本文件有 13 个常量指向**库里根本不存在的 dict_type**（如 'regist_type'、
//   'settlement_type'），全是死常量、零外部引用 —— 所以没有造成线上
//   渲染错误，但它们是**陷阱**：谁照着常量名去接字典，拿到的是空下拉且不报错。
//   现已全部修正为库里真实类型名（前缀规律：本库业务字典几乎都带 `his_` 前缀）。
//   （2026-09-23 新增了 sys_gender：员工/患者统一性别字典，见 sql/75 —— 这是少数
//   不带 his_ 前缀的例外，别按前缀规律把它当错值改掉。）
//
//   核对方法：`SELECT dict_type FROM sys_dict_type WHERE del_flag=0`，逐个对照。
//   接新字典前先跑这条 SQL —— 「看着像对的错值」比报错更难查。
//
// ⚠ 库里不存在的字典**不要**在这里加常量占位：无字典枚举请走 `lib/*.js` 单点定义。
export const DICT_TYPE = {
    // 系统通用
    // 性别已于 2026-09-23 合并为一张字典（sql/75）：员工与患者同一套码值 1-男 2-女 9-未知。
    // 原 his_gender_sys（员工 0女1男）/ his_patient_gender 已物理删除 —— 别再接回来。
    SYS_GENDER: 'sys_gender',                    // 性别（1男/2女/9未知，员工与患者同口径）
    // 启用/禁用（0-禁用 1-启用）。sys_user.status 同口径；原账号状态字典 his_user_status
    // （0停用/1启用/2锁定）已删除 —— 代码里从没有过「锁定」态。
    ENABLE_STATUS: 'his_enable_status',
    SYS_USER_TYPE: 'his_user_type',              // 用户类型（1院内/2院外/3患者/4其他）

    // 医保相关
    MEDICAL_INSURANCE_TYPE: 'medical_insurance_type', // 医保类型

    // 药品相关
    DRUG_TYPE: 'his_drug_type',                  // 药品类型（1西药 2中成药 3中药饮片）
    // 药品特殊管理分类（麻精毒放，G10）：0普通 1麻醉药品 2第一类精神药品 3第二类精神药品 4毒性药品。
    // ⚠ 这一档**必须是字典而不是前端常量** —— 管制目录会调整
    //   （2024-07-01 咪达唑仑原料药与注射剂由第二类升为第一类），
    //   文案写死在前端就意味着每次目录调整都要重新发版前端。
    //   语义判定（哪些档位需双人复核 / 需空安瓿回收）在 `lib/drugSpecialFlag.js`。
    DRUG_SPECIAL_FLAG: 'his_drug_special_flag',
    // ⚠ DRUG_SPEC / DRUG_ROUTE / DRUG_FREQUENCY 在库里**没有对应字典**，不要在此新增。
    //   规格是 `sys_drug.specification` 字段（随药品带出，不是字典）；
    //   给药途径、用药频次、剂量单位属「无字典枚举」，口径单点在 `lib/drugUsage.js`
    //   （住院医嘱开立用；门诊医生站另有 lib/drugFrequency，两者值域一致，勿各写一份）。

    // 收费相关
    CHARGE_TYPE: 'his_charge_type',              // 收费类型
    CHARGE_ITEM_TYPE: 'his_charge_item_type',    // 收费项目类型
    PAY_METHOD: 'his_pay_method',                // 支付方式
    PAYMENT_STATUS: 'his_payment_status',        // 缴费状态
    CHARGE_STATUS: 'his_charge_status',          // 收费状态（旧模型遗留，四层页面不再使用）
    // ===== 四层收费模型（sql/125）：L1 记账 / L2 结算 / L3 支付，码值权威在 his-common/enums =====
    ENCOUNTER_TYPE: 'his_encounter_type',        // 就诊类型（1门诊 2住院）≠ 初复诊 visit_type
    FEE_STATUS: 'his_fee_status',                // 记账行状态（1待结算 2已锁定 3已结算 4已红冲）
    FEE_SOURCE_TYPE: 'his_fee_source_type',      // 费用来源单据类型（1挂号 2处方 … 12其他）
    BILL_TYPE: 'his_bill_type',                  // 账单类型（1挂号费 2门诊诊间 3住院中途 4出院）
    BILL_STATUS: 'his_bill_status',              // 账单状态（1待支付 2部分支付 3已支付 4已作废 5已退费）
    PAY_DIRECTION: 'his_pay_direction',          // 资金方向（1收款 2退款），流水金额带符号
    PAY_TXN_STATUS: 'his_pay_txn_status',        // 流水状态（1成功 2已冲正）
    TXN_SOURCE: 'his_txn_source',                // 流水来源（1收费台 … 9手工补账）
    ACCOUNT_OWNER_TYPE: 'his_account_owner_type',// 资金账户主体（1患者门诊余额 2住院就诊次预交金）
    ACCOUNT_TXN_TYPE: 'his_account_txn_type',    // 资金账户流水类型（1预交金充值 … 6手工调整）
    // account_status 无字典（1正常 2冻结），枚举单点 lib/fundAccount.js

    // 挂号相关
    REGIST_TYPE: 'his_regist_type',              // 挂号类型
    REGIST_SOURCE: 'his_regist_source',          // 挂号来源
    // 结算方式有**三个**口径，别混（挂号五分 / 通用 / 二分）：
    SETTLEMENT_TYPE_REGIST: 'his_settlement_type_regist', // 结算方式(挂号五分)
    PAY_TYPE: 'his_pay_type',                    // 结算方式(通用)
    SETTLEMENT_MODE: 'his_settlement_mode',      // 结算方式(二分：医保/自费)

    // 就诊相关
    VISIT_TYPE: 'his_visit_type_enum',           // 就诊类型
    IS_REVISIT: 'his_is_revisit',                // 初复诊标志
    // 注意：排队状态的字典类型是 his_queue_status（与后端 QueueStatusEnum 同源），
    // 原值 'queue_status' 在库里根本不存在 —— 后端 getDictDataMapData 查不到只会返回空，
    // 页面拿到空下拉，不报错。这类「看着像对的错值」比报错更难查。
    QUEUE_STATUS: 'his_queue_status',            // 排队状态
    REGIST_STATUS: 'his_regist_status',          // 挂号状态
    // 复诊来源 / 复诊收费方式（sql/121）：来源决定占不占号源与匹配哪条收费策略，
    // 收费方式落在 biz_revisit_fee_policy.charge_mode 上，两者都是「复诊」这条链路的口径源头，
    // 别在前端另写一份中文映射 —— 策略改了文案不改，前台和收费处看到的就对不上。
    REVISIT_SOURCE: 'his_revisit_source',
    REVISIT_CHARGE_MODE: 'his_revisit_charge_mode',

    // 病历相关
    RECORD_STATUS: 'his_record_status',          // 文书记录状态
    REVIEW_STATUS: 'his_review_status',          // 病历审核状态
    INPATIENT_RECORD_STATUS: 'his_inpatient_record_status', // 住院文书状态
    INPATIENT_RECORD_TYPE: 'his_inpatient_record_type',     // 住院文书类型
    ARCHIVE_STATUS: 'his_archive_status',        // 病历归档状态

    // 医技回报（住院/门诊医生站看检查·检验回报状态用，与后端 InsRecordStatusEnum/LabRecordStatusEnum 同源）
    INSPECTION_RECORD_STATUS: 'his_inspection_record_status',
    LABORATORY_RECORD_STATUS: 'his_laboratory_record_status',

    // 患者相关
    PATIENT_TYPE: 'his_patient_type',            // 患者类型（1自费 2城镇职工医保 3城乡居民医保 4公费 5其他）
    SYS_NATIONALITY: 'sys_nationality',          // 民族
    // 与患者关系（biz_patient_contact.relationship 的 tinyint 码值来源）。
    // 注意主档 biz_patient.contact_relation 那一列存的是**文案**，不用这张字典查。
    SYS_PATIENT_RELATION: 'sys_patient_relation',
    // ⚠ BLOOD_TYPE / MARITAL_STATUS 曾指向 `blood_type` / `marital_status` 两张不存在的字典。
    // 婚姻状况的码值口径在 `lib/patientField.js` 的 MARITAL_STATUS_OPTIONS，
    // 血型目前是 biz_patient.blood_type 的自由文本（PatientsView 里手写选项），
    // 两者都不从 sys_dict_data 读 —— 别再往这里加同名的空壳常量。

    // 医院相关
    SYS_HOSPITAL_TITLE: 'sys_hospital_title',    // 医院职称（101 医士 … 401 主任医师；501+ 非卫技系列）
    // 医院职位（1 临床科室主任 … 15 护士 … 31 临床医师 … 45 病案与编码岗）。
    // 与职称是两张字典：职称=专业技术资格层级（卫技四系 + 非卫技），职位=院内岗位职务。
    // ⚠ 两者都只存 dictValue，**绝不存中文**（sql/174 已把存量中文全量迁成码值）。
    //   后端按职称码判「副高及以上」的集合单点在 EmpTitleCode.SENIOR ——
    //   谁要按「主任/副主任」筛人，走码值集合，不要 like 中文。
    HOSPITAL_POSITION: 'hospital_position',      // 医院职位

    // 药品采购链（G9，2026-09-23 核对 sys_dict_type 四张均在库）：语义判定在 lib/purchase.js
    PURCHASE_APPROVAL_STATUS: 'his_purchase_approval_status', // 采购审批状态（0待审批/1已通过/2已驳回）
    INBOUND_STATUS: 'his_inbound_status',        // 药品入库状态（1待审核/2已审核/3已入库/4已取消）
    DRUG_INBOUND_TYPE: 'his_drug_inbound_type',  // 药品入库类型（1采购/2退货/3盘盈/4其他）
    SUPPLIER_RATING: 'his_supplier_rating',      // 供应商评级（1差/2一般/3良好/4优秀）
    STOCKTAKE_STATUS: 'his_stocktake_status',    // 药房盘点状态（1盘点中/2待复核/3已过账/4已关单，sql/127）
    // 药品逆向链路（sql/154：患者退药 → 药房退回药库 → 供应商退货）。语义判定在 lib/drugTransfer.js / lib/supplierReturn.js
    STOCK_ROOM: 'his_stock_room',                    // 库存地点（1药库/2药房）
    DRUG_TRANSFER_TYPE: 'his_drug_transfer_type',    // 调拨方向（1药库下拨药房/2药房退回药库）
    DRUG_TRANSFER_STATUS: 'his_drug_transfer_status',// 调拨单状态（1待发出/2待接收/3已完成/4已作废）
    SUPPLIER_RETURN_STATUS: 'his_supplier_return_status', // 供应商退货状态（1待退货/2已退货/3已作废）
    DRUG_STOCK_LOG_TYPE: 'his_drug_stock_log_type',  // 库存流水变动类型（1入库…7调拨出库/8调拨入库/9退货出库）
    // 药品追溯码（sql/156：入库扫码采集 → 发药扫码核销 → 医保上传）。语义判定在 lib/drugTrace.js
    DRUG_TRACE_CODE_TYPE: 'his_drug_trace_code_type',        // 码制（1-GS1 2-中国药品追溯码20位 3-其他/未识别）
    DRUG_TRACE_STATUS: 'his_drug_trace_status',              // 码状态（1在库 2已发药核销 3已作废）
    DRUG_TRACE_SOURCE_TYPE: 'his_drug_trace_source_type',    // 采集来源（1入库采集 2存量补采）
    DRUG_TRACE_UPLOAD_STATUS: 'his_drug_trace_upload_status',// 上传状态（0待上传 1已上传 2上传失败）
    DRUG_TRACE_VOID_TYPE: 'his_drug_trace_void_type',        // 作废类型（1退药 2报损 3召回）
    // 中药饮片发药（T5，sql/139）：煎法的 dict_value 就是中文本体，直接存 biz_prescription_detail.route
    TCM_DECOCT_METHOD: 'his_tcm_decoct_method',  // 煎法脚注（水煎服/先煎/后下/包煎/另煎/冲服/烊化）
    TCM_DECOCT_FLAG: 'his_tcm_decoct_flag',      // 煎服方式（1代煎 2自煎）
    ADVERSE_EVENT_TYPE: 'his_adverse_event_type',    // 不良事件类型（1药品/2跌倒/3压疮/4职业暴露/5手术/6输血/7管路/8院感/9设备/10信息/11其他）
    ADVERSE_EVENT_LEVEL: 'his_adverse_event_level',  // 不良事件等级（1I级警讯/2II级不良后果/3III级未造成后果/4IV级隐患）
    ADVERSE_EVENT_STATUS: 'his_adverse_event_status', // 不良事件状态（1已上报待处理/2处理中/3已整改/4已结案）
    ADVERSE_ACQUIRED: 'his_adverse_acquired',         // 不良事件来源（1院内获得/2入院带入，压疮发生率只算 1，sql/168）
    // 护理质控（sql/168）：检查表评分 → 合格率，不良事件+床日 → 千床日率，月度台账
    NURSING_QC_CATEGORY: 'his_nursing_qc_category',    // 检查类别（1基础护理/2专科护理/3安全管理/4护理文书/5院感防控）
    NURSING_INDICATOR: 'his_nursing_indicator',        // 质量指标（BASIC_NURSING/NURSING_DOC/FALL_RATE/UPPR_RATE）
    NURSING_QC_STATUS: 'his_nursing_qc_status',        // 检查单状态（1草稿/2已确认）
    NURSING_QC_REPORT: 'his_nursing_qc_report',        // 台账上报状态（1未上报/2已上报，已上报不被重算覆盖）

    // 病案借阅/复印 + 编码任务池（G16 收口，2026-09-23 核对三张均在库）：语义判定在 lib/archiveBorrow.js / lib/codeTask.js
    ARCHIVE_BORROW_TYPE: 'his_archive_borrow_type',    // 借阅类型（1借阅/2复印）
    ARCHIVE_BORROW_STATUS: 'his_archive_borrow_status', // 借阅状态（1待审核/2已借出/3已归还/4已拒绝/5已复印）
    WARD_DISPENSE_STATUS: 'his_ward_dispense_status',   // 住院摆药单主单状态（1待配药/2配药中/3已配药/4已核对/5已退药，聚合派生）
    WARD_DISPENSE_ITEM_STATUS: 'his_ward_dispense_item_status', // 住院摆药明细状态（1待配药/2已配药/3已核对/4已退药）
    PIVAS_BATCH_STATUS: 'his_pivas_status',             // 静配主单状态（1待审方/2待排队/3待调配/4待核对/5已完成/6全拒配，聚合派生）
    PIVAS_ITEM_STATUS: 'his_pivas_item_status',         // 静配明细状态（0已拒配/1待审方/2已审方/3已排队/4已调配/5已核对发放）
    // 临床路径（sql/106）：模板/入径状态与变异、步骤类型口径
    PATHWAY_STATUS: 'his_pathway_status',               // 路径模板状态（1草稿/2使用中/3已停用）
    PATHWAY_ENROLL_STATUS: 'his_pathway_enroll_status', // 入径状态（1在径/2已完成/3已退径）
    PATHWAY_VARIANCE_TYPE: 'his_pathway_variance_type', // 变异类型（1医嘱/2检查检验/3手术操作/4用药/5出院延期/6其他）
    PATHWAY_ITEM_TYPE: 'his_pathway_item_type',         // 步骤项目类型（1诊疗/2用药/3手术操作/4护理/5病情评估/6宣教）
    // 血液净化（透析）与 ICU 专科监护（sql/108）
    DIALYSIS_STATUS: 'his_dialysis_status',             // 透析档案状态（1在透/2暂停/3退出）
    DIALYSIS_ACCESS: 'his_dialysis_access',             // 血管通路（1自体内瘘/2人工血管/3中心静脉导管/4动静脉外露）
    DIALYSIS_FREQ: 'his_dialysis_freq',                 // 透析频次（1每周1次~4每周≥4次）
    DIALYSIS_DIALYZER: 'his_dialysis_dialyzer',         // 透析器（1低通量纤维素膜/2低通量合成膜/3高通量合成膜）
    DIALYSIS_ANTICOAG: 'his_dialysis_anticoag',         // 抗凝方式（1普通肝素/2低分子肝素/3枸橼酸钠/4无肝素）
    DIALYSIS_SLOT: 'his_dialysis_slot',                 // 透析时段（1上午/2下午/3夜间）
    DIALYSIS_SESSION_STATUS: 'his_dialysis_session_status', // 透析单状态（1已排班/2透析中/3已完成/4已取消）
    DIALYSIS_ADVERSE: 'his_dialysis_adverse',           // 透析不良反应类型
    DIALYSIS_MACHINE_STATUS: 'his_dialysis_machine_status', // 机位状态（1可用/2维修/3停用）
    ICU_STAY_STATUS: 'his_icu_stay_status',             // ICU 在科状态（1在科/2已出科）
    ICU_CARE_LEVEL: 'his_icu_care_level',               // 监护等级（1特级/2I级/3II级）
    ICU_OUT_DEST: 'his_icu_out_dest',                   // 转出去向（1普通病房/2专科病房/3手术室/4转院/5死亡/6自动离院）
    ICU_VENT_MODE: 'his_icu_vent_mode',                 // 呼吸支持（1鼻导管面罩/2无创/3有创/4脱机）
    CODE_TASK_STATUS: 'his_archive_code_status',       // 编码任务状态（1待编码/2已提交/3已完成/4已退修）
    ASSESS_TYPE: 'his_assess_type',                    // 护理评估类型（1压疮Braden/2跌倒Morse/3疼痛NRS）
    ASSESS_RISK_LEVEL: 'his_assess_risk_level',        // 护理评估风险等级（1低/2中/3高/4极高，后端按分数段算）

    // 医技亚专业（G17，2026-09-23 随 sql/84 铺入库）：病理/内镜/超声/LIS 质控/血库
    PATHOLOGY_EXAM_TYPE: 'his_pathology_exam_type',    // 病理检查类型（1常规石蜡/2术中冰冻/3细胞学/4免疫组化/5疑难会诊）
    PATHOLOGY_STATUS: 'his_pathology_status',          // 病理状态（1已登记~7已发布/8已取消）
    PATHOLOGY_BLOCK_STATUS: 'his_pathology_block_status', // 蜡块状态（1待取材/2已取材/3已包埋/4已切片）
    ENDOSCOPY_TYPE: 'his_endoscopy_type',              // 内镜类型（1胃镜/2肠镜/…/8胶囊内镜）
    ENDOSCOPY_ANESTHESIA: 'his_endoscopy_anesthesia',  // 内镜麻醉方式（1无/2表面/3静脉/4全身）
    ULTRASOUND_TYPE: 'his_ultrasound_type',            // 超声类型（1腹部/2心脏/…/7腔内）
    ENDOUS_STATUS: 'his_endous_status',                // 内镜/超声共用状态（1已登记~6已发布/7已取消）
    LIS_QC_LEVEL: 'his_lis_qc_level',                  // 质控水平（1低值/2中值/3高值）
    LIS_QC_STATUS: 'his_lis_qc_status',                // 质控结果状态（1在控/2警告/3失控）
    LIS_QC_HANDLE_STATUS: 'his_lis_qc_handle_status',  // 失控处理状态（0无需/1待处理/2已处理）
    // 室间质评 EQA（sql/172）：批次流转 / 盲样流转 / 判定结果 / 判定口径 / 比对结论
    LIS_EQA_PLAN_STATUS: 'his_lis_eqa_plan_status',        // 批次状态（1待收样/2检测中/3已上报/4已回报/5已归档）
    LIS_EQA_SAMPLE_STATUS: 'his_lis_eqa_sample_status',    // 盲样流转（0待检测/1已检测/2已上报/3已回报）
    LIS_EQA_RESULT_STATUS: 'his_lis_eqa_result_status',    // 判定结果（0未判定/1满意/2尚可/3不合格）
    LIS_EQA_JUDGE_MODE: 'his_lis_eqa_judge_mode',          // 判定口径（0无法判定/1SDI/2TEa/3可接受范围）
    LIS_EQA_COMPARE_STATUS: 'his_lis_eqa_compare_status',  // 仪器间比对结论（1可接受/2超差）
    BLOOD_TYPE: 'his_blood_type',                      // 血型（1A/2B/3O/4AB）
    BLOOD_RH: 'his_blood_rh',                          // Rh（1阳性/2阴性）
    BLOOD_COMPONENT: 'his_blood_component',            // 血液成分（1全血/…/6冷沉淀）
    BLOOD_INVENTORY_STATUS: 'his_blood_inventory_status', // 血袋状态（1在库/2已预留/3已发血/4已报废/5已退回）
    BLOOD_SOURCE_TYPE: 'his_blood_source_type',        // 血液来源（1血站/2自体储血/3互助献血）
    CROSSMATCH_METHOD: 'his_crossmatch_method',        // 配血方法（1盐水/2凝聚胺/3抗人球/4微柱凝胶）
    CROSSMATCH_RESULT: 'his_crossmatch_result',        // 配血结果（1相合/2不相合/3可疑凝集）
    CROSSMATCH_STATUS: 'his_crossmatch_status',        // 配血单状态（1待配血/2已配血/3已复核/4已作废）

    // ===== G20：出院带药 / 双向转诊 / 随访 =====
    DISCHARGE_DRUG_STATUS: 'his_discharge_drug_status', // 出院带药发药状态（1待发药/2已发药）
    REFERRAL_DIRECTION: 'his_referral_direction',      // 转诊方向（1上转/2下转）
    REFERRAL_STATUS: 'his_referral_status',            // 转诊状态（0待确认/1已确认/2已完成/3已取消）
    FOLLOWUP_TYPE: 'his_followup_type',                // 随访类型（1复诊提醒/2慢病随访/3用药指导/4术后随访）
    FOLLOWUP_STATUS: 'his_followup_status',            // 随访状态（1待随访/2随访中/3已完成/4已取消）

    // ===== sql/164：满意度评价（问卷模板 → 发放回收 → 答卷 → 看板）=====
    //   ⚠ 一次 getDictDataMapList 最多 5 个 type，超了会整批返回空且不报错 → 分两批取。
    //   量表文案（1非常不满意…5非常满意）在 lib/surveyScale.js 单点定义，不是字典：
    //   它是打分口径，一旦被人在字典里改成「5-不满意」，历史分数就再也解释不通了。
    SURVEY_SCENE: 'his_survey_scene',                  // 调查场景（1出院随访/2门诊/3住院在院/4体检）
    SURVEY_TPL_STATUS: 'his_survey_tpl_status',        // 问卷状态（1启用/2停用，停用不再自动发放）
    SURVEY_DIMENSION: 'his_survey_dimension',          // 评价维度（1挂号便捷…7总体印象）
    SURVEY_QUESTION_TYPE: 'his_survey_question_type',  // 题型（1量表/2单选/3多选/4NPS/5开放文本）
    SURVEY_SOURCE: 'his_survey_source',                // 发放来源（1随访任务/2出院结算/3人工补发）
    SURVEY_CHANNEL: 'his_survey_channel',              // 回收渠道（1电话代填/2短信/3微信/4现场扫码）
    SURVEY_DISPATCH_STATUS: 'his_survey_dispatch_status', // 回收状态（1待推送/2待回收/3已回收/4已过期/5已拒答）
    SURVEY_ANSWER_STATUS: 'his_survey_answer_status',  // 答卷状态（1有效/2已作废）
    SURVEY_FILL_SOURCE: 'his_survey_fill_source',      // 填报方式（1患者自填/2随访员代填/3现场扫码）

    // ===== G21：检查预约中心（2026-09-23 核对四张均在库，随 sql/85 铺底）=====
    //   语义判定（哪一档可改约、哪种格子不可点）在 lib/examAppointment.js，这里只管取文案。
    EXAM_DEVICE_TYPE: 'his_exam_device_type',          // 检查设备类别（1CT/2MR·磁共振/3DR·CR/4超声/5心电/6内镜/7其他）
    EXAM_APPT_STATUS: 'his_exam_appoint_status',       // 检查预约状态（1已预约/2已到检/3已完成/4已取消/5爽约）
    EXAM_DEVICE_STATUS: 'his_exam_device_status',      // 设备开放状态（1开放预约/2暂停预约）
    EXAM_SLOT_STATUS: 'his_exam_slot_status',          // 号源段状态（0锁号/1正常）

    // ===== G22：设备后勤（2026-09-24 核对八张均在库，随 sql/86 铺底）=====
    EQUIP_MAINTAIN_TYPE: 'his_equipment_maintain_type',   // 设备维保类型（1保养/2维修/3巡检）
    EQUIP_METERING_TYPE: 'his_equipment_metering_type',   // 设备计量类型（1强检/2校准）
    EQUIP_METERING_RESULT: 'his_equipment_metering_result', // 计量结果（1合格/2不合格）
    CSSD_PACK_STATUS: 'his_cssd_pack_status',             // 器械包状态（1已回收~6已发放）
    CSSD_NODE_TYPE: 'his_cssd_node_type',                 // 追溯节点类型（1回收~6发放）
    CSSD_STERIL_METHOD: 'his_cssd_steril_method',         // 灭菌方式（1高压蒸汽/2环氧乙烷/3低温等离子）
    WASTE_TYPE: 'his_waste_type',                         // 医废类别（1感染性~5化学性）
    WASTE_STATUS: 'his_waste_status',                     // 医废状态（1已登记/2已交接/3已处置）

    // ===== G23：体检 / DRG 模拟 / 绩效（2026-09-24 核对五张均在库，随 sql/87 铺底）=====
    CHECKUP_RECORD_STATUS: 'his_checkup_record_status',   // 体检登记状态（1已登记/2检查中/3已完成/4已出报告）
    CHECKUP_PERSON_TYPE: 'his_checkup_person_type',       // 体检对象（1个人/2团体）
    CHECKUP_RESULT_FLAG: 'his_checkup_result_flag',       // 体检结果标志（0正常/1异常/2待查）
    DRG_SIM_STATUS: 'his_drg_sim_status',                 // DRG模拟入组（1已入组/2未入组）
    PERF_STATUS: 'his_perf_status',                       // 绩效核算状态（1草稿/2已核算/3已发布）

    // ===== G19：门诊治疗站（his_treatment_item_type / apply_status / record_status 随早期铺底在库，
    //   exec_status / charge_status 随 sql/88 新增）=====
    //   列表文案直接取后端 VO 的 *Text（单点在 TreatmentDictText），这里只给筛选下拉用。
    TREATMENT_ITEM_TYPE: 'his_treatment_item_type',       // 治疗项目类别（1注射/2输液/3换药/4拆线/5其他）
    TREATMENT_APPLY_STATUS: 'his_treatment_apply_status', // 疗程状态（0待执行/1已执行/2已取消）
    TREATMENT_EXEC_STATUS: 'his_treatment_exec_status',   // 按次执行状态（0待执行/1已执行/2已取消）
    TREATMENT_CHARGE_STATUS: 'his_treatment_charge_status', // 按次计费状态（0未计费/1已计费/2计费失败/3无需计费）
    INFECTIOUS_CLASS: 'his_infectious_class',               // 传染病类别（1甲类/2乙类/3丙类）
    INFECTIOUS_REPORT_STATUS: 'his_infectious_report_status', // 报卡状态（1待审核/2已审核/3已直报/4已退报）

    // ===== L10：院感监测（sql/92）=====
    //   列表文案取后端 VO 的 *Text，这里只给筛选/表单下拉用。
    INFECTION_CASE_STATUS: 'his_infection_case_status',     // 院感病例状态（1待核实/2已确认/3已排除）
    INFECTION_SOURCE: 'his_infection_source',               // 感染来源（1社区感染/2医院感染）
    INFECTION_SITE: 'his_infection_site',                   // 感染部位（1下呼吸道/2泌尿道/.../9其他）
    INFECTION_MONITOR_TYPE: 'his_infection_monitor_type',   // 目标性监测类型（1CAUTI/2CLABSI/3VAP）
    INFECTION_MONITOR_STATUS: 'his_infection_monitor_status', // 监测状态（1在管/2已拔管）
    HAND_OBS_OBJECT: 'his_hand_obs_object',                 // 手卫生观察对象（1医生/2护士/3工勤其他）

    // ===== L14：医疗纠纷 / 投诉登记（sql/109，八张字典）=====
    //   ⚠ 八张字典超过 getDictDataMapList 单次 5 个 type 的上限，页面必须分两批取
    //     （第一批 5 个、第二批 3 个），一次塞 8 个会**整个返回空**且零报错。
    DISPUTE_CASE_TYPE: 'his_dispute_case_type',       // 类型（1服务投诉/2医疗纠纷/3医疗损害争议/4其他）
    DISPUTE_SOURCE: 'his_dispute_source',             // 来源（1来电~7其他）
    DISPUTE_STATUS: 'his_dispute_status',             // 状态（1待受理→4已结案，5已撤销）
    DISPUTE_LEVEL: 'his_dispute_level',               // 等级（1一般/2较大/3重大）
    DISPUTE_DEAL_TYPE: 'his_dispute_deal_type',       // 处理途径（1院内协商~6其他）
    DISPUTE_DUTY: 'his_dispute_duty',                 // 责任认定（1无责~5完全责任）
    DISPUTE_RELATION: 'his_dispute_relation',         // 投诉人与患者关系（1本人/2家属/3代理人/4其他）
    DISPUTE_SEAL_STATUS: 'his_dispute_seal_status',   // 病历封存状态（0未申请/1已封存/2待归档后封存）

    // ===== L15：互联网医院 / 远程会诊（sql/110）=====
    TELE_CONSULT_TYPE: 'his_tele_consult_type',       // 远程会诊类型（1临床会诊/2影像/3心电/4病理/5其他）
    TELE_CONSULT_STATUS: 'his_tele_consult_status',   // 远程会诊状态（1待安排→3已完成，4已取消）
    ONLINE_CONSULT_TYPE: 'his_online_consult_type',   // 线上问诊方式（1图文/2电话/3视频）
    ONLINE_CONSULT_STATUS: 'his_online_consult_status', // 线上问诊状态（1待接诊→3已完成，4已退诊）

    // ===== L16：日间手术（sql/111）=====
    DAY_SURGERY_STATUS: 'his_day_surgery_status',     // 登记状态（1待评估→5已出院，6已取消/7已转住院）
    DAY_SURGERY_ANESTHESIA: 'his_day_surgery_anesthesia', // 麻醉方式（1局麻/2椎管内/3全麻/4神经阻滞/5其他）
    DAY_SURGERY_LEAVE_TYPE: 'his_day_surgery_leave_type', // 离院方式（1按时离院/2转普通住院/3非计划再入院）
    DAY_SURGERY_FOLLOW_RESULT: 'his_day_surgery_follow_result', // 随访结果（1无异常~4失联）
    DAY_SURGERY_FOLLOW_TYPE: 'his_day_surgery_follow_type',     // 随访方式（1电话/2门诊/3上门/4线上）
    DAY_SURGERY_EVAL_RESULT: 'his_day_surgery_eval_result',     // 术前评估结论（1通过/2不通过）

    // ===== 死亡证明与死亡登记（sql/157，八张字典）=====
    //   ⚠ 同样受 getDictDataMapList 单次 5 个 type 上限约束，页面必须分两批取。
    DEATH_PLACE: 'his_death_place',                 // 死亡地点（1医院/2来院途中/3家中/4民政管理机构/5其他机构/9未指明）
    DEATH_CERT_STATUS: 'his_death_cert_status',     // 证明状态（1草稿→2已审核→3已开具，4已作废）
    DEATH_REPORT_STATUS: 'his_death_report_status', // 死因监测上报状态（1未上报/2已上报/3上报失败）
    DEATH_CAUSE_PART: 'his_death_cause_part',       // 死因链部分（1Ⅰ部分死因链/2Ⅱ部分其他疾病）
    DEATH_TYPE: 'his_death_type',                   // 死亡类型（1疾病/2非疾病外部原因/3死因不明）
    DEATH_BODY_DISPOSAL: 'his_body_disposal',       // 尸体处理方式（1殡仪馆接运/2家属自行/3病理解剖/4其他）
    DEATH_CERT_COPY: 'his_death_cert_copy',         // 死亡证明联次（1记录联/2户籍联/3殡葬联/4家属联）
    DEATH_REGISTER_STATUS: 'his_death_register_status', // 登记状态（1草稿/2已登记/3已作废）

    // ===== 病危/病重通知与告知书签收回执（sql/161，四张字典）=====
    NOTICE_TYPE: 'his_notice_type',                       // 通知类别（1病危/2病重）
    NOTICE_STATUS: 'his_notice_status',                   // 通知状态（1草稿→2已签发→3已签收，4已作废）
    NOTICE_CONSCIOUSNESS: 'his_notice_consciousness',     // 意识状态（1清楚/2嗜睡/3昏睡/4昏迷/5谵妄）
    NOTICE_RELATION: 'his_notice_relation',               // 患方签收人与患者关系（法定代理人/单位负责人等）

    // ===== 住院患者请假/离院登记（sql/162，四张字典；关系复用 NOTICE_RELATION）=====
    LEAVE_TYPE: 'his_leave_type',                     // 请假类别（1临时外出当日往返/2离院过夜/9其他）
    LEAVE_STATUS: 'his_leave_status',                 // 状态（1待审批→2已批准→3已离院→4已返回，5已拒绝，6已取消）
    LEAVE_CONTACT: 'his_leave_contact',               // 超期联系结果（1联系上并约定返回/2联系不上/3家属已知晓）
    LEAVE_REPORT: 'his_leave_report',                 // 超期上报对象（1主管医师/2病区护士长/3医务科总值班）

    // ===== 医保审核扣款与飞检处理 + 慢特病备案（sql/163）=====
    YB_INSPECT_TYPE: 'his_yb_inspect_type',               // 检查类型（1国家飞检/2省级飞检/3智能审核转来/4日常驻点审核）
    YB_INSPECT_STATUS: 'his_yb_inspect_status',           // 批次状态（1进行中/2已结项/3已作废）
    YB_DEDUCT_SOURCE: 'his_yb_deduct_source',             // 扣款来源（1飞检现场发现/2智能审核或事后复核）
    YB_VIOLATION_TYPE: 'his_yb_violation_type',           // 违规类型（1重复收费/2超适应症/3串换项目/4超标准/5虚假住院/6无指征无文书/9其他）
    YB_DEDUCT_STATUS: 'his_yb_deduct_status',             // 扣款状态（1待确认/2申诉中/3申诉成功/4维持扣款待缴/5已缴回/6已作废）
    YB_APPEAL_RESULT: 'his_yb_appeal_result',             // 申诉结果（1成功/2驳回）
    YB_LOSS_BEAR: 'his_yb_loss_bear',                     // 损失承担方式（1院方/2科室/3个人/4科室+个人共担）
    YB_DEDUCT_ACTION: 'his_yb_deduct_action',             // 处理动作（1新建草稿/2发起申诉/3录入申诉结果/4确认扣款并追责/5录入缴回/6作废）
    CHRONIC_DISEASE_TYPE: 'his_chronic_disease_type',     // 慢特病类别（1慢性病/2特殊病）
    YB_CHRONIC_STATUS: 'his_yb_chronic_status',           // 备案状态（1有效/2已注销/3已驳回）

    // ===== 四处无入口数据模型补菜单（sql/165）=====
    PH_REPORT_TYPE: 'his_ph_report_type',                 // 公卫上报类型（1传染病/2死因监测/3慢性病/4其他）
    PH_REPORT_STATUS: 'his_ph_report_status',             // 公卫上报状态（1待审核/2审核通过/3审核驳回，口径以列注释为准）
    COMPLIANCE_AUDIT_TYPE: 'his_compliance_audit_type',   // 合规审核类型（1结算前自查/2批量筛查/3医保反馈复核）
    COMPLIANCE_RISK_LEVEL: 'his_compliance_risk_level',   // 合规风险等级（0未发现/1提示/2关注/3高危）
    COMPLIANCE_ITEM_RESULT: 'his_compliance_item_result', // 规则判定三态（1命中/2通过/3不适用）
    COMPLIANCE_TARGET_TYPE: 'his_compliance_target_type', // 规则作用对象（0清单级/1诊断/2手术操作）

    // ===== 护理管理：病区护理排班（sql/166，菜单 330/331）=====
    SHIFT_SCOPE: 'his_shift_scope',                       // 班次适用域（1门诊/急诊排班 2病区护理排班 3全院值守 4全院通用，biz_shift.use_scope）
    NURSE_SCHEDULE_STATUS: 'his_nurse_schedule_status',   // 护理排班状态（1上班/2休息/3请假/4培训/5停排）
    CHRONIC_CONFIRM_STATUS: 'his_chronic_confirm_status', // 慢病档案认定状态（0待认定/1已认定/2已取消）
    CHRONIC_DISEASE: 'his_chronic_disease',               // 常用慢病（dict_value 即 ICD-10 编码，选中可回填 diseaseCode+diseaseName）

    // ===== 组织与资源：全院岗位排班（sql/200 核心表）=====
    // 字典只是下拉数据源，Java 枚举才是权威；两处文案不一致以枚举为准。
    ORG_UNIT_TYPE: 'his_org_unit_type',                   // 排班单元类型（1科室/2病区/3全院）
    DUTY_STATUS: 'his_duty_status',                       // 出勤状态（1上班/2休息/3请假/4培训/5停班）
    ATTEND_MODE: 'his_attend_mode',                       // 值班响应形态（1坐班/2听班/3留院值班）
    STAFF_SCHEDULE_SOURCE: 'his_staff_schedule_source',   // 排班生成来源（1手工/2模板/3复制周期/4换班）
    SCHEDULE_CHANGE_TYPE: 'his_schedule_change_type',     // 排班变更类型（1换班/2代班/3停班/4加号/5减号/6出诊变更）
    DUTY_SCOPE: 'his_duty_scope',                         // 值守责任范围（1全院行政/2急诊/3感染/4总务/5信息）
}

/**
 * 获取字典数据（单个）
 */
export async function loadDictDataList(dictType) {
    try {
        const res = await getDictDataList(dictType)
        return res.data || []
    } catch (error) {
        console.error(`加载字典数据失败: ${dictType}`, error)
        return []
    }
}

/**
 * 获取字典数据（多个，返回Map结构）
 * @param {string} dictTypes 逗号分隔的字典类型，如 'sys_gender,sys_status'
 * @returns {Promise<Object>} { dictType: [dictData], ... }
 */
export async function loadDictDataMap(dictTypes) {
    try {
        // 后端限制单次最多 5 个 dictType（超出直接报「数据字典每次最多只能查询5个」），按 5 个一批切开再合并
        const types = String(dictTypes || '').split(',').map(s => s.trim()).filter(Boolean)
        const map = {}
        for (let i = 0; i < types.length; i += 5) {
            const res = await getDictDataMapList(types.slice(i, i + 5).join(','))
            Object.assign(map, res.data || {})
        }
        return map
    } catch (error) {
        console.error(`批量加载字典数据失败: ${dictTypes}`, error)
        return {}
    }
}


/**
 * Vue组合式函数：使用字典
 */
export function useDict() {
    return {
        DICT_TYPE,
        loadDictDataList,
        loadDictDataMap,
    }
}

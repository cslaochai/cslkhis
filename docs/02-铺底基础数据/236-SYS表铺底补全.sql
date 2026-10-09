-- =============================================================
-- sql/236 SYS 表铺底补全 + 薄表增厚 + 夹具清理
-- 库：hn_biz_his  元数据口径：create_by=admin / create_by_id=1 / create_time=2026-05-08
-- 范围：sys_diagnosis(0)、sys_price_change_history(0)、sys_workbench_layout(0)、
--       sys_knowledge_doc/chunk、sys_alert_rule、sys_insurance_policy、
--       sys_drug_price_history、sys_drug_interaction、sys_infectious_disease、
--       sys_supplier、sys_faq、sys_treatment_item、sys_imaging_plain_item、
--       sys_lab_plain_item；biz 规则类：复诊策略/输液座位/质控计划/医保目录规则
-- 不铺：运行时日志表（login_log/oper_log/audit_log/field_change_log/ai_call_log/
--       message/service_trace/attachment）与患者域业务数据
-- 幂等：编号段固定；传染病按病名 NOT EXISTS 防重；冲突语句执行器跳过
-- =============================================================

-- ---------- 段0 夹具清理：输液座位 m10-verify、质控计划 g17-verify ----------
UPDATE biz_infusion_seat SET del_flag=1, update_by='admin', update_time='2026-05-08 00:00:00' WHERE remark LIKE '%夹具%' OR area='验证区';
UPDATE biz_lis_qc_plan SET del_flag=1, update_by='admin', update_time='2026-05-08 00:00:00' WHERE plan_no LIKE 'QCG17%';

-- ---------- 段1 sys_diagnosis 常用诊断字典（80 条，真实 ICD-10 码） ----------
INSERT INTO sys_diagnosis (id, diagnosis_code, diagnosis_name, diagnosis_type, category_name, parent_id, sort_order, is_common, is_notifiable, disease_stage, status, create_by, create_time, update_by, update_time, del_flag, remark, create_by_id, update_by_id)
VALUES
(2360000000000001001,'I10.x00','原发性高血压',1,'心血管系统',0,10,1,0,NULL,1,'admin','2026-05-08 00:00:00','admin','2026-05-08 00:00:00',0,'常用诊断（sql/236）',1,1),
(2360000000000001002,'I25.103','冠状动脉粥样硬化性心脏病',1,'心血管系统',0,20,1,0,NULL,1,'admin','2026-05-08 00:00:00','admin','2026-05-08 00:00:00',0,NULL,1,1),
(2360000000000001003,'I21.900','急性心肌梗死',1,'心血管系统',0,30,1,0,'急性期',1,'admin','2026-05-08 00:00:00','admin','2026-05-08 00:00:00',0,NULL,1,1),
(2360000000000001004,'I48.x00','心房颤动',1,'心血管系统',0,40,1,0,NULL,1,'admin','2026-05-08 00:00:00','admin','2026-05-08 00:00:00',0,NULL,1,1),
(2360000000000001005,'I50.900','心力衰竭',1,'心血管系统',0,50,1,0,NULL,1,'admin','2026-05-08 00:00:00','admin','2026-05-08 00:00:00',0,NULL,1,1),
(2360000000000001006,'I20.000','稳定型心绞痛',1,'心血管系统',0,60,1,0,NULL,1,'admin','2026-05-08 00:00:00','admin','2026-05-08 00:00:00',0,NULL,1,1),
(2360000000000001007,'I63.900','脑梗死',1,'神经系统',0,70,1,0,NULL,1,'admin','2026-05-08 00:00:00','admin','2026-05-08 00:00:00',0,NULL,1,1),
(2360000000000001008,'I61.902','脑出血',1,'神经系统',0,80,1,0,NULL,1,'admin','2026-05-08 00:00:00','admin','2026-05-08 00:00:00',0,NULL,1,1),
(2360000000000001009,'G43.900','偏头痛',1,'神经系统',0,90,1,0,NULL,1,'admin','2026-05-08 00:00:00','admin','2026-05-08 00:00:00',0,NULL,1,1),
(2360000000000001010,'G47.100','发作性睡病',1,'神经系统',0,95,0,0,NULL,1,'admin','2026-05-08 00:00:00','admin','2026-05-08 00:00:00',0,NULL,1,1),
(2360000000000001011,'E11.900','2型糖尿病',1,'内分泌系统',0,100,1,0,NULL,1,'admin','2026-05-08 00:00:00','admin','2026-05-08 00:00:00',0,NULL,1,1),
(2360000000000001012,'E10.900','1型糖尿病',1,'内分泌系统',0,110,1,0,NULL,1,'admin','2026-05-08 00:00:00','admin','2026-05-08 00:00:00',0,NULL,1,1),
(2360000000000001013,'E04.900','非毒性结节性甲状腺肿',1,'内分泌系统',0,120,1,0,NULL,1,'admin','2026-05-08 00:00:00','admin','2026-05-08 00:00:00',0,NULL,1,1),
(2360000000000001014,'E05.900','甲状腺功能亢进',1,'内分泌系统',0,130,1,0,NULL,1,'admin','2026-05-08 00:00:00','admin','2026-05-08 00:00:00',0,NULL,1,1),
(2360000000000001015,'E03.900','甲状腺功能减退',1,'内分泌系统',0,140,1,0,NULL,1,'admin','2026-05-08 00:00:00','admin','2026-05-08 00:00:00',0,NULL,1,1),
(2360000000000001016,'E78.500','高脂血症',1,'内分泌系统',0,150,1,0,NULL,1,'admin','2026-05-08 00:00:00','admin','2026-05-08 00:00:00',0,NULL,1,1),
(2360000000000001017,'E66.900','肥胖症',1,'内分泌系统',0,160,1,0,NULL,1,'admin','2026-05-08 00:00:00','admin','2026-05-08 00:00:00',0,NULL,1,1),
(2360000000000001018,'E28.209','多囊卵巢综合征',1,'内分泌系统',0,165,1,0,NULL,1,'admin','2026-05-08 00:00:00','admin','2026-05-08 00:00:00',0,NULL,1,1),
(2360000000000001019,'J06.900','急性上呼吸道感染',1,'呼吸系统',0,170,1,0,NULL,1,'admin','2026-05-08 00:00:00','admin','2026-05-08 00:00:00',0,NULL,1,1),
(2360000000000001020,'J18.900','肺炎',1,'呼吸系统',0,180,1,0,NULL,1,'admin','2026-05-08 00:00:00','admin','2026-05-08 00:00:00',0,NULL,1,1),
(2360000000000001021,'J44.100','慢性阻塞性肺病急性加重',1,'呼吸系统',0,190,1,0,'急性加重期',1,'admin','2026-05-08 00:00:00','admin','2026-05-08 00:00:00',0,NULL,1,1),
(2360000000000001022,'J44.800','慢性阻塞性肺病',1,'呼吸系统',0,200,1,0,'稳定期',1,'admin','2026-05-08 00:00:00','admin','2026-05-08 00:00:00',0,NULL,1,1),
(2360000000000001023,'J45.900','支气管哮喘',1,'呼吸系统',0,210,1,0,NULL,1,'admin','2026-05-08 00:00:00','admin','2026-05-08 00:00:00',0,NULL,1,1),
(2360000000000001024,'J42.x00','慢性支气管炎',1,'呼吸系统',0,220,1,0,NULL,1,'admin','2026-05-08 00:00:00','admin','2026-05-08 00:00:00',0,NULL,1,1),
(2360000000000001025,'J15.700','支原体肺炎',1,'呼吸系统',0,230,1,0,NULL,1,'admin','2026-05-08 00:00:00','admin','2026-05-08 00:00:00',0,NULL,1,1),
(2360000000000001026,'A15.000','肺结核',1,'呼吸系统',0,240,1,1,NULL,1,'admin','2026-05-08 00:00:00','admin','2026-05-08 00:00:00',0,'乙类传染病',1,1),
(2360000000000001027,'K29.700','慢性胃炎',1,'消化系统',0,250,1,0,NULL,1,'admin','2026-05-08 00:00:00','admin','2026-05-08 00:00:00',0,NULL,1,1),
(2360000000000001028,'K29.300','慢性浅表性胃炎',1,'消化系统',0,260,1,0,NULL,1,'admin','2026-05-08 00:00:00','admin','2026-05-08 00:00:00',0,NULL,1,1),
(2360000000000001029,'K25.700','胃溃疡',1,'消化系统',0,270,1,0,NULL,1,'admin','2026-05-08 00:00:00','admin','2026-05-08 00:00:00',0,NULL,1,1),
(2360000000000001030,'K26.700','十二指肠溃疡',1,'消化系统',0,280,1,0,NULL,1,'admin','2026-05-08 00:00:00','admin','2026-05-08 00:00:00',0,NULL,1,1),
(2360000000000001031,'K35.900','急性阑尾炎',1,'消化系统',0,290,1,0,NULL,1,'admin','2026-05-08 00:00:00','admin','2026-05-08 00:00:00',0,NULL,1,1),
(2360000000000001032,'K80.200','胆囊结石',1,'消化系统',0,300,1,0,NULL,1,'admin','2026-05-08 00:00:00','admin','2026-05-08 00:00:00',0,NULL,1,1),
(2360000000000001033,'K85.900','急性胰腺炎',1,'消化系统',0,310,1,0,'水肿型',1,'admin','2026-05-08 00:00:00','admin','2026-05-08 00:00:00',0,NULL,1,1),
(2360000000000001034,'K74.600','肝硬化',1,'消化系统',0,320,1,0,'代偿期',1,'admin','2026-05-08 00:00:00','admin','2026-05-08 00:00:00',0,NULL,1,1),
(2360000000000001035,'K52.904','急性胃肠炎',1,'消化系统',0,330,1,0,NULL,1,'admin','2026-05-08 00:00:00','admin','2026-05-08 00:00:00',0,NULL,1,1),
(2360000000000001036,'K59.900','功能性肠病',1,'消化系统',0,340,1,0,NULL,1,'admin','2026-05-08 00:00:00','admin','2026-05-08 00:00:00',0,NULL,1,1),
(2360000000000001037,'B18.100','慢性乙型病毒性肝炎',1,'消化系统',0,350,1,0,NULL,1,'admin','2026-05-08 00:00:00','admin','2026-05-08 00:00:00',0,'乙类传染病',1,1),
(2360000000000001038,'N18.900','慢性肾脏病',1,'泌尿系统',0,360,1,0,'3期',1,'admin','2026-05-08 00:00:00','admin','2026-05-08 00:00:00',0,NULL,1,1),
(2360000000000001039,'N39.000','尿路感染',1,'泌尿系统',0,370,1,0,NULL,1,'admin','2026-05-08 00:00:00','admin','2026-05-08 00:00:00',0,NULL,1,1),
(2360000000000001040,'N20.000','肾结石',1,'泌尿系统',0,380,1,0,NULL,1,'admin','2026-05-08 00:00:00','admin','2026-05-08 00:00:00',0,NULL,1,1),
(2360000000000001041,'N04.900','肾病综合征',1,'泌尿系统',0,390,1,0,NULL,1,'admin','2026-05-08 00:00:00','admin','2026-05-08 00:00:00',0,NULL,1,1),
(2360000000000001042,'D64.900','贫血',1,'血液系统',0,400,1,0,NULL,1,'admin','2026-05-08 00:00:00','admin','2026-05-08 00:00:00',0,NULL,1,1),
(2360000000000001043,'D50.900','缺铁性贫血',1,'血液系统',0,410,1,0,NULL,1,'admin','2026-05-08 00:00:00','admin','2026-05-08 00:00:00',0,NULL,1,1),
(2360000000000001044,'D69.900','血小板减少症',1,'血液系统',0,420,1,0,NULL,1,'admin','2026-05-08 00:00:00','admin','2026-05-08 00:00:00',0,NULL,1,1),
(2360000000000001045,'D70.900','白细胞减少症',1,'血液系统',0,430,1,0,NULL,1,'admin','2026-05-08 00:00:00','admin','2026-05-08 00:00:00',0,NULL,1,1),
(2360000000000001046,'M54.500','腰痛',1,'骨骼肌肉系统',0,440,1,0,NULL,1,'admin','2026-05-08 00:00:00','admin','2026-05-08 00:00:00',0,NULL,1,1),
(2360000000000001047,'M17.900','膝骨关节病',1,'骨骼肌肉系统',0,450,1,0,NULL,1,'admin','2026-05-08 00:00:00','admin','2026-05-08 00:00:00',0,NULL,1,1),
(2360000000000001048,'M51.202','腰椎间盘突出症',1,'骨骼肌肉系统',0,460,1,0,NULL,1,'admin','2026-05-08 00:00:00','admin','2026-05-08 00:00:00',0,NULL,1,1),
(2360000000000001049,'M10.900','痛风',1,'骨骼肌肉系统',0,470,1,0,NULL,1,'admin','2026-05-08 00:00:00','admin','2026-05-08 00:00:00',0,NULL,1,1),
(2360000000000001050,'M81.900','骨质疏松',1,'骨骼肌肉系统',0,480,1,0,NULL,1,'admin','2026-05-08 00:00:00','admin','2026-05-08 00:00:00',0,NULL,1,1),
(2360000000000001051,'M79.100','肌痛',1,'骨骼肌肉系统',0,485,1,0,NULL,1,'admin','2026-05-08 00:00:00','admin','2026-05-08 00:00:00',0,NULL,1,1),
(2360000000000001052,'S72.001','股骨颈骨折',1,'骨骼肌肉系统',0,490,1,0,NULL,1,'admin','2026-05-08 00:00:00','admin','2026-05-08 00:00:00',0,NULL,1,1),
(2360000000000001053,'S82.201','胫骨干骨折',1,'骨骼肌肉系统',0,495,0,0,NULL,1,'admin','2026-05-08 00:00:00','admin','2026-05-08 00:00:00',0,NULL,1,1),
(2360000000000001054,'L50.900','荨麻疹',1,'皮肤与附属器',0,500,1,0,NULL,1,'admin','2026-05-08 00:00:00','admin','2026-05-08 00:00:00',0,NULL,1,1),
(2360000000000001055,'L20.900','特应性皮炎',1,'皮肤与附属器',0,510,1,0,NULL,1,'admin','2026-05-08 00:00:00','admin','2026-05-08 00:00:00',0,NULL,1,1),
(2360000000000001056,'L03.900','蜂窝织炎',1,'皮肤与附属器',0,515,1,0,NULL,1,'admin','2026-05-08 00:00:00','admin','2026-05-08 00:00:00',0,NULL,1,1),
(2360000000000001057,'L21.900','脂溢性皮炎',1,'皮肤与附属器',0,517,0,0,NULL,1,'admin','2026-05-08 00:00:00','admin','2026-05-08 00:00:00',0,NULL,1,1),
(2360000000000001058,'H10.900','结膜炎',1,'眼与附器',0,520,1,0,NULL,1,'admin','2026-05-08 00:00:00','admin','2026-05-08 00:00:00',0,NULL,1,1),
(2360000000000001059,'H66.900','中耳炎',1,'耳与乳突',0,530,1,0,NULL,1,'admin','2026-05-08 00:00:00','admin','2026-05-08 00:00:00',0,NULL,1,1),
(2360000000000001060,'J02.900','急性咽炎',1,'呼吸系统',0,540,1,0,NULL,1,'admin','2026-05-08 00:00:00','admin','2026-05-08 00:00:00',0,NULL,1,1),
(2360000000000001061,'J03.900','急性扁桃体炎',1,'呼吸系统',0,550,1,0,NULL,1,'admin','2026-05-08 00:00:00','admin','2026-05-08 00:00:00',0,NULL,1,1),
(2360000000000001062,'J31.200','慢性鼻炎',1,'呼吸系统',0,555,1,0,NULL,1,'admin','2026-05-08 00:00:00','admin','2026-05-08 00:00:00',0,NULL,1,1),
(2360000000000001063,'K04.000','龋齿',1,'口腔',0,560,1,0,NULL,1,'admin','2026-05-08 00:00:00','admin','2026-05-08 00:00:00',0,NULL,1,1),
(2360000000000001064,'K05.100','慢性牙龈炎',1,'口腔',0,565,1,0,NULL,1,'admin','2026-05-08 00:00:00','admin','2026-05-08 00:00:00',0,NULL,1,1),
(2360000000000001065,'F32.900','抑郁发作',1,'精神与行为',0,570,1,0,NULL,1,'admin','2026-05-08 00:00:00','admin','2026-05-08 00:00:00',0,NULL,1,1),
(2360000000000001066,'F41.100','广泛性焦虑障碍',1,'精神与行为',0,580,1,0,NULL,1,'admin','2026-05-08 00:00:00','admin','2026-05-08 00:00:00',0,NULL,1,1),
(2360000000000001067,'G47.000','入睡和维持睡眠障碍',1,'神经系统',0,590,1,0,NULL,1,'admin','2026-05-08 00:00:00','admin','2026-05-08 00:00:00',0,NULL,1,1),
(2360000000000001068,'O82.000','单胎顺产头位分娩',1,'妊娠与产科',0,600,1,0,NULL,1,'admin','2026-05-08 00:00:00','admin','2026-05-08 00:00:00',0,NULL,1,1),
(2360000000000001069,'O24.900','妊娠期糖尿病',1,'妊娠与产科',0,610,1,0,NULL,1,'admin','2026-05-08 00:00:00','admin','2026-05-08 00:00:00',0,NULL,1,1),
(2360000000000001070,'O26.900','妊娠期其他情况',1,'妊娠与产科',0,615,0,0,NULL,1,'admin','2026-05-08 00:00:00','admin','2026-05-08 00:00:00',0,NULL,1,1),
(2360000000000001071,'P59.900','新生儿黄疸',1,'围产儿',0,620,1,0,NULL,1,'admin','2026-05-08 00:00:00','admin','2026-05-08 00:00:00',0,NULL,1,1),
(2360000000000001072,'P23.900','新生儿肺炎',1,'围产儿',0,630,1,0,NULL,1,'admin','2026-05-08 00:00:00','admin','2026-05-08 00:00:00',0,NULL,1,1),
(2360000000000001073,'J21.900','急性毛细支气管炎',1,'呼吸系统',0,635,1,0,NULL,1,'admin','2026-05-08 00:00:00','admin','2026-05-08 00:00:00',0,NULL,1,1),
(2360000000000001074,'A09.900','感染性腹泻',1,'消化系统',0,640,1,0,NULL,1,'admin','2026-05-08 00:00:00','admin','2026-05-08 00:00:00',0,NULL,1,1),
(2360000000000001075,'R50.900','发热待查',1,'症状与体征',0,650,1,0,NULL,1,'admin','2026-05-08 00:00:00','admin','2026-05-08 00:00:00',0,NULL,1,1),
(2360000000000001076,'R10.400','其他和未明确的腹痛',1,'症状与体征',0,660,1,0,NULL,1,'admin','2026-05-08 00:00:00','admin','2026-05-08 00:00:00',0,NULL,1,1),
(2360000000000001077,'R51.x00','头痛',1,'症状与体征',0,670,1,0,NULL,1,'admin','2026-05-08 00:00:00','admin','2026-05-08 00:00:00',0,NULL,1,1),
(2360000000000001078,'R05.x00','咳嗽',1,'症状与体征',0,680,1,0,NULL,1,'admin','2026-05-08 00:00:00','admin','2026-05-08 00:00:00',0,NULL,1,1),
(2360000000000001079,'C34.900','支气管和肺恶性肿瘤',1,'肿瘤',0,690,1,0,NULL,1,'admin','2026-05-08 00:00:00','admin','2026-05-08 00:00:00',0,NULL,1,1),
(2360000000000001080,'C50.900','乳房恶性肿瘤',1,'肿瘤',0,700,1,0,NULL,1,'admin','2026-05-08 00:00:00','admin','2026-05-08 00:00:00',0,NULL,1,1),
(2360000000000001081,'C73.900','甲状腺恶性肿瘤',1,'肿瘤',0,710,1,0,NULL,1,'admin','2026-05-08 00:00:00','admin','2026-05-08 00:00:00',0,NULL,1,1),
(2360000000000001082,'C16.900','胃恶性肿瘤',1,'肿瘤',0,720,1,0,NULL,1,'admin','2026-05-08 00:00:00','admin','2026-05-08 00:00:00',0,NULL,1,1);

-- ---------- 段2 sys_price_change_history 项目调价史（12 条，item_type 大写白名单） ----------
INSERT INTO sys_price_change_history (id, item_type, item_id, item_code, item_name, old_price, new_price, change_reason, operator_id, operator_name, change_time, create_by, create_time, update_by, update_time, create_by_id, update_by_id)
SELECT 2360000000000002001,'TREATMENT', t.id, t.item_code, t.item_name, ROUND(t.price*0.9,2), t.price, '2026年度医疗服务价格调整', 1, 'admin', '2026-04-01 09:00:00', 'admin', '2026-05-08 00:00:00', 'admin', '2026-05-08 00:00:00', 1, 1 FROM sys_treatment_item t WHERE t.item_code='TR001';
INSERT INTO sys_price_change_history (id, item_type, item_id, item_code, item_name, old_price, new_price, change_reason, operator_id, operator_name, change_time, create_by, create_time, update_by, update_time, create_by_id, update_by_id)
SELECT 2360000000000002002,'TREATMENT', t.id, t.item_code, t.item_name, ROUND(t.price*0.9,2), t.price, '2026年度医疗服务价格调整', 1, 'admin', '2026-04-01 09:00:00', 'admin', '2026-05-08 00:00:00', 'admin', '2026-05-08 00:00:00', 1, 1 FROM sys_treatment_item t WHERE t.item_code='TR002';
INSERT INTO sys_price_change_history (id, item_type, item_id, item_code, item_name, old_price, new_price, change_reason, operator_id, operator_name, change_time, create_by, create_time, update_by, update_time, create_by_id, update_by_id)
SELECT 2360000000000002003,'LABORATORY', i.id, i.item_code, i.item_name, ROUND(i.price*0.85,2), i.price, '检验试剂集采降价传导', 1, 'admin', '2026-04-15 14:00:00', 'admin', '2026-05-08 00:00:00', 'admin', '2026-05-08 00:00:00', 1, 1 FROM sys_laboratory_item i WHERE i.item_code='LB001';
INSERT INTO sys_price_change_history (id, item_type, item_id, item_code, item_name, old_price, new_price, change_reason, operator_id, operator_name, change_time, create_by, create_time, update_by, update_time, create_by_id, update_by_id)
SELECT 2360000000000002004,'LABORATORY', i.id, i.item_code, i.item_name, ROUND(i.price*0.85,2), i.price, '检验试剂集采降价传导', 1, 'admin', '2026-04-15 14:00:00', 'admin', '2026-05-08 00:00:00', 'admin', '2026-05-08 00:00:00', 1, 1 FROM sys_laboratory_item i WHERE i.item_code='LB018';
INSERT INTO sys_price_change_history (id, item_type, item_id, item_code, item_name, old_price, new_price, change_reason, operator_id, operator_name, change_time, create_by, create_time, update_by, update_time, create_by_id, update_by_id)
SELECT 2360000000000002005,'LABORATORY', i.id, i.item_code, i.item_name, ROUND(i.price*0.9,2), i.price, '省物价目录更新', 1, 'admin', '2026-04-15 14:00:00', 'admin', '2026-05-08 00:00:00', 'admin', '2026-05-08 00:00:00', 1, 1 FROM sys_laboratory_item i WHERE i.item_code='LB020';
INSERT INTO sys_price_change_history (id, item_type, item_id, item_code, item_name, old_price, new_price, change_reason, operator_id, operator_name, change_time, create_by, create_time, update_by, update_time, create_by_id, update_by_id)
SELECT 2360000000000002006,'INSPECTION', x.id, x.item_code, x.item_name, ROUND(x.price*0.92,2), x.price, '大型设备检查价格下调', 1, 'admin', '2026-04-20 10:00:00', 'admin', '2026-05-08 00:00:00', 'admin', '2026-05-08 00:00:00', 1, 1 FROM sys_inspection_item x WHERE x.item_code='CT002';
INSERT INTO sys_price_change_history (id, item_type, item_id, item_code, item_name, old_price, new_price, change_reason, operator_id, operator_name, change_time, create_by, create_time, update_by, update_time, create_by_id, update_by_id)
SELECT 2360000000000002007,'INSPECTION', x.id, x.item_code, x.item_name, ROUND(x.price*0.92,2), x.price, '大型设备检查价格下调', 1, 'admin', '2026-04-20 10:00:00', 'admin', '2026-05-08 00:00:00', 'admin', '2026-05-08 00:00:00', 1, 1 FROM sys_inspection_item x WHERE x.item_code='CT001';
INSERT INTO sys_price_change_history (id, item_type, item_id, item_code, item_name, old_price, new_price, change_reason, operator_id, operator_name, change_time, create_by, create_time, update_by, update_time, create_by_id, update_by_id)
SELECT 2360000000000002008,'INSPECTION', x.id, x.item_code, x.item_name, ROUND(x.price*0.95,2), x.price, '大型设备检查价格下调', 1, 'admin', '2026-04-20 10:00:00', 'admin', '2026-05-08 00:00:00', 'admin', '2026-05-08 00:00:00', 1, 1 FROM sys_inspection_item x WHERE x.item_code='MR001';
INSERT INTO sys_price_change_history (id, item_type, item_id, item_code, item_name, old_price, new_price, change_reason, operator_id, operator_name, change_time, create_by, create_time, update_by, update_time, create_by_id, update_by_id)
SELECT 2360000000000002009,'CONSUMABLE', c.id, c.consumable_code, c.consumable_name, ROUND(c.retail_price*1.05,2), c.retail_price, '耗材挂网价联动调整', 1, 'admin', '2026-03-10 11:00:00', 'admin', '2026-05-08 00:00:00', 'admin', '2026-05-08 00:00:00', 1, 1 FROM sys_consumable c WHERE c.del_flag=0 ORDER BY c.id LIMIT 1;
INSERT INTO sys_price_change_history (id, item_type, item_id, item_code, item_name, old_price, new_price, change_reason, operator_id, operator_name, change_time, create_by, create_time, update_by, update_time, create_by_id, update_by_id)
SELECT 2360000000000002010,'CONSUMABLE', c.id, c.consumable_code, c.consumable_name, ROUND(c.retail_price*1.05,2), c.retail_price, '耗材挂网价联动调整', 1, 'admin', '2026-03-10 11:00:00', 'admin', '2026-05-08 00:00:00', 'admin', '2026-05-08 00:00:00', 1, 1 FROM sys_consumable c WHERE c.del_flag=0 ORDER BY c.id DESC LIMIT 1;
INSERT INTO sys_price_change_history (id, item_type, item_id, item_code, item_name, old_price, new_price, change_reason, operator_id, operator_name, change_time, create_by, create_time, update_by, update_time, create_by_id, update_by_id)
SELECT 2360000000000002011,'TREATMENT', t.id, t.item_code, t.item_name, ROUND(t.price*1.1,2), t.price, '护理类项目价格规范调整', 1, 'admin', '2026-03-01 09:00:00', 'admin', '2026-05-08 00:00:00', 'admin', '2026-05-08 00:00:00', 1, 1 FROM sys_treatment_item t WHERE t.item_code='TR003';
INSERT INTO sys_price_change_history (id, item_type, item_id, item_code, item_name, old_price, new_price, change_reason, operator_id, operator_name, change_time, create_by, create_time, update_by, update_time, create_by_id, update_by_id)
SELECT 2360000000000002012,'LABORATORY', i.id, i.item_code, i.item_name, ROUND(i.price*0.88,2), i.price, '检验试剂集采降价传导', 1, 'admin', '2026-04-15 14:00:00', 'admin', '2026-05-08 00:00:00', 'admin', '2026-05-08 00:00:00', 1, 1 FROM sys_laboratory_item i WHERE i.item_code='LB004';

-- ---------- 段3 sys_workbench_layout 管理员工作台个人布局（8 卡片） ----------
INSERT INTO sys_workbench_layout (id, user_id, widget_code, sort_order, visible, remark, create_by, create_time, update_by, update_time, del_flag, create_by_id, update_by_id)
VALUES
(2360000000000003001,1,'myTodo',10,1,'管理员默认布局（sql/236）','admin','2026-05-08 00:00:00','admin','2026-05-08 00:00:00',0,1,1),
(2360000000000003002,1,'quickEntry',20,1,NULL,'admin','2026-05-08 00:00:00','admin','2026-05-08 00:00:00',0,1,1),
(2360000000000003003,1,'hospitalToday',30,1,NULL,'admin','2026-05-08 00:00:00','admin','2026-05-08 00:00:00',0,1,1),
(2360000000000003004,1,'registToday',40,1,NULL,'admin','2026-05-08 00:00:00','admin','2026-05-08 00:00:00',0,1,1),
(2360000000000003005,1,'deptVisitRank',50,1,NULL,'admin','2026-05-08 00:00:00','admin','2026-05-08 00:00:00',0,1,1),
(2360000000000003006,1,'weekVisitTrend',60,1,NULL,'admin','2026-05-08 00:00:00','admin','2026-05-08 00:00:00',0,1,1),
(2360000000000003007,1,'chargeToday',70,1,NULL,'admin','2026-05-08 00:00:00','admin','2026-05-08 00:00:00',0,1,1),
(2360000000000003008,1,'insuranceSettle',80,0,'管理员端暂不关注医保结算，先隐藏','admin','2026-05-08 00:00:00','admin','2026-05-08 00:00:00',0,1,1);

-- ---------- 段4 sys_knowledge_doc / chunk 患者端知识库补全（8 篇 × 1 块） ----------
INSERT INTO sys_knowledge_doc (id, title, category, source_type, content, chunk_count, status, create_by, create_time, update_by, update_time, del_flag, remark, create_by_id, update_by_id)
VALUES
(2360000000000004001,'住院须知','住院服务',1,'# 住院须知\n\n办理入院：持门诊/急诊医生开具的入院通知单，到住院部一楼入院处办理登记、缴纳预交金，领取腕带与床头卡。请携带身份证、医保凭证及既往检查资料。\n\n病区管理：住院期间请佩戴腕带，不得擅自离开病区；外出检查须告知责任护士。贵重物品请自行妥善保管或交家属带回。\n\n陪护与探视：每位患者原则上留陪护 1 人，探视时间为每日 15:00—20:00，ICU 病区按科室规定执行。\n\n饮食：医生开具膳食医嘱后由营养食堂统一配送；自带食物请先咨询医生或护士。\n\n出院办理：医生下达出院医嘱后，持出院记录到入院处结算费用、打印清单，凭结算单领取出院带药。',1,0,'admin','2026-05-08 00:00:00','admin','2026-05-08 00:00:00',0,'铺底（sql/236）',1,1),
(2360000000000004002,'医保报销指南','医保服务',1,'# 医保报销指南\n\n参保类型：城镇职工医保、城乡居民医保、公费医疗。就诊时请主动出示医保电子凭证或社保卡。\n\n门诊报销：普通门诊统筹按比例报销，乙类药品先自付一定比例再纳入统筹；门诊慢特病需先在医保经办机构备案。\n\n住院报销：起付线以上、封顶线以内按政策比例报销，出院时在医院医保窗口直接结算（异地就医需提前备案）。\n\n不予报销范围：非疾病治疗项目（美容、矫形）、自费药品、第三方责任等。\n\n报销材料：发票原件、费用清单、出院记录、诊断证明。商业保险理赔请到病案室复印病历并盖章。',1,0,'admin','2026-05-08 00:00:00','admin','2026-05-08 00:00:00',0,'铺底（sql/236）',1,1),
(2360000000000004003,'门诊慢特病待遇办理','医保服务',1,'# 门诊慢特病待遇办理\n\n适用病种：高血压、糖尿病、冠心病、脑血管病后遗症、慢性肾功能不全（透析）、恶性肿瘤门诊治疗、系统性红斑狼疮等。\n\n办理材料：二级及以上医院的出院记录或门诊诊断证明、相关检查检验报告、身份证与社保卡复印件。\n\n办理流程：主管医师填写《门诊慢特病待遇认定申请表》→ 医保办审核盖章 → 参保地医保经办机构评审 → 通过后自次月起享受门诊慢特病待遇。\n\n待遇标准：认定病种范围内的门诊费用按住院或门诊特定比例报销，具体比例以参保地政策为准。',1,0,'admin','2026-05-08 00:00:00','admin','2026-05-08 00:00:00',0,'铺底（sql/236）',1,1),
(2360000000000004004,'体检须知','健康体检',1,'# 体检须知\n\n预约：通过公众号「健康体检」或电话预约，团体体检由单位统一安排。\n\n准备：体检前 3 天清淡饮食、忌酒；前 1 天晚 20:00 后禁食，保证充足睡眠。女性经期请避开尿常规、妇科检查；备孕或怀孕者禁做放射类项目。\n\n流程：体检当日晨起禁食禁水，携身份证到体检中心前台报到，按指引单顺序完成各科检查，空腹项目（采血、腹部彩超）优先。\n\n报告：一般 3—5 个工作日出报告，可在公众号查询电子报告，异常指标建议按报告提示专科门诊复查。',1,0,'admin','2026-05-08 00:00:00','admin','2026-05-08 00:00:00',0,'铺底（sql/236）',1,1),
(2360000000000004005,'输液与注射注意事项','门诊治疗',1,'# 输液与注射注意事项\n\n输液前请告知医护人员：过敏史、是否正在使用抗凝药物、今日是否已进食（某些药物需餐后使用）。\n\n输液中若出现心慌、胸闷、皮疹、寒战等不适，请立即按床头呼叫铃，切勿自行调节滴速。\n\n皮试类药物（如青霉素类）需在治疗区观察 20 分钟无异常方可离院；72 小时内需连续使用同一批号药物请保留皮试记录。\n\n输液结束后按压针眼 5 分钟（勿揉），24 小时内避免针眼沾水。拔针后如出现局部肿胀，可用冷敷，24 小时后改热敷。',1,0,'admin','2026-05-08 00:00:00','admin','2026-05-08 00:00:00',0,'铺底（sql/236）',1,1),
(2360000000000004006,'检验报告如何查询','检验检查',1,'# 检验报告如何查询\n\n电子报告：关注医院公众号，绑定就诊卡后在「报告查询」中查看检验与检查报告，一般采样后 30 分钟至 24 小时内出具。\n\n纸质报告：凭就诊卡到门诊大厅自助机打印，检验报告保留 30 天，请及时领取。\n\n危急值：检验发现危急值时会第一时间电话通知开单医生并同步病区，请保持登记电话畅通。\n\n报告解读：患者端报告页对常见指标提供白话解释，仅供参考；具体诊疗请以医生面诊意见为准。',1,0,'admin','2026-05-08 00:00:00','admin','2026-05-08 00:00:00',0,'铺底（sql/236）',1,1),
(2360000000000004007,'投诉与建议渠道','服务保障',1,'# 投诉与建议渠道\n\n院内投诉：门诊一楼服务台、住院部一楼客服中心均可现场受理；工作时间 08:00—17:30。\n\n电话渠道：客服热线工作日 08:00—17:30 人工接听，其余时间语音留言，1 个工作日内回访。\n\n线上渠道：小程序「意见反馈」提交，可上传图片，客服将在 1 个工作日内回复处理进度。\n\n处理时限：一般投诉 3 个工作日内答复；涉及医疗质量的投诉转医务部专项处理，15 个工作日内答复。所有投诉信息严格保密。',1,0,'admin','2026-05-08 00:00:00','admin','2026-05-08 00:00:00',0,'铺底（sql/236）',1,1),
(2360000000000004008,'出生医学证明办理','妇幼服务',1,'# 出生医学证明办理\n\n办理时间：新生儿分娩后即可申请，建议在出院前完成信息确认。\n\n所需材料：父母双方有效身份证原件及复印件、《出生医学证明首次签发登记表》（分娩机构领取填写）。\n\n办理地点：住院部三楼出生证明办理窗口，工作日 08:30—12:00、14:00—17:00。\n\n注意事项：新生儿姓名须用规范汉字，一经签发不可更改涂改；如信息有误需换发，请携带原证及父母身份证件办理。补办正、副页遗失的按签发机构补发流程办理。',1,0,'admin','2026-05-08 00:00:00','admin','2026-05-08 00:00:00',0,'铺底（sql/236）',1,1);
INSERT INTO sys_knowledge_chunk (id, doc_id, doc_title, category, chunk_index, content, create_by, create_time, update_by, update_time, del_flag, remark, create_by_id, update_by_id)
SELECT d.id+50, d.id, d.title, d.category, 0, d.content, 'admin', '2026-05-08 00:00:00', 'admin', '2026-05-08 00:00:00', 0, '铺底（sql/236）', 1, 1
FROM sys_knowledge_doc d WHERE d.id BETWEEN 2360000000000004001 AND 2360000000000004008;
UPDATE sys_knowledge_doc SET chunk_count=1 WHERE id BETWEEN 2360000000000004001 AND 2360000000000004008;

-- ---------- 段5 sys_alert_rule 预警规则（rule_id 6-25，20 条） ----------
INSERT INTO sys_alert_rule (rule_id, rule_name, rule_type, rule_condition, threshold, notify_channel, is_active, remark, create_by, create_time, update_by, update_time, create_by_id, update_by_id)
VALUES
(6,'检验危急值-血钾','critical_value','result_value OUT OF (2.5,6.5) mmol/L',NULL,'system+sms','1','血钾危急值即时通知开单医生与病区','admin','2026-05-08 00:00:00','admin','2026-05-08 00:00:00',1,1),
(7,'检验危急值-血糖','critical_value','result_value OUT OF (2.8,22.2) mmol/L',NULL,'system+sms','1','血糖危急值','admin','2026-05-08 00:00:00','admin','2026-05-08 00:00:00',1,1),
(8,'检验危急值-血小板','critical_value','result_value < 30 ×10^9/L',30,'system+sms','1','血小板危急值','admin','2026-05-08 00:00:00','admin','2026-05-08 00:00:00',1,1),
(9,'检验危急值-血红蛋白','critical_value','result_value < 50 g/L',50,'system+sms','1','重度贫血预警','admin','2026-05-08 00:00:00','admin','2026-05-08 00:00:00',1,1),
(10,'住院欠费预警','arrears','prepay_balance - daily_cost < ?',1000,'system','1','余额低于1000元提醒护士站催缴','admin','2026-05-08 00:00:00','admin','2026-05-08 00:00:00',1,1),
(11,'门诊候诊超时','queue_wait','waiting_time > ? MINUTE',30,'system','1','候诊超过30分钟提醒分诊台疏导','admin','2026-05-08 00:00:00','admin','2026-05-08 00:00:00',1,1),
(12,'药品近效期','expiry','expiry_date <= NOW() + INTERVAL ? DAY',90,'system','1','效期90天盘点推送','admin','2026-05-08 00:00:00','admin','2026-05-08 00:00:00',1,1),
(13,'药品呆滞库存','slow_moving','last_dispense_date < NOW() - INTERVAL ? DAY',180,'system','1','180天未动销提示','admin','2026-05-08 00:00:00','admin','2026-05-08 00:00:00',1,1),
(14,'耗材库存下限','low_stock','quantity <= safety_stock + ?',0,'system','1','触及安全库存提示补货','admin','2026-05-08 00:00:00','admin','2026-05-08 00:00:00',1,1),
(15,'号源紧张','slot_shortage','remain_slots / total_slots < 0.1',10,'system','1','号源余量不足10%提醒加号评估','admin','2026-05-08 00:00:00','admin','2026-05-08 00:00:00',1,1),
(16,'退号异常','refund_anomaly','refund_count_today > ? per_account',5,'system+audit','1','单账号当日退号超5次风控','admin','2026-05-08 00:00:00','admin','2026-05-08 00:00:00',1,1),
(17,'设备状态离线','device_offline','heartbeat_gap > ? MINUTE',30,'system','1','检查设备心跳超时提醒设备科','admin','2026-05-08 00:00:00','admin','2026-05-08 00:00:00',1,1),
(18,'危急值处理超时','critical_timeout','ack_elapsed > ? MINUTE',15,'system+sms','1','危急值15分钟未确认升级上报','admin','2026-05-08 00:00:00','admin','2026-05-08 00:00:00',1,1),
(19,'抗菌药使用强度','antibiotic_intensity','ddds_per_100_bed_days > ?',40,'system','1','抗菌药使用强度超40DDDs提醒药学部','admin','2026-05-08 00:00:00','admin','2026-05-08 00:00:00',1,1),
(20,'I类切口预防用药超时长','incision_prophylaxis','prophylaxis_duration > ? HOUR',24,'system','1','I类切口预防用药超24小时点评','admin','2026-05-08 00:00:00','admin','2026-05-08 00:00:00',1,1),
(21,'住院超30天','long_stay','length_of_stay > ? DAY',30,'system','1','超长住院病例提醒科主任评估','admin','2026-05-08 00:00:00','admin','2026-05-08 00:00:00',1,1),
(22,'再入院预警','readmission','readmit_within_days <= ?',31,'system','1','31天内非计划再入院提醒质控','admin','2026-05-08 00:00:00','admin','2026-05-08 00:00:00',1,1),
(23,'医保拒付预警','insurance_reject','reject_amount_month > ? YUAN',50000,'system','1','月度医保拒付超5万元提醒医保办','admin','2026-05-08 00:00:00','admin','2026-05-08 00:00:00',1,1),
(24,'手术间超时占用','or_overtime','scheduled_duration_actual_ratio > 1.3',130,'system','1','手术超时30%提醒手术室调度','admin','2026-05-08 00:00:00','admin','2026-05-08 00:00:00',1,1),
(25,'输血反应上报','transfusion_reaction','reaction_reported = 1',NULL,'system+sms','1','输血反应即时通知输血科与临床','admin','2026-05-08 00:00:00','admin','2026-05-08 00:00:00',1,1);

-- ---------- 段6 sys_insurance_policy 医保政策（id 7-16） ----------
INSERT INTO sys_insurance_policy (id, policy_name, insurance_type, settlement_type, coverage_ratio, self_pay_ratio, status, create_time, update_time, remark, create_by, update_by, create_by_id, update_by_id)
VALUES
(7,'城乡居民医保-成年居民','城乡居民',3,65.00,15.00,1,'2026-05-08 00:00:00','2026-05-08 00:00:00','居民医保成年普通参保','admin','admin',1,1),
(8,'城乡居民医保-学生儿童','城乡居民',3,75.00,10.00,1,'2026-05-08 00:00:00','2026-05-08 00:00:00','学生儿童档倾斜保障','admin','admin',1,1),
(9,'城乡居民医保-大病保险','城乡居民',3,70.00,10.00,1,'2026-05-08 00:00:00','2026-05-08 00:00:00','基本医保封顶后大病二次报销','admin','admin',1,1),
(10,'城镇职工医保-门诊统筹','在职职工',2,60.00,10.00,1,'2026-05-08 00:00:00','2026-05-08 00:00:00','职工普通门诊统筹','admin','admin',1,1),
(11,'城镇职工医保-住院统筹','在职职工',2,88.00,10.00,1,'2026-05-08 00:00:00','2026-05-08 00:00:00','职工住院统筹比例','admin','admin',1,1),
(12,'公费医疗-在职','公费医疗',4,90.00,5.00,1,'2026-05-08 00:00:00','2026-05-08 00:00:00','公费医疗在职人员','admin','admin',1,1),
(13,'公费医疗-离退休','公费医疗',4,95.00,5.00,1,'2026-05-08 00:00:00','2026-05-08 00:00:00','公费医疗离退休人员','admin','admin',1,1),
(14,'异地就医-职工直接结算','在职职工',2,80.00,10.00,1,'2026-05-08 00:00:00','2026-05-08 00:00:00','已备案异地就医直接结算','admin','admin',1,1),
(15,'异地就医-居民直接结算','城乡居民',3,58.00,15.00,1,'2026-05-08 00:00:00','2026-05-08 00:00:00','已备案异地就医直接结算','admin','admin',1,1),
(16,'生育保险-产前检查','在职职工',2,85.00,0.00,1,'2026-05-08 00:00:00','2026-05-08 00:00:00','产前检查限额内按比例支付','admin','admin',1,1);

-- ---------- 段7 sys_drug_price_history 药品调价史（history_id 6-20） ----------
INSERT INTO sys_drug_price_history (history_id, drug_id, old_price, new_price, change_reason, operator_id, change_time, create_by, create_time, update_by, update_time, create_by_id, update_by_id)
SELECT 2360000000000005100 + ROW_NUMBER() OVER (ORDER BY d.drug_code), d.id, ROUND(d.price*1.08,2), d.price, '国家集采第八批落地执行', 1, '2026-04-01 00:00:00', 'admin', '2026-05-08 00:00:00', 'admin', '2026-05-08 00:00:00', 1, 1
FROM sys_drug d WHERE d.drug_code IN ('BP0001','BP0005','BP0011','BP0043','BP0051','BP0057','BP0061','BP0070','BP0093','BP0114','BP0127','BP0148','BP0164','BP0170','BP0175') AND d.del_flag=0;

-- ---------- 段8 sys_drug_interaction 相互作用库补全（20 条） ----------
INSERT IGNORE INTO sys_drug_interaction (id, component_a, component_b, pair_key, severity, interaction_desc, suggestion, status, create_by, create_time, update_by, update_time, del_flag, remark, create_by_id, update_by_id)
VALUES
(2360000000000006001,'地高辛','呋塞米','地高辛&呋塞米',2,'袢利尿剂致低钾血症，增加地高辛中毒风险','监测血钾与地高辛浓度，常规补钾或联用保钾利尿剂',1,'admin','2026-05-08 00:00:00','admin','2026-05-08 00:00:00',0,'铺底（sql/236）',1,1),
(2360000000000006002,'地高辛','胺碘酮','地高辛&胺碘酮',1,'胺碘酮抑制P-糖蛋白，地高辛血药浓度升高','地高辛减半起始，1周内复测浓度',1,'admin','2026-05-08 00:00:00','admin','2026-05-08 00:00:00',0,NULL,1,1),
(2360000000000006003,'阿托伐他汀','克拉霉素','阿托伐他汀&克拉霉素',1,'CYP3A4强抑制，横纹肌溶解风险升高','克拉霉素期间暂停他汀或换用瑞舒伐他汀',1,'admin','2026-05-08 00:00:00','admin','2026-05-08 00:00:00',0,NULL,1,1),
(2360000000000006004,'辛伐他汀','胺碘酮','辛伐他汀&胺碘酮',1,'辛伐他汀日剂量超20mg时肌病风险显著升高','限辛伐他汀≤20mg/日，监测CK',1,'admin','2026-05-08 00:00:00','admin','2026-05-08 00:00:00',0,NULL,1,1),
(2360000000000006005,'二甲双胍','碘对比剂','二甲双胍&碘对比剂',2,'对比剂肾病风险叠加乳酸酸中毒风险','增强检查当日停用，48小时后复查肾功能正常再恢复',1,'admin','2026-05-08 00:00:00','admin','2026-05-08 00:00:00',0,NULL,1,1),
(2360000000000006006,'依那普利','螺内酯','依那普利&螺内酯',2,'ACEI与保钾利尿剂叠加致高钾血症','基线及1周内复测血钾，避免再联用补钾剂',1,'admin','2026-05-08 00:00:00','admin','2026-05-08 00:00:00',0,NULL,1,1),
(2360000000000006007,'氯吡格雷','奥美拉唑','氯吡格雷&奥美拉唑',2,'CYP2C19抑制削弱氯吡格雷活化','换用泮托拉唑或雷贝拉唑',1,'admin','2026-05-08 00:00:00','admin','2026-05-08 00:00:00',0,NULL,1,1),
(2360000000000006008,'硝苯地平','利福平','硝苯地平&利福平',1,'利福平强诱导CYP3A4，降压作用显著减弱','换用不受诱导影响的降压药并加强血压监测',1,'admin','2026-05-08 00:00:00','admin','2026-05-08 00:00:00',0,NULL,1,1),
(2360000000000006009,'华法林','左氧氟沙星','华法林&左氧氟沙星',2,'肠道菌群破坏与CYP抑制，INR升高出血风险','联用期间3天复测INR，必要时减量',1,'admin','2026-05-08 00:00:00','admin','2026-05-08 00:00:00',0,NULL,1,1),
(2360000000000006010,'茶碱','环丙沙星','茶碱&环丙沙星',1,'环丙沙星抑制茶碱代谢致心悸、抽搐风险','茶碱减量1/3~1/2，有条件监测血药浓度',1,'admin','2026-05-08 00:00:00','admin','2026-05-08 00:00:00',0,NULL,1,1),
(2360000000000006011,'甲氨蝶呤','布洛芬','甲氨蝶呤&布洛芬',1,'NSAID减少甲氨蝶呤肾排泄，骨髓抑制风险','避免大剂量联用，监测血常规',1,'admin','2026-05-08 00:00:00','admin','2026-05-08 00:00:00',0,NULL,1,1),
(2360000000000006012,'碳酸锂','氢氯噻嗪','碳酸锂&氢氯噻嗪',1,'噻嗪类升锂浓度，锂中毒风险','锂剂量下调，规律监测血锂',1,'admin','2026-05-08 00:00:00','admin','2026-05-08 00:00:00',0,NULL,1,1),
(2360000000000006013,'西沙必利','红霉素','西沙必利&红霉素',1,'叠加QT延长，尖端扭转型室速风险','禁止联用，换用其他胃动力药',1,'admin','2026-05-08 00:00:00','admin','2026-05-08 00:00:00',0,NULL,1,1),
(2360000000000006014,'格列本脲','环丙沙星','格列本脲&环丙沙星',2,'氟喹诺酮增强磺脲类降糖，低血糖风险','加强血糖监测，告知低血糖识别与处理',1,'admin','2026-05-08 00:00:00','admin','2026-05-08 00:00:00',0,NULL,1,1),
(2360000000000006015,'缬沙坦','氯化钾缓释片','缬沙坦&氯化钾缓释片',2,'ARB类减少钾排泄，高钾血症风险','一般不常规补钾，联用须监测血钾',1,'admin','2026-05-08 00:00:00','admin','2026-05-08 00:00:00',0,NULL,1,1),
(2360000000000006016,'泼尼松','布洛芬','泼尼松&布洛芬',2,'糖皮质激素与NSAID叠加消化道溃疡出血风险','加用质子泵抑制剂保护胃黏膜',1,'admin','2026-05-08 00:00:00','admin','2026-05-08 00:00:00',0,NULL,1,1),
(2360000000000006017,'曲马多','舍曲林','曲马多&舍曲林',1,'5-HT综合征与癫痫阈值降低风险','避免联用，必要时减量并密切观察',1,'admin','2026-05-08 00:00:00','admin','2026-05-08 00:00:00',0,NULL,1,1),
(2360000000000006018,'环孢素','瑞舒伐他汀','环孢素&瑞舒伐他汀',1,'环孢素抑制他汀转运，肌病风险升高','瑞舒伐他汀≤10mg/日，监测CK',1,'admin','2026-05-08 00:00:00','admin','2026-05-08 00:00:00',0,NULL,1,1),
(2360000000000006019,'苯妥英钠','华法林','苯妥英钠&华法林',2,'苯妥英置换蛋白结合并诱导代谢，效应不稳','加密INR监测，稳定后固定剂量',1,'admin','2026-05-08 00:00:00','admin','2026-05-08 00:00:00',0,NULL,1,1),
(2360000000000006020,'头孢曲松','葡萄糖酸钙','头孢曲松&葡萄糖酸钙',1,'可形成头孢曲松钙沉淀，严禁同一输液通路','错开输注并用生理盐水冲管',1,'admin','2026-05-08 00:00:00','admin','2026-05-08 00:00:00',0,NULL,1,1);

-- ---------- 段9 sys_infectious_disease 法定传染病补全（FD016-039，按病名防重） ----------
INSERT INTO sys_infectious_disease (id, disease_code, disease_name, infectious_class, deadline_hours, icd10, status, create_by, create_time, update_by, update_time, del_flag, remark, create_by_id, update_by_id)
SELECT 620016,'FD016',t.nm,2,24,'B24',1,'admin','2026-05-08 00:00:00','admin','2026-05-08 00:00:00',0,'法定传染病目录补全（sql/236）',1,1 FROM (SELECT '艾滋病' AS nm) t WHERE NOT EXISTS (SELECT 1 FROM sys_infectious_disease WHERE disease_name='艾滋病');
INSERT INTO sys_infectious_disease (id, disease_code, disease_name, infectious_class, deadline_hours, icd10, status, create_by, create_time, update_by, update_time, del_flag, remark, create_by_id, update_by_id)
SELECT 620017,'FD017',t.nm,2,24,'B16',1,'admin','2026-05-08 00:00:00','admin','2026-05-08 00:00:00',0,'法定传染病目录补全（sql/236）',1,1 FROM (SELECT '病毒性肝炎' AS nm) t WHERE NOT EXISTS (SELECT 1 FROM sys_infectious_disease WHERE disease_name='病毒性肝炎');
INSERT INTO sys_infectious_disease (id, disease_code, disease_name, infectious_class, deadline_hours, icd10, status, create_by, create_time, update_by, update_time, del_flag, remark, create_by_id, update_by_id)
SELECT 620018,'FD018',t.nm,2,24,'B05',1,'admin','2026-05-08 00:00:00','admin','2026-05-08 00:00:00',0,'法定传染病目录补全（sql/236）',1,1 FROM (SELECT '麻疹' AS nm) t WHERE NOT EXISTS (SELECT 1 FROM sys_infectious_disease WHERE disease_name='麻疹');
INSERT INTO sys_infectious_disease (id, disease_code, disease_name, infectious_class, deadline_hours, icd10, status, create_by, create_time, update_by, update_time, del_flag, remark, create_by_id, update_by_id)
SELECT 620019,'FD019',t.nm,2,24,'B26',1,'admin','2026-05-08 00:00:00','admin','2026-05-08 00:00:00',0,'法定传染病目录补全（sql/236）',1,1 FROM (SELECT '流行性腮腺炎' AS nm) t WHERE NOT EXISTS (SELECT 1 FROM sys_infectious_disease WHERE disease_name='流行性腮腺炎');
INSERT INTO sys_infectious_disease (id, disease_code, disease_name, infectious_class, deadline_hours, icd10, status, create_by, create_time, update_by, update_time, del_flag, remark, create_by_id, update_by_id)
SELECT 620020,'FD020',t.nm,2,24,'B06',1,'admin','2026-05-08 00:00:00','admin','2026-05-08 00:00:00',0,'法定传染病目录补全（sql/236）',1,1 FROM (SELECT '风疹' AS nm) t WHERE NOT EXISTS (SELECT 1 FROM sys_infectious_disease WHERE disease_name='风疹');
INSERT INTO sys_infectious_disease (id, disease_code, disease_name, infectious_class, deadline_hours, icd10, status, create_by, create_time, update_by, update_time, del_flag, remark, create_by_id, update_by_id)
SELECT 620021,'FD021',t.nm,2,24,'B33.4',1,'admin','2026-05-08 00:00:00','admin','2026-05-08 00:00:00',0,'法定传染病目录补全（sql/236）',1,1 FROM (SELECT '登革热' AS nm) t WHERE NOT EXISTS (SELECT 1 FROM sys_infectious_disease WHERE disease_name='登革热');
INSERT INTO sys_infectious_disease (id, disease_code, disease_name, infectious_class, deadline_hours, icd10, status, create_by, create_time, update_by, update_time, del_flag, remark, create_by_id, update_by_id)
SELECT 620022,'FD022',t.nm,2,24,'A82',1,'admin','2026-05-08 00:00:00','admin','2026-05-08 00:00:00',0,'法定传染病目录补全（sql/236）',1,1 FROM (SELECT '狂犬病' AS nm) t WHERE NOT EXISTS (SELECT 1 FROM sys_infectious_disease WHERE disease_name='狂犬病');
INSERT INTO sys_infectious_disease (id, disease_code, disease_name, infectious_class, deadline_hours, icd10, status, create_by, create_time, update_by, update_time, del_flag, remark, create_by_id, update_by_id)
SELECT 620023,'FD023',t.nm,2,24,'A22',1,'admin','2026-05-08 00:00:00','admin','2026-05-08 00:00:00',0,'法定传染病目录补全（sql/236）',1,1 FROM (SELECT '炭疽' AS nm) t WHERE NOT EXISTS (SELECT 1 FROM sys_infectious_disease WHERE disease_name='炭疽');
INSERT INTO sys_infectious_disease (id, disease_code, disease_name, infectious_class, deadline_hours, icd10, status, create_by, create_time, update_by, update_time, del_flag, remark, create_by_id, update_by_id)
SELECT 620024,'FD024',t.nm,2,24,'A03',1,'admin','2026-05-08 00:00:00','admin','2026-05-08 00:00:00',0,'法定传染病目录补全（sql/236）',1,1 FROM (SELECT '细菌性痢疾' AS nm) t WHERE NOT EXISTS (SELECT 1 FROM sys_infectious_disease WHERE disease_name='细菌性痢疾');
INSERT INTO sys_infectious_disease (id, disease_code, disease_name, infectious_class, deadline_hours, icd10, status, create_by, create_time, update_by, update_time, del_flag, remark, create_by_id, update_by_id)
SELECT 620025,'FD025',t.nm,2,24,'A15.0',1,'admin','2026-05-08 00:00:00','admin','2026-05-08 00:00:00',0,'法定传染病目录补全（sql/236）',1,1 FROM (SELECT '肺结核' AS nm) t WHERE NOT EXISTS (SELECT 1 FROM sys_infectious_disease WHERE disease_name='肺结核');
INSERT INTO sys_infectious_disease (id, disease_code, disease_name, infectious_class, deadline_hours, icd10, status, create_by, create_time, update_by, update_time, del_flag, remark, create_by_id, update_by_id)
SELECT 620026,'FD026',t.nm,2,24,'A01',1,'admin','2026-05-08 00:00:00','admin','2026-05-08 00:00:00',0,'法定传染病目录补全（sql/236）',1,1 FROM (SELECT '伤寒和副伤寒' AS nm) t WHERE NOT EXISTS (SELECT 1 FROM sys_infectious_disease WHERE disease_name='伤寒和副伤寒');
INSERT INTO sys_infectious_disease (id, disease_code, disease_name, infectious_class, deadline_hours, icd10, status, create_by, create_time, update_by, update_time, del_flag, remark, create_by_id, update_by_id)
SELECT 620027,'FD027',t.nm,2,24,'A39',1,'admin','2026-05-08 00:00:00','admin','2026-05-08 00:00:00',0,'法定传染病目录补全（sql/236）',1,1 FROM (SELECT '流行性脑脊髓膜炎' AS nm) t WHERE NOT EXISTS (SELECT 1 FROM sys_infectious_disease WHERE disease_name='流行性脑脊髓膜炎');
INSERT INTO sys_infectious_disease (id, disease_code, disease_name, infectious_class, deadline_hours, icd10, status, create_by, create_time, update_by, update_time, del_flag, remark, create_by_id, update_by_id)
SELECT 620028,'FD028',t.nm,2,24,'A37',1,'admin','2026-05-08 00:00:00','admin','2026-05-08 00:00:00',0,'法定传染病目录补全（sql/236）',1,1 FROM (SELECT '百日咳' AS nm) t WHERE NOT EXISTS (SELECT 1 FROM sys_infectious_disease WHERE disease_name='百日咳');
INSERT INTO sys_infectious_disease (id, disease_code, disease_name, infectious_class, deadline_hours, icd10, status, create_by, create_time, update_by, update_time, del_flag, remark, create_by_id, update_by_id)
SELECT 620029,'FD029',t.nm,2,24,'A36',1,'admin','2026-05-08 00:00:00','admin','2026-05-08 00:00:00',0,'法定传染病目录补全（sql/236）',1,1 FROM (SELECT '白喉' AS nm) t WHERE NOT EXISTS (SELECT 1 FROM sys_infectious_disease WHERE disease_name='白喉');
INSERT INTO sys_infectious_disease (id, disease_code, disease_name, infectious_class, deadline_hours, icd10, status, create_by, create_time, update_by, update_time, del_flag, remark, create_by_id, update_by_id)
SELECT 620030,'FD030',t.nm,2,24,'A35',1,'admin','2026-05-08 00:00:00','admin','2026-05-08 00:00:00',0,'法定传染病目录补全（sql/236）',1,1 FROM (SELECT '新生儿破伤风' AS nm) t WHERE NOT EXISTS (SELECT 1 FROM sys_infectious_disease WHERE disease_name='新生儿破伤风');
INSERT INTO sys_infectious_disease (id, disease_code, disease_name, infectious_class, deadline_hours, icd10, status, create_by, create_time, update_by, update_time, del_flag, remark, create_by_id, update_by_id)
SELECT 620031,'FD031',t.nm,2,24,'A38',1,'admin','2026-05-08 00:00:00','admin','2026-05-08 00:00:00',0,'法定传染病目录补全（sql/236）',1,1 FROM (SELECT '猩红热' AS nm) t WHERE NOT EXISTS (SELECT 1 FROM sys_infectious_disease WHERE disease_name='猩红热');
INSERT INTO sys_infectious_disease (id, disease_code, disease_name, infectious_class, deadline_hours, icd10, status, create_by, create_time, update_by, update_time, del_flag, remark, create_by_id, update_by_id)
SELECT 620032,'FD032',t.nm,2,24,'A23',1,'admin','2026-05-08 00:00:00','admin','2026-05-08 00:00:00',0,'法定传染病目录补全（sql/236）',1,1 FROM (SELECT '布鲁氏菌病' AS nm) t WHERE NOT EXISTS (SELECT 1 FROM sys_infectious_disease WHERE disease_name='布鲁氏菌病');
INSERT INTO sys_infectious_disease (id, disease_code, disease_name, infectious_class, deadline_hours, icd10, status, create_by, create_time, update_by, update_time, del_flag, remark, create_by_id, update_by_id)
SELECT 620033,'FD033',t.nm,2,24,'A54',1,'admin','2026-05-08 00:00:00','admin','2026-05-08 00:00:00',0,'法定传染病目录补全（sql/236）',1,1 FROM (SELECT '淋病' AS nm) t WHERE NOT EXISTS (SELECT 1 FROM sys_infectious_disease WHERE disease_name='淋病');
INSERT INTO sys_infectious_disease (id, disease_code, disease_name, infectious_class, deadline_hours, icd10, status, create_by, create_time, update_by, update_time, del_flag, remark, create_by_id, update_by_id)
SELECT 620034,'FD034',t.nm,2,24,'A53',1,'admin','2026-05-08 00:00:00','admin','2026-05-08 00:00:00',0,'法定传染病目录补全（sql/236）',1,1 FROM (SELECT '梅毒' AS nm) t WHERE NOT EXISTS (SELECT 1 FROM sys_infectious_disease WHERE disease_name='梅毒');
INSERT INTO sys_infectious_disease (id, disease_code, disease_name, infectious_class, deadline_hours, icd10, status, create_by, create_time, update_by, update_time, del_flag, remark, create_by_id, update_by_id)
SELECT 620035,'FD035',t.nm,3,24,'J11',1,'admin','2026-05-08 00:00:00','admin','2026-05-08 00:00:00',0,'法定传染病目录补全（sql/236）',1,1 FROM (SELECT '流行性感冒' AS nm) t WHERE NOT EXISTS (SELECT 1 FROM sys_infectious_disease WHERE disease_name='流行性感冒');
INSERT INTO sys_infectious_disease (id, disease_code, disease_name, infectious_class, deadline_hours, icd10, status, create_by, create_time, update_by, update_time, del_flag, remark, create_by_id, update_by_id)
SELECT 620036,'FD036',t.nm,3,24,'A86',1,'admin','2026-05-08 00:00:00','admin','2026-05-08 00:00:00',0,'法定传染病目录补全（sql/236）',1,1 FROM (SELECT '流行性乙型脑炎' AS nm) t WHERE NOT EXISTS (SELECT 1 FROM sys_infectious_disease WHERE disease_name='流行性乙型脑炎');
INSERT INTO sys_infectious_disease (id, disease_code, disease_name, infectious_class, deadline_hours, icd10, status, create_by, create_time, update_by, update_time, del_flag, remark, create_by_id, update_by_id)
SELECT 620037,'FD037',t.nm,3,24,'B08.4',1,'admin','2026-05-08 00:00:00','admin','2026-05-08 00:00:00',0,'法定传染病目录补全（sql/236）',1,1 FROM (SELECT '手足口病' AS nm) t WHERE NOT EXISTS (SELECT 1 FROM sys_infectious_disease WHERE disease_name='手足口病');
INSERT INTO sys_infectious_disease (id, disease_code, disease_name, infectious_class, deadline_hours, icd10, status, create_by, create_time, update_by, update_time, del_flag, remark, create_by_id, update_by_id)
SELECT 620038,'FD038',t.nm,3,24,'A04.9',1,'admin','2026-05-08 00:00:00','admin','2026-05-08 00:00:00',0,'法定传染病目录补全（sql/236）',1,1 FROM (SELECT '其他感染性腹泻病' AS nm) t WHERE NOT EXISTS (SELECT 1 FROM sys_infectious_disease WHERE disease_name='其他感染性腹泻病');
INSERT INTO sys_infectious_disease (id, disease_code, disease_name, infectious_class, deadline_hours, icd10, status, create_by, create_time, update_by, update_time, del_flag, remark, create_by_id, update_by_id)
SELECT 620039,'FD039',t.nm,2,24,'B50',1,'admin','2026-05-08 00:00:00','admin','2026-05-08 00:00:00',0,'法定传染病目录补全（sql/236）',1,1 FROM (SELECT '疟疾' AS nm) t WHERE NOT EXISTS (SELECT 1 FROM sys_infectious_disease WHERE disease_name='疟疾');

-- ---------- 段10 sys_supplier 供应商补全（SUP006-015） ----------
INSERT INTO sys_supplier (supplier_id, supplier_code, supplier_name, contact_person, phone, address, license_no, license_expiry, rating, status, remark, create_by, create_time, update_by, update_time, del_flag, create_by_id, update_by_id)
VALUES
(2360000000000007001,'SUP006','华北制药股份有限公司','王经理','13811110001','河北省石家庄市和平东路388号','911301001077011X','2027-05-08',4,1,'原料药与抗生素供应商','admin','2026-05-08 00:00:00','admin','2026-05-08 00:00:00',0,1,1),
(2360000000000007002,'SUP007','扬子江药业集团有限公司','刘经理','13822220002','江苏省泰州市高港区扬子江路1号','913212001414212Y','2027-05-08',4,1,'口服制剂供应商','admin','2026-05-08 00:00:00','admin','2026-05-08 00:00:00',0,1,1),
(2360000000000007003,'SUP008','恒瑞医药股份有限公司','陈经理','13833330003','江苏省连云港市黄河路38号','913207001415013Z','2027-05-08',4,1,'肿瘤与麻醉线药品','admin','2026-05-08 00:00:00','admin','2026-05-08 00:00:00',0,1,1),
(2360000000000007004,'SUP009','齐鲁制药有限公司','赵经理','13844440004','山东省济南市高新区新泺大街317号','91370100163' ,'2027-05-08',4,1,'注射剂供应商','admin','2026-05-08 00:00:00','admin','2026-05-08 00:00:00',0,1,1),
(2360000000000007005,'SUP010','华东医药股份有限公司','孙经理','13855550005','浙江省杭州市江干区五星路229号','913301001435014W','2027-05-08',3,1,'慢病用药供应商','admin','2026-05-08 00:00:00','admin','2026-05-08 00:00:00',0,1,1),
(2360000000000007006,'SUP011','迈瑞医疗国际有限公司','周经理','13866660006','广东省深圳市南山区高新技术产业园','914403007152015Q','2027-05-08',4,1,'监护与影像设备','admin','2026-05-08 00:00:00','admin','2026-05-08 00:00:00',0,1,1),
(2360000000000007007,'SUP012','威高集团医用高分子有限公司','吴经理','13877770007','山东省威海市火炬高新技术产业区','913710001650016R','2027-05-08',4,1,'一次性耗材供应商','admin','2026-05-08 00:00:00','admin','2026-05-08 00:00:00',0,1,1),
(2360000000000007008,'SUP013','乐普(北京)医疗器械股份有限公司','郑经理','13888880008','北京市昌平区超前路37号','911100007109017T','2027-05-08',3,1,'心血管介入耗材','admin','2026-05-08 00:00:00','admin','2026-05-08 00:00:00',0,1,1),
(2360000000000007009,'SUP014','上海科华生物工程股份有限公司','冯经理','13899990009','上海市徐汇区钦州北路1189号','913100001322018U','2027-05-08',4,1,'体外诊断试剂','admin','2026-05-08 00:00:00','admin','2026-05-08 00:00:00',0,1,1),
(2360000000000007010,'SUP015','鱼跃医疗设备股份有限公司','何经理','13900000010','江苏省丹阳市开发区百胜路1号','913211811418019V','2027-05-08',3,1,'家用与康复设备','admin','2026-05-08 00:00:00','admin','2026-05-08 00:00:00',0,1,1);

-- ---------- 段11 sys_faq 患者端常见问题补全（16 条，FAQ20260508 段） ----------
INSERT INTO sys_faq (id, faq_no, category_code, category_name, question, answer, keywords, hot_flag, view_count, helpful_count, useless_count, status, sort_order, create_by, create_time, update_by, update_time, del_flag, remark, create_by_id, update_by_id)
VALUES
(2360000000000008001,'FAQ202605080001','INSURANCE','医保报销','门诊看病医保能报多少','不同参保类型和项目比例不同，甲类全额纳入统筹按比例报销，乙类需先自付一定比例。具体结算金额以收费窗口医保结算单为准，可携带医保凭证到一楼医保窗口咨询。','医保、报销比例、统筹、乙类、自付',1,0,0,0,1,201,'admin','2026-05-08 00:00:00','admin','2026-05-08 00:00:00',0,NULL,1,1),
(2360000000000008002,'FAQ202605080002','INSURANCE','医保报销','住院押金（预交金）可以退吗','出院结算后预交金多退少补，余额按原缴纳渠道退回，一般1—3个工作日到账，具体以支付渠道为准。','押金、预交金、退款、出院结算',0,0,0,0,1,202,'admin','2026-05-08 00:00:00','admin','2026-05-08 00:00:00',0,NULL,1,1),
(2360000000000008003,'FAQ202605080003','INSURANCE','医保报销','外地参保能在你们医院直接结算吗','已办理异地就医备案的参保人员可持医保电子凭证或社保卡在出院时直接结算。备案可通过参保地医保公众号或国家医保服务平台APP办理。','异地就医、备案、直接结算、外地医保',1,0,0,0,1,203,'admin','2026-05-08 00:00:00','admin','2026-05-08 00:00:00',0,NULL,1,1),
(2360000000000008004,'FAQ202605080004','CHECKUP','健康体检','体检报告多久能出','一般3—5个工作日出报告，公众号可查电子版；个别特殊项目（如病理）时间略长，报告完成后会有短信提醒。','体检报告、多久、查询、电子报告',1,0,0,0,1,204,'admin','2026-05-08 00:00:00','admin','2026-05-08 00:00:00',0,NULL,1,1),
(2360000000000008005,'FAQ202605080005','CHECKUP','健康体检','入职体检需要带什么','携带本人身份证，空腹前来（前一天晚20点后禁食）。如单位有指定体检套餐请告知前台，报告可按单位要求加盖体检专用章。','入职体检、空腹、身份证、套餐',0,0,0,0,1,205,'admin','2026-05-08 00:00:00','admin','2026-05-08 00:00:00',0,NULL,1,1),
(2360000000000008006,'FAQ202605080006','INPATIENT','住院服务','住院需要带哪些东西','身份证、医保凭证、既往病历和检查资料、生活用品。住院部提供热水与陪护床租借，贵重物品建议勿带入病房。','住院、准备、物品、陪护',0,0,0,0,1,206,'admin','2026-05-08 00:00:00','admin','2026-05-08 00:00:00',0,NULL,1,1),
(2360000000000008007,'FAQ202605080007','INPATIENT','住院服务','探视时间是什么时候','普通病区探视时间为每日15:00—20:00，每位患者同时段探视人数建议不超过2人；ICU按科室规定预约探视。','探视、时间、ICU、家属',0,0,0,0,1,207,'admin','2026-05-08 00:00:00','admin','2026-05-08 00:00:00',0,NULL,1,1),
(2360000000000008008,'FAQ202605080008','INPATIENT','住院服务','出院小结和病历复印件怎么开','出院小结在办理出院时随出院记录一并发放。病历复印请到病案室窗口申请，携带患者及代办人身份证，5个工作日后凭回执领取（可邮寄）。','病历复印、病案室、出院小结、证明',1,0,0,0,1,208,'admin','2026-05-08 00:00:00','admin','2026-05-08 00:00:00',0,NULL,1,1),
(2360000000000008009,'FAQ202605080009','BILLING','费用票据','电子发票怎么获取','缴费成功后可通过公众号「电子票据」栏目开具电子发票，发送至预留邮箱；纸质发票可在自助机或收费窗口补打。','电子发票、票据、开票、打印',1,0,0,0,1,209,'admin','2026-05-08 00:00:00','admin','2026-05-08 00:00:00',0,NULL,1,1),
(2360000000000008010,'FAQ202605080010','BILLING','费用票据','检查缴费后不想做了能退吗','未执行的检查检验项目可退费：请先到开单医生处开具退费单，再凭缴费凭证到一楼收费窗口办理；已执行项目原则上不退费。','退费、退检查、不想做了、取消',1,0,0,0,1,210,'admin','2026-05-08 00:00:00','admin','2026-05-08 00:00:00',0,NULL,1,1),
(2360000000000008011,'FAQ202605080011','SERVICE','便民服务','医院有停车场吗 收费标准如何','院内设地面与立体停车库，就诊车辆凭当日就诊凭证首小时免费，之后按当地物价标准计费，具体以入口公示牌为准。','停车、停车场、收费、车位',0,0,0,0,1,211,'admin','2026-05-08 00:00:00','admin','2026-05-08 00:00:00',0,NULL,1,1),
(2360000000000008012,'FAQ202605080012','SERVICE','便民服务','有轮椅和平车可以借吗','门诊一楼服务台凭有效证件免费借用轮椅，住院部各病区备有平车与轮椅，由护士站协助安排。','轮椅、平车、借用、行动不便',0,0,0,0,1,212,'admin','2026-05-08 00:00:00','admin','2026-05-08 00:00:00',0,NULL,1,1),
(2360000000000008013,'FAQ202605080013','SERVICE','便民服务','晚上和周末能看病吗','急诊24小时开放。门诊周末及节假日安排以公众号每周公告为准，部分专科开设夜间门诊，可在预约挂号中查看「夜间门诊」标签。','夜间门诊、周末、节假日、急诊',1,0,0,0,1,213,'admin','2026-05-08 00:00:00','admin','2026-05-08 00:00:00',0,NULL,1,1),
(2360000000000008014,'FAQ202605080014','CHRONIC','慢病管理','高血压糖尿病慢病卡怎么办','需二级及以上医院出具诊断证明与相关报告，由主管医师协助填写申请表，交医保办审核后报参保地经办机构评审，通过后享受门诊慢特病待遇。','慢病卡、慢特病、高血压、糖尿病、备案',1,0,0,0,1,214,'admin','2026-05-08 00:00:00','admin','2026-05-08 00:00:00',0,NULL,1,1),
(2360000000000008015,'FAQ202605080015','REPORT','检验检查','检查结果旁边的箭头是什么意思','箭头表示该指标高于或低于参考区间，提示需要关注，但单一指标波动不一定代表疾病。患者端报告页对常见指标有白话解释，最终解读请以医生意见为准。','箭头、偏高、偏低、参考区间、解读',1,0,0,0,1,215,'admin','2026-05-08 00:00:00','admin','2026-05-08 00:00:00',0,NULL,1,1),
(2360000000000008016,'FAQ202605080016','APPOINT','挂号预约','过号了还能看吗','取号后超过预约时段未签到的，系统自动顺延3位或按现场排队处理；如当日号源已关闭，可与分诊台协商安排补号或重新预约。','过号、迟到、顺延、重新排队',0,0,0,0,1,216,'admin','2026-05-08 00:00:00','admin','2026-05-08 00:00:00',0,NULL,1,1);

-- ---------- 段12 sys_treatment_item 治疗项目补全（TR101-115，理疗康复类） ----------
INSERT INTO sys_treatment_item (id, item_code, item_name, item_type, dept_id, price, duration, usage_method, status, create_by, create_time, update_by, update_time, del_flag, remark, create_by_id, update_by_id)
SELECT 2360000000000009000 + ROW_NUMBER() OVER (ORDER BY v.ord), v.code, v.nm, 5, (SELECT d.id FROM sys_department d WHERE d.dept_name LIKE '%康复%' AND d.del_flag=0 LIMIT 1), v.p, v.mins, v.usage_method, 1, 'admin', '2026-05-08 00:00:00', 'admin', '2026-05-08 00:00:00', 0, '铺底（sql/236）', 1, 1
FROM (SELECT 1 AS ord,'TR101' AS code,'普通针刺' AS nm,25.00 AS p,30 AS mins,'体表穴位针刺治疗' AS usage_method
UNION ALL SELECT 2,'TR102','艾灸治疗',20.00,20,'艾条悬灸'
UNION ALL SELECT 3,'TR103','拔罐治疗',18.00,15,'留罐法'
UNION ALL SELECT 4,'TR104','推拿治疗(颈肩)',45.00,30,'手法推拿松解'
UNION ALL SELECT 5,'TR105','推拿治疗(腰背)',50.00,30,'手法推拿松解'
UNION ALL SELECT 6,'TR106','中频脉冲电治疗',15.00,20,'电极片贴敷治疗'
UNION ALL SELECT 7,'TR107','红外线治疗',10.00,20,'局部照射'
UNION ALL SELECT 8,'TR108','蜡疗',30.00,30,'蜡饼外敷'
UNION ALL SELECT 9,'TR109','颈椎牵引',20.00,25,'坐位牵引'
UNION ALL SELECT 10,'TR110','腰椎牵引',25.00,25,'仰卧位牵引'
UNION ALL SELECT 11,'TR111','运动疗法(大关节)',35.00,40,'主动+被动关节活动训练'
UNION ALL SELECT 12,'TR112','作业疗法',35.00,40,'日常生活能力训练'
UNION ALL SELECT 13,'TR113','言语训练',40.00,30,'构音与表达训练'
UNION ALL SELECT 14,'TR114','吞咽功能障碍训练',40.00,30,'吞咽手法+电刺激'
UNION ALL SELECT 15,'TR115','平衡功能训练',35.00,30,'静态+动态平衡训练') v;

-- ---------- 段13 sys_imaging_plain_item 影像白话词典补全（15 条） ----------
INSERT IGNORE INTO sys_imaging_plain_item (id, group_name, item_name, plain_name, what_it_does, notice_text, status, sort_order, remark, create_by, create_time, update_by, update_time, del_flag, create_by_id, update_by_id)
VALUES
(2360000000000010001,'放射','头颅CT','头颅CT','对头部做断层扫描，常用于脑血管意外、外伤后颅内情况的快速评估','检查时保持头部不动，几秒钟即可完成','1',10,'铺底（sql/236）','admin','2026-05-08 00:00:00','admin','2026-05-08 00:00:00',0,1,1),
(2360000000000010002,'放射','腰椎MRI','腰椎磁共振','用磁场对腰椎做多维成像，对椎间盘、神经根显示清楚，无辐射','体内有起搏器、金属植入物者须提前告知；检查约15—20分钟，保持不动','1',20,NULL,'admin','2026-05-08 00:00:00','admin','2026-05-08 00:00:00',0,1,1),
(2360000000000010003,'放射','膝关节MRI','膝关节磁共振','观察膝关节软骨、半月板、韧带损伤的首选检查，无辐射','检查侧肢体保持伸直不动，扫描约10—15分钟','1',30,NULL,'admin','2026-05-08 00:00:00','admin','2026-05-08 00:00:00',0,1,1),
(2360000000000010004,'放射','颈椎DR','颈椎X光片','用X光观察颈椎序列与骨质情况，常用于颈部不适的初步筛查','摘除项链、耳环，按技师口令摆位','1',40,NULL,'admin','2026-05-08 00:00:00','admin','2026-05-08 00:00:00',0,1,1),
(2360000000000010005,'放射','腹部立位片','腹部X光(立位)','立位拍摄腹部平片，主要用于排查肠梗阻与消化道穿孔','需站立位拍摄，行动不便者请告知技师','1',50,NULL,'admin','2026-05-08 00:00:00','admin','2026-05-08 00:00:00',0,1,1),
(2360000000000010006,'超声','心脏彩超','心脏彩超(超声心动图)','用超声波观察心脏结构与瓣膜活动、测量心脏泵血功能','无需空腹，安静平卧即可，检查约10—15分钟','1',60,NULL,'admin','2026-05-08 00:00:00','admin','2026-05-08 00:00:00',0,1,1),
(2360000000000010007,'超声','乳腺彩超','乳腺彩超','检查乳腺有无结节、囊肿及结构异常，无辐射','检查上衣穿着方便解开即可，经期前后乳腺胀痛可能影响判断','1',70,NULL,'admin','2026-05-08 00:00:00','admin','2026-05-08 00:00:00',0,1,1),
(2360000000000010008,'超声','颈部血管彩超','颈部血管彩超','观察颈动脉内膜与血流，评估动脉粥样硬化斑块','无需特殊准备，颈部放松即可','1',80,NULL,'admin','2026-05-08 00:00:00','admin','2026-05-08 00:00:00',0,1,1),
(2360000000000010009,'超声','产科彩超','胎儿(产科)彩超','观察胎儿发育情况与羊水、胎盘状态，无辐射','孕周不同检查内容不同，请按预约时间到检','1',90,NULL,'admin','2026-05-08 00:00:00','admin','2026-05-08 00:00:00',0,1,1),
(2360000000000010010,'超声','下肢血管彩超','下肢血管彩超','检查下肢动静脉通畅情况与血栓，常用于腿肿、腿痛评估','无需特殊准备，暴露下肢即可','1',100,NULL,'admin','2026-05-08 00:00:00','admin','2026-05-08 00:00:00',0,1,1),
(2360000000000010011,'心电','24小时动态心电图','动态心电图(Holter)','随身携带记录仪连续记录24小时心电，捕捉阵发性心律失常','检查期间避免剧烈运动与沾水，记录不适症状的时间点','1',110,NULL,'admin','2026-05-08 00:00:00','admin','2026-05-08 00:00:00',0,1,1),
(2360000000000010012,'心电','动态血压监测','动态血压监测','随身血压计自动定时测量24小时血压，评估血压昼夜规律','袖带侧手臂避免剧烈活动，按设定时间自动测量即可','1',120,NULL,'admin','2026-05-08 00:00:00','admin','2026-05-08 00:00:00',0,1,1),
(2360000000000010013,'心电','常规心电图','心电图','记录静息状态下的心脏电活动，排查心律失常与心肌缺血','检查前静坐5分钟，四肢与胸前需贴电极','1',130,NULL,'admin','2026-05-08 00:00:00','admin','2026-05-08 00:00:00',0,1,1),
(2360000000000010014,'内镜','胃镜','胃镜(上消化道内镜)','经口进入观察食管、胃与十二指肠黏膜，可同时取活检','术前空腹6小时以上；无痛胃镜需家属陪同并评估麻醉','1',140,NULL,'admin','2026-05-08 00:00:00','admin','2026-05-08 00:00:00',0,1,1),
(2360000000000010015,'内镜','肠镜','结肠镜(下消化道内镜)','经肛门进入观察全结肠黏膜，是肠道病变筛查与活检的主要手段','按医嘱提前口服清肠药至排出清水样便；检查需家属陪同','1',150,NULL,'admin','2026-05-08 00:00:00','admin','2026-05-08 00:00:00',0,1,1);

-- ---------- 段14 sys_lab_plain_item 检验白话词典补全（20 条） ----------
INSERT IGNORE INTO sys_lab_plain_item (id, group_name, item_name, plain_name, what_is_it, high_text, low_text, status, sort_order, remark, create_by, create_time, update_by, update_time, del_flag, create_by_id, update_by_id)
VALUES
(2360000000000011001,'血常规','血红蛋白','血色素','反映血液携氧能力的指标','常见于脱水、长期缺氧，需结合红细胞数值判断','偏低提示贫血，常见原因有缺铁、慢性失血等','1',10,'铺底（sql/236）','admin','2026-05-08 00:00:00','admin','2026-05-08 00:00:00',0,1,1),
(2360000000000011002,'血常规','血小板','凝血细胞','参与止血和凝血的关键细胞','偏高可能见于感染、缺铁，持续升高需专科评估','偏低时出血风险增加，明显偏低需及时就诊','1',20,NULL,'admin','2026-05-08 00:00:00','admin','2026-05-08 00:00:00',0,1,1),
(2360000000000011003,'血常规','中性粒细胞比率','细菌感染指示','白细胞中应对细菌感染的主力比例','升高常见于急性细菌感染、应激状态','偏低可能见于病毒感染或某些药物影响','1',30,NULL,'admin','2026-05-08 00:00:00','admin','2026-05-08 00:00:00',0,1,1),
(2360000000000011004,'血常规','淋巴细胞比率','病毒感染指示','白细胞中应对病毒感染的主力比例','升高常见于病毒感染、部分慢性疾病','偏低多为一过性，结合白细胞总数判断','1',40,NULL,'admin','2026-05-08 00:00:00','admin','2026-05-08 00:00:00',0,1,1),
(2360000000000011005,'血常规','C反应蛋白','炎症指标','反映体内急性炎症反应程度的灵敏指标','明显升高常见于细菌感染、组织损伤','一般无临床意义，需结合其他指标','1',50,NULL,'admin','2026-05-08 00:00:00','admin','2026-05-08 00:00:00',0,1,1),
(2360000000000011006,'血糖','空腹血糖','空腹血糖','反映空腹状态下血液中葡萄糖浓度','偏高需警惕糖尿病，建议复查并查糖化血红蛋白','偏低可能为低血糖状态，伴心慌出汗需及时处理','1',60,NULL,'admin','2026-05-08 00:00:00','admin','2026-05-08 00:00:00',0,1,1),
(2360000000000011007,'血糖','糖化血红蛋白','近3个月血糖平均线','反映近2—3个月平均血糖水平的指标','偏高提示近阶段血糖控制不佳','偏低需警惕贫血等因素干扰检测结果','1',70,NULL,'admin','2026-05-08 00:00:00','admin','2026-05-08 00:00:00',0,1,1),
(2360000000000011008,'肝功能','谷丙转氨酶','肝细胞损伤指标','反映肝细胞受损程度的常用酶学指标','升高常见于脂肪肝、肝炎、饮酒或药物影响','轻度偏低一般无临床意义','1',80,NULL,'admin','2026-05-08 00:00:00','admin','2026-05-08 00:00:00',0,1,1),
(2360000000000011009,'肝功能','谷草转氨酶','肝心损伤指标','存在于肝脏与心肌中的酶，升高提示相关细胞损伤','需结合谷丙转氨酶与心脏指标综合判断','轻度偏低一般无临床意义','1',90,NULL,'admin','2026-05-08 00:00:00','admin','2026-05-08 00:00:00',0,1,1),
(2360000000000011010,'肝功能','总胆红素','黄疸指标','反映胆红素代谢与排泄是否正常','升高可见皮肤巩膜发黄，需区分肝细胞性或梗阻性','一般无特殊临床意义','1',100,NULL,'admin','2026-05-08 00:00:00','admin','2026-05-08 00:00:00',0,1,1),
(2360000000000011011,'肾功能','血肌酐','肾脏滤过指标','反映肾脏排泄代谢废物能力的核心指标','持续升高提示肾功能减退，需肾内科评估','一般无特殊临床意义，瘦体型者可偏低','1',110,NULL,'admin','2026-05-08 00:00:00','admin','2026-05-08 00:00:00',0,1,1),
(2360000000000011012,'肾功能','尿素氮','肾脏排泄指标','反映肾脏排泄与蛋白代谢状态','升高可见于肾功能减退、脱水、高蛋白饮食','偏低常见于低蛋白饮食、肝功能异常','1',120,NULL,'admin','2026-05-08 00:00:00','admin','2026-05-08 00:00:00',0,1,1),
(2360000000000011013,'肾功能','血尿酸','痛风指标','嘌呤代谢的最终产物，与痛风密切相关','升高需控制高嘌呤饮食，反复关节痛需规范降尿酸治疗','一般无特殊临床意义','1',130,NULL,'admin','2026-05-08 00:00:00','admin','2026-05-08 00:00:00',0,1,1),
(2360000000000011014,'血脂','总胆固醇','血脂总指标','血液中胆固醇的总浓度','偏高是动脉粥样硬化的危险因素，建议饮食运动干预','过低需结合营养状态评估','1',140,NULL,'admin','2026-05-08 00:00:00','admin','2026-05-08 00:00:00',0,1,1),
(2360000000000011015,'血脂','甘油三酯','脂肪指标','血液中甘油三酯浓度，受饮食影响大','明显升高有急性胰腺炎风险，需严格控酒控油','一般无特殊临床意义','1',150,NULL,'admin','2026-05-08 00:00:00','admin','2026-05-08 00:00:00',0,1,1),
(2360000000000011016,'血脂','低密度脂蛋白胆固醇','坏胆固醇','动脉粥样硬化最主要的危险指标','越高越易形成血管斑块，需按危险分层控制达标','过低一般无临床意义','1',160,NULL,'admin','2026-05-08 00:00:00','admin','2026-05-08 00:00:00',0,1,1),
(2360000000000011017,'血脂','高密度脂蛋白胆固醇','好胆固醇','对血管有保护作用的脂蛋白','一般无特殊临床意义','偏低提示心血管保护作用减弱','1',170,NULL,'admin','2026-05-08 00:00:00','admin','2026-05-08 00:00:00',0,1,1),
(2360000000000011018,'凝血','D-二聚体','血栓指标','反映体内血栓形成与溶解活动的敏感指标','明显升高需排查血栓、感染、术后状态等','一般无特殊临床意义','1',180,NULL,'admin','2026-05-08 00:00:00','admin','2026-05-08 00:00:00',0,1,1),
(2360000000000011019,'心肌','肌钙蛋白','心肌损伤指标','心肌细胞受损后释放入血的特异性蛋白','升高提示心肌损伤，结合心电图与症状综合判断','一般无特殊临床意义','1',190,NULL,'admin','2026-05-08 00:00:00','admin','2026-05-08 00:00:00',0,1,1),
(2360000000000011020,'尿常规','尿微量白蛋白','早期肾损伤指标','反映肾脏早期滤过损伤的敏感指标','升高常见于糖尿病、高血压肾损害早期，建议复查','一般无特殊临床意义','1',200,NULL,'admin','2026-05-08 00:00:00','admin','2026-05-08 00:00:00',0,1,1);

-- ---------- 段15 biz_revisit_fee_policy 复诊收费策略补全（2 条真实） ----------
INSERT INTO biz_revisit_fee_policy (id, policy_name, revisit_source, same_doctor, same_dept, within_days, charge_mode, priority, status, create_by, create_time, update_by, update_time, del_flag, remark, create_by_id, update_by_id)
VALUES
(2360000000000012001,'随访计划复诊同科室免挂号费',4,0,1,30,2,60,1,'admin','2026-05-08 00:00:00','admin','2026-05-08 00:00:00',0,'随访计划生成的复诊预约，30天内同科室免收挂号费（sql/236）',1,1),
(2360000000000012002,'自助复诊不同医生全额收费',3,2,0,14,1,90,1,'admin','2026-05-08 00:00:00','admin','2026-05-08 00:00:00',0,'患者自助发起、非原医生复诊按正常收取（sql/236）',1,1);

-- ---------- 段16 biz_infusion_seat 输液室真实座位（20 个） ----------
INSERT INTO biz_infusion_seat (id, seat_no, area, seat_status, create_by, create_time, update_by, update_time, del_flag, remark, create_by_id, update_by_id)
VALUES
(2360000000000013001,'A01','成人区',1,'admin','2026-05-08 00:00:00','admin','2026-05-08 00:00:00',0,'门诊输液室（sql/236）',1,1),
(2360000000000013002,'A02','成人区',1,'admin','2026-05-08 00:00:00','admin','2026-05-08 00:00:00',0,NULL,1,1),
(2360000000000013003,'A03','成人区',1,'admin','2026-05-08 00:00:00','admin','2026-05-08 00:00:00',0,NULL,1,1),
(2360000000000013004,'A04','成人区',1,'admin','2026-05-08 00:00:00','admin','2026-05-08 00:00:00',0,NULL,1,1),
(2360000000000013005,'A05','成人区',1,'admin','2026-05-08 00:00:00','admin','2026-05-08 00:00:00',0,NULL,1,1),
(2360000000000013006,'A06','成人区',1,'admin','2026-05-08 00:00:00','admin','2026-05-08 00:00:00',0,NULL,1,1),
(2360000000000013007,'A07','成人区',1,'admin','2026-05-08 00:00:00','admin','2026-05-08 00:00:00',0,NULL,1,1),
(2360000000000013008,'A08','成人区',1,'admin','2026-05-08 00:00:00','admin','2026-05-08 00:00:00',0,NULL,1,1),
(2360000000000013009,'A09','成人区',1,'admin','2026-05-08 00:00:00','admin','2026-05-08 00:00:00',0,NULL,1,1),
(2360000000000013010,'A10','成人区',1,'admin','2026-05-08 00:00:00','admin','2026-05-08 00:00:00',0,NULL,1,1),
(2360000000000013011,'A11','成人区',1,'admin','2026-05-08 00:00:00','admin','2026-05-08 00:00:00',0,NULL,1,1),
(2360000000000013012,'A12','成人区',1,'admin','2026-05-08 00:00:00','admin','2026-05-08 00:00:00',0,NULL,1,1),
(2360000000000013013,'C01','儿童区',1,'admin','2026-05-08 00:00:00','admin','2026-05-08 00:00:00',0,NULL,1,1),
(2360000000000013014,'C02','儿童区',1,'admin','2026-05-08 00:00:00','admin','2026-05-08 00:00:00',0,NULL,1,1),
(2360000000000013015,'C03','儿童区',1,'admin','2026-05-08 00:00:00','admin','2026-05-08 00:00:00',0,NULL,1,1),
(2360000000000013016,'C04','儿童区',1,'admin','2026-05-08 00:00:00','admin','2026-05-08 00:00:00',0,NULL,1,1),
(2360000000000013017,'I01','隔离区',1,'admin','2026-05-08 00:00:00','admin','2026-05-08 00:00:00',0,NULL,1,1),
(2360000000000013018,'I02','隔离区',1,'admin','2026-05-08 00:00:00','admin','2026-05-08 00:00:00',0,NULL,1,1),
(2360000000000013019,'I03','隔离区',1,'admin','2026-05-08 00:00:00','admin','2026-05-08 00:00:00',0,NULL,1,1),
(2360000000000013020,'I04','隔离区',1,'admin','2026-05-08 00:00:00','admin','2026-05-08 00:00:00',0,NULL,1,1);

-- ---------- 段17 biz_lis_qc_plan 室内质控计划（10 条，真实检验项目） ----------
INSERT INTO biz_lis_qc_plan (id, plan_no, item_id, item_code, item_name, instrument_no, instrument_name, qc_level, control_name, control_lot_no, manufacturer, mean_value, sd_value, cv_limit, expire_date, status, create_by, create_time, update_by, update_time, del_flag, remark, create_by_id, update_by_id)
SELECT 2360000000000014000 + ROW_NUMBER() OVER (ORDER BY v.pn), CONCAT('QCL', v.pn, '-', v.lv) AS plan_no, i.id, i.item_code, i.item_name, v.ins_no, v.ins_nm, v.lv, CASE v.lv WHEN 1 THEN '低值质控血清' WHEN 2 THEN '中值质控血清' ELSE '高值质控血清' END, CONCAT('LOT2026', v.pn), '伯乐生物', v.mean, v.sd, 8.00, '2026-12-31', 1, 'admin', '2026-05-08 00:00:00', 'admin', '2026-05-08 00:00:00', 0, '铺底（sql/236）', 1, 1
FROM (SELECT 'WBC' AS pn,1 AS lv,'LIS-01' AS ins_no,'迈瑞BC-5390血球仪' AS ins_nm,4.50 AS mean,0.22 AS sd
UNION ALL SELECT 'WBC',2,'LIS-01','迈瑞BC-5390血球仪',9.20,0.46
UNION ALL SELECT 'GLU',1,'LIS-02','贝克曼AU5800生化仪',3.10,0.14
UNION ALL SELECT 'GLU',2,'LIS-02','贝克曼AU5800生化仪',5.60,0.25
UNION ALL SELECT 'GLU',3,'LIS-02','贝克曼AU5800生化仪',14.50,0.65
UNION ALL SELECT 'ALT',2,'LIS-02','贝克曼AU5800生化仪',42.00,1.90
UNION ALL SELECT 'CRE',2,'LIS-02','贝克曼AU5800生化仪',88.00,4.00
UNION ALL SELECT 'TC',2,'LIS-02','贝克曼AU5800生化仪',4.80,0.22
UNION ALL SELECT 'PT',2,'LIS-03','希森美康CS-5100凝血仪',12.50,0.55
UNION ALL SELECT 'HbA1c',2,'LIS-04','伯乐D-10糖化仪',6.50,0.20) v
JOIN sys_laboratory_item i ON (
  (v.pn='WBC' AND i.item_code='LB001') OR (v.pn='GLU' AND i.item_code='LB006') OR (v.pn='ALT' AND i.item_code='LB004')
  OR (v.pn='CRE' AND i.item_code='LB005') OR (v.pn='TC' AND i.item_code='LB008') OR (v.pn='PT' AND i.item_code='LB013')
  OR (v.pn='HbA1c' AND i.item_code='LB007'));

-- ---------- 段18 biz_insurance_catalog_rule 医保目录规则补全（10 条） ----------
INSERT INTO biz_insurance_catalog_rule (id, rule_no, item_code, item_name, catalog_type, encounter_type, insurance_type, self_pay_ratio, deductible, ceiling, pool_ratio, limit_flags, effective_date, expire_date, priority, status, create_by, create_time, update_by, update_time, del_flag, remark, create_by_id, update_by_id)
VALUES
(2360000000000015001,'ICR202605080001','CT001','头颅CT平扫',1,1,NULL,0.00,0.00,999999.99,80.00,0,'2026-01-01',NULL,0,1,'admin','2026-05-08 00:00:00','admin','2026-05-08 00:00:00',0,'门诊甲类：统筹报80%（sql/236）',1,1),
(2360000000000015002,'ICR202605080002','CT002','胸部CT平扫',1,2,NULL,0.00,0.00,999999.99,90.00,0,'2026-01-01',NULL,0,1,'admin','2026-05-08 00:00:00','admin','2026-05-08 00:00:00',0,'住院甲类：统筹报90%（sql/236）',1,1),
(2360000000000015003,'ICR202605080003','MR001','头颅MRI平扫',2,1,NULL,20.00,0.00,999999.99,75.00,0,'2026-01-01',NULL,0,1,'admin','2026-05-08 00:00:00','admin','2026-05-08 00:00:00',0,'门诊乙类：先自付20%（sql/236）',1,1),
(2360000000000015004,'ICR202605080004','US002','泌尿系彩超',1,1,NULL,0.00,0.00,999999.99,80.00,0,'2026-01-01',NULL,0,1,'admin','2026-05-08 00:00:00','admin','2026-05-08 00:00:00',0,'门诊甲类（sql/236）',1,1),
(2360000000000015005,'ICR202605080005','ECG001','常规心电图',1,1,NULL,0.00,0.00,999999.99,80.00,0,'2026-01-01',NULL,0,1,'admin','2026-05-08 00:00:00','admin','2026-05-08 00:00:00',0,'门诊甲类（sql/236）',1,1),
(2360000000000015006,'ICR202605080006','LB001','血常规',1,1,NULL,0.00,0.00,999999.99,80.00,0,'2026-01-01',NULL,0,1,'admin','2026-05-08 00:00:00','admin','2026-05-08 00:00:00',0,'门诊甲类（sql/236）',1,1),
(2360000000000015007,'ICR202605080007','LB004','肝功能全套',2,1,NULL,10.00,0.00,999999.99,75.00,0,'2026-01-01',NULL,0,1,'admin','2026-05-08 00:00:00','admin','2026-05-08 00:00:00',0,'门诊乙类：先自付10%（sql/236）',1,1),
(2360000000000015008,'ICR202605080008','LB020','降钙素原(PCT)',2,2,NULL,10.00,0.00,999999.99,80.00,0,'2026-01-01',NULL,0,1,'admin','2026-05-08 00:00:00','admin','2026-05-08 00:00:00',0,'住院乙类（sql/236）',1,1),
(2360000000000015009,'ICR202605080009','BP0007','头孢曲松钠粉针',1,2,NULL,0.00,0.00,999999.99,90.00,0,'2026-01-01',NULL,0,1,'admin','2026-05-08 00:00:00','admin','2026-05-08 00:00:00',0,'住院甲类（sql/236）',1,1),
(2360000000000015010,'ICR202605080010','BP0035','伏立康唑片',2,2,NULL,20.00,0.00,999999.99,80.00,0,'2026-01-01',NULL,0,1,'admin','2026-05-08 00:00:00','admin','2026-05-08 00:00:00',0,'住院乙类：先自付20%（sql/236）',1,1);

-- ============================= 自检 =============================
SELECT COUNT(*) AS chk_diagnosis FROM sys_diagnosis;
SELECT COUNT(*) AS chk_price_hist FROM sys_price_change_history;
SELECT COUNT(*) AS chk_wb_layout FROM sys_workbench_layout;
SELECT COUNT(*) AS chk_kb_doc FROM sys_knowledge_doc WHERE id BETWEEN 2360000000000004001 AND 2360000000000004008;
SELECT COUNT(*) AS chk_kb_chunk FROM sys_knowledge_chunk WHERE doc_id BETWEEN 2360000000000004001 AND 2360000000000004008;
SELECT COUNT(*) AS chk_alert_rule FROM sys_alert_rule;
SELECT COUNT(*) AS chk_ins_policy FROM sys_insurance_policy;
SELECT COUNT(*) AS chk_dph FROM sys_drug_price_history WHERE history_id > 5;
SELECT COUNT(*) AS chk_interaction FROM sys_drug_interaction WHERE id >= 2360000000000006001;
SELECT COUNT(*) AS chk_infectious FROM sys_infectious_disease;
SELECT COUNT(*) AS chk_supplier FROM sys_supplier;
SELECT COUNT(*) AS chk_faq FROM sys_faq WHERE faq_no LIKE 'FAQ20260508%';
SELECT COUNT(*) AS chk_treatment FROM sys_treatment_item WHERE item_code LIKE 'TR1%';
SELECT COUNT(*) AS chk_img_plain FROM sys_imaging_plain_item WHERE id >= 2360000000000010001;
SELECT COUNT(*) AS chk_lab_plain FROM sys_lab_plain_item WHERE id >= 2360000000000011001;
SELECT COUNT(*) AS chk_revisit FROM biz_revisit_fee_policy WHERE del_flag=0;
SELECT COUNT(*) AS chk_seat_active FROM biz_infusion_seat WHERE del_flag=0;
SELECT COUNT(*) AS chk_qc_active FROM biz_lis_qc_plan WHERE del_flag=0;
SELECT COUNT(*) AS chk_icr_new FROM biz_insurance_catalog_rule WHERE rule_no LIKE 'ICR20260508%';

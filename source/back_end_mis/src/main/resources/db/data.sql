-- 幂等初始化参保人（INSERT IGNORE 依赖身份证唯一键）。
-- 演示用测试人员；HIS 患者若不在此处，2304 上传会按报文中的险种兜底接收并给出告警，参保查询则返回未参保。
INSERT IGNORE INTO mss_insured_person
(id_card, insurance_no, name, gender, insurance_type, person_status, account_balance, region) VALUES
('430102199001011234', '430100001', '张三', '男', '2', 1, 3200.50, '长沙市'),
('430102198505056789', '430100002', '李四', '女', '3', 1, 0.00,    '长沙市'),
('430102197012121234', '430100003', '王五', '男', '2', 1, 1500.00, '长沙市'),
('430121200006069999', '430100004', '赵六', '女', '3', 1, 0.00,    '长沙市'),
('43010219660606001X', '430100005', '孙七', '男', '2', 0, 80.00,  '长沙市');

-- =====================================================================
-- 系统基础铺底（仿 HIS 02-铺底基础数据）：科室 / 角色 / 菜单 / 角色菜单 / 员工 / 岗位 / 字典 / 配置
-- 全部 INSERT IGNORE 幂等；用户表密码为 BCrypt，由 AuthDataInitializer 首次启动时播种。
-- =====================================================================

-- 经办科室
INSERT IGNORE INTO mss_department (id, dept_code, dept_name, dept_type, parent_id, sort_order, status) VALUES
(1, 'ZHK',  '综合管理科', 4, 0, 1, 1),
(2, 'JSJBK', '结算经办科', 1, 0, 2, 1),
(3, 'SHJHK', '审核稽核科', 2, 0, 3, 1),
(4, 'JJCWK', '基金财务科', 3, 0, 4, 1);

-- 经办人员
INSERT IGNORE INTO mss_employee (id, emp_code, emp_name, emp_type, gender, hire_date, phone, dept_id, dept_name, title, position, status) VALUES
(1, 'ADMIN001', '系统管理员', 3, 1, '2026-01-01', '13800000001', 1, '综合管理科', NULL, '平台管理员', 1),
(2, 'YB001',    '结算经办员', 1, 2, '2026-01-01', '13800000002', 2, '结算经办科', '初级', '结算经办', 1),
(3, 'YB002',    '审核稽核员', 2, 1, '2026-01-01', '13800000003', 3, '审核稽核科', '中级', '审核稽核', 1);

-- 角色
INSERT IGNORE INTO mss_role (id, role_code, role_name, role_type, data_scope, sort_order, status) VALUES
(1, 'MSS_ADMIN',    '经办管理员', 1, 1, 1, 1),
(2, 'MSS_OPERATOR', '经办员',     1, 3, 2, 1),
(3, 'MSS_AUDITOR',  '审核员',     1, 3, 3, 1);

-- 菜单（目录=1，菜单=2；component 对应 front_mss/src/views 下路径）
INSERT IGNORE INTO mss_menu (id, menu_name, parent_id, sort_order, menu_type, path, component, menu_key, icon, permission) VALUES
(1,  '经办工作台',      0, 10, 2, '/dashboard',         'dashboard/DashboardView',       'portal.dashboard', 'DataBoard',  'portal:dashboard:view'),
(2,  '医保结算',        0, 20, 1, '/settlement',        NULL,                            'settle',           'Money',      NULL),
(21, '门诊医保结算',    2, 1,  2, '/settlement/outpatient', 'settlement/OutpatientSettleView', 'settle.outpatient', 'Ticket', 'settle:outpatient:list'),
(22, '住院医保结算',    2, 2,  2, '/settlement/inpatient',  'settlement/InpatientSettleView',  'settle.inpatient',  'FirstAidKit','settle:inpatient:list'),
(23, '结算单管理',      2, 3,  2, '/settlement/bill',    'settlement/SettleBillView',    'settle.bill',      'Document',   'settle:bill:list'),
(24, '机构报盘接收',    2, 4,  2, '/settlement/gateway', 'settlement/GatewaySettleView', 'settle.gateway',   'Upload',     'settle:gateway:list'),
(25, '月度结算管理',    2, 5,  2, '/settlement/monthly', 'settlement/MonthlySettleView', 'settle.monthly',   'Calendar',   'settle:monthly:list'),
(3,  '参保管理',        0, 30, 1, '/insured',            NULL,                            'insured',          'User',       NULL),
(31, '参保人管理',      3, 1,  2, '/insured/person',     'insured/InsuredPersonView',    'insured.person',   'UserFilled', 'insured:person:list'),
(4,  '费用审核',        0, 40, 2, '/audit/fee',          'audit/FeeAuditView',           'audit.fee',        'Stamp',      'audit:fee:list'),
(5,  '三大目录与对照',  0, 50, 2, '/catalog',            'catalog/CatalogView',          'catalog',          'Notebook',   'catalog:list'),
(6,  '异地就医备案',    0, 60, 2, '/filing/cross-region','filing/CrossRegionFilingView', 'filing.crossRegion','Promotion','filing:list'),
(7,  '清算对账',        0, 70, 2, '/reconciliation',     'reconciliation/ReconciliationView', 'recon',       'DataAnalysis','recon:list'),
(8,  '统计报表',        0, 80, 2, '/stats',              'stats/StatsView',              'stats',            'TrendCharts','stats:view'),
(9,  '系统管理',        0, 90, 1, '/system',             NULL,                            'system',           'Setting',    NULL),
(91, '用户管理',        9, 1,  2, '/system/user',        'system/UserView',              'system.user',      'User',       'system:user:list'),
(92, '角色管理',        9, 2,  2, '/system/role',        'system/RoleView',              'system.role',      'Avatar',     'system:role:list'),
(93, '菜单管理',        9, 3,  2, '/system/menu',        'system/MenuView',              'system.menu',      'Menu',       'system:menu:list'),
(94, '岗位管理',        9, 4,  2, '/system/post',        'system/PostView',              'system.post',      'Postcard',   'system:post:list'),
(95, '字典管理',        9, 5,  2, '/system/dict',        'system/DictView',              'system.dict',      'Collection', 'system:dict:list'),
(96, '科室管理',        9, 6,  2, '/system/dept',        'system/DeptView',              'system.dept',      'OfficeBuilding','system:dept:list'),
(97, '员工管理',        9, 7,  2, '/system/employee',    'system/EmployeeView',          'system.employee',  'Avatar',      'org:employee:list');

-- 系统管理按钮权限码（menu_type=3，v-perm 显隐用；后端鉴权仍是接口白名单口径）
INSERT IGNORE INTO mss_menu (id, menu_name, parent_id, sort_order, menu_type, path, component, menu_key, icon, permission) VALUES
(101, '用户新增', 9, 1, 3, NULL, NULL, 'system.user.add',    NULL, 'system:user:add'),
(102, '用户编辑', 9, 2, 3, NULL, NULL, 'system.user.edit',   NULL, 'system:user:edit'),
(103, '用户删除', 9, 3, 3, NULL, NULL, 'system.user.delete', NULL, 'system:user:delete'),
(104, '角色新增', 9, 4, 3, NULL, NULL, 'system.role.add',    NULL, 'system:role:add'),
(105, '角色删除', 9, 5, 3, NULL, NULL, 'system.role.delete', NULL, 'system:role:delete'),
(106, '菜单新增', 9, 6, 3, NULL, NULL, 'system.menu.add',    NULL, 'system:menu:add'),
(107, '菜单删除', 9, 7, 3, NULL, NULL, 'system.menu.delete', NULL, 'system:menu:delete'),
(108, '字典新增', 9, 8, 3, NULL, NULL, 'system.dict.add',    NULL, 'system:dict:add'),
(109, '字典编辑', 9, 9, 3, NULL, NULL, 'system.dict.edit',   NULL, 'system:dict:edit'),
(110, '字典删除', 9, 10, 3, NULL, NULL, 'system.dict.delete', NULL, 'system:dict:delete'),
(111, '员工新增', 97, 1, 3, NULL, NULL, 'org.employee.add',    NULL, 'org:employee:add'),
(112, '员工删除', 97, 2, 3, NULL, NULL, 'org.employee.delete', NULL, 'org:employee:delete');

-- 角色菜单授权
-- 经办管理员：全部菜单
INSERT IGNORE INTO mss_role_menu (role_id, menu_id) VALUES
(1,1),(1,2),(1,21),(1,22),(1,23),(1,24),(1,25),(1,3),(1,31),(1,4),(1,5),(1,6),(1,7),(1,8),(1,9),(1,91),(1,92),(1,93),(1,94),(1,95),
(1,101),(1,102),(1,103),(1,104),(1,105),(1,106),(1,107),(1,108),(1,109),(1,110),
(1,96),(1,97),(1,111),(1,112);
-- 经办员：业务菜单（不含费用审核/系统管理）
INSERT IGNORE INTO mss_role_menu (role_id, menu_id) VALUES
(2,1),(2,2),(2,21),(2,22),(2,23),(2,24),(2,25),(2,3),(2,31),(2,5),(2,6),(2,7),(2,8);
-- 审核员：工作台 + 结算单 + 月度 + 费用审核 + 统计报表
INSERT IGNORE INTO mss_role_menu (role_id, menu_id) VALUES
(3,1),(3,2),(3,23),(3,25),(3,4),(3,8);

-- 员工岗位（角色 × 科室，每人恰好一条 is_primary=1）
INSERT IGNORE INTO mss_employee_post (employee_id, role_id, dept_id, is_primary, effective_date) VALUES
(1, 1, 1, 1, '2026-01-01'),
(2, 2, 2, 1, '2026-01-01'),
(3, 3, 3, 1, '2026-01-01');

-- 字典类型
INSERT IGNORE INTO mss_dict_type (id, dict_type, dict_name, status, dict_source) VALUES
(1, 'mss_menu_type',  '菜单类型',   1, 1),
(2, 'mss_user_status','用户状态',   1, 1),
(3, 'mss_user_type',  '用户类型',   1, 1),
(4, 'mss_role_type',  '角色类型',   1, 1),
(5, 'mss_data_scope', '数据权限范围',1, 1),
(6, 'mss_dept_type',  '科室类型',   1, 1),
(7, 'mss_emp_type',   '员工类型',   1, 1),
(8, 'mss_common_status','通用状态', 1, 1),
(9, 'mss_menu_visible','菜单可见',  1, 1),
(10, 'sys_hospital_title', '职称',     1, 1),
(11, 'hospital_position',  '职位',     1, 1),
(12, 'his_education',      '学历',     1, 1),
(13, 'his_emp_cert_type',  '证书类型', 1, 1),
(14, 'his_emp_cert_org',   '发证机关', 1, 1);

-- 字典数据
INSERT IGNORE INTO mss_dict_data (dict_type, dict_label, dict_value, dict_sort, is_default, status) VALUES
('mss_menu_type',   '目录', '1', 1, 0, 1),
('mss_menu_type',   '菜单', '2', 2, 1, 1),
('mss_menu_type',   '按钮', '3', 3, 0, 1),
('mss_user_status', '停用', '0', 1, 0, 1),
('mss_user_status', '启用', '1', 2, 1, 1),
('mss_user_status', '锁定', '2', 3, 0, 1),
('mss_user_type',   '系统用户', '1', 1, 1, 1),
('mss_user_type',   '外部用户', '2', 2, 0, 1),
('mss_role_type',   '系统角色', '1', 1, 1, 1),
('mss_role_type',   '自定义角色', '2', 2, 0, 1),
('mss_data_scope',  '全部数据', '1', 1, 1, 1),
('mss_data_scope',  '自定义数据', '2', 2, 0, 1),
('mss_data_scope',  '本部门数据', '3', 3, 0, 1),
('mss_data_scope',  '仅本人数据', '5', 4, 0, 1),
('mss_dept_type',   '前台经办', '1', 1, 1, 1),
('mss_dept_type',   '审核稽核', '2', 2, 0, 1),
('mss_dept_type',   '基金财务', '3', 3, 0, 1),
('mss_dept_type',   '综合管理', '4', 4, 0, 1),
('mss_emp_type',    '经办员', '1', 1, 1, 1),
('mss_emp_type',    '审核员', '2', 2, 0, 1),
('mss_emp_type',    '管理员', '3', 3, 0, 1),
('mss_emp_type',    '其他',   '4', 4, 0, 1),
('mss_common_status','停用', '0', 1, 0, 1),
('mss_common_status','启用', '1', 2, 1, 1),
('mss_menu_visible', '隐藏', '0', 1, 0, 1),
('mss_menu_visible', '显示', '1', 2, 1, 1),
('sys_hospital_title', '主任医师', '1', 1, 0, 1),
('sys_hospital_title', '副主任医师', '2', 2, 0, 1),
('sys_hospital_title', '主治医师', '3', 3, 0, 1),
('sys_hospital_title', '住院医师', '4', 4, 0, 1),
('sys_hospital_title', '主任药师', '5', 5, 0, 1),
('sys_hospital_title', '副主任药师', '6', 6, 0, 1),
('sys_hospital_title', '主管药师', '7', 7, 0, 1),
('sys_hospital_title', '药师', '8', 8, 0, 1),
('sys_hospital_title', '主任护师', '9', 9, 0, 1),
('sys_hospital_title', '副主任护师', '10', 10, 0, 1),
('sys_hospital_title', '主管护师', '11', 11, 0, 1),
('sys_hospital_title', '护师', '12', 12, 0, 1),
('hospital_position', '院长', '1', 1, 0, 1),
('hospital_position', '副院长', '2', 2, 0, 1),
('hospital_position', '科室主任', '3', 3, 0, 1),
('hospital_position', '科室副主任', '4', 4, 0, 1),
('hospital_position', '组长', '5', 5, 0, 1),
('hospital_position', '普通员工', '6', 6, 1, 1),
('his_education', '博士研究生', '1', 1, 0, 1),
('his_education', '硕士研究生', '2', 2, 0, 1),
('his_education', '本科', '3', 3, 1, 1),
('his_education', '大专', '4', 4, 0, 1),
('his_education', '中专', '5', 5, 0, 1),
('his_emp_cert_type', '医师执业证书', '1', 1, 0, 1),
('his_emp_cert_type', '护士执业证书', '2', 2, 0, 1),
('his_emp_cert_type', '药师执业证书', '3', 3, 0, 1),
('his_emp_cert_type', '卫生专业技术资格证书', '4', 4, 0, 1),
('his_emp_cert_type', '其他', '9', 9, 0, 1),
('his_emp_cert_org', '国家卫生健康委员会', '1', 1, 0, 1),
('his_emp_cert_org', '国家药品监督管理局', '2', 2, 0, 1),
('his_emp_cert_org', '省级卫生健康委员会', '3', 3, 0, 1),
('his_emp_cert_org', '市级卫生健康委员会', '4', 4, 0, 1),
('his_emp_cert_org', '其他', '9', 9, 0, 1);

-- 系统参数
INSERT IGNORE INTO mss_config (config_id, config_name, config_key, config_value, config_type, is_system) VALUES
(1, '平台名称',   'mss.platform.name', '医保结算经办平台', 0, 1),
(2, '定点机构编码', 'mss.medins.code',  'H4301000001',     1, 1),
(3, '定点机构名称', 'mss.medins.name',  '长沙市麓康医院',  1, 1);

-- =====================================================================
-- 基金监管铺底：内置监管规则 + 菜单 + 角色授权
-- =====================================================================

-- 内置监管规则（规则引擎按 rule_type 分派扫描逻辑）
INSERT IGNORE INTO mss_audit_rule (id, rule_code, rule_name, rule_type, target, severity, params, description, status) VALUES
(1, 'RULE_DUP_CHARGE',     '重复收费',          'duplicate_charge',         1, 2, '{"minAmount":200}',     '同一结算单同项目多次计费且合计达阈值', 1),
(2, 'RULE_OVER_LIMIT',     '超限价收费',        'over_limit_price',         1, 2, '{"overRatio":0.1}',      '项目单价高于医保限价比例阈值',       1),
(3, 'RULE_SPLIT_HOSP',     '分解住院',          'split_hospitalization',    2, 3, '{"windowDays":15,"minCount":2}', '同一参保人短期内多次住院结算', 1),
(4, 'RULE_BED_HANG',       '挂床住院',          'bed_hanging',              1, 2, '{"minDays":10,"maxAmount":500}', '住院天数长而费用极低',       1),
(5, 'RULE_ABNORMAL_COST',  '高额住院费用',      'abnormal_cost',            2, 3, '{"avgTimes":3}',         '住院费用显著高于同险种均值',         1),
(6, 'RULE_PAYCLASS_C',     '丙类项目入统筹',    'payclass_c',               1, 2, '{"minAmount":200}',     '结算单含丙类项目金额并发生统筹支付',   1);

-- 基金监管菜单（目录=13，子菜单 131-135）
INSERT IGNORE INTO mss_menu (id, menu_name, parent_id, sort_order, menu_type, path, component, menu_key, icon, permission) VALUES
(13,  '基金监管',         0,  45, 1, '/supervise',            NULL,                         'supervise',        'Monitor',    NULL),
(131, '监管工作台',       13, 1,  2, '/supervise/dashboard',  'supervise/SuperviseDashboard', 'supervise.dashboard', 'DataBoard', 'supervise:dashboard:view'),
(132, '规则配置',         13, 2,  2, '/supervise/rule',       'supervise/RuleView',         'supervise.rule',   'SetUp',      'supervise:rule:list'),
(133, '疑点稽核',         13, 3,  2, '/supervise/suspicion',  'supervise/SuspicionView',    'supervise.suspicion', 'Search',  'supervise:suspicion:list'),
(134, '飞检任务',         13, 4,  2, '/supervise/inspection', 'supervise/InspectionView',   'supervise.inspection', 'Van',      'supervise:inspection:list'),
(135, '违规追回',         13, 5,  2, '/supervise/recover',    'supervise/RecoverView',      'supervise.recover', 'RefreshLeft', 'supervise:recover:list');

-- 基金监管角色授权：经办管理员全量 + 审核员监管全量
INSERT IGNORE INTO mss_role_menu (role_id, menu_id) VALUES
(1,13),(1,131),(1,132),(1,133),(1,134),(1,135),
(3,13),(3,131),(3,132),(3,133),(3,134),(3,135);

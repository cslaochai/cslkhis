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
(95, '字典管理',        9, 5,  2, '/system/dict',        'system/DictView',              'system.dict',      'Collection', 'system:dict:list');

-- 角色菜单授权
-- 经办管理员：全部菜单
INSERT IGNORE INTO mss_role_menu (role_id, menu_id) VALUES
(1,1),(1,2),(1,21),(1,22),(1,23),(1,24),(1,25),(1,3),(1,31),(1,4),(1,5),(1,6),(1,7),(1,8),(1,9),(1,91),(1,92),(1,93),(1,94),(1,95);
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
(9, 'mss_menu_visible','菜单可见',  1, 1);

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
('mss_menu_visible', '显示', '1', 2, 1, 1);

-- 系统参数
INSERT IGNORE INTO mss_config (config_id, config_name, config_key, config_value, config_type, is_system) VALUES
(1, '平台名称',   'mss.platform.name', '医保结算经办平台', 0, 1),
(2, '定点机构编码', 'mss.medins.code',  'H4301000001',     1, 1),
(3, '定点机构名称', 'mss.medins.name',  '长沙市麓康医院',  1, 1);

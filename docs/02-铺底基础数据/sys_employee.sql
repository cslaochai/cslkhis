SET NAMES utf8mb4;
SET
FOREIGN_KEY_CHECKS=0;
START TRANSACTION;
INSERT INTO sys_employee (id, emp_code, emp_name, emp_type, gender, birth_date, hire_date, id_card, phone, email,
                          dept_id, dept_name, title, position, specialty, education, avatar, is_expert, expert_price,
                          status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('1', 'EMP001', '超级管理员', 1, 1, '2026-09-10', '2026-09-11', '430726199501048888', '18878885878',
        '1688888@qq.com', '1958001', '医院信息系统', '302', '2', '超级管理员', '1', NULL, 0, '0.00', 0, 'admin',
        'system', 0, NULL, '1', '1');
INSERT INTO sys_employee (id, emp_code, emp_name, emp_type, gender, birth_date, hire_date, id_card, phone, email,
                          dept_id, dept_name, title, position, specialty, education, avatar, is_expert, expert_price,
                          status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('9301', 'E0901', '周远', 1, 1, '1978-03-12', '2003-07-01', '110105197803120116', '13900000091',
        'zhouyuan@cslk-his.cn', '19580048', '急诊内科', '401', '31', '胸痛中心绿色通道、急性心脑血管事件急救',
        '博士研究生', NULL, 0, '0.00', 1, 'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_employee (id, emp_code, emp_name, emp_type, gender, birth_date, hire_date, id_card, phone, email,
                          dept_id, dept_name, title, position, specialty, education, avatar, is_expert, expert_price,
                          status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('9302', 'E0902', '林小舟', 1, 2, '1985-09-23', '2011-07-01', '32010219850923212X', '13900000092',
        'linxiaozhou@cslk-his.cn', '19580049', '急诊外科', '301', '31', '多发伤救治、四肢骨折与关节脱位急诊处理',
        '硕士研究生', NULL, 0, '0.00', 1, 'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_employee (id, emp_code, emp_name, emp_type, gender, birth_date, hire_date, id_card, phone, email,
                          dept_id, dept_name, title, position, specialty, education, avatar, is_expert, expert_price,
                          status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('9303', 'E0903', '吴桐', 1, 2, '1990-04-06', '2015-07-01', '440301199004062125', '13900000093',
        'wutong@cslk-his.cn', '19580051', '急诊儿科', '201', '31', '小儿高热惊厥、儿科急危重症复苏', '本科', NULL, 0,
        '0.00', 1, 'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_employee (id, emp_code, emp_name, emp_type, gender, birth_date, hire_date, id_card, phone, email,
                          dept_id, dept_name, title, position, specialty, education, avatar, is_expert, expert_price,
                          status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('897001001', 'H17001', '沈楠', 1, 1, '1972-08-10', '1995-06-01', '440305197208109737', '13869192686',
        'shennan@cslk-his.local', '19580003', '呼吸内科', '401', '1', '慢性气道疾病、肺部感染、呼吸介入', '硕士', NULL, 1,
        '100.00', 1, 'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_employee (id, emp_code, emp_name, emp_type, gender, birth_date, hire_date, id_card, phone, email,
                          dept_id, dept_name, title, position, specialty, education, avatar, is_expert, expert_price,
                          status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('897001002', 'H17002', '陈建安', 1, 1, '1979-09-17', '2004-11-01', '510107197909177919', '13787166898',
        'chenjianan@cslk-his.local', '19580003', '呼吸内科', '301', '2', '慢性气道疾病、肺部感染、呼吸介入', '博士', NULL,
        1, '50.00', 1, 'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_employee (id, emp_code, emp_name, emp_type, gender, birth_date, hire_date, id_card, phone, email,
                          dept_id, dept_name, title, position, specialty, education, avatar, is_expert, expert_price,
                          status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('897001003', 'H17003', '罗朝阳', 1, 1, '1984-12-23', '2009-08-01', '420106198412236270', '19939114649',
        'luochaoyang@cslk-his.local', '19580003', '呼吸内科', '201', '31', '慢性气道疾病、肺部感染、呼吸介入', '硕士',
        NULL, 0, '0.00', 1, 'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_employee (id, emp_code, emp_name, emp_type, gender, birth_date, hire_date, id_card, phone, email,
                          dept_id, dept_name, title, position, specialty, education, avatar, is_expert, expert_price,
                          status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('897001004', 'H17004', '卢鸣谦', 1, 1, '1993-05-07', '2016-09-01', '210102199305074114', '18626772110',
        'lumingqian@cslk-his.local', '19580003', '呼吸内科', '102', '6', '慢性气道疾病、肺部感染、呼吸介入', '本科', NULL,
        0, '0.00', 1, 'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_employee (id, emp_code, emp_name, emp_type, gender, birth_date, hire_date, id_card, phone, email,
                          dept_id, dept_name, title, position, specialty, education, avatar, is_expert, expert_price,
                          status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('897001005', 'H17005', '孟瑶', 2, 2, '1987-07-10', '2012-11-01', '110105198707107709', '15146205744',
        'mengyao@cslk-his.local', '19580003', '呼吸内科', '403', '9', '慢性气道疾病、肺部感染、呼吸介入', '本科', NULL, 0,
        '0.00', 1, 'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_employee (id, emp_code, emp_name, emp_type, gender, birth_date, hire_date, id_card, phone, email,
                          dept_id, dept_name, title, position, specialty, education, avatar, is_expert, expert_price,
                          status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('897001006', 'H17006', '杨乐怡', 2, 2, '1990-08-20', '2015-09-01', '37010219900820460X', '15196099499',
        'yangleyi@cslk-his.local', '19580003', '呼吸内科', '203', '15', '慢性气道疾病、肺部感染、呼吸介入', '本科', NULL,
        0, '0.00', 1, 'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_employee (id, emp_code, emp_name, emp_type, gender, birth_date, hire_date, id_card, phone, email,
                          dept_id, dept_name, title, position, specialty, education, avatar, is_expert, expert_price,
                          status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('897001007', 'H17007', '尚明珠', 2, 2, '1995-12-16', '2019-06-01', '510107199512160480', '18662277863',
        'shangmingzhu@cslk-his.local', '19580003', '呼吸内科', '105', '15', '慢性气道疾病、肺部感染、呼吸介入', '中专',
        NULL, 0, '0.00', 1, 'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_employee (id, emp_code, emp_name, emp_type, gender, birth_date, hire_date, id_card, phone, email,
                          dept_id, dept_name, title, position, specialty, education, avatar, is_expert, expert_price,
                          status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('897001008', 'H17008', '温昊天', 1, 1, '1973-05-06', '1997-02-01', '210102197305062317', '13911750937',
        'wenhaotian@cslk-his.local', '19580004', '消化内科', '401', '1', '消化道内镜、炎症性肠病、肝胆胰疾病', '博士',
        NULL, 1, '100.00', 1, 'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_employee (id, emp_code, emp_name, emp_type, gender, birth_date, hire_date, id_card, phone, email,
                          dept_id, dept_name, title, position, specialty, education, avatar, is_expert, expert_price,
                          status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('897001009', 'H17009', '徐明德', 1, 1, '1982-09-05', '2007-05-01', '320106198209056852', '13669015159',
        'xumingde@cslk-his.local', '19580004', '消化内科', '301', '2', '消化道内镜、炎症性肠病、肝胆胰疾病', '博士', NULL,
        1, '50.00', 0, 'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_employee (id, emp_code, emp_name, emp_type, gender, birth_date, hire_date, id_card, phone, email,
                          dept_id, dept_name, title, position, specialty, education, avatar, is_expert, expert_price,
                          status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('897001010', 'H17010', '李建安', 1, 1, '1984-02-15', '2008-09-01', '51010719840215363X', '18233065130',
        'lijianan@cslk-his.local', '19580004', '消化内科', '201', '31', '消化道内镜、炎症性肠病、肝胆胰疾病', '硕士',
        NULL, 0, '0.00', 1, 'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_employee (id, emp_code, emp_name, emp_type, gender, birth_date, hire_date, id_card, phone, email,
                          dept_id, dept_name, title, position, specialty, education, avatar, is_expert, expert_price,
                          status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('897001011', 'H17011', '马楠', 1, 1, '1990-04-12', '2016-11-01', '320106199004125636', '15973578320',
        'manan@cslk-his.local', '19580004', '消化内科', '102', '6', '消化道内镜、炎症性肠病、肝胆胰疾病', '硕士', NULL, 0,
        '0.00', 1, 'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_employee (id, emp_code, emp_name, emp_type, gender, birth_date, hire_date, id_card, phone, email,
                          dept_id, dept_name, title, position, specialty, education, avatar, is_expert, expert_price,
                          status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('897001012', 'H17012', '孔青禾', 2, 2, '1986-04-21', '2010-05-01', '510107198604211041', '15187830410',
        'kongqinghe@cslk-his.local', '19580004', '消化内科', '403', '9', '消化道内镜、炎症性肠病、肝胆胰疾病', '大专',
        NULL, 0, '0.00', 1, 'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_employee (id, emp_code, emp_name, emp_type, gender, birth_date, hire_date, id_card, phone, email,
                          dept_id, dept_name, title, position, specialty, education, avatar, is_expert, expert_price,
                          status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('897001013', 'H17013', '江佳', 2, 2, '1986-09-22', '2009-12-01', '510107198609224343', '15122033698',
        'jiangsjia@cslk-his.local', '19580004', '消化内科', '203', '15', '消化道内镜、炎症性肠病、肝胆胰疾病', '本科',
        NULL, 0, '0.00', 1, 'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_employee (id, emp_code, emp_name, emp_type, gender, birth_date, hire_date, id_card, phone, email,
                          dept_id, dept_name, title, position, specialty, education, avatar, is_expert, expert_price,
                          status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('897001014', 'H17014', '沈伟', 2, 1, '2000-05-20', '2023-06-01', '42010620000520751X', '18892790222',
        'shenwei@cslk-his.local', '19580004', '消化内科', '105', '15', '消化道内镜、炎症性肠病、肝胆胰疾病', '中专', NULL,
        0, '0.00', 1, 'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_employee (id, emp_code, emp_name, emp_type, gender, birth_date, hire_date, id_card, phone, email,
                          dept_id, dept_name, title, position, specialty, education, avatar, is_expert, expert_price,
                          status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('897001015', 'H17015', '施佳明', 1, 1, '1971-06-08', '1994-02-01', '310104197106084472', '13935556422',
        'shiqijiaming@cslk-his.local', '19580005', '心血管内科', '401', '1', '冠脉介入、心律失常、心力衰竭', '硕士', NULL,
        1, '100.00', 1, 'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_employee (id, emp_code, emp_name, emp_type, gender, birth_date, hire_date, id_card, phone, email,
                          dept_id, dept_name, title, position, specialty, education, avatar, is_expert, expert_price,
                          status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('897001016', 'H17016', '莫远航', 1, 1, '1975-03-17', '1998-03-01', '420106197503174656', '15890543775',
        'moyuanhang@cslk-his.local', '19580005', '心血管内科', '301', '2', '冠脉介入、心律失常、心力衰竭', '本科', NULL,
        1, '50.00', 1, 'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_employee (id, emp_code, emp_name, emp_type, gender, birth_date, hire_date, id_card, phone, email,
                          dept_id, dept_name, title, position, specialty, education, avatar, is_expert, expert_price,
                          status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('897001017', 'H17017', '曾洁', 1, 2, '1991-01-12', '2016-03-01', '330106199101128849', '15073132054',
        'zengjie@cslk-his.local', '19580005', '心血管内科', '201', '31', '冠脉介入、心律失常、心力衰竭', '硕士', NULL, 0,
        '0.00', 1, 'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_employee (id, emp_code, emp_name, emp_type, gender, birth_date, hire_date, id_card, phone, email,
                          dept_id, dept_name, title, position, specialty, education, avatar, is_expert, expert_price,
                          status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('897001018', 'H17018', '王升', 1, 1, '1996-01-07', '2018-09-01', '320106199601078911', '13523034776',
        'wangsheng@cslk-his.local', '19580005', '心血管内科', '102', '6', '冠脉介入、心律失常、心力衰竭', '本科', NULL, 0,
        '0.00', 1, 'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_employee (id, emp_code, emp_name, emp_type, gender, birth_date, hire_date, id_card, phone, email,
                          dept_id, dept_name, title, position, specialty, education, avatar, is_expert, expert_price,
                          status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('897001019', 'H17019', '邱昊天', 2, 1, '1983-03-15', '2006-03-01', '310104198303150859', '18743336317',
        'qiuhaotian@cslk-his.local', '19580005', '心血管内科', '403', '9', '冠脉介入、心律失常、心力衰竭', '大专', NULL,
        0, '0.00', 1, 'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_employee (id, emp_code, emp_name, emp_type, gender, birth_date, hire_date, id_card, phone, email,
                          dept_id, dept_name, title, position, specialty, education, avatar, is_expert, expert_price,
                          status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('897001020', 'H17020', '康一诺', 2, 2, '1986-05-14', '2010-10-01', '510107198605142340', '18719484881',
        'kangyinuo@cslk-his.local', '19580005', '心血管内科', '203', '15', '冠脉介入、心律失常、心力衰竭', '大专', NULL,
        0, '0.00', 1, 'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_employee (id, emp_code, emp_name, emp_type, gender, birth_date, hire_date, id_card, phone, email,
                          dept_id, dept_name, title, position, specialty, education, avatar, is_expert, expert_price,
                          status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('897001021', 'H17021', '顾寒梅', 2, 2, '1998-06-03', '2022-06-01', '510107199806037146', '13910894822',
        'guhanmei@cslk-his.local', '19580005', '心血管内科', '105', '15', '冠脉介入、心律失常、心力衰竭', '大专', NULL, 0,
        '0.00', 1, 'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_employee (id, emp_code, emp_name, emp_type, gender, birth_date, hire_date, id_card, phone, email,
                          dept_id, dept_name, title, position, specialty, education, avatar, is_expert, expert_price,
                          status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('897001022', 'H17022', '倪天', 1, 1, '1968-07-14', '1993-10-01', '330106196807148936', '15885567655',
        'nitian@cslk-his.local', '19580006', '神经内科', '401', '1', '脑血管病、癫痫、帕金森与认知障碍', '博士', NULL, 1,
        '100.00', 1, 'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_employee (id, emp_code, emp_name, emp_type, gender, birth_date, hire_date, id_card, phone, email,
                          dept_id, dept_name, title, position, specialty, education, avatar, is_expert, expert_price,
                          status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('897001023', 'H17023', '康亦航', 1, 1, '1975-02-06', '1998-04-01', '610113197502063910', '18230077707',
        'kangyihang@cslk-his.local', '19580006', '神经内科', '301', '2', '脑血管病、癫痫、帕金森与认知障碍', '本科', NULL,
        1, '50.00', 1, 'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_employee (id, emp_code, emp_name, emp_type, gender, birth_date, hire_date, id_card, phone, email,
                          dept_id, dept_name, title, position, specialty, education, avatar, is_expert, expert_price,
                          status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('897001024', 'H17024', '陈泽宇', 1, 1, '1982-11-02', '2008-08-01', '370102198211024175', '15148006601',
        'chenzeyu@cslk-his.local', '19580006', '神经内科', '201', '31', '脑血管病、癫痫、帕金森与认知障碍', '本科', NULL,
        0, '0.00', 1, 'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_employee (id, emp_code, emp_name, emp_type, gender, birth_date, hire_date, id_card, phone, email,
                          dept_id, dept_name, title, position, specialty, education, avatar, is_expert, expert_price,
                          status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('897001025', 'H17025', '范立诚', 1, 1, '1996-01-19', '2018-07-01', '610113199601190914', '18933475680',
        'fanlicheng@cslk-his.local', '19580006', '神经内科', '102', '6', '脑血管病、癫痫、帕金森与认知障碍', '硕士', NULL,
        0, '0.00', 1, 'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_employee (id, emp_code, emp_name, emp_type, gender, birth_date, hire_date, id_card, phone, email,
                          dept_id, dept_name, title, position, specialty, education, avatar, is_expert, expert_price,
                          status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('897001026', 'H17026', '徐和玉', 2, 2, '1978-07-21', '2002-11-01', '310104197807212000', '15096651082',
        'xuheyu@cslk-his.local', '19580006', '神经内科', '403', '9', '脑血管病、癫痫、帕金森与认知障碍', '本科', NULL, 0,
        '0.00', 1, 'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_employee (id, emp_code, emp_name, emp_type, gender, birth_date, hire_date, id_card, phone, email,
                          dept_id, dept_name, title, position, specialty, education, avatar, is_expert, expert_price,
                          status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('897001027', 'H17027', '金白露', 2, 2, '1989-01-20', '2012-02-01', '320106198901200507', '18296149924',
        'jinbailu@cslk-his.local', '19580006', '神经内科', '203', '15', '脑血管病、癫痫、帕金森与认知障碍', '大专', NULL,
        0, '0.00', 1, 'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_employee (id, emp_code, emp_name, emp_type, gender, birth_date, hire_date, id_card, phone, email,
                          dept_id, dept_name, title, position, specialty, education, avatar, is_expert, expert_price,
                          status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('897001028', 'H17028', '邓心怡', 2, 2, '1998-06-10', '2020-02-01', '320106199806109582', '17622893614',
        'dengxinyi@cslk-his.local', '19580006', '神经内科', '105', '15', '脑血管病、癫痫、帕金森与认知障碍', '中专', NULL,
        0, '0.00', 1, 'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_employee (id, emp_code, emp_name, emp_type, gender, birth_date, hire_date, id_card, phone, email,
                          dept_id, dept_name, title, position, specialty, education, avatar, is_expert, expert_price,
                          status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('897001029', 'H17029', '杨远航', 1, 1, '1971-10-20', '1995-03-01', '440305197110209713', '18745580049',
        'yangyuanhang@cslk-his.local', '19580007', '内分泌科', '401', '1', '糖尿病及并发症、甲状腺疾病、骨代谢', '博士',
        NULL, 1, '100.00', 1, 'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_employee (id, emp_code, emp_name, emp_type, gender, birth_date, hire_date, id_card, phone, email,
                          dept_id, dept_name, title, position, specialty, education, avatar, is_expert, expert_price,
                          status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('897001030', 'H17030', '肖蕾', 1, 2, '1975-08-24', '2000-12-01', '310104197508242568', '18284905772',
        'xiaolei@cslk-his.local', '19580007', '内分泌科', '301', '2', '糖尿病及并发症、甲状腺疾病、骨代谢', '硕士', NULL,
        1, '50.00', 1, 'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_employee (id, emp_code, emp_name, emp_type, gender, birth_date, hire_date, id_card, phone, email,
                          dept_id, dept_name, title, position, specialty, education, avatar, is_expert, expert_price,
                          status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('897001031', 'H17031', '田娜', 1, 2, '1989-08-14', '2012-01-01', '320106198908142442', '15047164980',
        'tianna@cslk-his.local', '19580007', '内分泌科', '201', '31', '糖尿病及并发症、甲状腺疾病、骨代谢', '本科', NULL,
        0, '0.00', 1, 'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_employee (id, emp_code, emp_name, emp_type, gender, birth_date, hire_date, id_card, phone, email,
                          dept_id, dept_name, title, position, specialty, education, avatar, is_expert, expert_price,
                          status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('897001032', 'H17032', '严树声', 1, 1, '1990-12-09', '2016-05-01', '440305199012098959', '15969331482',
        'yanshusheng@cslk-his.local', '19580007', '内分泌科', '102', '6', '糖尿病及并发症、甲状腺疾病、骨代谢', '硕士',
        NULL, 0, '0.00', 1, 'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_employee (id, emp_code, emp_name, emp_type, gender, birth_date, hire_date, id_card, phone, email,
                          dept_id, dept_name, title, position, specialty, education, avatar, is_expert, expert_price,
                          status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('897001033', 'H17033', '向宜修', 2, 2, '1983-10-19', '2007-03-01', '320106198310196403', '18695517975',
        'xiangyixiu@cslk-his.local', '19580007', '内分泌科', '403', '9', '糖尿病及并发症、甲状腺疾病、骨代谢', '大专',
        NULL, 0, '0.00', 1, 'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_employee (id, emp_code, emp_name, emp_type, gender, birth_date, hire_date, id_card, phone, email,
                          dept_id, dept_name, title, position, specialty, education, avatar, is_expert, expert_price,
                          status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('897001034', 'H17034', '罗明珠', 2, 2, '1988-07-13', '2013-04-01', '110105198807132207', '18941456872',
        'luomingzhu@cslk-his.local', '19580007', '内分泌科', '203', '15', '糖尿病及并发症、甲状腺疾病、骨代谢', '大专',
        NULL, 0, '0.00', 1, 'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_employee (id, emp_code, emp_name, emp_type, gender, birth_date, hire_date, id_card, phone, email,
                          dept_id, dept_name, title, position, specialty, education, avatar, is_expert, expert_price,
                          status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('897001035', 'H17035', '冯明', 2, 1, '1995-11-11', '2020-05-01', '210102199511110534', '13614692913',
        'fengming@cslk-his.local', '19580007', '内分泌科', '105', '15', '糖尿病及并发症、甲状腺疾病、骨代谢', '中专',
        NULL, 0, '0.00', 1, 'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_employee (id, emp_code, emp_name, emp_type, gender, birth_date, hire_date, id_card, phone, email,
                          dept_id, dept_name, title, position, specialty, education, avatar, is_expert, expert_price,
                          status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('897001036', 'H17036', '郭怀瑾', 1, 1, '1973-01-16', '1995-02-01', '210102197301160534', '15882759945',
        'guohuaijin@cslk-his.local', '19580008', '肾内科', '401', '1', '慢性肾病、血液净化、肾脏免疫', '博士', NULL, 1,
        '100.00', 1, 'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_employee (id, emp_code, emp_name, emp_type, gender, birth_date, hire_date, id_card, phone, email,
                          dept_id, dept_name, title, position, specialty, education, avatar, is_expert, expert_price,
                          status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('897001037', 'H17037', '许婷', 1, 2, '1980-03-10', '2002-01-01', '110105198003104606', '15951803246',
        'xushuting@cslk-his.local', '19580008', '肾内科', '301', '2', '慢性肾病、血液净化、肾脏免疫', '博士', NULL, 1,
        '50.00', 1, 'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_employee (id, emp_code, emp_name, emp_type, gender, birth_date, hire_date, id_card, phone, email,
                          dept_id, dept_name, title, position, specialty, education, avatar, is_expert, expert_price,
                          status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('897001038', 'H17038', '容纲', 1, 1, '1983-10-26', '2008-10-01', '420106198310264430', '18968426762',
        'ronggang@cslk-his.local', '19580008', '肾内科', '201', '31', '慢性肾病、血液净化、肾脏免疫', '本科', NULL, 0,
        '0.00', 1, 'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_employee (id, emp_code, emp_name, emp_type, gender, birth_date, hire_date, id_card, phone, email,
                          dept_id, dept_name, title, position, specialty, education, avatar, is_expert, expert_price,
                          status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('897001039', 'H17039', '毛采薇', 1, 2, '1992-08-06', '2017-08-01', '110105199208060365', '18845944218',
        'maocaiw@cslk-his.local', '19580008', '肾内科', '102', '6', '慢性肾病、血液净化、肾脏免疫', '本科', NULL, 0,
        '0.00', 1, 'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_employee (id, emp_code, emp_name, emp_type, gender, birth_date, hire_date, id_card, phone, email,
                          dept_id, dept_name, title, position, specialty, education, avatar, is_expert, expert_price,
                          status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('897001040', 'H17040', '洪晚晴', 2, 2, '1985-07-13', '2008-01-01', '310104198507133129', '15120866304',
        'hongwanqing@cslk-his.local', '19580008', '肾内科', '403', '9', '慢性肾病、血液净化、肾脏免疫', '本科', NULL, 0,
        '0.00', 1, 'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_employee (id, emp_code, emp_name, emp_type, gender, birth_date, hire_date, id_card, phone, email,
                          dept_id, dept_name, title, position, specialty, education, avatar, is_expert, expert_price,
                          status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('897001041', 'H17041', '沈舒', 2, 2, '1986-10-17', '2011-09-01', '32010619861017912X', '17838745255',
        'shenshu@cslk-his.local', '19580008', '肾内科', '203', '15', '慢性肾病、血液净化、肾脏免疫', '大专', NULL, 0,
        '0.00', 1, 'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_employee (id, emp_code, emp_name, emp_type, gender, birth_date, hire_date, id_card, phone, email,
                          dept_id, dept_name, title, position, specialty, education, avatar, is_expert, expert_price,
                          status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('897001042', 'H17042', '施静', 2, 2, '2001-02-23', '2025-06-01', '330106200102232344', '18683741596',
        'shiqijing@cslk-his.local', '19580008', '肾内科', '105', '15', '慢性肾病、血液净化、肾脏免疫', '本科', NULL, 0,
        '0.00', 1, 'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_employee (id, emp_code, emp_name, emp_type, gender, birth_date, hire_date, id_card, phone, email,
                          dept_id, dept_name, title, position, specialty, education, avatar, is_expert, expert_price,
                          status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('897001043', 'H17043', '吕明德', 1, 1, '1966-05-01', '1991-08-01', '320106196605014516', '15753239355',
        'lvmingde@cslk-his.local', '19580009', '血液内科', '401', '1', '白血病、淋巴瘤、出凝血疾病', '硕士', NULL, 1,
        '100.00', 1, 'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_employee (id, emp_code, emp_name, emp_type, gender, birth_date, hire_date, id_card, phone, email,
                          dept_id, dept_name, title, position, specialty, education, avatar, is_expert, expert_price,
                          status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('897001044', 'H17044', '崔紫涵', 1, 2, '1974-08-17', '1998-04-01', '420106197408173946', '15210052614',
        'cuizihan@cslk-his.local', '19580009', '血液内科', '301', '2', '白血病、淋巴瘤、出凝血疾病', '本科', NULL, 1,
        '50.00', 1, 'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_employee (id, emp_code, emp_name, emp_type, gender, birth_date, hire_date, id_card, phone, email,
                          dept_id, dept_name, title, position, specialty, education, avatar, is_expert, expert_price,
                          status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('897001045', 'H17045', '尹一诺', 1, 2, '1989-10-27', '2012-05-01', '320106198910272182', '19978780143',
        'yinyinuo@cslk-his.local', '19580009', '血液内科', '201', '31', '白血病、淋巴瘤、出凝血疾病', '本科', NULL, 0,
        '0.00', 1, 'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_employee (id, emp_code, emp_name, emp_type, gender, birth_date, hire_date, id_card, phone, email,
                          dept_id, dept_name, title, position, specialty, education, avatar, is_expert, expert_price,
                          status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('897001046', 'H17046', '苏乐怡', 1, 2, '1997-10-07', '2022-12-01', '510107199710070320', '15859053225',
        'suleyi@cslk-his.local', '19580009', '血液内科', '102', '6', '白血病、淋巴瘤、出凝血疾病', '硕士', NULL, 0,
        '0.00', 1, 'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_employee (id, emp_code, emp_name, emp_type, gender, birth_date, hire_date, id_card, phone, email,
                          dept_id, dept_name, title, position, specialty, education, avatar, is_expert, expert_price,
                          status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('897001047', 'H17047', '黄蓉', 2, 2, '1984-11-09', '2009-12-01', '510107198411099321', '15227657743',
        'huangrong@cslk-his.local', '19580009', '血液内科', '403', '9', '白血病、淋巴瘤、出凝血疾病', '本科', NULL, 0,
        '0.00', 1, 'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_employee (id, emp_code, emp_name, emp_type, gender, birth_date, hire_date, id_card, phone, email,
                          dept_id, dept_name, title, position, specialty, education, avatar, is_expert, expert_price,
                          status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('897001048', 'H17048', '董宜修', 2, 2, '1989-03-18', '2013-12-01', '210102198903180166', '15226015266',
        'dongyixiu@cslk-his.local', '19580009', '血液内科', '203', '15', '白血病、淋巴瘤、出凝血疾病', '大专', NULL, 0,
        '0.00', 1, 'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_employee (id, emp_code, emp_name, emp_type, gender, birth_date, hire_date, id_card, phone, email,
                          dept_id, dept_name, title, position, specialty, education, avatar, is_expert, expert_price,
                          status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('897001049', 'H17049', '苏秀英', 2, 2, '1997-11-06', '2020-12-01', '110105199711069586', '17690277099',
        'suxiuying@cslk-his.local', '19580009', '血液内科', '105', '15', '白血病、淋巴瘤、出凝血疾病', '大专', NULL, 0,
        '0.00', 0, 'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_employee (id, emp_code, emp_name, emp_type, gender, birth_date, hire_date, id_card, phone, email,
                          dept_id, dept_name, title, position, specialty, education, avatar, is_expert, expert_price,
                          status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('897001050', 'H17050', '胡正', 1, 1, '1972-01-06', '1996-12-01', '420106197201065630', '13837285782',
        'huzheng@cslk-his.local', '19580010', '风湿免疫科', '401', '1', '类风湿关节炎、系统性红斑狼疮', '硕士', NULL, 1,
        '100.00', 1, 'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_employee (id, emp_code, emp_name, emp_type, gender, birth_date, hire_date, id_card, phone, email,
                          dept_id, dept_name, title, position, specialty, education, avatar, is_expert, expert_price,
                          status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('897001051', 'H17051', '容明珠', 1, 2, '1979-01-28', '2001-10-01', '510107197901288585', '15269806255',
        'rongmingzhu@cslk-his.local', '19580010', '风湿免疫科', '301', '2', '类风湿关节炎、系统性红斑狼疮', '博士', NULL,
        1, '50.00', 1, 'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_employee (id, emp_code, emp_name, emp_type, gender, birth_date, hire_date, id_card, phone, email,
                          dept_id, dept_name, title, position, specialty, education, avatar, is_expert, expert_price,
                          status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('897001052', 'H17052', '林映竹', 1, 2, '1992-02-11', '2014-08-01', '210102199202113387', '18692268521',
        'linyingzhu@cslk-his.local', '19580010', '风湿免疫科', '201', '31', '类风湿关节炎、系统性红斑狼疮', '本科', NULL,
        0, '0.00', 1, 'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_employee (id, emp_code, emp_name, emp_type, gender, birth_date, hire_date, id_card, phone, email,
                          dept_id, dept_name, title, position, specialty, education, avatar, is_expert, expert_price,
                          status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('897001053', 'H17053', '卢升', 1, 1, '1996-09-15', '2018-06-01', '420106199609155014', '18899277487',
        'lusheng@cslk-his.local', '19580010', '风湿免疫科', '102', '6', '类风湿关节炎、系统性红斑狼疮', '本科', NULL, 0,
        '0.00', 1, 'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_employee (id, emp_code, emp_name, emp_type, gender, birth_date, hire_date, id_card, phone, email,
                          dept_id, dept_name, title, position, specialty, education, avatar, is_expert, expert_price,
                          status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('897001054', 'H17054', '朱婷', 2, 2, '1985-07-17', '2008-02-01', '370102198507178966', '15797421764',
        'zhuting@cslk-his.local', '19580010', '风湿免疫科', '403', '9', '类风湿关节炎、系统性红斑狼疮', '本科', NULL, 0,
        '0.00', 1, 'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_employee (id, emp_code, emp_name, emp_type, gender, birth_date, hire_date, id_card, phone, email,
                          dept_id, dept_name, title, position, specialty, education, avatar, is_expert, expert_price,
                          status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('897001055', 'H17055', '莫琪', 2, 2, '1986-06-02', '2010-12-01', '420106198606020144', '15214880204',
        'moqi@cslk-his.local', '19580010', '风湿免疫科', '203', '15', '类风湿关节炎、系统性红斑狼疮', '大专', NULL, 0,
        '0.00', 1, 'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_employee (id, emp_code, emp_name, emp_type, gender, birth_date, hire_date, id_card, phone, email,
                          dept_id, dept_name, title, position, specialty, education, avatar, is_expert, expert_price,
                          status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('897001056', 'H17056', '曾心怡', 2, 2, '1994-10-03', '2019-10-01', '110105199410034283', '15731066745',
        'zengxinyi@cslk-his.local', '19580010', '风湿免疫科', '105', '15', '类风湿关节炎、系统性红斑狼疮', '大专', NULL,
        0, '0.00', 1, 'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_employee (id, emp_code, emp_name, emp_type, gender, birth_date, hire_date, id_card, phone, email,
                          dept_id, dept_name, title, position, specialty, education, avatar, is_expert, expert_price,
                          status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('897001057', 'H17057', '钱松', 1, 1, '1968-01-21', '1990-05-01', '210102196801215293', '15825955399',
        'qiansong@cslk-his.local', '19580011', '老年病科', '401', '1', '老年综合评估、多病共存与共病用药', '硕士', NULL,
        1, '100.00', 1, 'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_employee (id, emp_code, emp_name, emp_type, gender, birth_date, hire_date, id_card, phone, email,
                          dept_id, dept_name, title, position, specialty, education, avatar, is_expert, expert_price,
                          status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('897001058', 'H17058', '郑海艳', 1, 2, '1976-04-16', '1999-05-01', '32010619760416754X', '18684457886',
        'zhenghaiyan@cslk-his.local', '19580011', '老年病科', '301', '2', '老年综合评估、多病共存与共病用药', '硕士',
        NULL, 1, '50.00', 1, 'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_employee (id, emp_code, emp_name, emp_type, gender, birth_date, hire_date, id_card, phone, email,
                          dept_id, dept_name, title, position, specialty, education, avatar, is_expert, expert_price,
                          status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('897001059', 'H17059', '黄延平', 1, 1, '1987-05-25', '2010-05-01', '310104198705252372', '13583989492',
        'huangyanping@cslk-his.local', '19580011', '老年病科', '201', '31', '老年综合评估、多病共存与共病用药', '硕士',
        NULL, 0, '0.00', 1, 'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_employee (id, emp_code, emp_name, emp_type, gender, birth_date, hire_date, id_card, phone, email,
                          dept_id, dept_name, title, position, specialty, education, avatar, is_expert, expert_price,
                          status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('897001060', 'H17060', '郝如初', 1, 2, '1994-07-05', '2017-05-01', '210102199407053082', '15031451098',
        'haoruchu@cslk-his.local', '19580011', '老年病科', '102', '6', '老年综合评估、多病共存与共病用药', '本科', NULL,
        0, '0.00', 1, 'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_employee (id, emp_code, emp_name, emp_type, gender, birth_date, hire_date, id_card, phone, email,
                          dept_id, dept_name, title, position, specialty, education, avatar, is_expert, expert_price,
                          status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('897001061', 'H17061', '何敏', 2, 2, '1983-12-09', '2007-11-01', '210102198312095146', '18729183845',
        'hemin@cslk-his.local', '19580011', '老年病科', '403', '9', '老年综合评估、多病共存与共病用药', '本科', NULL, 0,
        '0.00', 1, 'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_employee (id, emp_code, emp_name, emp_type, gender, birth_date, hire_date, id_card, phone, email,
                          dept_id, dept_name, title, position, specialty, education, avatar, is_expert, expert_price,
                          status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('897001062', 'H17062', '于彤', 2, 2, '1994-10-20', '2019-03-01', '21010219941020200X', '15062332979',
        'yutong@cslk-his.local', '19580011', '老年病科', '203', '15', '老年综合评估、多病共存与共病用药', '大专', NULL,
        0, '0.00', 1, 'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_employee (id, emp_code, emp_name, emp_type, gender, birth_date, hire_date, id_card, phone, email,
                          dept_id, dept_name, title, position, specialty, education, avatar, is_expert, expert_price,
                          status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('897001063', 'H17063', '蔡念安', 2, 2, '1996-11-09', '2019-11-01', '320106199611094843', '17620309306',
        'cainianan@cslk-his.local', '19580011', '老年病科', '105', '15', '老年综合评估、多病共存与共病用药', '中专',
        NULL, 0, '0.00', 1, 'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_employee (id, emp_code, emp_name, emp_type, gender, birth_date, hire_date, id_card, phone, email,
                          dept_id, dept_name, title, position, specialty, education, avatar, is_expert, expert_price,
                          status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('897001064', 'H17064', '孙树声', 1, 1, '1971-04-19', '1993-07-01', '370102197104193550', '18992609194',
        'sunshusheng@cslk-his.local', '19580086', '全科医学科', '401', '1', '未分化疾病、慢病管理、家庭医疗', '硕士',
        NULL, 1, '100.00', 1, 'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_employee (id, emp_code, emp_name, emp_type, gender, birth_date, hire_date, id_card, phone, email,
                          dept_id, dept_name, title, position, specialty, education, avatar, is_expert, expert_price,
                          status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('897001065', 'H17065', '冯若彤', 1, 2, '1975-03-15', '1998-05-01', '330106197503158267', '18832984267',
        'fengruotong@cslk-his.local', '19580086', '全科医学科', '301', '2', '未分化疾病、慢病管理、家庭医疗', '硕士',
        NULL, 1, '50.00', 1, 'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_employee (id, emp_code, emp_name, emp_type, gender, birth_date, hire_date, id_card, phone, email,
                          dept_id, dept_name, title, position, specialty, education, avatar, is_expert, expert_price,
                          status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('897001066', 'H17066', '林一诺', 1, 2, '1988-07-02', '2010-05-01', '610113198807023228', '18679377117',
        'linyinuo@cslk-his.local', '19580086', '全科医学科', '201', '31', '未分化疾病、慢病管理、家庭医疗', '本科', NULL,
        0, '0.00', 1, 'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_employee (id, emp_code, emp_name, emp_type, gender, birth_date, hire_date, id_card, phone, email,
                          dept_id, dept_name, title, position, specialty, education, avatar, is_expert, expert_price,
                          status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('897001067', 'H17067', '孟鹏飞', 1, 1, '1998-02-23', '2020-09-01', '61011319980223619X', '18997077786',
        'mengpengfei@cslk-his.local', '19580086', '全科医学科', '102', '6', '未分化疾病、慢病管理、家庭医疗', '本科',
        NULL, 0, '0.00', 1, 'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_employee (id, emp_code, emp_name, emp_type, gender, birth_date, hire_date, id_card, phone, email,
                          dept_id, dept_name, title, position, specialty, education, avatar, is_expert, expert_price,
                          status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('897001068', 'H17068', '裘佳', 2, 2, '1988-02-07', '2012-09-01', '320106198802071906', '13950989005',
        'qiuqiujia@cslk-his.local', '19580086', '全科医学科', '403', '9', '未分化疾病、慢病管理、家庭医疗', '大专', NULL,
        0, '0.00', 1, 'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_employee (id, emp_code, emp_name, emp_type, gender, birth_date, hire_date, id_card, phone, email,
                          dept_id, dept_name, title, position, specialty, education, avatar, is_expert, expert_price,
                          status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('897001069', 'H17069', '贺青禾', 2, 2, '1991-10-04', '2015-01-01', '440305199110044241', '18663772207',
        'hesunqinghe@cslk-his.local', '19580086', '全科医学科', '203', '15', '未分化疾病、慢病管理、家庭医疗', '大专',
        NULL, 0, '0.00', 1, 'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_employee (id, emp_code, emp_name, emp_type, gender, birth_date, hire_date, id_card, phone, email,
                          dept_id, dept_name, title, position, specialty, education, avatar, is_expert, expert_price,
                          status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('897001070', 'H17070', '侯慧', 2, 2, '2001-11-24', '2025-08-01', '42010620011124172X', '18551899634',
        'houhui@cslk-his.local', '19580086', '全科医学科', '105', '15', '未分化疾病、慢病管理、家庭医疗', '本科', NULL, 0,
        '0.00', 1, 'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_employee (id, emp_code, emp_name, emp_type, gender, birth_date, hire_date, id_card, phone, email,
                          dept_id, dept_name, title, position, specialty, education, avatar, is_expert, expert_price,
                          status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('897001071', 'H17071', '肖致远', 1, 1, '1971-06-07', '1994-02-01', '440305197106077359', '13516379475',
        'xiaozhiyuan2@cslk-his.local', '19580013', '普通外科', '401', '1', '胃肠肿瘤、腹腔镜手术、疝与腹壁', '博士', NULL,
        1, '100.00', 1, 'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_employee (id, emp_code, emp_name, emp_type, gender, birth_date, hire_date, id_card, phone, email,
                          dept_id, dept_name, title, position, specialty, education, avatar, is_expert, expert_price,
                          status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('897001072', 'H17072', '罗知远', 1, 1, '1978-01-20', '2000-09-01', '320106197801208638', '18669135745',
        'luozhiyuan@cslk-his.local', '19580013', '普通外科', '301', '2', '胃肠肿瘤、腹腔镜手术、疝与腹壁', '硕士', NULL,
        1, '50.00', 1, 'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_employee (id, emp_code, emp_name, emp_type, gender, birth_date, hire_date, id_card, phone, email,
                          dept_id, dept_name, title, position, specialty, education, avatar, is_expert, expert_price,
                          status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('897001073', 'H17073', '谢立诚', 1, 1, '1983-02-27', '2008-03-01', '42010619830227927X', '15081441637',
        'xielicheng@cslk-his.local', '19580013', '普通外科', '201', '31', '胃肠肿瘤、腹腔镜手术、疝与腹壁', '本科', NULL,
        0, '0.00', 1, 'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_employee (id, emp_code, emp_name, emp_type, gender, birth_date, hire_date, id_card, phone, email,
                          dept_id, dept_name, title, position, specialty, education, avatar, is_expert, expert_price,
                          status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('897001074', 'H17074', '徐庆', 1, 1, '1998-03-13', '2021-02-01', '440305199803132818', '13830650136',
        'xuqing@cslk-his.local', '19580013', '普通外科', '102', '6', '胃肠肿瘤、腹腔镜手术、疝与腹壁', '硕士', NULL, 0,
        '0.00', 1, 'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_employee (id, emp_code, emp_name, emp_type, gender, birth_date, hire_date, id_card, phone, email,
                          dept_id, dept_name, title, position, specialty, education, avatar, is_expert, expert_price,
                          status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('897001075', 'H17075', '沈淑华', 2, 2, '1980-11-15', '2004-05-01', '610113198011150328', '15268187037',
        'shenshuhua@cslk-his.local', '19580013', '普通外科', '403', '9', '胃肠肿瘤、腹腔镜手术、疝与腹壁', '大专', NULL,
        0, '0.00', 1, 'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_employee (id, emp_code, emp_name, emp_type, gender, birth_date, hire_date, id_card, phone, email,
                          dept_id, dept_name, title, position, specialty, education, avatar, is_expert, expert_price,
                          status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('897001076', 'H17076', '秦舒', 2, 2, '1985-07-11', '2008-09-01', '440305198507111209', '13668011358',
        'qinshu@cslk-his.local', '19580013', '普通外科', '203', '15', '胃肠肿瘤、腹腔镜手术、疝与腹壁', '本科', NULL, 0,
        '0.00', 1, 'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_employee (id, emp_code, emp_name, emp_type, gender, birth_date, hire_date, id_card, phone, email,
                          dept_id, dept_name, title, position, specialty, education, avatar, is_expert, expert_price,
                          status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('897001077', 'H17077', '康子轩', 2, 1, '1998-09-08', '2023-11-01', '320106199809085774', '18522299129',
        'kangzixuan@cslk-his.local', '19580013', '普通外科', '105', '15', '胃肠肿瘤、腹腔镜手术、疝与腹壁', '本科', NULL,
        0, '0.00', 1, 'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_employee (id, emp_code, emp_name, emp_type, gender, birth_date, hire_date, id_card, phone, email,
                          dept_id, dept_name, title, position, specialty, education, avatar, is_expert, expert_price,
                          status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('897001078', 'H17078', '杜致远', 1, 1, '1970-05-16', '1992-05-01', '370102197005162273', '15869829525',
        'duzhiyuan2@cslk-his.local', '19580014', '骨科', '401', '1', '关节置换、脊柱外科、创伤骨科', '博士', NULL, 1,
        '100.00', 1, 'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_employee (id, emp_code, emp_name, emp_type, gender, birth_date, hire_date, id_card, phone, email,
                          dept_id, dept_name, title, position, specialty, education, avatar, is_expert, expert_price,
                          status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('897001079', 'H17079', '许成', 1, 1, '1982-04-27', '2005-03-01', '420106198204273552', '13722829625',
        'xushuchengda@cslk-his.local', '19580014', '骨科', '301', '2', '关节置换、脊柱外科、创伤骨科', '博士', NULL, 1,
        '50.00', 1, 'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_employee (id, emp_code, emp_name, emp_type, gender, birth_date, hire_date, id_card, phone, email,
                          dept_id, dept_name, title, position, specialty, education, avatar, is_expert, expert_price,
                          status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('897001080', 'H17080', '叶涛', 1, 1, '1985-06-04', '2008-04-01', '210102198506049754', '13565627644',
        'yetao@cslk-his.local', '19580014', '骨科', '201', '31', '关节置换、脊柱外科、创伤骨科', '本科', NULL, 0, '0.00',
        1, 'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_employee (id, emp_code, emp_name, emp_type, gender, birth_date, hire_date, id_card, phone, email,
                          dept_id, dept_name, title, position, specialty, education, avatar, is_expert, expert_price,
                          status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('897001081', 'H17081', '刘山', 1, 1, '1993-03-03', '2016-02-01', '110105199303032215', '13785060495',
        'liushan@cslk-his.local', '19580014', '骨科', '102', '6', '关节置换、脊柱外科、创伤骨科', '本科', NULL, 0, '0.00',
        1, 'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_employee (id, emp_code, emp_name, emp_type, gender, birth_date, hire_date, id_card, phone, email,
                          dept_id, dept_name, title, position, specialty, education, avatar, is_expert, expert_price,
                          status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('897001082', 'H17082', '康雪', 2, 2, '1984-08-19', '2008-07-01', '330106198408193587', '17612619380',
        'kangxueli@cslk-his.local', '19580014', '骨科', '403', '9', '关节置换、脊柱外科、创伤骨科', '大专', NULL, 0,
        '0.00', 1, 'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_employee (id, emp_code, emp_name, emp_type, gender, birth_date, hire_date, id_card, phone, email,
                          dept_id, dept_name, title, position, specialty, education, avatar, is_expert, expert_price,
                          status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('897001083', 'H17083', '董欣', 2, 2, '1990-10-24', '2015-06-01', '440305199010246305', '18784117288',
        'dongxin@cslk-his.local', '19580014', '骨科', '203', '15', '关节置换、脊柱外科、创伤骨科', '大专', NULL, 0,
        '0.00', 1, 'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_employee (id, emp_code, emp_name, emp_type, gender, birth_date, hire_date, id_card, phone, email,
                          dept_id, dept_name, title, position, specialty, education, avatar, is_expert, expert_price,
                          status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('897001084', 'H17084', '姚丽', 2, 2, '1996-12-22', '2018-12-01', '210102199612224127', '15248694652',
        'yaoli@cslk-his.local', '19580014', '骨科', '105', '15', '关节置换、脊柱外科、创伤骨科', '本科', NULL, 0, '0.00',
        1, 'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_employee (id, emp_code, emp_name, emp_type, gender, birth_date, hire_date, id_card, phone, email,
                          dept_id, dept_name, title, position, specialty, education, avatar, is_expert, expert_price,
                          status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('897001085', 'H17085', '谢晨曦', 1, 1, '1970-02-01', '1994-04-01', '210102197002010456', '13853576940',
        'xiechenxi@cslk-his.local', '19580015', '神经外科', '401', '1', '颅内肿瘤、脑血管外科、脊髓疾病', '博士', NULL, 1,
        '100.00', 1, 'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_employee (id, emp_code, emp_name, emp_type, gender, birth_date, hire_date, id_card, phone, email,
                          dept_id, dept_name, title, position, specialty, education, avatar, is_expert, expert_price,
                          status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('897001086', 'H17086', '谭玲', 1, 2, '1981-04-17', '2003-12-01', '440305198104171100', '18573907663',
        'tanling@cslk-his.local', '19580015', '神经外科', '301', '2', '颅内肿瘤、脑血管外科、脊髓疾病', '博士', NULL, 1,
        '50.00', 1, 'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_employee (id, emp_code, emp_name, emp_type, gender, birth_date, hire_date, id_card, phone, email,
                          dept_id, dept_name, title, position, specialty, education, avatar, is_expert, expert_price,
                          status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('897001087', 'H17087', '宋维', 1, 1, '1991-09-06', '2014-01-01', '610113199109065353', '18016566916',
        'songweiqi@cslk-his.local', '19580015', '神经外科', '201', '31', '颅内肿瘤、脑血管外科、脊髓疾病', '硕士', NULL,
        0, '0.00', 1, 'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_employee (id, emp_code, emp_name, emp_type, gender, birth_date, hire_date, id_card, phone, email,
                          dept_id, dept_name, title, position, specialty, education, avatar, is_expert, expert_price,
                          status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('897001088', 'H17088', '陆成', 1, 1, '1991-12-12', '2016-12-01', '310104199112127757', '19886999354',
        'lukuchengda@cslk-his.local', '19580015', '神经外科', '102', '6', '颅内肿瘤、脑血管外科、脊髓疾病', '本科', NULL,
        0, '0.00', 1, 'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_employee (id, emp_code, emp_name, emp_type, gender, birth_date, hire_date, id_card, phone, email,
                          dept_id, dept_name, title, position, specialty, education, avatar, is_expert, expert_price,
                          status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('897001089', 'H17089', '万欣', 2, 2, '1982-09-03', '2005-02-01', '210102198209039883', '15884751459',
        'wanxin@cslk-his.local', '19580015', '神经外科', '403', '9', '颅内肿瘤、脑血管外科、脊髓疾病', '本科', NULL, 0,
        '0.00', 0, 'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_employee (id, emp_code, emp_name, emp_type, gender, birth_date, hire_date, id_card, phone, email,
                          dept_id, dept_name, title, position, specialty, education, avatar, is_expert, expert_price,
                          status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('897001090', 'H17090', '沈倩', 2, 2, '1985-04-09', '2009-01-01', '610113198504091186', '18288400036',
        'shenqian@cslk-his.local', '19580015', '神经外科', '203', '15', '颅内肿瘤、脑血管外科、脊髓疾病', '本科', NULL, 0,
        '0.00', 1, 'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_employee (id, emp_code, emp_name, emp_type, gender, birth_date, hire_date, id_card, phone, email,
                          dept_id, dept_name, title, position, specialty, education, avatar, is_expert, expert_price,
                          status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('897001091', 'H17091', '蒋一诺', 2, 2, '2000-03-21', '2022-04-01', '370102200003215583', '15026172368',
        'jiangyinuo@cslk-his.local', '19580015', '神经外科', '105', '15', '颅内肿瘤、脑血管外科、脊髓疾病', '中专', NULL,
        0, '0.00', 1, 'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_employee (id, emp_code, emp_name, emp_type, gender, birth_date, hire_date, id_card, phone, email,
                          dept_id, dept_name, title, position, specialty, education, avatar, is_expert, expert_price,
                          status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('897001092', 'H17092', '廖明德', 1, 1, '1973-08-04', '1995-04-01', '210102197308041538', '15963464107',
        'liaomingde@cslk-his.local', '19580016', '心胸外科', '401', '1', '冠脉搭桥、肺结节微创手术、食管疾病', '硕士',
        NULL, 1, '100.00', 1, 'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_employee (id, emp_code, emp_name, emp_type, gender, birth_date, hire_date, id_card, phone, email,
                          dept_id, dept_name, title, position, specialty, education, avatar, is_expert, expert_price,
                          status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('897001093', 'H17093', '蔡婧', 1, 2, '1975-06-10', '2000-03-01', '510107197506100941', '18077434833',
        'caijingjuan@cslk-his.local', '19580016', '心胸外科', '301', '2', '冠脉搭桥、肺结节微创手术、食管疾病', '本科',
        NULL, 1, '50.00', 1, 'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_employee (id, emp_code, emp_name, emp_type, gender, birth_date, hire_date, id_card, phone, email,
                          dept_id, dept_name, title, position, specialty, education, avatar, is_expert, expert_price,
                          status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('897001094', 'H17094', '孟子轩', 1, 1, '1991-10-03', '2013-12-01', '370102199110031610', '13570398513',
        'mengzixuan@cslk-his.local', '19580016', '心胸外科', '201', '31', '冠脉搭桥、肺结节微创手术、食管疾病', '硕士',
        NULL, 0, '0.00', 1, 'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_employee (id, emp_code, emp_name, emp_type, gender, birth_date, hire_date, id_card, phone, email,
                          dept_id, dept_name, title, position, specialty, education, avatar, is_expert, expert_price,
                          status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('897001095', 'H17095', '江梦洁', 1, 2, '1995-04-24', '2018-05-01', '320106199504245685', '18831475009',
        'jiangsmengjie@cslk-his.local', '19580016', '心胸外科', '102', '6', '冠脉搭桥、肺结节微创手术、食管疾病', '本科',
        NULL, 0, '0.00', 1, 'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_employee (id, emp_code, emp_name, emp_type, gender, birth_date, hire_date, id_card, phone, email,
                          dept_id, dept_name, title, position, specialty, education, avatar, is_expert, expert_price,
                          status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('897001096', 'H17096', '钱语嫣', 2, 2, '1988-12-14', '2011-07-01', '320106198812143387', '13929317682',
        'qianyuyan@cslk-his.local', '19580016', '心胸外科', '403', '9', '冠脉搭桥、肺结节微创手术、食管疾病', '本科',
        NULL, 0, '0.00', 1, 'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_employee (id, emp_code, emp_name, emp_type, gender, birth_date, hire_date, id_card, phone, email,
                          dept_id, dept_name, title, position, specialty, education, avatar, is_expert, expert_price,
                          status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('897001097', 'H17097', '许子轩', 2, 1, '1988-02-12', '2013-01-01', '11010519880212577X', '15064078700',
        'xushuzixuan@cslk-his.local', '19580016', '心胸外科', '203', '15', '冠脉搭桥、肺结节微创手术、食管疾病', '大专',
        NULL, 0, '0.00', 1, 'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_employee (id, emp_code, emp_name, emp_type, gender, birth_date, hire_date, id_card, phone, email,
                          dept_id, dept_name, title, position, specialty, education, avatar, is_expert, expert_price,
                          status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('897001098', 'H17098', '邱国强', 2, 1, '2001-06-11', '2025-01-01', '440305200106112673', '13686453903',
        'qiuguoqiang@cslk-his.local', '19580016', '心胸外科', '105', '15', '冠脉搭桥、肺结节微创手术、食管疾病', '中专',
        NULL, 0, '0.00', 1, 'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_employee (id, emp_code, emp_name, emp_type, gender, birth_date, hire_date, id_card, phone, email,
                          dept_id, dept_name, title, position, specialty, education, avatar, is_expert, expert_price,
                          status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('897001099', 'H17099', '肖乐怡', 1, 2, '1973-04-14', '1996-12-01', '330106197304149149', '15062877393',
        'xiaoleyi@cslk-his.local', '19580017', '泌尿外科', '401', '1', '泌尿系结石、前列腺疾病、肾肿瘤', '硕士', NULL, 1,
        '100.00', 1, 'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_employee (id, emp_code, emp_name, emp_type, gender, birth_date, hire_date, id_card, phone, email,
                          dept_id, dept_name, title, position, specialty, education, avatar, is_expert, expert_price,
                          status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('897001100', 'H17100', '魏妍', 1, 2, '1980-07-05', '2005-06-01', '510107198007056960', '13880382068',
        'weiyan@cslk-his.local', '19580017', '泌尿外科', '301', '2', '泌尿系结石、前列腺疾病、肾肿瘤', '硕士', NULL, 1,
        '50.00', 1, 'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_employee (id, emp_code, emp_name, emp_type, gender, birth_date, hire_date, id_card, phone, email,
                          dept_id, dept_name, title, position, specialty, education, avatar, is_expert, expert_price,
                          status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('897001101', 'H17101', '马远航', 1, 1, '1988-03-08', '2012-07-01', '510107198803084751', '18073304080',
        'mayuanhang@cslk-his.local', '19580017', '泌尿外科', '201', '31', '泌尿系结石、前列腺疾病、肾肿瘤', '硕士', NULL,
        0, '0.00', 1, 'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_employee (id, emp_code, emp_name, emp_type, gender, birth_date, hire_date, id_card, phone, email,
                          dept_id, dept_name, title, position, specialty, education, avatar, is_expert, expert_price,
                          status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('897001102', 'H17102', '吴佳', 1, 2, '1994-02-20', '2019-04-01', '44030519940220258X', '18619623111',
        'wujia@cslk-his.local', '19580017', '泌尿外科', '102', '6', '泌尿系结石、前列腺疾病、肾肿瘤', '本科', NULL, 0,
        '0.00', 1, 'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_employee (id, emp_code, emp_name, emp_type, gender, birth_date, hire_date, id_card, phone, email,
                          dept_id, dept_name, title, position, specialty, education, avatar, is_expert, expert_price,
                          status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('897001103', 'H17103', '冯青禾', 2, 2, '1980-11-12', '2005-04-01', '420106198011128260', '19810391141',
        'fengqinghe@cslk-his.local', '19580017', '泌尿外科', '403', '9', '泌尿系结石、前列腺疾病、肾肿瘤', '大专', NULL,
        0, '0.00', 1, 'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_employee (id, emp_code, emp_name, emp_type, gender, birth_date, hire_date, id_card, phone, email,
                          dept_id, dept_name, title, position, specialty, education, avatar, is_expert, expert_price,
                          status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('897001104', 'H17104', '杨语嫣', 2, 2, '1994-08-07', '2016-07-01', '21010219940807968X', '18928529665',
        'yangyuyan@cslk-his.local', '19580017', '泌尿外科', '203', '15', '泌尿系结石、前列腺疾病、肾肿瘤', '大专', NULL,
        0, '0.00', 1, 'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_employee (id, emp_code, emp_name, emp_type, gender, birth_date, hire_date, id_card, phone, email,
                          dept_id, dept_name, title, position, specialty, education, avatar, is_expert, expert_price,
                          status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('897001105', 'H17105', '曾梦洁', 2, 2, '1994-05-14', '2017-04-01', '320106199405144280', '18250122070',
        'zengmengjie@cslk-his.local', '19580017', '泌尿外科', '105', '15', '泌尿系结石、前列腺疾病、肾肿瘤', '本科', NULL,
        0, '0.00', 1, 'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_employee (id, emp_code, emp_name, emp_type, gender, birth_date, hire_date, id_card, phone, email,
                          dept_id, dept_name, title, position, specialty, education, avatar, is_expert, expert_price,
                          status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('897001106', 'H17106', '罗仁心', 1, 1, '1974-02-09', '1998-01-01', '330106197402090611', '17657065708',
        'luorenxin@cslk-his.local', '19580018', '烧伤整形外科', '401', '1', '大面积烧伤、创面修复、瘢痕整形', '硕士',
        NULL, 1, '100.00', 1, 'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_employee (id, emp_code, emp_name, emp_type, gender, birth_date, hire_date, id_card, phone, email,
                          dept_id, dept_name, title, position, specialty, education, avatar, is_expert, expert_price,
                          status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('897001107', 'H17107', '李树声', 1, 1, '1979-10-24', '2001-10-01', '420106197910248114', '13672502659',
        'lishusheng@cslk-his.local', '19580018', '烧伤整形外科', '301', '2', '大面积烧伤、创面修复、瘢痕整形', '博士',
        NULL, 1, '50.00', 1, 'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_employee (id, emp_code, emp_name, emp_type, gender, birth_date, hire_date, id_card, phone, email,
                          dept_id, dept_name, title, position, specialty, education, avatar, is_expert, expert_price,
                          status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('897001108', 'H17108', '郭桂芳', 1, 2, '1984-10-16', '2008-09-01', '420106198410161789', '18845217994',
        'guoguifang@cslk-his.local', '19580018', '烧伤整形外科', '201', '31', '大面积烧伤、创面修复、瘢痕整形', '硕士',
        NULL, 0, '0.00', 1, 'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_employee (id, emp_code, emp_name, emp_type, gender, birth_date, hire_date, id_card, phone, email,
                          dept_id, dept_name, title, position, specialty, education, avatar, is_expert, expert_price,
                          status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('897001109', 'H17109', '宋海', 1, 1, '1990-08-10', '2016-03-01', '330106199008109715', '15879249084',
        'songhai@cslk-his.local', '19580018', '烧伤整形外科', '102', '6', '大面积烧伤、创面修复、瘢痕整形', '硕士', NULL,
        0, '0.00', 1, 'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_employee (id, emp_code, emp_name, emp_type, gender, birth_date, hire_date, id_card, phone, email,
                          dept_id, dept_name, title, position, specialty, education, avatar, is_expert, expert_price,
                          status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('897001110', 'H17110', '孟心怡', 2, 2, '1986-10-01', '2008-11-01', '330106198610014888', '13954675133',
        'mengxinyi@cslk-his.local', '19580018', '烧伤整形外科', '403', '9', '大面积烧伤、创面修复、瘢痕整形', '大专',
        NULL, 0, '0.00', 1, 'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_employee (id, emp_code, emp_name, emp_type, gender, birth_date, hire_date, id_card, phone, email,
                          dept_id, dept_name, title, position, specialty, education, avatar, is_expert, expert_price,
                          status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('897001111', 'H17111', '彭柏', 2, 1, '1995-10-12', '2019-02-01', '44030519951012737X', '19994039444',
        'pengbai@cslk-his.local', '19580018', '烧伤整形外科', '203', '15', '大面积烧伤、创面修复、瘢痕整形', '本科', NULL,
        0, '0.00', 1, 'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_employee (id, emp_code, emp_name, emp_type, gender, birth_date, hire_date, id_card, phone, email,
                          dept_id, dept_name, title, position, specialty, education, avatar, is_expert, expert_price,
                          status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('897001112', 'H17112', '吴婉', 2, 2, '1994-10-04', '2018-11-01', '330106199410048404', '15260335857',
        'wuwan@cslk-his.local', '19580018', '烧伤整形外科', '105', '15', '大面积烧伤、创面修复、瘢痕整形', '大专', NULL,
        0, '0.00', 1, 'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_employee (id, emp_code, emp_name, emp_type, gender, birth_date, hire_date, id_card, phone, email,
                          dept_id, dept_name, title, position, specialty, education, avatar, is_expert, expert_price,
                          status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('897001113', 'H17113', '严立诚', 1, 1, '1968-10-23', '1992-03-01', '420106196810238395', '18922319976',
        'yanlicheng@cslk-his.local', '19580019', '血管外科', '401', '1', '动脉瘤、下肢静脉曲张、静脉血栓', '博士', NULL,
        1, '100.00', 1, 'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_employee (id, emp_code, emp_name, emp_type, gender, birth_date, hire_date, id_card, phone, email,
                          dept_id, dept_name, title, position, specialty, education, avatar, is_expert, expert_price,
                          status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('897001114', 'H17114', '谢纲', 1, 1, '1972-07-06', '1998-02-01', '320106197207061290', '15078679241',
        'xiegang@cslk-his.local', '19580019', '血管外科', '301', '2', '动脉瘤、下肢静脉曲张、静脉血栓', '硕士', NULL, 1,
        '50.00', 1, 'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_employee (id, emp_code, emp_name, emp_type, gender, birth_date, hire_date, id_card, phone, email,
                          dept_id, dept_name, title, position, specialty, education, avatar, is_expert, expert_price,
                          status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('897001115', 'H17115', '丁岚', 1, 2, '1985-02-08', '2008-06-01', '320106198502084588', '15298914091',
        'dinglan@cslk-his.local', '19580019', '血管外科', '201', '31', '动脉瘤、下肢静脉曲张、静脉血栓', '硕士', NULL, 0,
        '0.00', 1, 'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_employee (id, emp_code, emp_name, emp_type, gender, birth_date, hire_date, id_card, phone, email,
                          dept_id, dept_name, title, position, specialty, education, avatar, is_expert, expert_price,
                          status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('897001116', 'H17116', '赵宜修', 1, 2, '1995-07-04', '2019-06-01', '310104199507046687', '15794729834',
        'zhaoyixiu@cslk-his.local', '19580019', '血管外科', '102', '6', '动脉瘤、下肢静脉曲张、静脉血栓', '本科', NULL, 0,
        '0.00', 1, 'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_employee (id, emp_code, emp_name, emp_type, gender, birth_date, hire_date, id_card, phone, email,
                          dept_id, dept_name, title, position, specialty, education, avatar, is_expert, expert_price,
                          status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('897001117', 'H17117', '朱洁', 2, 2, '1982-06-02', '2006-07-01', '370102198206026782', '19874986931',
        'zhujie@cslk-his.local', '19580019', '血管外科', '403', '9', '动脉瘤、下肢静脉曲张、静脉血栓', '本科', NULL, 0,
        '0.00', 1, 'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_employee (id, emp_code, emp_name, emp_type, gender, birth_date, hire_date, id_card, phone, email,
                          dept_id, dept_name, title, position, specialty, education, avatar, is_expert, expert_price,
                          status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('897001118', 'H17118', '胡远航', 2, 1, '1986-06-03', '2011-07-01', '420106198606038619', '15849421058',
        'huyuanhang@cslk-his.local', '19580019', '血管外科', '203', '15', '动脉瘤、下肢静脉曲张、静脉血栓', '本科', NULL,
        0, '0.00', 1, 'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_employee (id, emp_code, emp_name, emp_type, gender, birth_date, hire_date, id_card, phone, email,
                          dept_id, dept_name, title, position, specialty, education, avatar, is_expert, expert_price,
                          status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('897001119', 'H17119', '倪心怡', 2, 2, '2000-11-14', '2024-11-01', '370102200011142206', '15814840089',
        'nixinyi@cslk-his.local', '19580019', '血管外科', '105', '15', '动脉瘤、下肢静脉曲张、静脉血栓', '中专', NULL, 0,
        '0.00', 1, 'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_employee (id, emp_code, emp_name, emp_type, gender, birth_date, hire_date, id_card, phone, email,
                          dept_id, dept_name, title, position, specialty, education, avatar, is_expert, expert_price,
                          status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('897001120', 'H17120', '徐达', 1, 1, '1969-05-08', '1992-10-01', '310104196905081353', '18568931329',
        'xuda@cslk-his.local', '19580020', '肝胆外科', '401', '1', '肝胆肿瘤、胆道结石、肝移植评估', '博士', NULL, 1,
        '100.00', 1, 'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_employee (id, emp_code, emp_name, emp_type, gender, birth_date, hire_date, id_card, phone, email,
                          dept_id, dept_name, title, position, specialty, education, avatar, is_expert, expert_price,
                          status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('897001121', 'H17121', '钱庆', 1, 1, '1975-06-01', '1998-04-01', '110105197506017411', '17847954941',
        'qianqing@cslk-his.local', '19580020', '肝胆外科', '301', '2', '肝胆肿瘤、胆道结石、肝移植评估', '博士', NULL, 1,
        '50.00', 1, 'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_employee (id, emp_code, emp_name, emp_type, gender, birth_date, hire_date, id_card, phone, email,
                          dept_id, dept_name, title, position, specialty, education, avatar, is_expert, expert_price,
                          status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('897001122', 'H17122', '向观澜', 1, 1, '1990-12-21', '2015-03-01', '330106199012218519', '18611767919',
        'xiangguanlan@cslk-his.local', '19580020', '肝胆外科', '201', '31', '肝胆肿瘤、胆道结石、肝移植评估', '硕士',
        NULL, 0, '0.00', 1, 'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_employee (id, emp_code, emp_name, emp_type, gender, birth_date, hire_date, id_card, phone, email,
                          dept_id, dept_name, title, position, specialty, education, avatar, is_expert, expert_price,
                          status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('897001123', 'H17123', '林婧', 1, 2, '1996-05-10', '2021-08-01', '440305199605108649', '15843800050',
        'linjingjuan@cslk-his.local', '19580020', '肝胆外科', '102', '6', '肝胆肿瘤、胆道结石、肝移植评估', '硕士', NULL,
        0, '0.00', 1, 'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_employee (id, emp_code, emp_name, emp_type, gender, birth_date, hire_date, id_card, phone, email,
                          dept_id, dept_name, title, position, specialty, education, avatar, is_expert, expert_price,
                          status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('897001124', 'H17124', '彭若彤', 2, 2, '1978-09-27', '2002-08-01', '330106197809270606', '15947723778',
        'pengruotong@cslk-his.local', '19580020', '肝胆外科', '403', '9', '肝胆肿瘤、胆道结石、肝移植评估', '本科', NULL,
        0, '0.00', 1, 'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_employee (id, emp_code, emp_name, emp_type, gender, birth_date, hire_date, id_card, phone, email,
                          dept_id, dept_name, title, position, specialty, education, avatar, is_expert, expert_price,
                          status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('897001125', 'H17125', '贺薇', 2, 2, '1992-02-25', '2016-02-01', '320106199202253022', '17858888773',
        'hesunweiwei@cslk-his.local', '19580020', '肝胆外科', '203', '15', '肝胆肿瘤、胆道结石、肝移植评估', '大专', NULL,
        0, '0.00', 1, 'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_employee (id, emp_code, emp_name, emp_type, gender, birth_date, hire_date, id_card, phone, email,
                          dept_id, dept_name, title, position, specialty, education, avatar, is_expert, expert_price,
                          status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('897001126', 'H17126', '李栋', 2, 1, '2000-02-16', '2025-09-01', '310104200002160578', '13948100352',
        'lidong@cslk-his.local', '19580020', '肝胆外科', '105', '15', '肝胆肿瘤、胆道结石、肝移植评估', '中专', NULL, 0,
        '0.00', 1, 'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_employee (id, emp_code, emp_name, emp_type, gender, birth_date, hire_date, id_card, phone, email,
                          dept_id, dept_name, title, position, specialty, education, avatar, is_expert, expert_price,
                          status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('897001127', 'H17127', '崔磊', 1, 1, '1967-08-06', '1991-05-01', '510107196708064972', '15729769940',
        'cuilei@cslk-his.local', '19580087', '甲乳外科', '401', '1', '甲状腺结节、乳腺肿瘤、腔镜手术', '硕士', NULL, 1,
        '100.00', 1, 'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_employee (id, emp_code, emp_name, emp_type, gender, birth_date, hire_date, id_card, phone, email,
                          dept_id, dept_name, title, position, specialty, education, avatar, is_expert, expert_price,
                          status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('897001128', 'H17128', '毛楠', 1, 1, '1973-01-03', '1998-07-01', '510107197301036034', '19927773497',
        'maonan@cslk-his.local', '19580087', '甲乳外科', '301', '2', '甲状腺结节、乳腺肿瘤、腔镜手术', '本科', NULL, 1,
        '50.00', 1, 'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_employee (id, emp_code, emp_name, emp_type, gender, birth_date, hire_date, id_card, phone, email,
                          dept_id, dept_name, title, position, specialty, education, avatar, is_expert, expert_price,
                          status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('897001129', 'H17129', '钱捷', 1, 1, '1992-05-17', '2015-08-01', '610113199205170794', '18994396243',
        'qianjie@cslk-his.local', '19580087', '甲乳外科', '201', '31', '甲状腺结节、乳腺肿瘤、腔镜手术', '本科', NULL, 0,
        '0.00', 0, 'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_employee (id, emp_code, emp_name, emp_type, gender, birth_date, hire_date, id_card, phone, email,
                          dept_id, dept_name, title, position, specialty, education, avatar, is_expert, expert_price,
                          status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('897001130', 'H17130', '朱跃龙', 1, 1, '1997-01-10', '2019-05-01', '310104199701100237', '18039467520',
        'zhuyuelong@cslk-his.local', '19580087', '甲乳外科', '102', '6', '甲状腺结节、乳腺肿瘤、腔镜手术', '本科', NULL,
        0, '0.00', 1, 'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_employee (id, emp_code, emp_name, emp_type, gender, birth_date, hire_date, id_card, phone, email,
                          dept_id, dept_name, title, position, specialty, education, avatar, is_expert, expert_price,
                          status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('897001131', 'H17131', '谢佳', 2, 2, '1985-07-15', '2010-01-01', '370102198507158623', '15266363618',
        'xiejia@cslk-his.local', '19580087', '甲乳外科', '403', '9', '甲状腺结节、乳腺肿瘤、腔镜手术', '本科', NULL, 0,
        '0.00', 1, 'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_employee (id, emp_code, emp_name, emp_type, gender, birth_date, hire_date, id_card, phone, email,
                          dept_id, dept_name, title, position, specialty, education, avatar, is_expert, expert_price,
                          status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('897001132', 'H17132', '邓春燕', 2, 2, '1991-08-01', '2016-11-01', '610113199108017982', '18723305280',
        'dengchunyan@cslk-his.local', '19580087', '甲乳外科', '203', '15', '甲状腺结节、乳腺肿瘤、腔镜手术', '大专', NULL,
        0, '0.00', 1, 'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_employee (id, emp_code, emp_name, emp_type, gender, birth_date, hire_date, id_card, phone, email,
                          dept_id, dept_name, title, position, specialty, education, avatar, is_expert, expert_price,
                          status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('897001133', 'H17133', '董明珠', 2, 2, '1994-09-17', '2019-11-01', '310104199409174829', '18584565625',
        'dongmingzhu@cslk-his.local', '19580087', '甲乳外科', '105', '15', '甲状腺结节、乳腺肿瘤、腔镜手术', '大专', NULL,
        0, '0.00', 1, 'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_employee (id, emp_code, emp_name, emp_type, gender, birth_date, hire_date, id_card, phone, email,
                          dept_id, dept_name, title, position, specialty, education, avatar, is_expert, expert_price,
                          status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('897001134', 'H17134', '于涛', 1, 1, '1971-06-24', '1996-03-01', '110105197106246119', '18719914109',
        'yutao@cslk-his.local', '19580088', '肛肠外科', '401', '1', '痔瘘裂、盆底疾病、结直肠肿瘤', '硕士', NULL, 1,
        '100.00', 1, 'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_employee (id, emp_code, emp_name, emp_type, gender, birth_date, hire_date, id_card, phone, email,
                          dept_id, dept_name, title, position, specialty, education, avatar, is_expert, expert_price,
                          status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('897001135', 'H17135', '岑瑞', 1, 1, '1974-07-19', '1998-03-01', '110105197407190577', '13575817154',
        'cenrui@cslk-his.local', '19580088', '肛肠外科', '301', '2', '痔瘘裂、盆底疾病、结直肠肿瘤', '博士', NULL, 1,
        '50.00', 1, 'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_employee (id, emp_code, emp_name, emp_type, gender, birth_date, hire_date, id_card, phone, email,
                          dept_id, dept_name, title, position, specialty, education, avatar, is_expert, expert_price,
                          status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('897001136', 'H17136', '裘月白', 1, 2, '1990-07-23', '2014-09-01', '310104199007231800', '19953659271',
        'qiuqiuyuebai@cslk-his.local', '19580088', '肛肠外科', '201', '31', '痔瘘裂、盆底疾病、结直肠肿瘤', '硕士', NULL,
        0, '0.00', 1, 'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_employee (id, emp_code, emp_name, emp_type, gender, birth_date, hire_date, id_card, phone, email,
                          dept_id, dept_name, title, position, specialty, education, avatar, is_expert, expert_price,
                          status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('897001137', 'H17137', '曹婧', 1, 2, '1997-03-13', '2022-11-01', '510107199703132263', '15262791656',
        'caojingjuan@cslk-his.local', '19580088', '肛肠外科', '102', '6', '痔瘘裂、盆底疾病、结直肠肿瘤', '本科', NULL, 0,
        '0.00', 1, 'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_employee (id, emp_code, emp_name, emp_type, gender, birth_date, hire_date, id_card, phone, email,
                          dept_id, dept_name, title, position, specialty, education, avatar, is_expert, expert_price,
                          status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('897001138', 'H17138', '薛达', 2, 1, '1981-12-04', '2004-04-01', '320106198112045719', '18042661184',
        'xueda@cslk-his.local', '19580088', '肛肠外科', '403', '9', '痔瘘裂、盆底疾病、结直肠肿瘤', '大专', NULL, 0,
        '0.00', 1, 'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_employee (id, emp_code, emp_name, emp_type, gender, birth_date, hire_date, id_card, phone, email,
                          dept_id, dept_name, title, position, specialty, education, avatar, is_expert, expert_price,
                          status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('897001139', 'H17139', '杜萱', 2, 2, '1987-12-25', '2009-02-01', '11010519871225026X', '18648623359',
        'duxuan@cslk-his.local', '19580088', '肛肠外科', '203', '15', '痔瘘裂、盆底疾病、结直肠肿瘤', '本科', NULL, 0,
        '0.00', 1, 'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_employee (id, emp_code, emp_name, emp_type, gender, birth_date, hire_date, id_card, phone, email,
                          dept_id, dept_name, title, position, specialty, education, avatar, is_expert, expert_price,
                          status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('897001140', 'H17140', '温知微', 2, 2, '1997-05-23', '2019-04-01', '330106199705237229', '13935418879',
        'wenzhiwei@cslk-his.local', '19580088', '肛肠外科', '105', '15', '痔瘘裂、盆底疾病、结直肠肿瘤', '本科', NULL, 0,
        '0.00', 1, 'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_employee (id, emp_code, emp_name, emp_type, gender, birth_date, hire_date, id_card, phone, email,
                          dept_id, dept_name, title, position, specialty, education, avatar, is_expert, expert_price,
                          status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('897001141', 'H17141', '许玉兰', 1, 2, '1968-12-17', '1992-01-01', '440305196812177686', '15826674177',
        'xushuyulan@cslk-his.local', '19580022', '妇科', '401', '1', '妇科肿瘤、腔镜手术、妇科内分泌', '硕士', NULL, 1,
        '100.00', 1, 'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_employee (id, emp_code, emp_name, emp_type, gender, birth_date, hire_date, id_card, phone, email,
                          dept_id, dept_name, title, position, specialty, education, avatar, is_expert, expert_price,
                          status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('897001142', 'H17142', '秦浩', 1, 1, '1972-04-11', '1998-03-01', '610113197204111435', '18186628415',
        'qinhao@cslk-his.local', '19580022', '妇科', '301', '2', '妇科肿瘤、腔镜手术、妇科内分泌', '博士', NULL, 1,
        '50.00', 1, 'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_employee (id, emp_code, emp_name, emp_type, gender, birth_date, hire_date, id_card, phone, email,
                          dept_id, dept_name, title, position, specialty, education, avatar, is_expert, expert_price,
                          status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('897001143', 'H17143', '尚寒梅', 1, 2, '1983-05-08', '2008-04-01', '610113198305084864', '19894059357',
        'shanghanmei@cslk-his.local', '19580022', '妇科', '201', '31', '妇科肿瘤、腔镜手术、妇科内分泌', '硕士', NULL, 0,
        '0.00', 1, 'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_employee (id, emp_code, emp_name, emp_type, gender, birth_date, hire_date, id_card, phone, email,
                          dept_id, dept_name, title, position, specialty, education, avatar, is_expert, expert_price,
                          status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('897001144', 'H17144', '王鸣谦', 1, 1, '1992-04-19', '2016-12-01', '330106199204193918', '13633052655',
        'wangmingqian@cslk-his.local', '19580022', '妇科', '102', '6', '妇科肿瘤、腔镜手术、妇科内分泌', '本科', NULL, 0,
        '0.00', 1, 'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_employee (id, emp_code, emp_name, emp_type, gender, birth_date, hire_date, id_card, phone, email,
                          dept_id, dept_name, title, position, specialty, education, avatar, is_expert, expert_price,
                          status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('897001145', 'H17145', '李玲', 2, 2, '1987-09-17', '2009-04-01', '110105198709173021', '18989399834',
        'liling@cslk-his.local', '19580022', '妇科', '403', '9', '妇科肿瘤、腔镜手术、妇科内分泌', '大专', NULL, 0,
        '0.00', 1, 'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_employee (id, emp_code, emp_name, emp_type, gender, birth_date, hire_date, id_card, phone, email,
                          dept_id, dept_name, title, position, specialty, education, avatar, is_expert, expert_price,
                          status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('897001146', 'H17146', '王敏', 2, 2, '1986-04-02', '2008-08-01', '310104198604027360', '18217530720',
        'wangmin@cslk-his.local', '19580022', '妇科', '203', '15', '妇科肿瘤、腔镜手术、妇科内分泌', '本科', NULL, 0,
        '0.00', 1, 'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_employee (id, emp_code, emp_name, emp_type, gender, birth_date, hire_date, id_card, phone, email,
                          dept_id, dept_name, title, position, specialty, education, avatar, is_expert, expert_price,
                          status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('897001147', 'H17147', '向寒梅', 2, 2, '2001-06-24', '2024-03-01', '420106200106242007', '18567057913',
        'xianghanmei@cslk-his.local', '19580022', '妇科', '105', '15', '妇科肿瘤、腔镜手术、妇科内分泌', '本科', NULL, 0,
        '0.00', 1, 'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_employee (id, emp_code, emp_name, emp_type, gender, birth_date, hire_date, id_card, phone, email,
                          dept_id, dept_name, title, position, specialty, education, avatar, is_expert, expert_price,
                          status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('897001148', 'H17148', '胡天', 1, 1, '1973-04-01', '1997-11-01', '61011319730401187X', '15774625959',
        'hutian@cslk-his.local', '19580023', '产科', '401', '1', '高危妊娠、产科急救、产前监测', '硕士', NULL, 1,
        '100.00', 1, 'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_employee (id, emp_code, emp_name, emp_type, gender, birth_date, hire_date, id_card, phone, email,
                          dept_id, dept_name, title, position, specialty, education, avatar, is_expert, expert_price,
                          status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('897001149', 'H17149', '卢静', 1, 2, '1977-09-05', '2002-10-01', '370102197709052662', '18666639735',
        'lujing@cslk-his.local', '19580023', '产科', '301', '2', '高危妊娠、产科急救、产前监测', '硕士', NULL, 1, '50.00',
        1, 'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_employee (id, emp_code, emp_name, emp_type, gender, birth_date, hire_date, id_card, phone, email,
                          dept_id, dept_name, title, position, specialty, education, avatar, is_expert, expert_price,
                          status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('897001150', 'H17150', '于国强', 1, 1, '1990-06-25', '2013-02-01', '440305199006255719', '18153064142',
        'yuguoqiang@cslk-his.local', '19580023', '产科', '201', '31', '高危妊娠、产科急救、产前监测', '硕士', NULL, 0,
        '0.00', 1, 'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_employee (id, emp_code, emp_name, emp_type, gender, birth_date, hire_date, id_card, phone, email,
                          dept_id, dept_name, title, position, specialty, education, avatar, is_expert, expert_price,
                          status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('897001151', 'H17151', '黄萱', 1, 2, '1993-04-24', '2016-08-01', '110105199304240825', '17637696973',
        'huangxuan@cslk-his.local', '19580023', '产科', '102', '6', '高危妊娠、产科急救、产前监测', '本科', NULL, 0,
        '0.00', 1, 'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_employee (id, emp_code, emp_name, emp_type, gender, birth_date, hire_date, id_card, phone, email,
                          dept_id, dept_name, title, position, specialty, education, avatar, is_expert, expert_price,
                          status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('897001152', 'H17152', '任采薇', 2, 2, '1978-07-17', '2000-11-01', '510107197807175189', '18297992763',
        'rencaiw@cslk-his.local', '19580023', '产科', '403', '9', '高危妊娠、产科急救、产前监测', '大专', NULL, 0, '0.00',
        1, 'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_employee (id, emp_code, emp_name, emp_type, gender, birth_date, hire_date, id_card, phone, email,
                          dept_id, dept_name, title, position, specialty, education, avatar, is_expert, expert_price,
                          status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('897001153', 'H17153', '曾思琪', 2, 2, '1987-11-18', '2012-12-01', '110105198711188484', '18615205749',
        'zengsiqi@cslk-his.local', '19580023', '产科', '203', '15', '高危妊娠、产科急救、产前监测', '本科', NULL, 0,
        '0.00', 1, 'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_employee (id, emp_code, emp_name, emp_type, gender, birth_date, hire_date, id_card, phone, email,
                          dept_id, dept_name, title, position, specialty, education, avatar, is_expert, expert_price,
                          status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('897001154', 'H17154', '周和玉', 2, 2, '1994-09-20', '2017-12-01', '32010619940920288X', '18220728074',
        'zhouheyu@cslk-his.local', '19580023', '产科', '105', '15', '高危妊娠、产科急救、产前监测', '大专', NULL, 0,
        '0.00', 1, 'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_employee (id, emp_code, emp_name, emp_type, gender, birth_date, hire_date, id_card, phone, email,
                          dept_id, dept_name, title, position, specialty, education, avatar, is_expert, expert_price,
                          status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('897001155', 'H17155', '龙松', 1, 1, '1971-04-20', '1995-06-01', '610113197104203033', '15245134180',
        'longsong@cslk-his.local', '19580024', '生殖医学中心', '401', '1', '辅助生殖、不孕不育、胚胎实验室', '博士', NULL,
        1, '100.00', 1, 'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_employee (id, emp_code, emp_name, emp_type, gender, birth_date, hire_date, id_card, phone, email,
                          dept_id, dept_name, title, position, specialty, education, avatar, is_expert, expert_price,
                          status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('897001156', 'H17156', '苏修齐', 1, 1, '1980-11-20', '2002-05-01', '370102198011204454', '13672606687',
        'suxiuqi@cslk-his.local', '19580024', '生殖医学中心', '301', '2', '辅助生殖、不孕不育、胚胎实验室', '本科', NULL,
        1, '50.00', 1, 'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_employee (id, emp_code, emp_name, emp_type, gender, birth_date, hire_date, id_card, phone, email,
                          dept_id, dept_name, title, position, specialty, education, avatar, is_expert, expert_price,
                          status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('897001157', 'H17157', '高捷', 1, 1, '1984-03-10', '2008-01-01', '440305198403106739', '18070702853',
        'gaojie@cslk-his.local', '19580024', '生殖医学中心', '201', '31', '辅助生殖、不孕不育、胚胎实验室', '硕士', NULL,
        0, '0.00', 1, 'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_employee (id, emp_code, emp_name, emp_type, gender, birth_date, hire_date, id_card, phone, email,
                          dept_id, dept_name, title, position, specialty, education, avatar, is_expert, expert_price,
                          status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('897001158', 'H17158', '钱国强', 1, 1, '1993-05-24', '2016-02-01', '610113199305242492', '13810004101',
        'qianguoqiang@cslk-his.local', '19580024', '生殖医学中心', '102', '6', '辅助生殖、不孕不育、胚胎实验室', '本科',
        NULL, 0, '0.00', 1, 'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_employee (id, emp_code, emp_name, emp_type, gender, birth_date, hire_date, id_card, phone, email,
                          dept_id, dept_name, title, position, specialty, education, avatar, is_expert, expert_price,
                          status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('897001159', 'H17159', '彭安雅', 2, 2, '1978-12-12', '2002-07-01', '370102197812126420', '18085448822',
        'penganya@cslk-his.local', '19580024', '生殖医学中心', '403', '9', '辅助生殖、不孕不育、胚胎实验室', '本科', NULL,
        0, '0.00', 1, 'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_employee (id, emp_code, emp_name, emp_type, gender, birth_date, hire_date, id_card, phone, email,
                          dept_id, dept_name, title, position, specialty, education, avatar, is_expert, expert_price,
                          status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('897001160', 'H17160', '黄雪', 2, 2, '1986-02-14', '2009-10-01', '370102198602146444', '13983357668',
        'huangxueli@cslk-his.local', '19580024', '生殖医学中心', '203', '15', '辅助生殖、不孕不育、胚胎实验室', '大专',
        NULL, 0, '0.00', 1, 'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_employee (id, emp_code, emp_name, emp_type, gender, birth_date, hire_date, id_card, phone, email,
                          dept_id, dept_name, title, position, specialty, education, avatar, is_expert, expert_price,
                          status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('897001161', 'H17161', '陆秀英', 2, 2, '1999-01-25', '2024-10-01', '370102199901255100', '17816160165',
        'lukuxiuying@cslk-his.local', '19580024', '生殖医学中心', '105', '15', '辅助生殖、不孕不育、胚胎实验室', '大专',
        NULL, 0, '0.00', 1, 'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_employee (id, emp_code, emp_name, emp_type, gender, birth_date, hire_date, id_card, phone, email,
                          dept_id, dept_name, title, position, specialty, education, avatar, is_expert, expert_price,
                          status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('897001162', 'H17162', '康国强', 1, 1, '1966-06-16', '1990-01-01', '370102196606161271', '18942069832',
        'kangguoqiang@cslk-his.local', '19580089', '产前诊断中心', '401', '1', '胎儿医学、遗传筛查、羊水诊断', '硕士',
        NULL, 1, '100.00', 1, 'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_employee (id, emp_code, emp_name, emp_type, gender, birth_date, hire_date, id_card, phone, email,
                          dept_id, dept_name, title, position, specialty, education, avatar, is_expert, expert_price,
                          status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('897001163', 'H17163', '范静文', 1, 2, '1979-10-25', '2002-12-01', '420106197910259008', '15723299926',
        'fanjingwen@cslk-his.local', '19580089', '产前诊断中心', '301', '2', '胎儿医学、遗传筛查、羊水诊断', '博士', NULL,
        1, '50.00', 1, 'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_employee (id, emp_code, emp_name, emp_type, gender, birth_date, hire_date, id_card, phone, email,
                          dept_id, dept_name, title, position, specialty, education, avatar, is_expert, expert_price,
                          status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('897001164', 'H17164', '肖秀英', 1, 2, '1989-12-20', '2013-12-01', '370102198912201584', '15270844752',
        'xiaoxiuying@cslk-his.local', '19580089', '产前诊断中心', '201', '31', '胎儿医学、遗传筛查、羊水诊断', '本科',
        NULL, 0, '0.00', 1, 'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_employee (id, emp_code, emp_name, emp_type, gender, birth_date, hire_date, id_card, phone, email,
                          dept_id, dept_name, title, position, specialty, education, avatar, is_expert, expert_price,
                          status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('897001165', 'H17165', '唐子轩', 1, 1, '1994-11-01', '2016-08-01', '310104199411018497', '19814504244',
        'tangzixuan@cslk-his.local', '19580089', '产前诊断中心', '102', '6', '胎儿医学、遗传筛查、羊水诊断', '本科', NULL,
        0, '0.00', 1, 'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_employee (id, emp_code, emp_name, emp_type, gender, birth_date, hire_date, id_card, phone, email,
                          dept_id, dept_name, title, position, specialty, education, avatar, is_expert, expert_price,
                          status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('897001166', 'H17166', '徐乐怡', 2, 2, '1979-09-19', '2001-09-01', '370102197909196329', '17619540617',
        'xuleyi@cslk-his.local', '19580089', '产前诊断中心', '403', '9', '胎儿医学、遗传筛查、羊水诊断', '大专', NULL, 0,
        '0.00', 1, 'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_employee (id, emp_code, emp_name, emp_type, gender, birth_date, hire_date, id_card, phone, email,
                          dept_id, dept_name, title, position, specialty, education, avatar, is_expert, expert_price,
                          status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('897001167', 'H17167', '尹天', 2, 1, '1988-09-13', '2010-05-01', '320106198809139352', '17683214027',
        'yintian@cslk-his.local', '19580089', '产前诊断中心', '203', '15', '胎儿医学、遗传筛查、羊水诊断', '本科', NULL,
        0, '0.00', 1, 'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_employee (id, emp_code, emp_name, emp_type, gender, birth_date, hire_date, id_card, phone, email,
                          dept_id, dept_name, title, position, specialty, education, avatar, is_expert, expert_price,
                          status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('897001168', 'H17168', '唐娜', 2, 2, '1997-02-06', '2022-07-01', '420106199702061629', '15253314113',
        'tangna@cslk-his.local', '19580089', '产前诊断中心', '105', '15', '胎儿医学、遗传筛查、羊水诊断', '本科', NULL, 0,
        '0.00', 1, 'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_employee (id, emp_code, emp_name, emp_type, gender, birth_date, hire_date, id_card, phone, email,
                          dept_id, dept_name, title, position, specialty, education, avatar, is_expert, expert_price,
                          status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('897001169', 'H17169', '侯桂芳', 1, 2, '1973-10-14', '1995-03-01', '610113197310141785', '17813442990',
        'houguifang@cslk-his.local', '19580026', '小儿内科', '401', '1', '小儿呼吸、儿童生长发育、新生儿黄疸', '博士',
        NULL, 1, '100.00', 0, 'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_employee (id, emp_code, emp_name, emp_type, gender, birth_date, hire_date, id_card, phone, email,
                          dept_id, dept_name, title, position, specialty, education, avatar, is_expert, expert_price,
                          status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('897001170', 'H17170', '向厚朴', 1, 1, '1976-03-22', '2000-04-01', '44030519760322169X', '15767217215',
        'xianghoupu@cslk-his.local', '19580026', '小儿内科', '301', '2', '小儿呼吸、儿童生长发育、新生儿黄疸', '硕士',
        NULL, 1, '50.00', 1, 'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_employee (id, emp_code, emp_name, emp_type, gender, birth_date, hire_date, id_card, phone, email,
                          dept_id, dept_name, title, position, specialty, education, avatar, is_expert, expert_price,
                          status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('897001171', 'H17171', '齐璐', 1, 2, '1990-08-21', '2012-08-01', '370102199008210380', '15767360638',
        'qilu@cslk-his.local', '19580026', '小儿内科', '201', '31', '小儿呼吸、儿童生长发育、新生儿黄疸', '本科', NULL, 0,
        '0.00', 1, 'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_employee (id, emp_code, emp_name, emp_type, gender, birth_date, hire_date, id_card, phone, email,
                          dept_id, dept_name, title, position, specialty, education, avatar, is_expert, expert_price,
                          status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('897001172', 'H17172', '岑仁心', 1, 1, '1996-06-08', '2018-01-01', '210102199606089133', '15714341530',
        'cenrenxin@cslk-his.local', '19580026', '小儿内科', '102', '6', '小儿呼吸、儿童生长发育、新生儿黄疸', '本科',
        NULL, 0, '0.00', 1, 'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_employee (id, emp_code, emp_name, emp_type, gender, birth_date, hire_date, id_card, phone, email,
                          dept_id, dept_name, title, position, specialty, education, avatar, is_expert, expert_price,
                          status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('897001173', 'H17173', '严瑶', 2, 2, '1978-03-25', '2000-10-01', '110105197803252565', '18111273158',
        'yanyao@cslk-his.local', '19580026', '小儿内科', '403', '9', '小儿呼吸、儿童生长发育、新生儿黄疸', '大专', NULL,
        0, '0.00', 1, 'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_employee (id, emp_code, emp_name, emp_type, gender, birth_date, hire_date, id_card, phone, email,
                          dept_id, dept_name, title, position, specialty, education, avatar, is_expert, expert_price,
                          status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('897001174', 'H17174', '温静文', 2, 2, '1987-09-06', '2011-11-01', '320106198709065069', '18164240317',
        'wenjingwen@cslk-his.local', '19580026', '小儿内科', '203', '15', '小儿呼吸、儿童生长发育、新生儿黄疸', '大专',
        NULL, 0, '0.00', 1, 'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_employee (id, emp_code, emp_name, emp_type, gender, birth_date, hire_date, id_card, phone, email,
                          dept_id, dept_name, title, position, specialty, education, avatar, is_expert, expert_price,
                          status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('897001175', 'H17175', '赵芳', 2, 2, '1996-09-22', '2018-05-01', '370102199609221683', '15153661373',
        'zhaofang@cslk-his.local', '19580026', '小儿内科', '105', '15', '小儿呼吸、儿童生长发育、新生儿黄疸', '中专',
        NULL, 0, '0.00', 1, 'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_employee (id, emp_code, emp_name, emp_type, gender, birth_date, hire_date, id_card, phone, email,
                          dept_id, dept_name, title, position, specialty, education, avatar, is_expert, expert_price,
                          status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('897001176', 'H17176', '钱观澜', 1, 1, '1970-10-05', '1992-02-01', '510107197010058030', '15964288350',
        'qianguanlan@cslk-his.local', '19580027', '小儿外科', '401', '1', '小儿先天畸形、儿童创伤、新生儿手术', '博士',
        NULL, 1, '100.00', 1, 'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_employee (id, emp_code, emp_name, emp_type, gender, birth_date, hire_date, id_card, phone, email,
                          dept_id, dept_name, title, position, specialty, education, avatar, is_expert, expert_price,
                          status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('897001177', 'H17177', '宋瑶', 1, 2, '1977-10-18', '1999-09-01', '440305197710184943', '17827526283',
        'songyao@cslk-his.local', '19580027', '小儿外科', '301', '2', '小儿先天畸形、儿童创伤、新生儿手术', '本科', NULL,
        1, '50.00', 1, 'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_employee (id, emp_code, emp_name, emp_type, gender, birth_date, hire_date, id_card, phone, email,
                          dept_id, dept_name, title, position, specialty, education, avatar, is_expert, expert_price,
                          status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('897001178', 'H17178', '韩鹏飞', 1, 1, '1992-05-11', '2017-12-01', '370102199205117011', '19891478450',
        'hanpengfei@cslk-his.local', '19580027', '小儿外科', '201', '31', '小儿先天畸形、儿童创伤、新生儿手术', '硕士',
        NULL, 0, '0.00', 1, 'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_employee (id, emp_code, emp_name, emp_type, gender, birth_date, hire_date, id_card, phone, email,
                          dept_id, dept_name, title, position, specialty, education, avatar, is_expert, expert_price,
                          status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('897001179', 'H17179', '江寒梅', 1, 2, '1992-03-10', '2017-02-01', '440305199203106042', '17849141454',
        'jiangshanmei@cslk-his.local', '19580027', '小儿外科', '102', '6', '小儿先天畸形、儿童创伤、新生儿手术', '本科',
        NULL, 0, '0.00', 1, 'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_employee (id, emp_code, emp_name, emp_type, gender, birth_date, hire_date, id_card, phone, email,
                          dept_id, dept_name, title, position, specialty, education, avatar, is_expert, expert_price,
                          status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('897001180', 'H17180', '杜桂芳', 2, 2, '1984-06-26', '2008-07-01', '610113198406269606', '18514245014',
        'duguifang@cslk-his.local', '19580027', '小儿外科', '403', '9', '小儿先天畸形、儿童创伤、新生儿手术', '大专',
        NULL, 0, '0.00', 1, 'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_employee (id, emp_code, emp_name, emp_type, gender, birth_date, hire_date, id_card, phone, email,
                          dept_id, dept_name, title, position, specialty, education, avatar, is_expert, expert_price,
                          status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('897001181', 'H17181', '邱青禾', 2, 2, '1990-08-07', '2012-06-01', '21010219900807330X', '13724446529',
        'qiuqinghe@cslk-his.local', '19580027', '小儿外科', '203', '15', '小儿先天畸形、儿童创伤、新生儿手术', '本科',
        NULL, 0, '0.00', 1, 'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_employee (id, emp_code, emp_name, emp_type, gender, birth_date, hire_date, id_card, phone, email,
                          dept_id, dept_name, title, position, specialty, education, avatar, is_expert, expert_price,
                          status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('897001182', 'H17182', '王乐怡', 2, 2, '1997-03-06', '2021-09-01', '510107199703066382', '17639146835',
        'wangleyi@cslk-his.local', '19580027', '小儿外科', '105', '15', '小儿先天畸形、儿童创伤、新生儿手术', '中专',
        NULL, 0, '0.00', 1, 'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_employee (id, emp_code, emp_name, emp_type, gender, birth_date, hire_date, id_card, phone, email,
                          dept_id, dept_name, title, position, specialty, education, avatar, is_expert, expert_price,
                          status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('897001183', 'H17183', '冯达', 1, 1, '1971-06-16', '1996-11-01', '320106197106163212', '18991949478',
        'fengda@cslk-his.local', '19580028', '新生儿科', '401', '1', '早产儿救治、新生儿窒息、NICU 管理', '博士', NULL, 1,
        '100.00', 1, 'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_employee (id, emp_code, emp_name, emp_type, gender, birth_date, hire_date, id_card, phone, email,
                          dept_id, dept_name, title, position, specialty, education, avatar, is_expert, expert_price,
                          status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('897001184', 'H17184', '秦春燕', 1, 2, '1977-10-14', '2002-12-01', '32010619771014312X', '15235204964',
        'qinchunyan@cslk-his.local', '19580028', '新生儿科', '301', '2', '早产儿救治、新生儿窒息、NICU 管理', '本科',
        NULL, 1, '50.00', 1, 'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_employee (id, emp_code, emp_name, emp_type, gender, birth_date, hire_date, id_card, phone, email,
                          dept_id, dept_name, title, position, specialty, education, avatar, is_expert, expert_price,
                          status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('897001185', 'H17185', '丁军', 1, 1, '1988-12-15', '2013-09-01', '320106198812150712', '13722470775',
        'dingjun@cslk-his.local', '19580028', '新生儿科', '201', '31', '早产儿救治、新生儿窒息、NICU 管理', '硕士', NULL,
        0, '0.00', 1, 'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_employee (id, emp_code, emp_name, emp_type, gender, birth_date, hire_date, id_card, phone, email,
                          dept_id, dept_name, title, position, specialty, education, avatar, is_expert, expert_price,
                          status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('897001186', 'H17186', '马秉钧', 1, 1, '1998-06-09', '2021-11-01', '440305199806096314', '18878954980',
        'mabingjun@cslk-his.local', '19580028', '新生儿科', '102', '6', '早产儿救治、新生儿窒息、NICU 管理', '本科', NULL,
        0, '0.00', 1, 'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_employee (id, emp_code, emp_name, emp_type, gender, birth_date, hire_date, id_card, phone, email,
                          dept_id, dept_name, title, position, specialty, education, avatar, is_expert, expert_price,
                          status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('897001187', 'H17187', '蔡萱', 2, 2, '1983-07-17', '2008-05-01', '330106198307171549', '18915543932',
        'caixuan@cslk-his.local', '19580028', '新生儿科', '403', '9', '早产儿救治、新生儿窒息、NICU 管理', '本科', NULL,
        0, '0.00', 1, 'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_employee (id, emp_code, emp_name, emp_type, gender, birth_date, hire_date, id_card, phone, email,
                          dept_id, dept_name, title, position, specialty, education, avatar, is_expert, expert_price,
                          status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('897001188', 'H17188', '万桂芳', 2, 2, '1994-03-16', '2017-03-01', '440305199403163121', '17672597608',
        'wanguifang@cslk-his.local', '19580028', '新生儿科', '203', '15', '早产儿救治、新生儿窒息、NICU 管理', '本科',
        NULL, 0, '0.00', 1, 'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_employee (id, emp_code, emp_name, emp_type, gender, birth_date, hire_date, id_card, phone, email,
                          dept_id, dept_name, title, position, specialty, education, avatar, is_expert, expert_price,
                          status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('897001189', 'H17189', '毛宏', 2, 1, '1997-06-03', '2022-06-01', '330106199706039670', '18753617750',
        'maohong@cslk-his.local', '19580028', '新生儿科', '105', '15', '早产儿救治、新生儿窒息、NICU 管理', '大专', NULL,
        0, '0.00', 1, 'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_employee (id, emp_code, emp_name, emp_type, gender, birth_date, hire_date, id_card, phone, email,
                          dept_id, dept_name, title, position, specialty, education, avatar, is_expert, expert_price,
                          status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('897001190', 'H17190', '黄明德', 1, 1, '1968-02-25', '1991-01-01', '440305196802253219', '15069576392',
        'huangmingde@cslk-his.local', '19580090', '儿童保健科', '401', '1', '儿童营养、发育行为评估、眼保健', '硕士',
        NULL, 1, '100.00', 1, 'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_employee (id, emp_code, emp_name, emp_type, gender, birth_date, hire_date, id_card, phone, email,
                          dept_id, dept_name, title, position, specialty, education, avatar, is_expert, expert_price,
                          status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('897001191', 'H17191', '苏山', 1, 1, '1976-01-06', '2001-04-01', '110105197601066537', '19916157472',
        'sushan@cslk-his.local', '19580090', '儿童保健科', '301', '2', '儿童营养、发育行为评估、眼保健', '硕士', NULL, 1,
        '50.00', 1, 'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_employee (id, emp_code, emp_name, emp_type, gender, birth_date, hire_date, id_card, phone, email,
                          dept_id, dept_name, title, position, specialty, education, avatar, is_expert, expert_price,
                          status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('897001192', 'H17192', '龙桂芳', 1, 2, '1987-08-02', '2011-08-01', '44030519870802454X', '19831833417',
        'longguifang@cslk-his.local', '19580090', '儿童保健科', '201', '31', '儿童营养、发育行为评估、眼保健', '本科',
        NULL, 0, '0.00', 1, 'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_employee (id, emp_code, emp_name, emp_type, gender, birth_date, hire_date, id_card, phone, email,
                          dept_id, dept_name, title, position, specialty, education, avatar, is_expert, expert_price,
                          status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('897001193', 'H17193', '程天', 1, 1, '1990-06-27', '2016-09-01', '310104199006272539', '18678686126',
        'chengtian@cslk-his.local', '19580090', '儿童保健科', '102', '6', '儿童营养、发育行为评估、眼保健', '本科', NULL,
        0, '0.00', 1, 'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_employee (id, emp_code, emp_name, emp_type, gender, birth_date, hire_date, id_card, phone, email,
                          dept_id, dept_name, title, position, specialty, education, avatar, is_expert, expert_price,
                          status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('897001194', 'H17194', '刘岚', 2, 2, '1985-10-04', '2008-09-01', '420106198510042509', '18923900719',
        'liulan@cslk-his.local', '19580090', '儿童保健科', '403', '9', '儿童营养、发育行为评估、眼保健', '大专', NULL, 0,
        '0.00', 1, 'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_employee (id, emp_code, emp_name, emp_type, gender, birth_date, hire_date, id_card, phone, email,
                          dept_id, dept_name, title, position, specialty, education, avatar, is_expert, expert_price,
                          status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('897001195', 'H17195', '崔洁', 2, 2, '1993-06-17', '2015-05-01', '610113199306177469', '18844075630',
        'cuijie@cslk-his.local', '19580090', '儿童保健科', '203', '15', '儿童营养、发育行为评估、眼保健', '本科', NULL, 0,
        '0.00', 1, 'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_employee (id, emp_code, emp_name, emp_type, gender, birth_date, hire_date, id_card, phone, email,
                          dept_id, dept_name, title, position, specialty, education, avatar, is_expert, expert_price,
                          status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('897001196', 'H17196', '侯明珠', 2, 2, '1996-07-02', '2018-11-01', '510107199607025265', '15112100759',
        'houmingzhu@cslk-his.local', '19580090', '儿童保健科', '105', '15', '儿童营养、发育行为评估、眼保健', '中专',
        NULL, 0, '0.00', 1, 'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_employee (id, emp_code, emp_name, emp_type, gender, birth_date, hire_date, id_card, phone, email,
                          dept_id, dept_name, title, position, specialty, education, avatar, is_expert, expert_price,
                          status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('897001197', 'H17197', '郭柏', 1, 1, '1967-11-09', '1990-12-01', '330106196711091939', '19920201663',
        'guobai@cslk-his.local', '19580030', '眼科', '401', '1', '白内障超声乳化、青光眼、眼底病', '博士', NULL, 1,
        '100.00', 1, 'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_employee (id, emp_code, emp_name, emp_type, gender, birth_date, hire_date, id_card, phone, email,
                          dept_id, dept_name, title, position, specialty, education, avatar, is_expert, expert_price,
                          status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('897001198', 'H17198', '梁琪', 1, 2, '1975-12-13', '1998-05-01', '420106197512138868', '18916324959',
        'liangqi@cslk-his.local', '19580030', '眼科', '301', '2', '白内障超声乳化、青光眼、眼底病', '硕士', NULL, 1,
        '50.00', 1, 'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_employee (id, emp_code, emp_name, emp_type, gender, birth_date, hire_date, id_card, phone, email,
                          dept_id, dept_name, title, position, specialty, education, avatar, is_expert, expert_price,
                          status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('897001199', 'H17199', '袁念安', 1, 2, '1989-04-26', '2013-09-01', '510107198904267047', '15235263994',
        'yuannianan@cslk-his.local', '19580030', '眼科', '201', '31', '白内障超声乳化、青光眼、眼底病', '硕士', NULL, 0,
        '0.00', 1, 'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_employee (id, emp_code, emp_name, emp_type, gender, birth_date, hire_date, id_card, phone, email,
                          dept_id, dept_name, title, position, specialty, education, avatar, is_expert, expert_price,
                          status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('897001200', 'H17200', '袁乃文', 1, 1, '1997-06-08', '2022-07-01', '610113199706084595', '18134297530',
        'yuannaiwen@cslk-his.local', '19580030', '眼科', '102', '6', '白内障超声乳化、青光眼、眼底病', '本科', NULL, 0,
        '0.00', 1, 'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_employee (id, emp_code, emp_name, emp_type, gender, birth_date, hire_date, id_card, phone, email,
                          dept_id, dept_name, title, position, specialty, education, avatar, is_expert, expert_price,
                          status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('897001201', 'H17201', '罗妍', 2, 2, '1988-12-08', '2012-11-01', '320106198812088840', '15854461648',
        'luoyan@cslk-his.local', '19580030', '眼科', '403', '9', '白内障超声乳化、青光眼、眼底病', '大专', NULL, 0,
        '0.00', 1, 'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_employee (id, emp_code, emp_name, emp_type, gender, birth_date, hire_date, id_card, phone, email,
                          dept_id, dept_name, title, position, specialty, education, avatar, is_expert, expert_price,
                          status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('897001202', 'H17202', '莫欣', 2, 2, '1988-04-22', '2012-05-01', '330106198804229481', '15239383342',
        'moxin@cslk-his.local', '19580030', '眼科', '203', '15', '白内障超声乳化、青光眼、眼底病', '大专', NULL, 0,
        '0.00', 1, 'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_employee (id, emp_code, emp_name, emp_type, gender, birth_date, hire_date, id_card, phone, email,
                          dept_id, dept_name, title, position, specialty, education, avatar, is_expert, expert_price,
                          status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('897001203', 'H17203', '莫婉', 2, 2, '1997-05-12', '2020-08-01', '330106199705125366', '17642820088',
        'mowan@cslk-his.local', '19580030', '眼科', '105', '15', '白内障超声乳化、青光眼、眼底病', '中专', NULL, 0,
        '0.00', 1, 'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_employee (id, emp_code, emp_name, emp_type, gender, birth_date, hire_date, id_card, phone, email,
                          dept_id, dept_name, title, position, specialty, education, avatar, is_expert, expert_price,
                          status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('897001204', 'H17204', '陈庆', 1, 1, '1974-02-05', '1999-05-01', '310104197402055617', '15189026074',
        'chenqing@cslk-his.local', '19580031', '耳鼻喉科', '401', '1', '鼻内镜手术、听力植入、头颈肿瘤', '博士', NULL, 1,
        '100.00', 1, 'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_employee (id, emp_code, emp_name, emp_type, gender, birth_date, hire_date, id_card, phone, email,
                          dept_id, dept_name, title, position, specialty, education, avatar, is_expert, expert_price,
                          status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('897001205', 'H17205', '宋淑华', 1, 2, '1980-12-01', '2004-03-01', '420106198012017909', '15154766916',
        'songshuhua@cslk-his.local', '19580031', '耳鼻喉科', '301', '2', '鼻内镜手术、听力植入、头颈肿瘤', '硕士', NULL,
        1, '50.00', 1, 'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_employee (id, emp_code, emp_name, emp_type, gender, birth_date, hire_date, id_card, phone, email,
                          dept_id, dept_name, title, position, specialty, education, avatar, is_expert, expert_price,
                          status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('897001206', 'H17206', '施娜', 1, 2, '1986-07-21', '2011-01-01', '31010419860721468X', '18175542062',
        'shiqina@cslk-his.local', '19580031', '耳鼻喉科', '201', '31', '鼻内镜手术、听力植入、头颈肿瘤', '本科', NULL, 0,
        '0.00', 1, 'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_employee (id, emp_code, emp_name, emp_type, gender, birth_date, hire_date, id_card, phone, email,
                          dept_id, dept_name, title, position, specialty, education, avatar, is_expert, expert_price,
                          status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('897001207', 'H17207', '毛子轩', 1, 1, '1995-03-17', '2019-11-01', '370102199503171710', '13835225899',
        'maozixuan@cslk-his.local', '19580031', '耳鼻喉科', '102', '6', '鼻内镜手术、听力植入、头颈肿瘤', '本科', NULL, 0,
        '0.00', 1, 'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_employee (id, emp_code, emp_name, emp_type, gender, birth_date, hire_date, id_card, phone, email,
                          dept_id, dept_name, title, position, specialty, education, avatar, is_expert, expert_price,
                          status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('897001208', 'H17208', '毛岚', 2, 2, '1980-06-02', '2002-07-01', '320106198006020665', '13659816906',
        'maolan@cslk-his.local', '19580031', '耳鼻喉科', '403', '9', '鼻内镜手术、听力植入、头颈肿瘤', '大专', NULL, 0,
        '0.00', 1, 'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_employee (id, emp_code, emp_name, emp_type, gender, birth_date, hire_date, id_card, phone, email,
                          dept_id, dept_name, title, position, specialty, education, avatar, is_expert, expert_price,
                          status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('897001209', 'H17209', '杨彤', 2, 2, '1985-12-24', '2010-03-01', '510107198512242000', '13756598336',
        'yangtong@cslk-his.local', '19580031', '耳鼻喉科', '203', '15', '鼻内镜手术、听力植入、头颈肿瘤', '本科', NULL, 0,
        '0.00', 0, 'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_employee (id, emp_code, emp_name, emp_type, gender, birth_date, hire_date, id_card, phone, email,
                          dept_id, dept_name, title, position, specialty, education, avatar, is_expert, expert_price,
                          status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('897001210', 'H17210', '杨知微', 2, 2, '1994-02-20', '2017-05-01', '310104199402205602', '13591762219',
        'yangzhiwei@cslk-his.local', '19580031', '耳鼻喉科', '105', '15', '鼻内镜手术、听力植入、头颈肿瘤', '本科', NULL,
        0, '0.00', 1, 'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_employee (id, emp_code, emp_name, emp_type, gender, birth_date, hire_date, id_card, phone, email,
                          dept_id, dept_name, title, position, specialty, education, avatar, is_expert, expert_price,
                          status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('897001211', 'H17211', '邱宏', 1, 1, '1969-09-03', '1992-11-01', '110105196909030835', '18955514469',
        'qiuhong@cslk-his.local', '19580032', '口腔科', '401', '1', '口腔种植、正畸、牙体牙髓', '硕士', NULL, 1, '100.00',
        1, 'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_employee (id, emp_code, emp_name, emp_type, gender, birth_date, hire_date, id_card, phone, email,
                          dept_id, dept_name, title, position, specialty, education, avatar, is_expert, expert_price,
                          status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('897001212', 'H17212', '徐春燕', 1, 2, '1981-10-15', '2004-12-01', '110105198110154369', '17612476193',
        'xuchunyan@cslk-his.local', '19580032', '口腔科', '301', '2', '口腔种植、正畸、牙体牙髓', '本科', NULL, 1,
        '50.00', 1, 'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_employee (id, emp_code, emp_name, emp_type, gender, birth_date, hire_date, id_card, phone, email,
                          dept_id, dept_name, title, position, specialty, education, avatar, is_expert, expert_price,
                          status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('897001213', 'H17213', '梁厚朴', 1, 1, '1986-02-27', '2008-02-01', '31010419860227013X', '17659162681',
        'lianghoupu@cslk-his.local', '19580032', '口腔科', '201', '31', '口腔种植、正畸、牙体牙髓', '硕士', NULL, 0,
        '0.00', 1, 'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_employee (id, emp_code, emp_name, emp_type, gender, birth_date, hire_date, id_card, phone, email,
                          dept_id, dept_name, title, position, specialty, education, avatar, is_expert, expert_price,
                          status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('897001214', 'H17214', '向婉', 1, 2, '1995-07-03', '2019-10-01', '370102199507039207', '18520648487',
        'xiangwan@cslk-his.local', '19580032', '口腔科', '102', '6', '口腔种植、正畸、牙体牙髓', '硕士', NULL, 0, '0.00',
        1, 'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_employee (id, emp_code, emp_name, emp_type, gender, birth_date, hire_date, id_card, phone, email,
                          dept_id, dept_name, title, position, specialty, education, avatar, is_expert, expert_price,
                          status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('897001215', 'H17215', '胡雪', 2, 2, '1979-06-28', '2004-09-01', '110105197906286821', '13616323235',
        'huxueli@cslk-his.local', '19580032', '口腔科', '403', '9', '口腔种植、正畸、牙体牙髓', '大专', NULL, 0, '0.00',
        1, 'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_employee (id, emp_code, emp_name, emp_type, gender, birth_date, hire_date, id_card, phone, email,
                          dept_id, dept_name, title, position, specialty, education, avatar, is_expert, expert_price,
                          status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('897001216', 'H17216', '潘语嫣', 2, 2, '1985-01-21', '2010-03-01', '440305198501218144', '13571504173',
        'panyuyan@cslk-his.local', '19580032', '口腔科', '203', '15', '口腔种植、正畸、牙体牙髓', '本科', NULL, 0, '0.00',
        1, 'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_employee (id, emp_code, emp_name, emp_type, gender, birth_date, hire_date, id_card, phone, email,
                          dept_id, dept_name, title, position, specialty, education, avatar, is_expert, expert_price,
                          status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('897001217', 'H17217', '梁桂芳', 2, 2, '2000-12-24', '2023-12-01', '320106200012245684', '15952236552',
        'liangguifang@cslk-his.local', '19580032', '口腔科', '105', '15', '口腔种植、正畸、牙体牙髓', '本科', NULL, 0,
        '0.00', 1, 'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_employee (id, emp_code, emp_name, emp_type, gender, birth_date, hire_date, id_card, phone, email,
                          dept_id, dept_name, title, position, specialty, education, avatar, is_expert, expert_price,
                          status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('897001218', 'H17218', '任瑞', 1, 1, '1970-04-15', '1992-06-01', '51010719700415195X', '19929145296',
        'renrui@cslk-his.local', '19580033', '皮肤科', '401', '1', '免疫性皮肤病、皮肤外科、激光美容', '博士', NULL, 1,
        '100.00', 1, 'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_employee (id, emp_code, emp_name, emp_type, gender, birth_date, hire_date, id_card, phone, email,
                          dept_id, dept_name, title, position, specialty, education, avatar, is_expert, expert_price,
                          status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('897001219', 'H17219', '康静文', 1, 2, '1973-12-10', '1998-03-01', '11010519731210164X', '18693504998',
        'kangjingwen@cslk-his.local', '19580033', '皮肤科', '301', '2', '免疫性皮肤病、皮肤外科、激光美容', '博士', NULL,
        1, '50.00', 1, 'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_employee (id, emp_code, emp_name, emp_type, gender, birth_date, hire_date, id_card, phone, email,
                          dept_id, dept_name, title, position, specialty, education, avatar, is_expert, expert_price,
                          status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('897001220', 'H17220', '曹仁心', 1, 1, '1988-05-28', '2011-03-01', '330106198805285178', '15851933580',
        'caorenxin@cslk-his.local', '19580033', '皮肤科', '201', '31', '免疫性皮肤病、皮肤外科、激光美容', '硕士', NULL,
        0, '0.00', 1, 'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_employee (id, emp_code, emp_name, emp_type, gender, birth_date, hire_date, id_card, phone, email,
                          dept_id, dept_name, title, position, specialty, education, avatar, is_expert, expert_price,
                          status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('897001221', 'H17221', '顾桂芳', 1, 2, '1992-09-27', '2017-10-01', '210102199209272305', '18766993304',
        'guguifang@cslk-his.local', '19580033', '皮肤科', '102', '6', '免疫性皮肤病、皮肤外科、激光美容', '本科', NULL, 0,
        '0.00', 1, 'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_employee (id, emp_code, emp_name, emp_type, gender, birth_date, hire_date, id_card, phone, email,
                          dept_id, dept_name, title, position, specialty, education, avatar, is_expert, expert_price,
                          status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('897001222', 'H17222', '姚彤', 2, 2, '1988-07-02', '2013-11-01', '110105198807025583', '15721456100',
        'yaotong@cslk-his.local', '19580033', '皮肤科', '403', '9', '免疫性皮肤病、皮肤外科、激光美容', '大专', NULL, 0,
        '0.00', 1, 'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_employee (id, emp_code, emp_name, emp_type, gender, birth_date, hire_date, id_card, phone, email,
                          dept_id, dept_name, title, position, specialty, education, avatar, is_expert, expert_price,
                          status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('897001223', 'H17223', '石妍', 2, 2, '1995-05-18', '2017-05-01', '210102199505189362', '18962196302',
        'shiyan@cslk-his.local', '19580033', '皮肤科', '203', '15', '免疫性皮肤病、皮肤外科、激光美容', '大专', NULL, 0,
        '0.00', 1, 'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_employee (id, emp_code, emp_name, emp_type, gender, birth_date, hire_date, id_card, phone, email,
                          dept_id, dept_name, title, position, specialty, education, avatar, is_expert, expert_price,
                          status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('897001224', 'H17224', '贺俊杰', 2, 1, '1994-01-02', '2017-12-01', '420106199401020831', '18235652861',
        'hesunjunjie@cslk-his.local', '19580033', '皮肤科', '105', '15', '免疫性皮肤病、皮肤外科、激光美容', '本科', NULL,
        0, '0.00', 1, 'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_employee (id, emp_code, emp_name, emp_type, gender, birth_date, hire_date, id_card, phone, email,
                          dept_id, dept_name, title, position, specialty, education, avatar, is_expert, expert_price,
                          status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('897001225', 'H17225', '吕瑞', 1, 1, '1968-04-02', '1992-06-01', '330106196804026458', '13746034397',
        'lvrui@cslk-his.local', '19580091', '医疗美容科', '401', '1', '注射美容、体表缺损修复、光电治疗', '硕士', NULL, 1,
        '100.00', 1, 'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_employee (id, emp_code, emp_name, emp_type, gender, birth_date, hire_date, id_card, phone, email,
                          dept_id, dept_name, title, position, specialty, education, avatar, is_expert, expert_price,
                          status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('897001226', 'H17226', '杜白露', 1, 2, '1982-09-15', '2006-01-01', '330106198209157081', '15950097951',
        'dubailu@cslk-his.local', '19580091', '医疗美容科', '301', '2', '注射美容、体表缺损修复、光电治疗', '博士', NULL,
        1, '50.00', 1, 'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_employee (id, emp_code, emp_name, emp_type, gender, birth_date, hire_date, id_card, phone, email,
                          dept_id, dept_name, title, position, specialty, education, avatar, is_expert, expert_price,
                          status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('897001227', 'H17227', '彭昊天', 1, 1, '1991-08-15', '2013-01-01', '320106199108159734', '13922766220',
        'penghaotian@cslk-his.local', '19580091', '医疗美容科', '201', '31', '注射美容、体表缺损修复、光电治疗', '硕士',
        NULL, 0, '0.00', 1, 'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_employee (id, emp_code, emp_name, emp_type, gender, birth_date, hire_date, id_card, phone, email,
                          dept_id, dept_name, title, position, specialty, education, avatar, is_expert, expert_price,
                          status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('897001228', 'H17228', '田达', 1, 1, '1994-12-19', '2019-06-01', '210102199412197558', '19990768136',
        'tianda@cslk-his.local', '19580091', '医疗美容科', '102', '6', '注射美容、体表缺损修复、光电治疗', '硕士', NULL,
        0, '0.00', 1, 'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_employee (id, emp_code, emp_name, emp_type, gender, birth_date, hire_date, id_card, phone, email,
                          dept_id, dept_name, title, position, specialty, education, avatar, is_expert, expert_price,
                          status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('897001229', 'H17229', '唐彤', 2, 2, '1979-07-26', '2001-01-01', '310104197907269784', '13921559690',
        'tangtong@cslk-his.local', '19580091', '医疗美容科', '403', '9', '注射美容、体表缺损修复、光电治疗', '本科', NULL,
        0, '0.00', 1, 'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_employee (id, emp_code, emp_name, emp_type, gender, birth_date, hire_date, id_card, phone, email,
                          dept_id, dept_name, title, position, specialty, education, avatar, is_expert, expert_price,
                          status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('897001230', 'H17230', '郭青禾', 2, 2, '1985-03-27', '2009-09-01', '510107198503276225', '18577654441',
        'guoqinghe@cslk-his.local', '19580091', '医疗美容科', '203', '15', '注射美容、体表缺损修复、光电治疗', '大专',
        NULL, 0, '0.00', 1, 'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_employee (id, emp_code, emp_name, emp_type, gender, birth_date, hire_date, id_card, phone, email,
                          dept_id, dept_name, title, position, specialty, education, avatar, is_expert, expert_price,
                          status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('897001231', 'H17231', '梁彤', 2, 2, '2000-08-06', '2024-07-01', '610113200008064007', '13799475384',
        'liangtong@cslk-his.local', '19580091', '医疗美容科', '105', '15', '注射美容、体表缺损修复、光电治疗', '大专',
        NULL, 0, '0.00', 1, 'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_employee (id, emp_code, emp_name, emp_type, gender, birth_date, hire_date, id_card, phone, email,
                          dept_id, dept_name, title, position, specialty, education, avatar, is_expert, expert_price,
                          status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('897001232', 'H17232', '郑一诺', 1, 2, '1970-06-02', '1994-04-01', '330106197006029667', '19855345597',
        'zhengyinuo@cslk-his.local', '19580053', '中医内科', '401', '1', '中医脾胃病、心脑血管病、治未病', '博士', NULL,
        1, '100.00', 1, 'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_employee (id, emp_code, emp_name, emp_type, gender, birth_date, hire_date, id_card, phone, email,
                          dept_id, dept_name, title, position, specialty, education, avatar, is_expert, expert_price,
                          status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('897001233', 'H17233', '谢楠', 1, 1, '1981-02-22', '2006-05-01', '310104198102228170', '18015395649',
        'xienan@cslk-his.local', '19580053', '中医内科', '301', '2', '中医脾胃病、心脑血管病、治未病', '硕士', NULL, 1,
        '50.00', 1, 'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_employee (id, emp_code, emp_name, emp_type, gender, birth_date, hire_date, id_card, phone, email,
                          dept_id, dept_name, title, position, specialty, education, avatar, is_expert, expert_price,
                          status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('897001234', 'H17234', '胡秀英', 1, 2, '1986-12-06', '2009-08-01', '420106198612065007', '15282420233',
        'huxiuying@cslk-his.local', '19580053', '中医内科', '201', '31', '中医脾胃病、心脑血管病、治未病', '硕士', NULL,
        0, '0.00', 1, 'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_employee (id, emp_code, emp_name, emp_type, gender, birth_date, hire_date, id_card, phone, email,
                          dept_id, dept_name, title, position, specialty, education, avatar, is_expert, expert_price,
                          status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('897001235', 'H17235', '彭佳', 1, 2, '1993-03-12', '2016-10-01', '610113199303128661', '17863670626',
        'pengjia@cslk-his.local', '19580053', '中医内科', '102', '6', '中医脾胃病、心脑血管病、治未病', '本科', NULL, 0,
        '0.00', 1, 'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_employee (id, emp_code, emp_name, emp_type, gender, birth_date, hire_date, id_card, phone, email,
                          dept_id, dept_name, title, position, specialty, education, avatar, is_expert, expert_price,
                          status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('897001236', 'H17236', '魏月白', 2, 2, '1982-01-09', '2004-06-01', '420106198201093863', '15938361184',
        'weiyuebai@cslk-his.local', '19580053', '中医内科', '403', '9', '中医脾胃病、心脑血管病、治未病', '本科', NULL, 0,
        '0.00', 1, 'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_employee (id, emp_code, emp_name, emp_type, gender, birth_date, hire_date, id_card, phone, email,
                          dept_id, dept_name, title, position, specialty, education, avatar, is_expert, expert_price,
                          status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('897001237', 'H17237', '马青禾', 2, 2, '1985-01-22', '2009-11-01', '110105198501221688', '18699125589',
        'maqinghe@cslk-his.local', '19580053', '中医内科', '203', '15', '中医脾胃病、心脑血管病、治未病', '本科', NULL, 0,
        '0.00', 1, 'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_employee (id, emp_code, emp_name, emp_type, gender, birth_date, hire_date, id_card, phone, email,
                          dept_id, dept_name, title, position, specialty, education, avatar, is_expert, expert_price,
                          status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('897001238', 'H17238', '杨怡', 2, 2, '1996-11-08', '2019-04-01', '610113199611084226', '13666860108',
        'yangyi@cslk-his.local', '19580053', '中医内科', '105', '15', '中医脾胃病、心脑血管病、治未病', '本科', NULL, 0,
        '0.00', 1, 'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_employee (id, emp_code, emp_name, emp_type, gender, birth_date, hire_date, id_card, phone, email,
                          dept_id, dept_name, title, position, specialty, education, avatar, is_expert, expert_price,
                          status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('897001239', 'H17239', '郑世安', 1, 1, '1974-11-20', '1999-09-01', '51010719741120449X', '18781363925',
        'zhengshian@cslk-his.local', '19580054', '中医骨伤科', '401', '1', '中医正骨、颈肩腰腿痛、筋骨病', '硕士', NULL,
        1, '100.00', 1, 'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_employee (id, emp_code, emp_name, emp_type, gender, birth_date, hire_date, id_card, phone, email,
                          dept_id, dept_name, title, position, specialty, education, avatar, is_expert, expert_price,
                          status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('897001240', 'H17240', '齐昊天', 1, 1, '1977-02-28', '1999-06-01', '310104197702285553', '18052230082',
        'qihaotian@cslk-his.local', '19580054', '中医骨伤科', '301', '2', '中医正骨、颈肩腰腿痛、筋骨病', '博士', NULL, 1,
        '50.00', 1, 'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_employee (id, emp_code, emp_name, emp_type, gender, birth_date, hire_date, id_card, phone, email,
                          dept_id, dept_name, title, position, specialty, education, avatar, is_expert, expert_price,
                          status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('897001241', 'H17241', '周海艳', 1, 2, '1985-06-08', '2009-05-01', '320106198506081763', '15012841685',
        'zhouhaiyan@cslk-his.local', '19580054', '中医骨伤科', '201', '31', '中医正骨、颈肩腰腿痛、筋骨病', '本科', NULL,
        0, '0.00', 1, 'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_employee (id, emp_code, emp_name, emp_type, gender, birth_date, hire_date, id_card, phone, email,
                          dept_id, dept_name, title, position, specialty, education, avatar, is_expert, expert_price,
                          status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('897001242', 'H17242', '裘采薇', 1, 2, '1991-02-20', '2016-12-01', '440305199102202967', '17845986297',
        'qiuqiucaiw@cslk-his.local', '19580054', '中医骨伤科', '102', '6', '中医正骨、颈肩腰腿痛、筋骨病', '本科', NULL,
        0, '0.00', 1, 'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_employee (id, emp_code, emp_name, emp_type, gender, birth_date, hire_date, id_card, phone, email,
                          dept_id, dept_name, title, position, specialty, education, avatar, is_expert, expert_price,
                          status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('897001243', 'H17243', '齐雪', 2, 2, '1985-03-06', '2007-03-01', '320106198503065485', '18216291807',
        'qixueli@cslk-his.local', '19580054', '中医骨伤科', '403', '9', '中医正骨、颈肩腰腿痛、筋骨病', '大专', NULL, 0,
        '0.00', 1, 'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_employee (id, emp_code, emp_name, emp_type, gender, birth_date, hire_date, id_card, phone, email,
                          dept_id, dept_name, title, position, specialty, education, avatar, is_expert, expert_price,
                          status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('897001244', 'H17244', '卢淑华', 2, 2, '1993-10-19', '2016-07-01', '440305199310193364', '15925811407',
        'lushuhua@cslk-his.local', '19580054', '中医骨伤科', '203', '15', '中医正骨、颈肩腰腿痛、筋骨病', '大专', NULL, 0,
        '0.00', 1, 'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_employee (id, emp_code, emp_name, emp_type, gender, birth_date, hire_date, id_card, phone, email,
                          dept_id, dept_name, title, position, specialty, education, avatar, is_expert, expert_price,
                          status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('897001245', 'H17245', '施婧', 2, 2, '1996-08-06', '2019-09-01', '370102199608065068', '13740607054',
        'shiqijingjuan@cslk-his.local', '19580054', '中医骨伤科', '105', '15', '中医正骨、颈肩腰腿痛、筋骨病', '本科',
        NULL, 0, '0.00', 1, 'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_employee (id, emp_code, emp_name, emp_type, gender, birth_date, hire_date, id_card, phone, email,
                          dept_id, dept_name, title, position, specialty, education, avatar, is_expert, expert_price,
                          status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('897001246', 'H17246', '叶一诺', 1, 2, '1974-09-06', '1997-10-01', '210102197409064341', '19943418800',
        'yeyinuo@cslk-his.local', '19580055', '针灸推拿科', '401', '1', '针灸镇痛、推拿康复、面瘫调治', '博士', NULL, 1,
        '100.00', 1, 'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_employee (id, emp_code, emp_name, emp_type, gender, birth_date, hire_date, id_card, phone, email,
                          dept_id, dept_name, title, position, specialty, education, avatar, is_expert, expert_price,
                          status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('897001247', 'H17247', '莫梦洁', 1, 2, '1973-01-28', '1998-07-01', '110105197301284081', '18876545820',
        'momengjie@cslk-his.local', '19580055', '针灸推拿科', '301', '2', '针灸镇痛、推拿康复、面瘫调治', '本科', NULL, 1,
        '50.00', 1, 'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_employee (id, emp_code, emp_name, emp_type, gender, birth_date, hire_date, id_card, phone, email,
                          dept_id, dept_name, title, position, specialty, education, avatar, is_expert, expert_price,
                          status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('897001248', 'H17248', '陈瑞', 1, 1, '1984-09-04', '2008-09-01', '510107198409042131', '19948744530',
        'chenrui@cslk-his.local', '19580055', '针灸推拿科', '201', '31', '针灸镇痛、推拿康复、面瘫调治', '本科', NULL, 0,
        '0.00', 1, 'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_employee (id, emp_code, emp_name, emp_type, gender, birth_date, hire_date, id_card, phone, email,
                          dept_id, dept_name, title, position, specialty, education, avatar, is_expert, expert_price,
                          status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('897001249', 'H17249', '陈捷', 1, 1, '1993-05-25', '2016-05-01', '420106199305259092', '18984989736',
        'chenjie@cslk-his.local', '19580055', '针灸推拿科', '102', '6', '针灸镇痛、推拿康复、面瘫调治', '硕士', NULL, 0,
        '0.00', 0, 'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_employee (id, emp_code, emp_name, emp_type, gender, birth_date, hire_date, id_card, phone, email,
                          dept_id, dept_name, title, position, specialty, education, avatar, is_expert, expert_price,
                          status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('897001250', 'H17250', '尹欣', 2, 2, '1982-01-12', '2007-12-01', '310104198201126962', '13796285229',
        'yinxin@cslk-his.local', '19580055', '针灸推拿科', '403', '9', '针灸镇痛、推拿康复、面瘫调治', '大专', NULL, 0,
        '0.00', 1, 'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_employee (id, emp_code, emp_name, emp_type, gender, birth_date, hire_date, id_card, phone, email,
                          dept_id, dept_name, title, position, specialty, education, avatar, is_expert, expert_price,
                          status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('897001251', 'H17251', '苏桂芳', 2, 2, '1995-09-21', '2018-02-01', '310104199509219182', '18093016364',
        'suguifang@cslk-his.local', '19580055', '针灸推拿科', '203', '15', '针灸镇痛、推拿康复、面瘫调治', '大专', NULL,
        0, '0.00', 1, 'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_employee (id, emp_code, emp_name, emp_type, gender, birth_date, hire_date, id_card, phone, email,
                          dept_id, dept_name, title, position, specialty, education, avatar, is_expert, expert_price,
                          status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('897001252', 'H17252', '孟念安', 2, 2, '2000-01-13', '2022-09-01', '110105200001139741', '15972202433',
        'mengnianan@cslk-his.local', '19580055', '针灸推拿科', '105', '15', '针灸镇痛、推拿康复、面瘫调治', '本科', NULL,
        0, '0.00', 1, 'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_employee (id, emp_code, emp_name, emp_type, gender, birth_date, hire_date, id_card, phone, email,
                          dept_id, dept_name, title, position, specialty, education, avatar, is_expert, expert_price,
                          status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('897001253', 'H17253', '康承翼', 1, 1, '1970-02-08', '1992-10-01', '210102197002084113', '19882267197',
        'kangchengyi@cslk-his.local', '19580056', '中医妇科', '401', '1', '月经病、不孕不育、孕前调理', '硕士', NULL, 1,
        '100.00', 1, 'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_employee (id, emp_code, emp_name, emp_type, gender, birth_date, hire_date, id_card, phone, email,
                          dept_id, dept_name, title, position, specialty, education, avatar, is_expert, expert_price,
                          status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('897001254', 'H17254', '彭紫涵', 1, 2, '1977-05-24', '2002-07-01', '33010619770524856X', '13916697886',
        'pengzihan@cslk-his.local', '19580056', '中医妇科', '301', '2', '月经病、不孕不育、孕前调理', '硕士', NULL, 1,
        '50.00', 1, 'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_employee (id, emp_code, emp_name, emp_type, gender, birth_date, hire_date, id_card, phone, email,
                          dept_id, dept_name, title, position, specialty, education, avatar, is_expert, expert_price,
                          status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('897001255', 'H17255', '温立诚', 1, 1, '1989-08-19', '2011-06-01', '110105198908193914', '13548287398',
        'wenlicheng@cslk-his.local', '19580056', '中医妇科', '201', '31', '月经病、不孕不育、孕前调理', '硕士', NULL, 0,
        '0.00', 1, 'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_employee (id, emp_code, emp_name, emp_type, gender, birth_date, hire_date, id_card, phone, email,
                          dept_id, dept_name, title, position, specialty, education, avatar, is_expert, expert_price,
                          status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('897001256', 'H17256', '余舒', 1, 2, '1996-07-05', '2021-07-01', '310104199607059186', '15092855383',
        'yushengshu@cslk-his.local', '19580056', '中医妇科', '102', '6', '月经病、不孕不育、孕前调理', '硕士', NULL, 0,
        '0.00', 1, 'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_employee (id, emp_code, emp_name, emp_type, gender, birth_date, hire_date, id_card, phone, email,
                          dept_id, dept_name, title, position, specialty, education, avatar, is_expert, expert_price,
                          status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('897001257', 'H17257', '雷亦航', 2, 1, '1986-12-15', '2009-05-01', '370102198612153875', '13586658641',
        'leiwangyihang@cslk-his.local', '19580056', '中医妇科', '403', '9', '月经病、不孕不育、孕前调理', '大专', NULL, 0,
        '0.00', 1, 'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_employee (id, emp_code, emp_name, emp_type, gender, birth_date, hire_date, id_card, phone, email,
                          dept_id, dept_name, title, position, specialty, education, avatar, is_expert, expert_price,
                          status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('897001258', 'H17258', '容佳', 2, 2, '1990-11-02', '2015-10-01', '330106199011027040', '15254146606',
        'rongjia@cslk-his.local', '19580056', '中医妇科', '203', '15', '月经病、不孕不育、孕前调理', '大专', NULL, 0,
        '0.00', 1, 'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_employee (id, emp_code, emp_name, emp_type, gender, birth_date, hire_date, id_card, phone, email,
                          dept_id, dept_name, title, position, specialty, education, avatar, is_expert, expert_price,
                          status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('897001259', 'H17259', '邓山', 2, 1, '1994-01-19', '2017-10-01', '320106199401190319', '18240655031',
        'dengshan@cslk-his.local', '19580056', '中医妇科', '105', '15', '月经病、不孕不育、孕前调理', '中专', NULL, 0,
        '0.00', 1, 'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_employee (id, emp_code, emp_name, emp_type, gender, birth_date, hire_date, id_card, phone, email,
                          dept_id, dept_name, title, position, specialty, education, avatar, is_expert, expert_price,
                          status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('897001260', 'H17260', '钱远航', 1, 1, '1974-08-18', '1996-01-01', '110105197408180530', '19887843703',
        'qianyuanhang@cslk-his.local', '19580057', '中医儿科', '401', '1', '小儿推拿、体质调理、反复呼吸道感染', '硕士',
        NULL, 1, '100.00', 1, 'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_employee (id, emp_code, emp_name, emp_type, gender, birth_date, hire_date, id_card, phone, email,
                          dept_id, dept_name, title, position, specialty, education, avatar, is_expert, expert_price,
                          status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('897001261', 'H17261', '邓彤', 1, 2, '1975-11-26', '1999-07-01', '33010619751126588X', '18851018325',
        'dengtong@cslk-his.local', '19580057', '中医儿科', '301', '2', '小儿推拿、体质调理、反复呼吸道感染', '博士', NULL,
        1, '50.00', 1, 'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_employee (id, emp_code, emp_name, emp_type, gender, birth_date, hire_date, id_card, phone, email,
                          dept_id, dept_name, title, position, specialty, education, avatar, is_expert, expert_price,
                          status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('897001262', 'H17262', '严秀英', 1, 2, '1989-02-22', '2013-09-01', '610113198902228627', '15257515869',
        'yanxiuying@cslk-his.local', '19580057', '中医儿科', '201', '31', '小儿推拿、体质调理、反复呼吸道感染', '硕士',
        NULL, 0, '0.00', 1, 'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_employee (id, emp_code, emp_name, emp_type, gender, birth_date, hire_date, id_card, phone, email,
                          dept_id, dept_name, title, position, specialty, education, avatar, is_expert, expert_price,
                          status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('897001263', 'H17263', '莫瑞', 1, 1, '1992-05-13', '2016-04-01', '610113199205130477', '13528981115',
        'morui@cslk-his.local', '19580057', '中医儿科', '102', '6', '小儿推拿、体质调理、反复呼吸道感染', '硕士', NULL, 0,
        '0.00', 1, 'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_employee (id, emp_code, emp_name, emp_type, gender, birth_date, hire_date, id_card, phone, email,
                          dept_id, dept_name, title, position, specialty, education, avatar, is_expert, expert_price,
                          status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('897001264', 'H17264', '贺琪', 2, 2, '1987-08-25', '2010-04-01', '370102198708253088', '18734771187',
        'hesunqi@cslk-his.local', '19580057', '中医儿科', '403', '9', '小儿推拿、体质调理、反复呼吸道感染', '本科', NULL,
        0, '0.00', 1, 'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_employee (id, emp_code, emp_name, emp_type, gender, birth_date, hire_date, id_card, phone, email,
                          dept_id, dept_name, title, position, specialty, education, avatar, is_expert, expert_price,
                          status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('897001265', 'H17265', '王心怡', 2, 2, '1991-01-17', '2014-12-01', '610113199101177983', '13652559836',
        'wangxinyi@cslk-his.local', '19580057', '中医儿科', '203', '15', '小儿推拿、体质调理、反复呼吸道感染', '本科',
        NULL, 0, '0.00', 1, 'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_employee (id, emp_code, emp_name, emp_type, gender, birth_date, hire_date, id_card, phone, email,
                          dept_id, dept_name, title, position, specialty, education, avatar, is_expert, expert_price,
                          status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('897001266', 'H17266', '蒋安雅', 2, 2, '1998-12-26', '2022-10-01', '610113199812266747', '15863189138',
        'jianganya@cslk-his.local', '19580057', '中医儿科', '105', '15', '小儿推拿、体质调理、反复呼吸道感染', '中专',
        NULL, 0, '0.00', 1, 'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_employee (id, emp_code, emp_name, emp_type, gender, birth_date, hire_date, id_card, phone, email,
                          dept_id, dept_name, title, position, specialty, education, avatar, is_expert, expert_price,
                          status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('897001267', 'H17267', '石嘉杰', 1, 1, '1972-11-06', '1994-06-01', '510107197211069633', '15836005534',
        'shijiajie@cslk-his.local', '19580059', '肿瘤内科', '401', '1', '实体瘤化疗、靶向与免疫治疗', '博士', NULL, 1,
        '100.00', 1, 'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_employee (id, emp_code, emp_name, emp_type, gender, birth_date, hire_date, id_card, phone, email,
                          dept_id, dept_name, title, position, specialty, education, avatar, is_expert, expert_price,
                          status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('897001268', 'H17268', '金欣', 1, 2, '1976-02-25', '2000-05-01', '420106197602252728', '13885666674',
        'jinxin@cslk-his.local', '19580059', '肿瘤内科', '301', '2', '实体瘤化疗、靶向与免疫治疗', '硕士', NULL, 1,
        '50.00', 1, 'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_employee (id, emp_code, emp_name, emp_type, gender, birth_date, hire_date, id_card, phone, email,
                          dept_id, dept_name, title, position, specialty, education, avatar, is_expert, expert_price,
                          status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('897001269', 'H17269', '齐楠', 1, 1, '1986-08-13', '2011-09-01', '61011319860813171X', '15995944113',
        'qinan@cslk-his.local', '19580059', '肿瘤内科', '201', '31', '实体瘤化疗、靶向与免疫治疗', '本科', NULL, 0,
        '0.00', 1, 'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_employee (id, emp_code, emp_name, emp_type, gender, birth_date, hire_date, id_card, phone, email,
                          dept_id, dept_name, title, position, specialty, education, avatar, is_expert, expert_price,
                          status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('897001270', 'H17270', '李敏', 1, 2, '1990-07-23', '2016-11-01', '370102199007235105', '18074877507',
        'limin@cslk-his.local', '19580059', '肿瘤内科', '102', '6', '实体瘤化疗、靶向与免疫治疗', '硕士', NULL, 0,
        '0.00', 1, 'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_employee (id, emp_code, emp_name, emp_type, gender, birth_date, hire_date, id_card, phone, email,
                          dept_id, dept_name, title, position, specialty, education, avatar, is_expert, expert_price,
                          status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('897001271', 'H17271', '温明珠', 2, 2, '1983-09-28', '2007-10-01', '440305198309282701', '18151811454',
        'wenmingzhu@cslk-his.local', '19580059', '肿瘤内科', '403', '9', '实体瘤化疗、靶向与免疫治疗', '大专', NULL, 0,
        '0.00', 1, 'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_employee (id, emp_code, emp_name, emp_type, gender, birth_date, hire_date, id_card, phone, email,
                          dept_id, dept_name, title, position, specialty, education, avatar, is_expert, expert_price,
                          status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('897001272', 'H17272', '苏敏', 2, 2, '1985-11-11', '2008-12-01', '510107198511115261', '13863458952',
        'sumin@cslk-his.local', '19580059', '肿瘤内科', '203', '15', '实体瘤化疗、靶向与免疫治疗', '本科', NULL, 0,
        '0.00', 1, 'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_employee (id, emp_code, emp_name, emp_type, gender, birth_date, hire_date, id_card, phone, email,
                          dept_id, dept_name, title, position, specialty, education, avatar, is_expert, expert_price,
                          status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('897001273', 'H17273', '朱春燕', 2, 2, '1998-07-25', '2023-09-01', '210102199807258204', '17615196897',
        'zhuchunyan@cslk-his.local', '19580059', '肿瘤内科', '105', '15', '实体瘤化疗、靶向与免疫治疗', '大专', NULL, 0,
        '0.00', 1, 'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_employee (id, emp_code, emp_name, emp_type, gender, birth_date, hire_date, id_card, phone, email,
                          dept_id, dept_name, title, position, specialty, education, avatar, is_expert, expert_price,
                          status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('897001274', 'H17274', '严国强', 1, 1, '1969-04-03', '1992-08-01', '44030519690403085X', '18973002643',
        'yanguoqiang@cslk-his.local', '19580060', '肿瘤外科', '401', '1', '肿瘤根治手术、综合治疗与 MDT', '硕士', NULL,
        1, '100.00', 1, 'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_employee (id, emp_code, emp_name, emp_type, gender, birth_date, hire_date, id_card, phone, email,
                          dept_id, dept_name, title, position, specialty, education, avatar, is_expert, expert_price,
                          status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('897001275', 'H17275', '康建国', 1, 1, '1979-01-05', '2003-01-01', '330106197901056151', '19990199894',
        'kangjianguo@cslk-his.local', '19580060', '肿瘤外科', '301', '2', '肿瘤根治手术、综合治疗与 MDT', '博士', NULL,
        1, '50.00', 1, 'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_employee (id, emp_code, emp_name, emp_type, gender, birth_date, hire_date, id_card, phone, email,
                          dept_id, dept_name, title, position, specialty, education, avatar, is_expert, expert_price,
                          status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('897001276', 'H17276', '金瑶', 1, 2, '1983-01-25', '2008-11-01', '440305198301250502', '17635387752',
        'jinyao@cslk-his.local', '19580060', '肿瘤外科', '201', '31', '肿瘤根治手术、综合治疗与 MDT', '本科', NULL, 0,
        '0.00', 1, 'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_employee (id, emp_code, emp_name, emp_type, gender, birth_date, hire_date, id_card, phone, email,
                          dept_id, dept_name, title, position, specialty, education, avatar, is_expert, expert_price,
                          status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('897001277', 'H17277', '尹维', 1, 1, '1994-04-02', '2019-01-01', '610113199404025653', '13977480148',
        'yinweiqi@cslk-his.local', '19580060', '肿瘤外科', '102', '6', '肿瘤根治手术、综合治疗与 MDT', '硕士', NULL, 0,
        '0.00', 1, 'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_employee (id, emp_code, emp_name, emp_type, gender, birth_date, hire_date, id_card, phone, email,
                          dept_id, dept_name, title, position, specialty, education, avatar, is_expert, expert_price,
                          status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('897001278', 'H17278', '范寒梅', 2, 2, '1984-11-11', '2007-09-01', '610113198411114529', '13877501665',
        'fanhanmei@cslk-his.local', '19580060', '肿瘤外科', '403', '9', '肿瘤根治手术、综合治疗与 MDT', '大专', NULL, 0,
        '0.00', 1, 'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_employee (id, emp_code, emp_name, emp_type, gender, birth_date, hire_date, id_card, phone, email,
                          dept_id, dept_name, title, position, specialty, education, avatar, is_expert, expert_price,
                          status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('897001279', 'H17279', '程寒梅', 2, 2, '1994-10-03', '2016-09-01', '51010719941003810X', '13563920849',
        'chenghanmei@cslk-his.local', '19580060', '肿瘤外科', '203', '15', '肿瘤根治手术、综合治疗与 MDT', '本科', NULL,
        0, '0.00', 1, 'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_employee (id, emp_code, emp_name, emp_type, gender, birth_date, hire_date, id_card, phone, email,
                          dept_id, dept_name, title, position, specialty, education, avatar, is_expert, expert_price,
                          status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('897001280', 'H17280', '马梦洁', 2, 2, '1994-06-27', '2017-06-01', '440305199406279904', '13985007623',
        'mamengjie@cslk-his.local', '19580060', '肿瘤外科', '105', '15', '肿瘤根治手术、综合治疗与 MDT', '中专', NULL, 0,
        '0.00', 1, 'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_employee (id, emp_code, emp_name, emp_type, gender, birth_date, hire_date, id_card, phone, email,
                          dept_id, dept_name, title, position, specialty, education, avatar, is_expert, expert_price,
                          status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('897001281', 'H17281', '秦厚朴', 1, 1, '1973-05-10', '1998-04-01', '110105197305109774', '18581458243',
        'qinhoupu@cslk-his.local', '19580062', '安宁疗护病房', '401', '1', '终末期症状控制、人文关怀、哀伤辅导', '博士',
        NULL, 1, '100.00', 1, 'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_employee (id, emp_code, emp_name, emp_type, gender, birth_date, hire_date, id_card, phone, email,
                          dept_id, dept_name, title, position, specialty, education, avatar, is_expert, expert_price,
                          status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('897001282', 'H17282', '杜晓峰', 1, 2, '1973-12-10', '1998-08-01', '210102197312106306', '18877309166',
        'duxiaofeng@cslk-his.local', '19580062', '安宁疗护病房', '301', '2', '终末期症状控制、人文关怀、哀伤辅导', '博士',
        NULL, 1, '50.00', 1, 'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_employee (id, emp_code, emp_name, emp_type, gender, birth_date, hire_date, id_card, phone, email,
                          dept_id, dept_name, title, position, specialty, education, avatar, is_expert, expert_price,
                          status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('897001283', 'H17283', '叶洁', 1, 2, '1987-10-01', '2012-08-01', '370102198710013227', '18852400091',
        'yejie@cslk-his.local', '19580062', '安宁疗护病房', '201', '31', '终末期症状控制、人文关怀、哀伤辅导', '本科',
        NULL, 0, '0.00', 1, 'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_employee (id, emp_code, emp_name, emp_type, gender, birth_date, hire_date, id_card, phone, email,
                          dept_id, dept_name, title, position, specialty, education, avatar, is_expert, expert_price,
                          status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('897001284', 'H17284', '丁璐', 1, 2, '1991-10-07', '2016-03-01', '32010619911007246X', '18185648947',
        'dinglu@cslk-his.local', '19580062', '安宁疗护病房', '102', '6', '终末期症状控制、人文关怀、哀伤辅导', '硕士',
        NULL, 0, '0.00', 1, 'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_employee (id, emp_code, emp_name, emp_type, gender, birth_date, hire_date, id_card, phone, email,
                          dept_id, dept_name, title, position, specialty, education, avatar, is_expert, expert_price,
                          status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('897001285', 'H17285', '岑青禾', 2, 2, '1986-09-13', '2010-08-01', '330106198609133564', '15877658915',
        'cenqinghe@cslk-his.local', '19580062', '安宁疗护病房', '403', '9', '终末期症状控制、人文关怀、哀伤辅导', '大专',
        NULL, 0, '0.00', 1, 'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_employee (id, emp_code, emp_name, emp_type, gender, birth_date, hire_date, id_card, phone, email,
                          dept_id, dept_name, title, position, specialty, education, avatar, is_expert, expert_price,
                          status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('897001286', 'H17286', '丁一诺', 2, 2, '1995-12-01', '2019-01-01', '110105199512010223', '18775634276',
        'dingyinuo@cslk-his.local', '19580062', '安宁疗护病房', '203', '15', '终末期症状控制、人文关怀、哀伤辅导', '本科',
        NULL, 0, '0.00', 1, 'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_employee (id, emp_code, emp_name, emp_type, gender, birth_date, hire_date, id_card, phone, email,
                          dept_id, dept_name, title, position, specialty, education, avatar, is_expert, expert_price,
                          status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('897001287', 'H17287', '周颖', 2, 2, '1998-04-24', '2021-04-01', '610113199804244468', '19928237390',
        'zhouying@cslk-his.local', '19580062', '安宁疗护病房', '105', '15', '终末期症状控制、人文关怀、哀伤辅导', '本科',
        NULL, 0, '0.00', 1, 'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_employee (id, emp_code, emp_name, emp_type, gender, birth_date, hire_date, id_card, phone, email,
                          dept_id, dept_name, title, position, specialty, education, avatar, is_expert, expert_price,
                          status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('897001288', 'H17288', '石和玉', 1, 2, '1969-02-16', '1991-10-01', '110105196902160100', '15212584640',
        'shiheyu@cslk-his.local', '19580064', '精神科门诊', '401', '1', '精神分裂症、情感障碍、精神康复', '硕士', NULL, 1,
        '100.00', 1, 'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_employee (id, emp_code, emp_name, emp_type, gender, birth_date, hire_date, id_card, phone, email,
                          dept_id, dept_name, title, position, specialty, education, avatar, is_expert, expert_price,
                          status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('897001289', 'H17289', '彭婷', 1, 2, '1973-10-25', '1998-10-01', '21010219731025580X', '18816253709',
        'pengting@cslk-his.local', '19580064', '精神科门诊', '301', '2', '精神分裂症、情感障碍、精神康复', '本科', NULL,
        1, '50.00', 0, 'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_employee (id, emp_code, emp_name, emp_type, gender, birth_date, hire_date, id_card, phone, email,
                          dept_id, dept_name, title, position, specialty, education, avatar, is_expert, expert_price,
                          status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('897001290', 'H17290', '卢松', 1, 1, '1988-05-08', '2013-02-01', '110105198805088337', '15993036550',
        'lusong@cslk-his.local', '19580064', '精神科门诊', '201', '31', '精神分裂症、情感障碍、精神康复', '硕士', NULL, 0,
        '0.00', 1, 'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_employee (id, emp_code, emp_name, emp_type, gender, birth_date, hire_date, id_card, phone, email,
                          dept_id, dept_name, title, position, specialty, education, avatar, is_expert, expert_price,
                          status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('897001291', 'H17291', '尚思琪', 1, 2, '1997-10-27', '2022-10-01', '320106199710279341', '19912231941',
        'shangsiqi@cslk-his.local', '19580064', '精神科门诊', '102', '6', '精神分裂症、情感障碍、精神康复', '本科', NULL,
        0, '0.00', 1, 'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_employee (id, emp_code, emp_name, emp_type, gender, birth_date, hire_date, id_card, phone, email,
                          dept_id, dept_name, title, position, specialty, education, avatar, is_expert, expert_price,
                          status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('897001292', 'H17292', '侯秀英', 2, 2, '1986-01-11', '2010-05-01', '210102198601117188', '18951762498',
        'houxiuying@cslk-his.local', '19580064', '精神科门诊', '403', '9', '精神分裂症、情感障碍、精神康复', '大专', NULL,
        0, '0.00', 1, 'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_employee (id, emp_code, emp_name, emp_type, gender, birth_date, hire_date, id_card, phone, email,
                          dept_id, dept_name, title, position, specialty, education, avatar, is_expert, expert_price,
                          status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('897001293', 'H17293', '龙丽', 2, 2, '1995-06-14', '2017-06-01', '440305199506142569', '15129744094',
        'longli@cslk-his.local', '19580064', '精神科门诊', '203', '15', '精神分裂症、情感障碍、精神康复', '大专', NULL, 0,
        '0.00', 1, 'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_employee (id, emp_code, emp_name, emp_type, gender, birth_date, hire_date, id_card, phone, email,
                          dept_id, dept_name, title, position, specialty, education, avatar, is_expert, expert_price,
                          status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('897001294', 'H17294', '吕薇', 2, 2, '1997-05-18', '2022-04-01', '310104199705188088', '13878207990',
        'lvweiwei@cslk-his.local', '19580064', '精神科门诊', '105', '15', '精神分裂症、情感障碍、精神康复', '大专', NULL,
        0, '0.00', 1, 'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_employee (id, emp_code, emp_name, emp_type, gender, birth_date, hire_date, id_card, phone, email,
                          dept_id, dept_name, title, position, specialty, education, avatar, is_expert, expert_price,
                          status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('897001295', 'H17295', '何宏', 1, 1, '1968-07-05', '1991-05-01', '110105196807050376', '18012602064',
        'hehong@cslk-his.local', '19580065', '临床心理科', '401', '1', '焦虑抑郁、心理评估与治疗、危机干预', '博士', NULL,
        1, '100.00', 1, 'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_employee (id, emp_code, emp_name, emp_type, gender, birth_date, hire_date, id_card, phone, email,
                          dept_id, dept_name, title, position, specialty, education, avatar, is_expert, expert_price,
                          status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('897001296', 'H17296', '周嘉怡', 1, 2, '1972-07-07', '1998-07-01', '110105197207072402', '19881958228',
        'zhoujiayi@cslk-his.local', '19580065', '临床心理科', '301', '2', '焦虑抑郁、心理评估与治疗、危机干预', '本科',
        NULL, 1, '50.00', 1, 'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_employee (id, emp_code, emp_name, emp_type, gender, birth_date, hire_date, id_card, phone, email,
                          dept_id, dept_name, title, position, specialty, education, avatar, is_expert, expert_price,
                          status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('897001297', 'H17297', '徐寒梅', 1, 2, '1983-03-14', '2008-03-01', '370102198303147404', '18263702212',
        'xuhanmei@cslk-his.local', '19580065', '临床心理科', '201', '31', '焦虑抑郁、心理评估与治疗、危机干预', '本科',
        NULL, 0, '0.00', 1, 'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_employee (id, emp_code, emp_name, emp_type, gender, birth_date, hire_date, id_card, phone, email,
                          dept_id, dept_name, title, position, specialty, education, avatar, is_expert, expert_price,
                          status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('897001298', 'H17298', '程静', 1, 2, '1997-07-06', '2021-12-01', '210102199707062343', '18228185709',
        'chengjing@cslk-his.local', '19580065', '临床心理科', '102', '6', '焦虑抑郁、心理评估与治疗、危机干预', '硕士',
        NULL, 0, '0.00', 1, 'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_employee (id, emp_code, emp_name, emp_type, gender, birth_date, hire_date, id_card, phone, email,
                          dept_id, dept_name, title, position, specialty, education, avatar, is_expert, expert_price,
                          status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('897001299', 'H17299', '陆敏', 2, 2, '1986-01-10', '2008-12-01', '310104198601109184', '13782470876',
        'lukumin@cslk-his.local', '19580065', '临床心理科', '403', '9', '焦虑抑郁、心理评估与治疗、危机干预', '本科',
        NULL, 0, '0.00', 1, 'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_employee (id, emp_code, emp_name, emp_type, gender, birth_date, hire_date, id_card, phone, email,
                          dept_id, dept_name, title, position, specialty, education, avatar, is_expert, expert_price,
                          status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('897001300', 'H17300', '夏浩', 2, 1, '1986-02-19', '2010-10-01', '210102198602198477', '15791852822',
        'xiahao@cslk-his.local', '19580065', '临床心理科', '203', '15', '焦虑抑郁、心理评估与治疗、危机干预', '大专',
        NULL, 0, '0.00', 1, 'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_employee (id, emp_code, emp_name, emp_type, gender, birth_date, hire_date, id_card, phone, email,
                          dept_id, dept_name, title, position, specialty, education, avatar, is_expert, expert_price,
                          status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('897001301', 'H17301', '孟玲', 2, 2, '1996-04-03', '2020-11-01', '320106199604031000', '17883641818',
        'mengling@cslk-his.local', '19580065', '临床心理科', '105', '15', '焦虑抑郁、心理评估与治疗、危机干预', '中专',
        NULL, 0, '0.00', 1, 'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_employee (id, emp_code, emp_name, emp_type, gender, birth_date, hire_date, id_card, phone, email,
                          dept_id, dept_name, title, position, specialty, education, avatar, is_expert, expert_price,
                          status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('897001302', 'H17302', '谭薇', 1, 2, '1966-03-05', '1990-09-01', '110105196603059280', '18042641196',
        'tanweiwei@cslk-his.local', '19580066', '睡眠医学中心', '401', '1', '睡眠呼吸障碍、失眠认知行为治疗', '博士',
        NULL, 1, '100.00', 1, 'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_employee (id, emp_code, emp_name, emp_type, gender, birth_date, hire_date, id_card, phone, email,
                          dept_id, dept_name, title, position, specialty, education, avatar, is_expert, expert_price,
                          status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('897001303', 'H17303', '孙致远', 1, 1, '1973-05-26', '1998-06-01', '61011319730526377X', '15963743035',
        'sunzhiyuan2@cslk-his.local', '19580066', '睡眠医学中心', '301', '2', '睡眠呼吸障碍、失眠认知行为治疗', '本科',
        NULL, 1, '50.00', 1, 'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_employee (id, emp_code, emp_name, emp_type, gender, birth_date, hire_date, id_card, phone, email,
                          dept_id, dept_name, title, position, specialty, education, avatar, is_expert, expert_price,
                          status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('897001304', 'H17304', '姚宇轩', 1, 1, '1992-11-08', '2017-08-01', '330106199211088358', '13626089493',
        'yaoyuxuan@cslk-his.local', '19580066', '睡眠医学中心', '201', '31', '睡眠呼吸障碍、失眠认知行为治疗', '硕士',
        NULL, 0, '0.00', 1, 'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_employee (id, emp_code, emp_name, emp_type, gender, birth_date, hire_date, id_card, phone, email,
                          dept_id, dept_name, title, position, specialty, education, avatar, is_expert, expert_price,
                          status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('897001305', 'H17305', '徐延平', 1, 1, '1993-02-27', '2017-03-01', '610113199302277251', '15765730885',
        'xuyanping@cslk-his.local', '19580066', '睡眠医学中心', '102', '6', '睡眠呼吸障碍、失眠认知行为治疗', '硕士',
        NULL, 0, '0.00', 1, 'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_employee (id, emp_code, emp_name, emp_type, gender, birth_date, hire_date, id_card, phone, email,
                          dept_id, dept_name, title, position, specialty, education, avatar, is_expert, expert_price,
                          status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('897001306', 'H17306', '刘乐怡', 2, 2, '1982-02-12', '2004-11-01', '330106198202127947', '13561146330',
        'liuleyi@cslk-his.local', '19580066', '睡眠医学中心', '403', '9', '睡眠呼吸障碍、失眠认知行为治疗', '本科', NULL,
        0, '0.00', 1, 'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_employee (id, emp_code, emp_name, emp_type, gender, birth_date, hire_date, id_card, phone, email,
                          dept_id, dept_name, title, position, specialty, education, avatar, is_expert, expert_price,
                          status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('897001307', 'H17307', '董瑶', 2, 2, '1992-05-06', '2016-10-01', '210102199205062060', '17811850005',
        'dongyao@cslk-his.local', '19580066', '睡眠医学中心', '203', '15', '睡眠呼吸障碍、失眠认知行为治疗', '本科',
        NULL, 0, '0.00', 1, 'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_employee (id, emp_code, emp_name, emp_type, gender, birth_date, hire_date, id_card, phone, email,
                          dept_id, dept_name, title, position, specialty, education, avatar, is_expert, expert_price,
                          status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('897001308', 'H17308', '唐欣', 2, 2, '1998-12-09', '2023-04-01', '320106199812097303', '18064531905',
        'tangxin@cslk-his.local', '19580066', '睡眠医学中心', '105', '15', '睡眠呼吸障碍、失眠认知行为治疗', '中专',
        NULL, 0, '0.00', 1, 'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_employee (id, emp_code, emp_name, emp_type, gender, birth_date, hire_date, id_card, phone, email,
                          dept_id, dept_name, title, position, specialty, education, avatar, is_expert, expert_price,
                          status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('897001309', 'H17309', '余明德', 1, 1, '1972-06-26', '1995-12-01', '610113197206260231', '15914383523',
        'yushengmingde@cslk-his.local', '19580070', '肝病科', '401', '1', '病毒性肝炎、脂肪性肝病、肝硬化', '博士', NULL,
        1, '100.00', 1, 'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_employee (id, emp_code, emp_name, emp_type, gender, birth_date, hire_date, id_card, phone, email,
                          dept_id, dept_name, title, position, specialty, education, avatar, is_expert, expert_price,
                          status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('897001310', 'H17310', '朱海艳', 1, 2, '1977-08-21', '2002-11-01', '33010619770821818X', '15953757997',
        'zhuhaiyan@cslk-his.local', '19580070', '肝病科', '301', '2', '病毒性肝炎、脂肪性肝病、肝硬化', '硕士', NULL, 1,
        '50.00', 1, 'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_employee (id, emp_code, emp_name, emp_type, gender, birth_date, hire_date, id_card, phone, email,
                          dept_id, dept_name, title, position, specialty, education, avatar, is_expert, expert_price,
                          status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('897001311', 'H17311', '何栋', 1, 1, '1989-01-13', '2011-08-01', '210102198901134510', '18883629212',
        'hedong@cslk-his.local', '19580070', '肝病科', '201', '31', '病毒性肝炎、脂肪性肝病、肝硬化', '本科', NULL, 0,
        '0.00', 1, 'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_employee (id, emp_code, emp_name, emp_type, gender, birth_date, hire_date, id_card, phone, email,
                          dept_id, dept_name, title, position, specialty, education, avatar, is_expert, expert_price,
                          status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('897001312', 'H17312', '梁映竹', 1, 2, '1994-04-08', '2017-05-01', '330106199404080360', '15116083550',
        'liangyingzhu@cslk-his.local', '19580070', '肝病科', '102', '6', '病毒性肝炎、脂肪性肝病、肝硬化', '硕士', NULL,
        0, '0.00', 1, 'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_employee (id, emp_code, emp_name, emp_type, gender, birth_date, hire_date, id_card, phone, email,
                          dept_id, dept_name, title, position, specialty, education, avatar, is_expert, expert_price,
                          status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('897001313', 'H17313', '郭洁', 2, 2, '1982-05-27', '2004-07-01', '320106198205275601', '18852616117',
        'guojie@cslk-his.local', '19580070', '肝病科', '403', '9', '病毒性肝炎、脂肪性肝病、肝硬化', '大专', NULL, 0,
        '0.00', 1, 'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_employee (id, emp_code, emp_name, emp_type, gender, birth_date, hire_date, id_card, phone, email,
                          dept_id, dept_name, title, position, specialty, education, avatar, is_expert, expert_price,
                          status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('897001314', 'H17314', '马静文', 2, 2, '1985-03-10', '2009-04-01', '33010619850310102X', '13528570944',
        'majingwen@cslk-his.local', '19580070', '肝病科', '203', '15', '病毒性肝炎、脂肪性肝病、肝硬化', '本科', NULL, 0,
        '0.00', 1, 'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_employee (id, emp_code, emp_name, emp_type, gender, birth_date, hire_date, id_card, phone, email,
                          dept_id, dept_name, title, position, specialty, education, avatar, is_expert, expert_price,
                          status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('897001315', 'H17315', '刘月白', 2, 2, '1995-04-04', '2020-03-01', '440305199504046645', '18019537205',
        'liuyuebai@cslk-his.local', '19580070', '肝病科', '105', '15', '病毒性肝炎、脂肪性肝病、肝硬化', '中专', NULL, 0,
        '0.00', 1, 'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_employee (id, emp_code, emp_name, emp_type, gender, birth_date, hire_date, id_card, phone, email,
                          dept_id, dept_name, title, position, specialty, education, avatar, is_expert, expert_price,
                          status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('897001316', 'H17316', '薛月白', 1, 2, '1979-04-04', '2002-10-01', '33010619790404608X', '17639354277',
        'xueyuebai@cslk-his.local', '19580072', '普通体检科', '301', '16', '健康体检、慢病风险筛查与报告解读', '硕士',
        NULL, 1, '0.00', 1, 'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_employee (id, emp_code, emp_name, emp_type, gender, birth_date, hire_date, id_card, phone, email,
                          dept_id, dept_name, title, position, specialty, education, avatar, is_expert, expert_price,
                          status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('897001317', 'H17317', '魏磊', 1, 1, '1992-12-02', '2017-10-01', '370102199212021173', '13584895303',
        'weilei@cslk-his.local', '19580072', '普通体检科', '201', '31', '健康体检、慢病风险筛查与报告解读', '硕士', NULL,
        0, '0.00', 1, 'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_employee (id, emp_code, emp_name, emp_type, gender, birth_date, hire_date, id_card, phone, email,
                          dept_id, dept_name, title, position, specialty, education, avatar, is_expert, expert_price,
                          status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('897001318', 'H17318', '苏海艳', 2, 2, '1998-03-17', '2023-05-01', '210102199803170504', '17840463542',
        'suhaiyan@cslk-his.local', '19580072', '普通体检科', '106', '32', '健康体检、慢病风险筛查与报告解读', '硕士',
        NULL, 0, '0.00', 1, 'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_employee (id, emp_code, emp_name, emp_type, gender, birth_date, hire_date, id_card, phone, email,
                          dept_id, dept_name, title, position, specialty, education, avatar, is_expert, expert_price,
                          status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('897001319', 'H17319', '康蓉', 6, 2, '1995-10-21', '2018-09-01', '320106199510217824', '18150192290',
        'kangrong@cslk-his.local', '19580072', '普通体检科', '105', '32', '健康体检、慢病风险筛查与报告解读', '本科',
        NULL, 0, '0.00', 1, 'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_employee (id, emp_code, emp_name, emp_type, gender, birth_date, hire_date, id_card, phone, email,
                          dept_id, dept_name, title, position, specialty, education, avatar, is_expert, expert_price,
                          status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('897001320', 'H17320', '薛白露', 6, 2, '1989-12-28', '2014-06-01', '510107198912280727', '15078406074',
        'xuebailu@cslk-his.local', '19580072', '普通体检科', '102', '33', '健康体检、慢病风险筛查与报告解读', '硕士',
        NULL, 0, '0.00', 1, 'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_employee (id, emp_code, emp_name, emp_type, gender, birth_date, hire_date, id_card, phone, email,
                          dept_id, dept_name, title, position, specialty, education, avatar, is_expert, expert_price,
                          status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('897001321', 'H17321', '胡朝阳', 1, 1, '1968-07-08', '1993-09-01', '330106196807088232', '13956655604',
        'huchaoyang@cslk-his.local', '19580073', 'VIP体检中心', '401', '31', '个性化体检套餐、深度筛查与健康管理',
        '本科', NULL, 1, '80.00', 1, 'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_employee (id, emp_code, emp_name, emp_type, gender, birth_date, hire_date, id_card, phone, email,
                          dept_id, dept_name, title, position, specialty, education, avatar, is_expert, expert_price,
                          status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('897001322', 'H17322', '许宜修', 1, 2, '1988-02-05', '2012-08-01', '210102198802057548', '18177797081',
        'xushuyixiu@cslk-his.local', '19580073', 'VIP体检中心', '201', '31', '个性化体检套餐、深度筛查与健康管理',
        '本科', NULL, 0, '0.00', 1, 'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_employee (id, emp_code, emp_name, emp_type, gender, birth_date, hire_date, id_card, phone, email,
                          dept_id, dept_name, title, position, specialty, education, avatar, is_expert, expert_price,
                          status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('897001323', 'H17323', '郑静文', 6, 2, '1997-07-20', '2020-11-01', '370102199707201627', '18887653448',
        'zhengjingwen@cslk-his.local', '19580073', 'VIP体检中心', '105', '33', '个性化体检套餐、深度筛查与健康管理',
        '硕士', NULL, 0, '0.00', 1, 'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_employee (id, emp_code, emp_name, emp_type, gender, birth_date, hire_date, id_card, phone, email,
                          dept_id, dept_name, title, position, specialty, education, avatar, is_expert, expert_price,
                          status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('897001324', 'H17324', '胡怡', 6, 2, '1990-06-01', '2015-08-01', '510107199006013568', '17811761589',
        'huyi@cslk-his.local', '19580073', 'VIP体检中心', '508', '34', '个性化体检套餐、深度筛查与健康管理', '本科',
        NULL, 0, '0.00', 1, 'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_employee (id, emp_code, emp_name, emp_type, gender, birth_date, hire_date, id_card, phone, email,
                          dept_id, dept_name, title, position, specialty, education, avatar, is_expert, expert_price,
                          status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('897001325', 'H17325', '毛升', 6, 1, '1982-12-20', '2004-12-01', '510107198212206032', '13683914076',
        'maosheng@cslk-his.local', '19580074', '预防保健科', '201', '22', '疾病监测、免疫规划、慢病建档与公卫上报',
        '本科', NULL, 0, '0.00', 1, 'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_employee (id, emp_code, emp_name, emp_type, gender, birth_date, hire_date, id_card, phone, email,
                          dept_id, dept_name, title, position, specialty, education, avatar, is_expert, expert_price,
                          status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('897001326', 'H17326', '尹厚朴', 6, 1, '1995-08-18', '2017-03-01', '110105199508186170', '13599714357',
        'yinhoupu@cslk-his.local', '19580074', '预防保健科', '102', '35', '疾病监测、免疫规划、慢病建档与公卫上报',
        '硕士', NULL, 0, '0.00', 1, 'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_employee (id, emp_code, emp_name, emp_type, gender, birth_date, hire_date, id_card, phone, email,
                          dept_id, dept_name, title, position, specialty, education, avatar, is_expert, expert_price,
                          status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('897001327', 'H17327', '向白露', 6, 2, '2000-08-01', '2023-01-01', '320106200008013223', '13631932769',
        'xiangbailu@cslk-his.local', '19580074', '预防保健科', '108', '35', '疾病监测、免疫规划、慢病建档与公卫上报',
        '本科', NULL, 0, '0.00', 1, 'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_employee (id, emp_code, emp_name, emp_type, gender, birth_date, hire_date, id_card, phone, email,
                          dept_id, dept_name, title, position, specialty, education, avatar, is_expert, expert_price,
                          status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('897001328', 'H17328', '石岚', 2, 2, '1994-10-21', '2017-11-01', '310104199410214568', '18531101710',
        'shilan@cslk-his.local', '19580074', '预防保健科', '106', '15', '疾病监测、免疫规划、慢病建档与公卫上报', '硕士',
        NULL, 0, '0.00', 1, 'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_employee (id, emp_code, emp_name, emp_type, gender, birth_date, hire_date, id_card, phone, email,
                          dept_id, dept_name, title, position, specialty, education, avatar, is_expert, expert_price,
                          status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('897001329', 'H17329', '李蓉', 6, 2, '1976-03-16', '1999-12-01', '610113197603161245', '13728974432',
        'lirong@cslk-his.local', '19580035', '医学检验科', '404', '16', '临床检验、微生物与分子诊断、室间质评', '硕士',
        NULL, 0, '0.00', 0, 'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_employee (id, emp_code, emp_name, emp_type, gender, birth_date, hire_date, id_card, phone, email,
                          dept_id, dept_name, title, position, specialty, education, avatar, is_expert, expert_price,
                          status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('897001330', 'H17330', '岑静', 6, 2, '1982-07-23', '2006-08-01', '610113198207234742', '18767721449',
        'cenjing@cslk-his.local', '19580035', '医学检验科', '304', '19', '临床检验、微生物与分子诊断、室间质评', '硕士',
        NULL, 0, '0.00', 1, 'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_employee (id, emp_code, emp_name, emp_type, gender, birth_date, hire_date, id_card, phone, email,
                          dept_id, dept_name, title, position, specialty, education, avatar, is_expert, expert_price,
                          status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('897001331', 'H17331', '薛思琪', 6, 2, '1993-02-03', '2018-01-01', '370102199302035763', '13854585365',
        'xuesiqi@cslk-his.local', '19580035', '医学检验科', '204', '36', '临床检验、微生物与分子诊断、室间质评', '本科',
        NULL, 0, '0.00', 1, 'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_employee (id, emp_code, emp_name, emp_type, gender, birth_date, hire_date, id_card, phone, email,
                          dept_id, dept_name, title, position, specialty, education, avatar, is_expert, expert_price,
                          status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('897001332', 'H17332', '董春燕', 6, 2, '1995-05-21', '2017-03-01', '370102199505215705', '15028616762',
        'dongchunyan@cslk-his.local', '19580035', '医学检验科', '108', '20', '临床检验、微生物与分子诊断、室间质评',
        '本科', NULL, 0, '0.00', 1, 'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_employee (id, emp_code, emp_name, emp_type, gender, birth_date, hire_date, id_card, phone, email,
                          dept_id, dept_name, title, position, specialty, education, avatar, is_expert, expert_price,
                          status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('897001333', 'H17333', '姚紫涵', 6, 2, '1997-05-08', '2021-06-01', '610113199705084606', '15233474822',
        'yaozihan@cslk-his.local', '19580035', '医学检验科', '107', '20', '临床检验、微生物与分子诊断、室间质评', '硕士',
        NULL, 0, '0.00', 1, 'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_employee (id, emp_code, emp_name, emp_type, gender, birth_date, hire_date, id_card, phone, email,
                          dept_id, dept_name, title, position, specialty, education, avatar, is_expert, expert_price,
                          status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('897001334', 'H17334', '李晨曦', 1, 1, '1971-04-03', '1995-04-01', '110105197104030410', '18646429827',
        'lichenxi@cslk-his.local', '19580036', '放射科', '401', '16', 'CT/MRI 诊断、介入放射、乳腺影像', '本科', NULL, 1,
        '30.00', 1, 'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_employee (id, emp_code, emp_name, emp_type, gender, birth_date, hire_date, id_card, phone, email,
                          dept_id, dept_name, title, position, specialty, education, avatar, is_expert, expert_price,
                          status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('897001335', 'H17335', '岑国强', 1, 1, '1987-08-24', '2012-10-01', '310104198708247819', '18824086039',
        'cenguoqiang@cslk-his.local', '19580036', '放射科', '301', '36', 'CT/MRI 诊断、介入放射、乳腺影像', '硕士', NULL,
        1, '30.00', 1, 'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_employee (id, emp_code, emp_name, emp_type, gender, birth_date, hire_date, id_card, phone, email,
                          dept_id, dept_name, title, position, specialty, education, avatar, is_expert, expert_price,
                          status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('897001336', 'H17336', '朱厚朴', 1, 1, '1994-09-25', '2018-02-01', '210102199409250274', '18773577902',
        'zhuhoupu@cslk-his.local', '19580036', '放射科', '201', '31', 'CT/MRI 诊断、介入放射、乳腺影像', '硕士', NULL, 0,
        '0.00', 1, 'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_employee (id, emp_code, emp_name, emp_type, gender, birth_date, hire_date, id_card, phone, email,
                          dept_id, dept_name, title, position, specialty, education, avatar, is_expert, expert_price,
                          status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('897001337', 'H17337', '王安雅', 6, 2, '1989-12-06', '2013-04-01', '32010619891206378X', '18063266793',
        'wanganya@cslk-his.local', '19580036', '放射科', '204', '36', 'CT/MRI 诊断、介入放射、乳腺影像', '硕士', NULL, 0,
        '0.00', 1, 'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_employee (id, emp_code, emp_name, emp_type, gender, birth_date, hire_date, id_card, phone, email,
                          dept_id, dept_name, title, position, specialty, education, avatar, is_expert, expert_price,
                          status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('897001338', 'H17338', '齐伟', 6, 1, '1993-11-20', '2017-07-01', '510107199311205312', '18111616342',
        'qiwei@cslk-his.local', '19580036', '放射科', '108', '20', 'CT/MRI 诊断、介入放射、乳腺影像', '硕士', NULL, 0,
        '0.00', 1, 'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_employee (id, emp_code, emp_name, emp_type, gender, birth_date, hire_date, id_card, phone, email,
                          dept_id, dept_name, title, position, specialty, education, avatar, is_expert, expert_price,
                          status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('897001339', 'H17339', '高佳', 1, 2, '1976-10-26', '1998-07-01', '420106197610261106', '18022184535',
        'gaojia@cslk-his.local', '19580037', '超声医学科', '401', '16', '腹部超声、心血管超声、介入超声', '硕士', NULL, 1,
        '30.00', 1, 'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_employee (id, emp_code, emp_name, emp_type, gender, birth_date, hire_date, id_card, phone, email,
                          dept_id, dept_name, title, position, specialty, education, avatar, is_expert, expert_price,
                          status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('897001340', 'H17340', '金妍', 1, 2, '1981-07-24', '2004-12-01', '37010219810724416X', '18731372452',
        'jinyan@cslk-his.local', '19580037', '超声医学科', '301', '36', '腹部超声、心血管超声、介入超声', '本科', NULL, 1,
        '30.00', 1, 'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_employee (id, emp_code, emp_name, emp_type, gender, birth_date, hire_date, id_card, phone, email,
                          dept_id, dept_name, title, position, specialty, education, avatar, is_expert, expert_price,
                          status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('897001341', 'H17341', '薛语嫣', 1, 2, '1995-06-08', '2017-03-01', '210102199506086365', '13593645602',
        'xueyuyan@cslk-his.local', '19580037', '超声医学科', '201', '31', '腹部超声、心血管超声、介入超声', '硕士', NULL,
        0, '0.00', 1, 'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_employee (id, emp_code, emp_name, emp_type, gender, birth_date, hire_date, id_card, phone, email,
                          dept_id, dept_name, title, position, specialty, education, avatar, is_expert, expert_price,
                          status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('897001342', 'H17342', '顾宜修', 6, 2, '1994-04-13', '2018-02-01', '420106199404132505', '13610398450',
        'guyixiu@cslk-his.local', '19580037', '超声医学科', '108', '20', '腹部超声、心血管超声、介入超声', '硕士', NULL,
        0, '0.00', 1, 'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_employee (id, emp_code, emp_name, emp_type, gender, birth_date, hire_date, id_card, phone, email,
                          dept_id, dept_name, title, position, specialty, education, avatar, is_expert, expert_price,
                          status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('897001343', 'H17343', '潘乐怡', 1, 2, '1977-09-11', '2000-11-01', '33010619770911338X', '15254845279',
        'panleyi@cslk-his.local', '19580038', '病理科', '401', '16', '组织病理、细胞学、免疫组化与分子病理', '本科', NULL,
        1, '30.00', 1, 'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_employee (id, emp_code, emp_name, emp_type, gender, birth_date, hire_date, id_card, phone, email,
                          dept_id, dept_name, title, position, specialty, education, avatar, is_expert, expert_price,
                          status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('897001344', 'H17344', '宋春燕', 1, 2, '1977-09-28', '2003-07-01', '330106197709281789', '17853849450',
        'songchunyan@cslk-his.local', '19580038', '病理科', '301', '36', '组织病理、细胞学、免疫组化与分子病理', '硕士',
        NULL, 0, '0.00', 1, 'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_employee (id, emp_code, emp_name, emp_type, gender, birth_date, hire_date, id_card, phone, email,
                          dept_id, dept_name, title, position, specialty, education, avatar, is_expert, expert_price,
                          status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('897001345', 'H17345', '马海艳', 6, 2, '1987-11-23', '2010-03-01', '210102198711237025', '19949867410',
        'mahaiyan@cslk-his.local', '19580038', '病理科', '204', '20', '组织病理、细胞学、免疫组化与分子病理', '本科',
        NULL, 0, '0.00', 1, 'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_employee (id, emp_code, emp_name, emp_type, gender, birth_date, hire_date, id_card, phone, email,
                          dept_id, dept_name, title, position, specialty, education, avatar, is_expert, expert_price,
                          status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('897001346', 'H17346', '袁国强', 6, 1, '1994-03-21', '2016-01-01', '510107199403214419', '13719238066',
        'yuanguoqiang@cslk-his.local', '19580038', '病理科', '108', '20', '组织病理、细胞学、免疫组化与分子病理', '本科',
        NULL, 0, '0.00', 1, 'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_employee (id, emp_code, emp_name, emp_type, gender, birth_date, hire_date, id_card, phone, email,
                          dept_id, dept_name, title, position, specialty, education, avatar, is_expert, expert_price,
                          status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('897001347', 'H17347', '袁和玉', 1, 2, '1973-03-05', '1998-08-01', '420106197303057586', '13727751784',
        'yuanheyu@cslk-his.local', '19580039', '麻醉科', '401', '16', '全身麻醉、区域阻滞、危重症麻醉与镇痛', '硕士',
        NULL, 0, '0.00', 1, 'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_employee (id, emp_code, emp_name, emp_type, gender, birth_date, hire_date, id_card, phone, email,
                          dept_id, dept_name, title, position, specialty, education, avatar, is_expert, expert_price,
                          status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('897001348', 'H17348', '刘思琪', 1, 2, '1979-10-24', '2003-02-01', '320106197910249703', '15081967980',
        'liusiqi@cslk-his.local', '19580039', '麻醉科', '301', '36', '全身麻醉、区域阻滞、危重症麻醉与镇痛', '本科', NULL,
        0, '0.00', 1, 'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_employee (id, emp_code, emp_name, emp_type, gender, birth_date, hire_date, id_card, phone, email,
                          dept_id, dept_name, title, position, specialty, education, avatar, is_expert, expert_price,
                          status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('897001349', 'H17349', '江娜', 1, 2, '1985-09-16', '2011-08-01', '330106198509165323', '15023289146',
        'jiangsna@cslk-his.local', '19580039', '麻醉科', '201', '31', '全身麻醉、区域阻滞、危重症麻醉与镇痛', '硕士',
        NULL, 0, '0.00', 1, 'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_employee (id, emp_code, emp_name, emp_type, gender, birth_date, hire_date, id_card, phone, email,
                          dept_id, dept_name, title, position, specialty, education, avatar, is_expert, expert_price,
                          status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('897001350', 'H17350', '陈亦航', 1, 1, '1998-03-18', '2023-02-01', '420106199803180256', '18026557984',
        'chenyihang@cslk-his.local', '19580039', '麻醉科', '102', '31', '全身麻醉、区域阻滞、危重症麻醉与镇痛', '本科',
        NULL, 0, '0.00', 1, 'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_employee (id, emp_code, emp_name, emp_type, gender, birth_date, hire_date, id_card, phone, email,
                          dept_id, dept_name, title, position, specialty, education, avatar, is_expert, expert_price,
                          status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('897001351', 'H17351', '温俊杰', 2, 1, '1993-04-28', '2018-01-01', '320106199304283417', '15928401927',
        'wenjunjie@cslk-his.local', '19580039', '麻醉科', '203', '15', '全身麻醉、区域阻滞、危重症麻醉与镇痛', '硕士',
        NULL, 0, '0.00', 1, 'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_employee (id, emp_code, emp_name, emp_type, gender, birth_date, hire_date, id_card, phone, email,
                          dept_id, dept_name, title, position, specialty, education, avatar, is_expert, expert_price,
                          status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('897001352', 'H17352', '向玲', 6, 2, '1978-10-14', '2000-05-01', '310104197810146809', '15165061008',
        'xiangling@cslk-his.local', '19580040', '输血科', '404', '16', '血型血清学、成分输血、输血不良反应处置', '本科',
        NULL, 0, '0.00', 1, 'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_employee (id, emp_code, emp_name, emp_type, gender, birth_date, hire_date, id_card, phone, email,
                          dept_id, dept_name, title, position, specialty, education, avatar, is_expert, expert_price,
                          status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('897001353', 'H17353', '何璐', 6, 2, '1983-05-21', '2008-01-01', '370102198305217920', '15188462064',
        'helu@cslk-his.local', '19580040', '输血科', '204', '36', '血型血清学、成分输血、输血不良反应处置', '本科', NULL,
        0, '0.00', 1, 'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_employee (id, emp_code, emp_name, emp_type, gender, birth_date, hire_date, id_card, phone, email,
                          dept_id, dept_name, title, position, specialty, education, avatar, is_expert, expert_price,
                          status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('897001354', 'H17354', '袁庆', 6, 1, '1998-06-03', '2020-12-01', '370102199806035935', '18236389589',
        'yuanqing@cslk-his.local', '19580040', '输血科', '108', '20', '血型血清学、成分输血、输血不良反应处置', '本科',
        NULL, 0, '0.00', 1, 'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_employee (id, emp_code, emp_name, emp_type, gender, birth_date, hire_date, id_card, phone, email,
                          dept_id, dept_name, title, position, specialty, education, avatar, is_expert, expert_price,
                          status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('897001355', 'H17355', '康慧', 6, 2, '1998-09-21', '2021-12-01', '510107199809218348', '19884761136',
        'kanghui@cslk-his.local', '19580040', '输血科', '107', '20', '血型血清学、成分输血、输血不良反应处置', '本科',
        NULL, 0, '0.00', 1, 'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_employee (id, emp_code, emp_name, emp_type, gender, birth_date, hire_date, id_card, phone, email,
                          dept_id, dept_name, title, position, specialty, education, avatar, is_expert, expert_price,
                          status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('897001356', 'H17356', '徐远航', 1, 1, '1975-10-20', '2001-03-01', '110105197510209037', '19824712302',
        'xuyuanhang@cslk-his.local', '19580041', '康复医学科', '301', '16', '神经康复、骨关节康复、心肺康复', '硕士',
        NULL, 1, '30.00', 1, 'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_employee (id, emp_code, emp_name, emp_type, gender, birth_date, hire_date, id_card, phone, email,
                          dept_id, dept_name, title, position, specialty, education, avatar, is_expert, expert_price,
                          status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('897001357', 'H17357', '钱宜修', 1, 2, '1986-05-14', '2011-08-01', '32010619860514036X', '13562578186',
        'qianyixiu@cslk-his.local', '19580041', '康复医学科', '201', '31', '神经康复、骨关节康复、心肺康复', '硕士', NULL,
        0, '0.00', 1, 'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_employee (id, emp_code, emp_name, emp_type, gender, birth_date, hire_date, id_card, phone, email,
                          dept_id, dept_name, title, position, specialty, education, avatar, is_expert, expert_price,
                          status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('897001358', 'H17358', '袁岚', 6, 2, '1993-07-01', '2017-08-01', '370102199307011525', '15979133036',
        'yuanlan@cslk-his.local', '19580041', '康复医学科', '204', '37', '神经康复、骨关节康复、心肺康复', '硕士', NULL,
        0, '0.00', 1, 'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_employee (id, emp_code, emp_name, emp_type, gender, birth_date, hire_date, id_card, phone, email,
                          dept_id, dept_name, title, position, specialty, education, avatar, is_expert, expert_price,
                          status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('897001359', 'H17359', '杨天', 6, 1, '1993-01-06', '2017-08-01', '330106199301060519', '13716697457',
        'yangtian@cslk-his.local', '19580041', '康复医学科', '108', '37', '神经康复、骨关节康复、心肺康复', '本科', NULL,
        0, '0.00', 1, 'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_employee (id, emp_code, emp_name, emp_type, gender, birth_date, hire_date, id_card, phone, email,
                          dept_id, dept_name, title, position, specialty, education, avatar, is_expert, expert_price,
                          status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('897001360', 'H17360', '宋琪', 2, 2, '1997-10-08', '2019-02-01', '610113199710083325', '17629622139',
        'songqi@cslk-his.local', '19580041', '康复医学科', '106', '15', '神经康复、骨关节康复、心肺康复', '本科', NULL, 0,
        '0.00', 1, 'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_employee (id, emp_code, emp_name, emp_type, gender, birth_date, hire_date, id_card, phone, email,
                          dept_id, dept_name, title, position, specialty, education, avatar, is_expert, expert_price,
                          status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('897001361', 'H17361', '施庆', 1, 1, '1983-11-04', '2006-11-01', '310104198311046816', '18937815462',
        'shiqiqing@cslk-his.local', '19580092', '核医学科', '301', '16', 'SPECT/PET 显像、甲状腺核素治疗、放射防护',
        '本科', NULL, 1, '30.00', 1, 'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_employee (id, emp_code, emp_name, emp_type, gender, birth_date, hire_date, id_card, phone, email,
                          dept_id, dept_name, title, position, specialty, education, avatar, is_expert, expert_price,
                          status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('897001362', 'H17362', '尹寒梅', 1, 2, '1994-03-25', '2016-05-01', '420106199403258640', '19946112682',
        'yinhanmei@cslk-his.local', '19580092', '核医学科', '201', '31', 'SPECT/PET 显像、甲状腺核素治疗、放射防护',
        '硕士', NULL, 0, '0.00', 1, 'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_employee (id, emp_code, emp_name, emp_type, gender, birth_date, hire_date, id_card, phone, email,
                          dept_id, dept_name, title, position, specialty, education, avatar, is_expert, expert_price,
                          status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('897001363', 'H17363', '裘天', 6, 1, '1991-02-04', '2015-01-01', '320106199102040919', '13546261789',
        'qiuqiutian@cslk-his.local', '19580092', '核医学科', '204', '20', 'SPECT/PET 显像、甲状腺核素治疗、放射防护',
        '本科', NULL, 0, '0.00', 1, 'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_employee (id, emp_code, emp_name, emp_type, gender, birth_date, hire_date, id_card, phone, email,
                          dept_id, dept_name, title, position, specialty, education, avatar, is_expert, expert_price,
                          status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('897001364', 'H17364', '陈明珠', 6, 2, '1992-12-03', '2017-09-01', '110105199212034063', '15945796042',
        'chenmingzhu@cslk-his.local', '19580092', '核医学科', '108', '20', 'SPECT/PET 显像、甲状腺核素治疗、放射防护',
        '本科', NULL, 0, '0.00', 1, 'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_employee (id, emp_code, emp_name, emp_type, gender, birth_date, hire_date, id_card, phone, email,
                          dept_id, dept_name, title, position, specialty, education, avatar, is_expert, expert_price,
                          status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('897001365', 'H17365', '雷白露', 6, 2, '1973-04-19', '1998-02-01', '320106197304194909', '18151526432',
        'leiwangbailu@cslk-his.local', '19580093', '营养科', '510', '16', '临床营养评估、肠内肠外营养、膳食管理', '硕士',
        NULL, 0, '0.00', 1, 'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_employee (id, emp_code, emp_name, emp_type, gender, birth_date, hire_date, id_card, phone, email,
                          dept_id, dept_name, title, position, specialty, education, avatar, is_expert, expert_price,
                          status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('897001366', 'H17366', '向思琪', 6, 2, '1985-04-06', '2008-12-01', '420106198504065683', '18945510462',
        'xiangsiqi@cslk-his.local', '19580093', '营养科', '509', '36', '临床营养评估、肠内肠外营养、膳食管理', '硕士',
        NULL, 0, '0.00', 1, 'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_employee (id, emp_code, emp_name, emp_type, gender, birth_date, hire_date, id_card, phone, email,
                          dept_id, dept_name, title, position, specialty, education, avatar, is_expert, expert_price,
                          status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('897001367', 'H17367', '曾光', 6, 1, '1998-03-24', '2023-08-01', '370102199803248230', '18080623185',
        'zengguang@cslk-his.local', '19580093', '营养科', '508', '34', '临床营养评估、肠内肠外营养、膳食管理', '硕士',
        NULL, 0, '0.00', 1, 'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_employee (id, emp_code, emp_name, emp_type, gender, birth_date, hire_date, id_card, phone, email,
                          dept_id, dept_name, title, position, specialty, education, avatar, is_expert, expert_price,
                          status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('897001368', 'H17368', '夏蕾', 6, 2, '1998-02-07', '2022-03-01', '320106199802076904', '18729056537',
        'xialei@cslk-his.local', '19580093', '营养科', '508', '34', '临床营养评估、肠内肠外营养、膳食管理', '本科', NULL,
        0, '0.00', 1, 'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_employee (id, emp_code, emp_name, emp_type, gender, birth_date, hire_date, id_card, phone, email,
                          dept_id, dept_name, title, position, specialty, education, avatar, is_expert, expert_price,
                          status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('897001369', 'H17369', '叶川', 6, 1, '1979-04-15', '2001-02-01', '370102197904155915', '19994440038',
        'yechuan@cslk-his.local', '19580094', '消毒供应中心', '303', '9', '复用器械清洗消毒灭菌、追溯与包模板管理',
        '硕士', NULL, 0, '0.00', 0, 'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_employee (id, emp_code, emp_name, emp_type, gender, birth_date, hire_date, id_card, phone, email,
                          dept_id, dept_name, title, position, specialty, education, avatar, is_expert, expert_price,
                          status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('897001370', 'H17370', '蔡寒梅', 6, 2, '1985-05-03', '2009-06-01', '320106198505032281', '18558032526',
        'caihanmei@cslk-his.local', '19580094', '消毒供应中心', '203', '38', '复用器械清洗消毒灭菌、追溯与包模板管理',
        '本科', NULL, 0, '0.00', 1, 'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_employee (id, emp_code, emp_name, emp_type, gender, birth_date, hire_date, id_card, phone, email,
                          dept_id, dept_name, title, position, specialty, education, avatar, is_expert, expert_price,
                          status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('897001371', 'H17371', '周妍', 6, 2, '1994-03-23', '2019-10-01', '310104199403232688', '17622704122',
        'zhouyan@cslk-his.local', '19580094', '消毒供应中心', '105', '38', '复用器械清洗消毒灭菌、追溯与包模板管理',
        '本科', NULL, 0, '0.00', 1, 'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_employee (id, emp_code, emp_name, emp_type, gender, birth_date, hire_date, id_card, phone, email,
                          dept_id, dept_name, title, position, specialty, education, avatar, is_expert, expert_price,
                          status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('897001372', 'H17372', '廖纲', 6, 1, '2002-11-14', '2025-10-01', '510107200211143957', '18916895240',
        'liaogang@cslk-his.local', '19580094', '消毒供应中心', '107', '38', '复用器械清洗消毒灭菌、追溯与包模板管理',
        '本科', NULL, 0, '0.00', 1, 'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_employee (id, emp_code, emp_name, emp_type, gender, birth_date, hire_date, id_card, phone, email,
                          dept_id, dept_name, title, position, specialty, education, avatar, is_expert, expert_price,
                          status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('897001373', 'H17373', '肖延平', 1, 1, '1973-01-21', '1997-11-01', '320106197301218856', '13881214111',
        'xiaoyanping@cslk-his.local', '19580061', '放射治疗科', '401', '16', '调强放疗、精确计划与影像引导', '硕士',
        NULL, 1, '50.00', 1, 'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_employee (id, emp_code, emp_name, emp_type, gender, birth_date, hire_date, id_card, phone, email,
                          dept_id, dept_name, title, position, specialty, education, avatar, is_expert, expert_price,
                          status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('897001374', 'H17374', '江跃龙', 1, 1, '1983-01-28', '2006-07-01', '210102198301281197', '18792731063',
        'jiangsyuelong@cslk-his.local', '19580061', '放射治疗科', '301', '31', '调强放疗、精确计划与影像引导', '硕士',
        NULL, 1, '30.00', 1, 'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_employee (id, emp_code, emp_name, emp_type, gender, birth_date, hire_date, id_card, phone, email,
                          dept_id, dept_name, title, position, specialty, education, avatar, is_expert, expert_price,
                          status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('897001375', 'H17375', '廖寒梅', 6, 2, '1990-03-07', '2012-05-01', '610113199003072803', '15288006309',
        'liaohanmei@cslk-his.local', '19580061', '放射治疗科', '204', '20', '调强放疗、精确计划与影像引导', '本科', NULL,
        0, '0.00', 1, 'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_employee (id, emp_code, emp_name, emp_type, gender, birth_date, hire_date, id_card, phone, email,
                          dept_id, dept_name, title, position, specialty, education, avatar, is_expert, expert_price,
                          status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('897001376', 'H17376', '田远航', 6, 1, '1994-08-01', '2016-04-01', '370102199408017539', '19916892397',
        'tianyuanhang@cslk-his.local', '19580061', '放射治疗科', '108', '20', '调强放疗、精确计划与影像引导', '硕士',
        NULL, 0, '0.00', 1, 'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_employee (id, emp_code, emp_name, emp_type, gender, birth_date, hire_date, id_card, phone, email,
                          dept_id, dept_name, title, position, specialty, education, avatar, is_expert, expert_price,
                          status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('897001377', 'H17377', '冯嘉怡', 2, 2, '1999-04-24', '2024-12-01', '610113199904248685', '18765654335',
        'fengjiayi@cslk-his.local', '19580061', '放射治疗科', '106', '15', '调强放疗、精确计划与影像引导', '本科', NULL,
        0, '0.00', 1, 'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_employee (id, emp_code, emp_name, emp_type, gender, birth_date, hire_date, id_card, phone, email,
                          dept_id, dept_name, title, position, specialty, education, avatar, is_expert, expert_price,
                          status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('897001378', 'H17378', '杨如初', 4, 2, '1973-11-28', '1998-11-01', '320106197311283547', '15963856182',
        'yangruchu@cslk-his.local', '19580043', '门诊西药房', '402', '16', '处方调剂、用药咨询、抗菌药物处方点评', '本科',
        NULL, 1, '30.00', 1, 'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_employee (id, emp_code, emp_name, emp_type, gender, birth_date, hire_date, id_card, phone, email,
                          dept_id, dept_name, title, position, specialty, education, avatar, is_expert, expert_price,
                          status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('897001379', 'H17379', '向秀英', 4, 2, '1981-01-12', '2004-04-01', '210102198101128809', '15168467221',
        'xiangxiuying@cslk-his.local', '19580043', '门诊西药房', '202', '20', '处方调剂、用药咨询、抗菌药物处方点评',
        '硕士', NULL, 0, '0.00', 1, 'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_employee (id, emp_code, emp_name, emp_type, gender, birth_date, hire_date, id_card, phone, email,
                          dept_id, dept_name, title, position, specialty, education, avatar, is_expert, expert_price,
                          status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('897001380', 'H17380', '蒋致远', 4, 1, '1996-06-27', '2018-01-01', '370102199606272872', '13898071258',
        'jiangzhiyuan2@cslk-his.local', '19580043', '门诊西药房', '104', '20', '处方调剂、用药咨询、抗菌药物处方点评',
        '硕士', NULL, 0, '0.00', 1, 'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_employee (id, emp_code, emp_name, emp_type, gender, birth_date, hire_date, id_card, phone, email,
                          dept_id, dept_name, title, position, specialty, education, avatar, is_expert, expert_price,
                          status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('897001381', 'H17381', '薛萱', 4, 2, '1986-12-01', '2008-10-01', '310104198612019889', '18793322114',
        'xuexuan@cslk-his.local', '19580043', '门诊西药房', '202', '20', '处方调剂、用药咨询、抗菌药物处方点评', '硕士',
        NULL, 0, '0.00', 1, 'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_employee (id, emp_code, emp_name, emp_type, gender, birth_date, hire_date, id_card, phone, email,
                          dept_id, dept_name, title, position, specialty, education, avatar, is_expert, expert_price,
                          status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('897001382', 'H17382', '肖薇', 4, 2, '1977-04-25', '2001-03-01', '420106197704257706', '18514396451',
        'xiaoweiwei@cslk-his.local', '19580045', '住院药房', '402', '16', '摆药与单剂量调剂、麻精药品管理、退药', '硕士',
        NULL, 1, '30.00', 1, 'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_employee (id, emp_code, emp_name, emp_type, gender, birth_date, hire_date, id_card, phone, email,
                          dept_id, dept_name, title, position, specialty, education, avatar, is_expert, expert_price,
                          status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('897001383', 'H17383', '董心怡', 4, 2, '1989-03-01', '2012-03-01', '420106198903015026', '13673663789',
        'dongxinyi@cslk-his.local', '19580045', '住院药房', '202', '20', '摆药与单剂量调剂、麻精药品管理、退药', '本科',
        NULL, 0, '0.00', 1, 'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_employee (id, emp_code, emp_name, emp_type, gender, birth_date, hire_date, id_card, phone, email,
                          dept_id, dept_name, title, position, specialty, education, avatar, is_expert, expert_price,
                          status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('897001384', 'H17384', '周立诚', 4, 1, '1997-05-23', '2022-09-01', '370102199705231152', '15895567284',
        'zhoulicheng@cslk-his.local', '19580045', '住院药房', '104', '20', '摆药与单剂量调剂、麻精药品管理、退药', '本科',
        NULL, 0, '0.00', 1, 'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_employee (id, emp_code, emp_name, emp_type, gender, birth_date, hire_date, id_card, phone, email,
                          dept_id, dept_name, title, position, specialty, education, avatar, is_expert, expert_price,
                          status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('897001385', 'H17385', '严映竹', 4, 2, '1987-12-03', '2010-07-01', '610113198712034426', '13796568781',
        'yanyingzhu@cslk-his.local', '19580045', '住院药房', '202', '20', '摆药与单剂量调剂、麻精药品管理、退药', '硕士',
        NULL, 0, '0.00', 1, 'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_employee (id, emp_code, emp_name, emp_type, gender, birth_date, hire_date, id_card, phone, email,
                          dept_id, dept_name, title, position, specialty, education, avatar, is_expert, expert_price,
                          status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('897001386', 'H17386', '邓厚朴', 4, 1, '1979-10-22', '2004-04-01', '370102197910224593', '19812635656',
        'denghoupu@cslk-his.local', '19580044', '门诊中药房', '402', '16', '中药饮片调剂、处方点评、煎药管理', '本科',
        NULL, 1, '30.00', 1, 'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_employee (id, emp_code, emp_name, emp_type, gender, birth_date, hire_date, id_card, phone, email,
                          dept_id, dept_name, title, position, specialty, education, avatar, is_expert, expert_price,
                          status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('897001387', 'H17387', '金立诚', 4, 1, '1986-02-20', '2010-03-01', '320106198602201972', '18763116214',
        'jinlicheng@cslk-his.local', '19580044', '门诊中药房', '202', '36', '中药饮片调剂、处方点评、煎药管理', '硕士',
        NULL, 0, '0.00', 1, 'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_employee (id, emp_code, emp_name, emp_type, gender, birth_date, hire_date, id_card, phone, email,
                          dept_id, dept_name, title, position, specialty, education, avatar, is_expert, expert_price,
                          status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('897001388', 'H17388', '邓庆', 4, 1, '1995-11-15', '2019-05-01', '440305199511158856', '15924722580',
        'dengqing@cslk-his.local', '19580044', '门诊中药房', '104', '20', '中药饮片调剂、处方点评、煎药管理', '硕士',
        NULL, 0, '0.00', 1, 'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_employee (id, emp_code, emp_name, emp_type, gender, birth_date, hire_date, id_card, phone, email,
                          dept_id, dept_name, title, position, specialty, education, avatar, is_expert, expert_price,
                          status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('897001389', 'H17389', '田成', 4, 1, '2002-07-05', '2025-12-01', '610113200207054637', '18520769315',
        'tianchengda@cslk-his.local', '19580044', '门诊中药房', '103', '20', '中药饮片调剂、处方点评、煎药管理', '本科',
        NULL, 0, '0.00', 1, 'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_employee (id, emp_code, emp_name, emp_type, gender, birth_date, hire_date, id_card, phone, email,
                          dept_id, dept_name, title, position, specialty, education, avatar, is_expert, expert_price,
                          status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('897001390', 'H17390', '朱语嫣', 4, 2, '1980-10-26', '2002-07-01', '370102198010262089', '19918619031',
        'zhuyuyan@cslk-his.local', '19580046', '静脉配液中心', '302', '16', '肠外营养与细胞毒药物配置、处方前置审核',
        '硕士', NULL, 0, '0.00', 1, 'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_employee (id, emp_code, emp_name, emp_type, gender, birth_date, hire_date, id_card, phone, email,
                          dept_id, dept_name, title, position, specialty, education, avatar, is_expert, expert_price,
                          status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('897001391', 'H17391', '江倩', 4, 2, '1989-04-05', '2011-02-01', '370102198904055521', '13765274504',
        'jiangsqian@cslk-his.local', '19580046', '静脉配液中心', '202', '20', '肠外营养与细胞毒药物配置、处方前置审核',
        '硕士', NULL, 0, '0.00', 1, 'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_employee (id, emp_code, emp_name, emp_type, gender, birth_date, hire_date, id_card, phone, email,
                          dept_id, dept_name, title, position, specialty, education, avatar, is_expert, expert_price,
                          status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('897001392', 'H17392', '莫丽', 4, 2, '1992-04-04', '2016-07-01', '320106199204046342', '15842298924',
        'moli@cslk-his.local', '19580046', '静脉配液中心', '104', '20', '肠外营养与细胞毒药物配置、处方前置审核', '本科',
        NULL, 0, '0.00', 1, 'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_employee (id, emp_code, emp_name, emp_type, gender, birth_date, hire_date, id_card, phone, email,
                          dept_id, dept_name, title, position, specialty, education, avatar, is_expert, expert_price,
                          status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('897001393', 'H17393', '程子轩', 6, 1, '1998-06-21', '2021-11-01', '51010719980621013X', '15012866294',
        'chengzixuan@cslk-his.local', '19580046', '静脉配液中心', '107', '20', '肠外营养与细胞毒药物配置、处方前置审核',
        '硕士', NULL, 0, '0.00', 1, 'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_employee (id, emp_code, emp_name, emp_type, gender, birth_date, hire_date, id_card, phone, email,
                          dept_id, dept_name, title, position, specialty, education, avatar, is_expert, expert_price,
                          status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('897001394', 'H17394', '石若彤', 1, 2, '1976-01-06', '2000-10-01', '42010619760106838X', '13716800188',
        'shiruotong@cslk-his.local', '19580048', '急诊内科', '301', '2', '急诊内科危重症、中毒、胸痛卒中绿色通道', '本科',
        NULL, 1, '50.00', 1, 'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_employee (id, emp_code, emp_name, emp_type, gender, birth_date, hire_date, id_card, phone, email,
                          dept_id, dept_name, title, position, specialty, education, avatar, is_expert, expert_price,
                          status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('897001395', 'H17395', '董寒梅', 1, 2, '1987-08-01', '2010-02-01', '370102198708015709', '15262085929',
        'donghanmei@cslk-his.local', '19580048', '急诊内科', '201', '31', '急诊内科危重症、中毒、胸痛卒中绿色通道',
        '本科', NULL, 0, '0.00', 1, 'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_employee (id, emp_code, emp_name, emp_type, gender, birth_date, hire_date, id_card, phone, email,
                          dept_id, dept_name, title, position, specialty, education, avatar, is_expert, expert_price,
                          status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('897001396', 'H17396', '蒋月白', 1, 2, '1995-11-01', '2018-10-01', '310104199511018988', '18894450885',
        'jiangyuebai@cslk-his.local', '19580048', '急诊内科', '102', '31', '急诊内科危重症、中毒、胸痛卒中绿色通道',
        '本科', NULL, 0, '0.00', 1, 'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_employee (id, emp_code, emp_name, emp_type, gender, birth_date, hire_date, id_card, phone, email,
                          dept_id, dept_name, title, position, specialty, education, avatar, is_expert, expert_price,
                          status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('897001397', 'H17397', '郑婉', 2, 2, '1988-02-10', '2013-08-01', '370102198802103764', '15955176467',
        'zhengwan@cslk-his.local', '19580048', '急诊内科', '203', '9', '急诊内科危重症、中毒、胸痛卒中绿色通道', '本科',
        NULL, 0, '0.00', 1, 'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_employee (id, emp_code, emp_name, emp_type, gender, birth_date, hire_date, id_card, phone, email,
                          dept_id, dept_name, title, position, specialty, education, avatar, is_expert, expert_price,
                          status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('897001398', 'H17398', '张月白', 2, 2, '1999-01-14', '2023-03-01', '320106199901144642', '18589213829',
        'zhangyuebai@cslk-his.local', '19580048', '急诊内科', '105', '15', '急诊内科危重症、中毒、胸痛卒中绿色通道',
        '本科', NULL, 0, '0.00', 1, 'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_employee (id, emp_code, emp_name, emp_type, gender, birth_date, hire_date, id_card, phone, email,
                          dept_id, dept_name, title, position, specialty, education, avatar, is_expert, expert_price,
                          status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('897001399', 'H17399', '顾涛', 1, 1, '1979-05-10', '2003-01-01', '44030519790510531X', '17632676285',
        'gutao@cslk-his.local', '19580049', '急诊外科', '301', '2', '创伤急救、急腹症、多发伤救治', '硕士', NULL, 1,
        '50.00', 1, 'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_employee (id, emp_code, emp_name, emp_type, gender, birth_date, hire_date, id_card, phone, email,
                          dept_id, dept_name, title, position, specialty, education, avatar, is_expert, expert_price,
                          status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('897001400', 'H17400', '向维', 1, 1, '1990-05-28', '2013-02-01', '110105199005287615', '19852973505',
        'xiangweiqi@cslk-his.local', '19580049', '急诊外科', '201', '31', '创伤急救、急腹症、多发伤救治', '硕士', NULL, 0,
        '0.00', 1, 'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_employee (id, emp_code, emp_name, emp_type, gender, birth_date, hire_date, id_card, phone, email,
                          dept_id, dept_name, title, position, specialty, education, avatar, is_expert, expert_price,
                          status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('897001401', 'H17401', '程若彤', 1, 2, '1991-07-23', '2018-02-01', '510107199107239523', '15937006218',
        'chengruotong@cslk-his.local', '19580049', '急诊外科', '102', '31', '创伤急救、急腹症、多发伤救治', '本科', NULL,
        0, '0.00', 1, 'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_employee (id, emp_code, emp_name, emp_type, gender, birth_date, hire_date, id_card, phone, email,
                          dept_id, dept_name, title, position, specialty, education, avatar, is_expert, expert_price,
                          status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('897001402', 'H17402', '杨雪', 2, 2, '1992-12-13', '2017-10-01', '440305199212133085', '13845351968',
        'yangxueli@cslk-his.local', '19580049', '急诊外科', '106', '15', '创伤急救、急腹症、多发伤救治', '本科', NULL, 0,
        '0.00', 1, 'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_employee (id, emp_code, emp_name, emp_type, gender, birth_date, hire_date, id_card, phone, email,
                          dept_id, dept_name, title, position, specialty, education, avatar, is_expert, expert_price,
                          status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('897001403', 'H17403', '董琪', 2, 2, '1995-09-24', '2018-06-01', '440305199509242207', '15129894030',
        'dongqi@cslk-his.local', '19580049', '急诊外科', '105', '15', '创伤急救、急腹症、多发伤救治', '本科', NULL, 0,
        '0.00', 1, 'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_employee (id, emp_code, emp_name, emp_type, gender, birth_date, hire_date, id_card, phone, email,
                          dept_id, dept_name, title, position, specialty, education, avatar, is_expert, expert_price,
                          status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('897001404', 'H17404', '何立诚', 1, 1, '1975-07-18', '2001-07-01', '420106197507186232', '17649070773',
        'helicheng@cslk-his.local', '19580051', '急诊儿科', '301', '1', '儿科急危重症、高热惊厥、气道急症', '本科', NULL,
        1, '50.00', 1, 'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_employee (id, emp_code, emp_name, emp_type, gender, birth_date, hire_date, id_card, phone, email,
                          dept_id, dept_name, title, position, specialty, education, avatar, is_expert, expert_price,
                          status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('897001405', 'H17405', '施敏', 1, 2, '1992-07-10', '2014-12-01', '110105199207109825', '15075387105',
        'shiqimin@cslk-his.local', '19580051', '急诊儿科', '201', '31', '儿科急危重症、高热惊厥、气道急症', '硕士', NULL,
        0, '0.00', 1, 'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_employee (id, emp_code, emp_name, emp_type, gender, birth_date, hire_date, id_card, phone, email,
                          dept_id, dept_name, title, position, specialty, education, avatar, is_expert, expert_price,
                          status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('897001406', 'H17406', '卢寒梅', 2, 2, '1990-02-10', '2013-05-01', '320106199002104946', '18111688729',
        'luhanmei@cslk-his.local', '19580051', '急诊儿科', '106', '15', '儿科急危重症、高热惊厥、气道急症', '本科', NULL,
        0, '0.00', 1, 'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_employee (id, emp_code, emp_name, emp_type, gender, birth_date, hire_date, id_card, phone, email,
                          dept_id, dept_name, title, position, specialty, education, avatar, is_expert, expert_price,
                          status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('897001407', 'H17407', '尚慧', 2, 2, '1995-02-09', '2019-03-01', '370102199502096800', '17876147930',
        'shanghui@cslk-his.local', '19580051', '急诊儿科', '105', '15', '儿科急危重症、高热惊厥、气道急症', '硕士', NULL,
        0, '0.00', 1, 'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_employee (id, emp_code, emp_name, emp_type, gender, birth_date, hire_date, id_card, phone, email,
                          dept_id, dept_name, title, position, specialty, education, avatar, is_expert, expert_price,
                          status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('897001408', 'H17408', '石娜', 1, 2, '1970-10-15', '1994-10-01', '310104197010153445', '18971840860',
        'shina@cslk-his.local', '19580050', '重症医学科(ICU)', '401', '1', '多器官功能支持、机械通气、CRRT', '博士', NULL,
        1, '100.00', 1, 'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_employee (id, emp_code, emp_name, emp_type, gender, birth_date, hire_date, id_card, phone, email,
                          dept_id, dept_name, title, position, specialty, education, avatar, is_expert, expert_price,
                          status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('897001409', 'H17409', '万月白', 1, 2, '1977-06-19', '2002-03-01', '330106197706196001', '15131839693',
        'wanyuebai@cslk-his.local', '19580050', '重症医学科(ICU)', '301', '2', '多器官功能支持、机械通气、CRRT', '本科',
        NULL, 1, '50.00', 0, 'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_employee (id, emp_code, emp_name, emp_type, gender, birth_date, hire_date, id_card, phone, email,
                          dept_id, dept_name, title, position, specialty, education, avatar, is_expert, expert_price,
                          status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('897001410', 'H17410', '侯山', 1, 1, '1985-02-28', '2012-01-01', '420106198502283978', '18228186192',
        'houshan@cslk-his.local', '19580050', '重症医学科(ICU)', '201', '31', '多器官功能支持、机械通气、CRRT', '硕士',
        NULL, 0, '0.00', 1, 'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_employee (id, emp_code, emp_name, emp_type, gender, birth_date, hire_date, id_card, phone, email,
                          dept_id, dept_name, title, position, specialty, education, avatar, is_expert, expert_price,
                          status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('897001411', 'H17411', '李如初', 2, 2, '1980-03-03', '2004-03-01', '320106198003030147', '13752606238',
        'liruchu@cslk-his.local', '19580050', '重症医学科(ICU)', '303', '9', '多器官功能支持、机械通气、CRRT', '硕士',
        NULL, 0, '0.00', 1, 'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_employee (id, emp_code, emp_name, emp_type, gender, birth_date, hire_date, id_card, phone, email,
                          dept_id, dept_name, title, position, specialty, education, avatar, is_expert, expert_price,
                          status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('897001412', 'H17412', '赵晓峰', 2, 2, '1998-03-05', '2022-04-01', '110105199803053565', '19961431314',
        'zhaoxiaofeng@cslk-his.local', '19580050', '重症医学科(ICU)', '106', '15', '多器官功能支持、机械通气、CRRT',
        '本科', NULL, 0, '0.00', 1, 'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_employee (id, emp_code, emp_name, emp_type, gender, birth_date, hire_date, id_card, phone, email,
                          dept_id, dept_name, title, position, specialty, education, avatar, is_expert, expert_price,
                          status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('897001413', 'H17413', '洪薇', 2, 2, '1995-08-10', '2019-09-01', '32010619950810680X', '13696412926',
        'hongweiwei@cslk-his.local', '19580050', '重症医学科(ICU)', '105', '15', '多器官功能支持、机械通气、CRRT', '本科',
        NULL, 0, '0.00', 1, 'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_employee (id, emp_code, emp_name, emp_type, gender, birth_date, hire_date, id_card, phone, email,
                          dept_id, dept_name, title, position, specialty, education, avatar, is_expert, expert_price,
                          status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('897001414', 'H17414', '苏倩', 1, 2, '1979-06-05', '2003-01-01', '510107197906059220', '17686165380',
        'suqian@cslk-his.local', '19580068', '发热门诊', '301', '1', '发热待查、呼吸道传染病筛查与院感防控', '本科',
        NULL, 1, '30.00', 1, 'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_employee (id, emp_code, emp_name, emp_type, gender, birth_date, hire_date, id_card, phone, email,
                          dept_id, dept_name, title, position, specialty, education, avatar, is_expert, expert_price,
                          status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('897001415', 'H17415', '莫静文', 1, 2, '1993-02-22', '2017-02-01', '110105199302229809', '18711572268',
        'mojingwen@cslk-his.local', '19580068', '发热门诊', '201', '31', '发热待查、呼吸道传染病筛查与院感防控', '本科',
        NULL, 0, '0.00', 1, 'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_employee (id, emp_code, emp_name, emp_type, gender, birth_date, hire_date, id_card, phone, email,
                          dept_id, dept_name, title, position, specialty, education, avatar, is_expert, expert_price,
                          status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('897001416', 'H17416', '李彤', 2, 2, '1991-10-01', '2015-03-01', '210102199110017101', '18951908625',
        'litong@cslk-his.local', '19580068', '发热门诊', '106', '15', '发热待查、呼吸道传染病筛查与院感防控', '硕士',
        NULL, 0, '0.00', 1, 'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_employee (id, emp_code, emp_name, emp_type, gender, birth_date, hire_date, id_card, phone, email,
                          dept_id, dept_name, title, position, specialty, education, avatar, is_expert, expert_price,
                          status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('897001417', 'H17417', '严白露', 6, 2, '1986-11-25', '2011-12-01', '370102198611255722', '15166399205',
        'yanbailu@cslk-his.local', '19580068', '发热门诊', '201', '39', '发热待查、呼吸道传染病筛查与院感防控', '硕士',
        NULL, 0, '0.00', 1, 'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_employee (id, emp_code, emp_name, emp_type, gender, birth_date, hire_date, id_card, phone, email,
                          dept_id, dept_name, title, position, specialty, education, avatar, is_expert, expert_price,
                          status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('897001418', 'H17418', '江白露', 1, 2, '1992-02-01', '2016-10-01', '310104199202015329', '18198732548',
        'jiangsbailu@cslk-his.local', '19580069', '肠道门诊', '201', '1', '感染性腹泻、肠道传染病报卡与标本送检', '硕士',
        NULL, 0, '0.00', 1, 'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_employee (id, emp_code, emp_name, emp_type, gender, birth_date, hire_date, id_card, phone, email,
                          dept_id, dept_name, title, position, specialty, education, avatar, is_expert, expert_price,
                          status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('897001419', 'H17419', '于仁心', 1, 1, '1993-09-07', '2018-09-01', '310104199309070774', '15950381171',
        'yurenxin@cslk-his.local', '19580069', '肠道门诊', '102', '31', '感染性腹泻、肠道传染病报卡与标本送检', '硕士',
        NULL, 0, '0.00', 1, 'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_employee (id, emp_code, emp_name, emp_type, gender, birth_date, hire_date, id_card, phone, email,
                          dept_id, dept_name, title, position, specialty, education, avatar, is_expert, expert_price,
                          status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('897001420', 'H17420', '苏婉', 2, 2, '2001-02-28', '2025-03-01', '210102200102282621', '13828100493',
        'suwan@cslk-his.local', '19580069', '肠道门诊', '105', '15', '感染性腹泻、肠道传染病报卡与标本送检', '本科',
        NULL, 0, '0.00', 1, 'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_employee (id, emp_code, emp_name, emp_type, gender, birth_date, hire_date, id_card, phone, email,
                          dept_id, dept_name, title, position, specialty, education, avatar, is_expert, expert_price,
                          status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('897001421', 'H17421', '吴升', 6, 1, '1990-11-01', '2013-01-01', '370102199011018277', '13650000590',
        'wusheng@cslk-his.local', '19580069', '肠道门诊', '201', '35', '感染性腹泻、肠道传染病报卡与标本送检', '硕士',
        NULL, 0, '0.00', 1, 'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_employee (id, emp_code, emp_name, emp_type, gender, birth_date, hire_date, id_card, phone, email,
                          dept_id, dept_name, title, position, specialty, education, avatar, is_expert, expert_price,
                          status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('897001422', 'H17422', '程宏', 5, 1, '1968-10-14', '1992-11-01', '420106196810145930', '18135744003',
        'chenghong@cslk-his.local', '19580076', '院办', '401', '21', '医院行政运行、综合协调与总值班', '博士', NULL, 0,
        '0.00', 1, 'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_employee (id, emp_code, emp_name, emp_type, gender, birth_date, hire_date, id_card, phone, email,
                          dept_id, dept_name, title, position, specialty, education, avatar, is_expert, expert_price,
                          status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('897001423', 'H17423', '温秉钧', 5, 1, '1974-08-08', '1997-07-01', '420106197408082913', '18076834061',
        'wenbingjun@cslk-his.local', '19580076', '院办', '301', '21', '医院行政运行、综合协调与总值班', '博士', NULL, 0,
        '0.00', 1, 'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_employee (id, emp_code, emp_name, emp_type, gender, birth_date, hire_date, id_card, phone, email,
                          dept_id, dept_name, title, position, specialty, education, avatar, is_expert, expert_price,
                          status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('897001424', 'H17424', '余璐', 5, 2, '1978-06-14', '2003-06-01', '440305197806149587', '15277972862',
        'yushenglu@cslk-his.local', '19580076', '院办', '506', '22', '医院行政运行、综合协调与总值班', '本科', NULL, 0,
        '0.00', 1, 'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_employee (id, emp_code, emp_name, emp_type, gender, birth_date, hire_date, id_card, phone, email,
                          dept_id, dept_name, title, position, specialty, education, avatar, is_expert, expert_price,
                          status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('897001425', 'H17425', '蔡璐', 5, 2, '1995-08-13', '2019-07-01', '330106199508133447', '15817549635',
        'cailu@cslk-his.local', '19580076', '院办', '501', '24', '医院行政运行、综合协调与总值班', '本科', NULL, 0,
        '0.00', 1, 'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_employee (id, emp_code, emp_name, emp_type, gender, birth_date, hire_date, id_card, phone, email,
                          dept_id, dept_name, title, position, specialty, education, avatar, is_expert, expert_price,
                          status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('897001426', 'H17426', '许妍', 6, 2, '2002-09-07', '2024-07-01', '510107200209074921', '18820237036',
        'xushuyan@cslk-his.local', '19580076', '院办', '513', '32', '医院行政运行、综合协调与总值班', '大专', NULL, 0,
        '0.00', 1, 'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_employee (id, emp_code, emp_name, emp_type, gender, birth_date, hire_date, id_card, phone, email,
                          dept_id, dept_name, title, position, specialty, education, avatar, is_expert, expert_price,
                          status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('897001427', 'H17427', '江鹏飞', 1, 1, '1978-10-03', '2003-06-01', '310104197810039392', '15918988307',
        'jiangspengfei@cslk-his.local', '19580077', '医务科', '401', '22', '医疗质量、技术临床应用授权与医疗安全管理',
        '硕士', NULL, 0, '0.00', 1, 'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_employee (id, emp_code, emp_name, emp_type, gender, birth_date, hire_date, id_card, phone, email,
                          dept_id, dept_name, title, position, specialty, education, avatar, is_expert, expert_price,
                          status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('897001428', 'H17428', '康敏', 5, 2, '1980-04-24', '2005-08-01', '320106198004246345', '13621674084',
        'kangmin@cslk-his.local', '19580077', '医务科', '301', '24', '医疗质量、技术临床应用授权与医疗安全管理', '本科',
        NULL, 0, '0.00', 1, 'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_employee (id, emp_code, emp_name, emp_type, gender, birth_date, hire_date, id_card, phone, email,
                          dept_id, dept_name, title, position, specialty, education, avatar, is_expert, expert_price,
                          status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('897001429', 'H17429', '夏梦洁', 5, 2, '1988-02-11', '2011-12-01', '440305198802118446', '17633636720',
        'xiamengjie@cslk-his.local', '19580077', '医务科', '501', '24', '医疗质量、技术临床应用授权与医疗安全管理',
        '硕士', NULL, 0, '0.00', 1, 'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_employee (id, emp_code, emp_name, emp_type, gender, birth_date, hire_date, id_card, phone, email,
                          dept_id, dept_name, title, position, specialty, education, avatar, is_expert, expert_price,
                          status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('897001430', 'H17430', '秦观澜', 5, 1, '1992-11-13', '2015-02-01', '420106199211136198', '18565306549',
        'qinguanlan@cslk-his.local', '19580077', '医务科', '501', '24', '医疗质量、技术临床应用授权与医疗安全管理',
        '本科', NULL, 0, '0.00', 1, 'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_employee (id, emp_code, emp_name, emp_type, gender, birth_date, hire_date, id_card, phone, email,
                          dept_id, dept_name, title, position, specialty, education, avatar, is_expert, expert_price,
                          status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('897001431', 'H17431', '罗慧', 2, 2, '1973-01-05', '1998-05-01', '440305197301051824', '19864958795',
        'luohui@cslk-his.local', '19580078', '护理部', '403', '7', '全院护理人力调配、质控与培训', '本科', NULL, 0,
        '0.00', 1, 'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_employee (id, emp_code, emp_name, emp_type, gender, birth_date, hire_date, id_card, phone, email,
                          dept_id, dept_name, title, position, specialty, education, avatar, is_expert, expert_price,
                          status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('897001432', 'H17432', '龙月白', 2, 2, '1980-01-20', '2003-03-01', '510107198001207924', '15891618826',
        'longyuebai@cslk-his.local', '19580078', '护理部', '303', '8', '全院护理人力调配、质控与培训', '本科', NULL, 0,
        '0.00', 1, 'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_employee (id, emp_code, emp_name, emp_type, gender, birth_date, hire_date, id_card, phone, email,
                          dept_id, dept_name, title, position, specialty, education, avatar, is_expert, expert_price,
                          status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('897001433', 'H17433', '王一诺', 2, 2, '1988-04-17', '2012-02-01', '510107198804176826', '18129492417',
        'wangyinuo@cslk-his.local', '19580078', '护理部', '203', '24', '全院护理人力调配、质控与培训', '本科', NULL, 0,
        '0.00', 1, 'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_employee (id, emp_code, emp_name, emp_type, gender, birth_date, hire_date, id_card, phone, email,
                          dept_id, dept_name, title, position, specialty, education, avatar, is_expert, expert_price,
                          status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('897001434', 'H17434', '苏慧', 5, 2, '1976-02-24', '1998-06-01', '610113197602246909', '15058487703',
        'suhui@cslk-his.local', '19580079', '财务科', '505', '22', '院内经济核算、班结日结与价格管理', '本科', NULL, 0,
        '0.00', 1, 'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_employee (id, emp_code, emp_name, emp_type, gender, birth_date, hire_date, id_card, phone, email,
                          dept_id, dept_name, title, position, specialty, education, avatar, is_expert, expert_price,
                          status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('897001435', 'H17435', '吕伟', 5, 1, '1983-07-13', '2008-10-01', '210102198307131296', '18713959037',
        'lvwei@cslk-his.local', '19580079', '财务科', '504', '24', '院内经济核算、班结日结与价格管理', '本科', NULL, 0,
        '0.00', 1, 'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_employee (id, emp_code, emp_name, emp_type, gender, birth_date, hire_date, id_card, phone, email,
                          dept_id, dept_name, title, position, specialty, education, avatar, is_expert, expert_price,
                          status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('897001436', 'H17436', '韩蕾', 3, 2, '1993-07-13', '2017-11-01', '320106199307131929', '17856493389',
        'hanlei@cslk-his.local', '19580079', '财务科', '504', '40', '院内经济核算、班结日结与价格管理', '本科', NULL, 0,
        '0.00', 1, 'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_employee (id, emp_code, emp_name, emp_type, gender, birth_date, hire_date, id_card, phone, email,
                          dept_id, dept_name, title, position, specialty, education, avatar, is_expert, expert_price,
                          status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('897001437', 'H17437', '袁晨曦', 3, 1, '1993-06-24', '2016-02-01', '510107199306249353', '18293712075',
        'yuanchenxi@cslk-his.local', '19580079', '财务科', '501', '40', '院内经济核算、班结日结与价格管理', '大专', NULL,
        0, '0.00', 1, 'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_employee (id, emp_code, emp_name, emp_type, gender, birth_date, hire_date, id_card, phone, email,
                          dept_id, dept_name, title, position, specialty, education, avatar, is_expert, expert_price,
                          status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('897001438', 'H17438', '冯如初', 3, 2, '1988-12-04', '2012-07-01', '510107198812048023', '18637865512',
        'fengruchu@cslk-his.local', '19580079', '财务科', '504', '40', '院内经济核算、班结日结与价格管理', '本科', NULL,
        0, '0.00', 1, 'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_employee (id, emp_code, emp_name, emp_type, gender, birth_date, hire_date, id_card, phone, email,
                          dept_id, dept_name, title, position, specialty, education, avatar, is_expert, expert_price,
                          status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('897001439', 'H17439', '顾乐怡', 5, 2, '1977-03-17', '2001-06-01', '610113197703177287', '13743141902',
        'guleyi@cslk-his.local', '19580080', '医保科', '201', '22', '医保结算、目录对码、基金合规与飞检应对', '硕士',
        NULL, 0, '0.00', 1, 'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_employee (id, emp_code, emp_name, emp_type, gender, birth_date, hire_date, id_card, phone, email,
                          dept_id, dept_name, title, position, specialty, education, avatar, is_expert, expert_price,
                          status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('897001440', 'H17440', '侯亦航', 5, 1, '1986-12-23', '2011-09-01', '510107198612232117', '15161654388',
        'houyihang@cslk-his.local', '19580080', '医保科', '102', '40', '医保结算、目录对码、基金合规与飞检应对', '硕士',
        NULL, 0, '0.00', 1, 'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_employee (id, emp_code, emp_name, emp_type, gender, birth_date, hire_date, id_card, phone, email,
                          dept_id, dept_name, title, position, specialty, education, avatar, is_expert, expert_price,
                          status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('897001441', 'H17441', '夏桂芳', 5, 2, '1993-04-05', '2016-05-01', '420106199304056621', '18259250335',
        'xiaguifang@cslk-his.local', '19580080', '医保科', '501', '40', '医保结算、目录对码、基金合规与飞检应对', '硕士',
        NULL, 0, '0.00', 1, 'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_employee (id, emp_code, emp_name, emp_type, gender, birth_date, hire_date, id_card, phone, email,
                          dept_id, dept_name, title, position, specialty, education, avatar, is_expert, expert_price,
                          status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('897001442', 'H17442', '彭颖', 5, 2, '1993-10-11', '2017-01-01', '44030519931011550X', '13757469845',
        'pengying@cslk-his.local', '19580080', '医保科', '501', '40', '医保结算、目录对码、基金合规与飞检应对', '本科',
        NULL, 0, '0.00', 1, 'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_employee (id, emp_code, emp_name, emp_type, gender, birth_date, hire_date, id_card, phone, email,
                          dept_id, dept_name, title, position, specialty, education, avatar, is_expert, expert_price,
                          status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('897001443', 'H17443', '李瑞', 5, 1, '1979-08-02', '2001-02-01', '370102197908023338', '15215191758',
        'lirui@cslk-his.local', '19580081', '信息科', '503', '22', 'HIS 运行、接口集成、数据安全与可信时间戳', '本科',
        NULL, 0, '0.00', 1, 'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_employee (id, emp_code, emp_name, emp_type, gender, birth_date, hire_date, id_card, phone, email,
                          dept_id, dept_name, title, position, specialty, education, avatar, is_expert, expert_price,
                          status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('897001444', 'H17444', '彭慧', 5, 2, '1987-10-12', '2012-05-01', '33010619871012366X', '18078414236',
        'penghui@cslk-his.local', '19580081', '信息科', '502', '41', 'HIS 运行、接口集成、数据安全与可信时间戳', '本科',
        NULL, 0, '0.00', 1, 'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_employee (id, emp_code, emp_name, emp_type, gender, birth_date, hire_date, id_card, phone, email,
                          dept_id, dept_name, title, position, specialty, education, avatar, is_expert, expert_price,
                          status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('897001445', 'H17445', '容欣', 5, 2, '1990-07-19', '2015-07-01', '510107199007192641', '18620135231',
        'rongxin@cslk-his.local', '19580081', '信息科', '502', '41', 'HIS 运行、接口集成、数据安全与可信时间戳', '硕士',
        NULL, 0, '0.00', 1, 'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_employee (id, emp_code, emp_name, emp_type, gender, birth_date, hire_date, id_card, phone, email,
                          dept_id, dept_name, title, position, specialty, education, avatar, is_expert, expert_price,
                          status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('897001446', 'H17446', '蒋俊杰', 5, 1, '1993-07-08', '2017-06-01', '110105199307089438', '18225446273',
        'jiangjunjie@cslk-his.local', '19580081', '信息科', '507', '41', 'HIS 运行、接口集成、数据安全与可信时间戳',
        '本科', NULL, 0, '0.00', 1, 'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_employee (id, emp_code, emp_name, emp_type, gender, birth_date, hire_date, id_card, phone, email,
                          dept_id, dept_name, title, position, specialty, education, avatar, is_expert, expert_price,
                          status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('897001447', 'H17447', '尹子轩', 6, 1, '1979-05-16', '2004-05-01', '210102197905165758', '18876278178',
        'yinzixuan@cslk-his.local', '19580082', '设备科', '502', '36', '医疗设备全生命周期、维保计量与采购', '本科',
        NULL, 0, '0.00', 1, 'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_employee (id, emp_code, emp_name, emp_type, gender, birth_date, hire_date, id_card, phone, email,
                          dept_id, dept_name, title, position, specialty, education, avatar, is_expert, expert_price,
                          status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('897001448', 'H17448', '许跃龙', 6, 1, '1992-06-21', '2017-12-01', '110105199206218958', '17838724935',
        'xushuyuelong@cslk-his.local', '19580082', '设备科', '507', '42', '医疗设备全生命周期、维保计量与采购', '本科',
        NULL, 0, '0.00', 1, 'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_employee (id, emp_code, emp_name, emp_type, gender, birth_date, hire_date, id_card, phone, email,
                          dept_id, dept_name, title, position, specialty, education, avatar, is_expert, expert_price,
                          status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('897001449', 'H17449', '吕建安', 5, 1, '1992-06-11', '2015-05-01', '370102199206117371', '17612563794',
        'lvjianan@cslk-his.local', '19580082', '设备科', '501', '42', '医疗设备全生命周期、维保计量与采购', '本科', NULL,
        0, '0.00', 0, 'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_employee (id, emp_code, emp_name, emp_type, gender, birth_date, hire_date, id_card, phone, email,
                          dept_id, dept_name, title, position, specialty, education, avatar, is_expert, expert_price,
                          status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('897001450', 'H17450', '龙佳明', 5, 1, '1978-09-03', '2001-10-01', '310104197809038317', '15992517259',
        'longjiaming@cslk-his.local', '19580083', '保卫科', '516', '22', '院内治安、消防与医废出入管理', '大专', NULL, 0,
        '0.00', 1, 'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_employee (id, emp_code, emp_name, emp_type, gender, birth_date, hire_date, id_card, phone, email,
                          dept_id, dept_name, title, position, specialty, education, avatar, is_expert, expert_price,
                          status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('897001451', 'H17451', '龙子轩', 5, 1, '1993-12-21', '2015-08-01', '330106199312212936', '15248753145',
        'longzixuan@cslk-his.local', '19580083', '保卫科', '516', '43', '院内治安、消防与医废出入管理', '大专', NULL, 0,
        '0.00', 1, 'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_employee (id, emp_code, emp_name, emp_type, gender, birth_date, hire_date, id_card, phone, email,
                          dept_id, dept_name, title, position, specialty, education, avatar, is_expert, expert_price,
                          status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('897001452', 'H17452', '郑天', 5, 1, '1990-08-20', '2015-04-01', '11010519900820589X', '18914306275',
        'zhengtian@cslk-his.local', '19580083', '保卫科', '516', '43', '院内治安、消防与医废出入管理', '中专', NULL, 0,
        '0.00', 1, 'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_employee (id, emp_code, emp_name, emp_type, gender, birth_date, hire_date, id_card, phone, email,
                          dept_id, dept_name, title, position, specialty, education, avatar, is_expert, expert_price,
                          status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('897001453', 'H17453', '钱海艳', 5, 2, '1980-11-25', '2004-08-01', '320106198011259283', '18232671618',
        'qianhaiyan@cslk-his.local', '19580084', '总务科', '502', '22', '后勤保障、报修响应与环境被服管理', '本科', NULL,
        0, '0.00', 1, 'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_employee (id, emp_code, emp_name, emp_type, gender, birth_date, hire_date, id_card, phone, email,
                          dept_id, dept_name, title, position, specialty, education, avatar, is_expert, expert_price,
                          status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('897001454', 'H17454', '贺若彤', 5, 2, '1992-05-21', '2014-04-01', '330106199205219649', '19933495796',
        'hesunruotong@cslk-his.local', '19580084', '总务科', '507', '44', '后勤保障、报修响应与环境被服管理', '大专',
        NULL, 0, '0.00', 1, 'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_employee (id, emp_code, emp_name, emp_type, gender, birth_date, hire_date, id_card, phone, email,
                          dept_id, dept_name, title, position, specialty, education, avatar, is_expert, expert_price,
                          status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('897001455', 'H17455', '高和玉', 5, 2, '1992-08-18', '2016-03-01', '310104199208188861', '13992401004',
        'gaoheyu@cslk-his.local', '19580084', '总务科', '513', '44', '后勤保障、报修响应与环境被服管理', '中专', NULL, 0,
        '0.00', 1, 'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_employee (id, emp_code, emp_name, emp_type, gender, birth_date, hire_date, id_card, phone, email,
                          dept_id, dept_name, title, position, specialty, education, avatar, is_expert, expert_price,
                          status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('897001456', 'H17456', '康瑶', 5, 2, '1982-04-27', '2006-04-01', '310104198204279606', '18544573749',
        'kangyao@cslk-his.local', '19580085', '病案室', '507', '22', '病案归档、国际疾病分类编码与统计上报', '本科',
        NULL, 0, '0.00', 1, 'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_employee (id, emp_code, emp_name, emp_type, gender, birth_date, hire_date, id_card, phone, email,
                          dept_id, dept_name, title, position, specialty, education, avatar, is_expert, expert_price,
                          status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('897001457', 'H17457', '陆雪', 5, 2, '1986-07-14', '2011-01-01', '610113198607142222', '19816490387',
        'lukuxueli@cslk-his.local', '19580085', '病案室', '511', '45', '病案归档、国际疾病分类编码与统计上报', '本科',
        NULL, 0, '0.00', 1, 'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_employee (id, emp_code, emp_name, emp_type, gender, birth_date, hire_date, id_card, phone, email,
                          dept_id, dept_name, title, position, specialty, education, avatar, is_expert, expert_price,
                          status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('897001458', 'H17458', '沈鸣谦', 5, 1, '1988-09-05', '2013-10-01', '370102198809058871', '19929889373',
        'shenmingqian@cslk-his.local', '19580085', '病案室', '512', '45', '病案归档、国际疾病分类编码与统计上报', '本科',
        NULL, 0, '0.00', 1, 'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_employee (id, emp_code, emp_name, emp_type, gender, birth_date, hire_date, id_card, phone, email,
                          dept_id, dept_name, title, position, specialty, education, avatar, is_expert, expert_price,
                          status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('897001459', 'H17459', '林蓉', 5, 2, '1993-07-03', '2016-11-01', '440305199307036966', '13675947379',
        'linrong@cslk-his.local', '19580085', '病案室', '501', '45', '病案归档、国际疾病分类编码与统计上报', '大专',
        NULL, 0, '0.00', 1, 'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_employee (id, emp_code, emp_name, emp_type, gender, birth_date, hire_date, id_card, phone, email,
                          dept_id, dept_name, title, position, specialty, education, avatar, is_expert, expert_price,
                          status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('896600000000000101', 'N101', '李桂芳', 5, 2, '1979-03-12', '2005-02-01', '110105197903121124', '13523855079',
        'n101@cslk-his.local', '19580003', '呼吸内科', '515', '9', NULL, '本科', NULL, 0, '0.00', 1, 'admin', 'system',
        0, NULL, '1', '1');
INSERT INTO sys_employee (id, emp_code, emp_name, emp_type, gender, birth_date, hire_date, id_card, phone, email,
                          dept_id, dept_name, title, position, specialty, education, avatar, is_expert, expert_price,
                          status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('896600000000000102', 'N102', '周丽', 5, 2, '1990-03-08', '2008-03-02', '110105199003081429', '13523992258',
        'n102@cslk-his.local', '19580003', '呼吸内科', '203', '15', NULL, '大专', NULL, 0, '0.00', 1, 'admin', 'system',
        0, NULL, '1', '1');
INSERT INTO sys_employee (id, emp_code, emp_name, emp_type, gender, birth_date, hire_date, id_card, phone, email,
                          dept_id, dept_name, title, position, specialty, education, avatar, is_expert, expert_price,
                          status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('896600000000000103', 'N103', '陈静', 5, 2, '1993-06-15', '2011-04-03', '11010519930615172X', '13524129437',
        'n103@cslk-his.local', '19580003', '呼吸内科', '106', '15', NULL, '本科', NULL, 0, '0.00', 1, 'admin', 'system',
        0, NULL, '1', '1');
INSERT INTO sys_employee (id, emp_code, emp_name, emp_type, gender, birth_date, hire_date, id_card, phone, email,
                          dept_id, dept_name, title, position, specialty, education, avatar, is_expert, expert_price,
                          status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('896600000000000104', 'N104', '王雪梅', 5, 2, '1987-09-20', '2014-05-04', '110105198709202021', '13524266616',
        'n104@cslk-his.local', '19580003', '呼吸内科', '105', '15', NULL, '中专', NULL, 0, '0.00', 1, 'admin', 'system',
        0, NULL, '1', '1');
INSERT INTO sys_employee (id, emp_code, emp_name, emp_type, gender, birth_date, hire_date, id_card, phone, email,
                          dept_id, dept_name, title, position, specialty, education, avatar, is_expert, expert_price,
                          status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('896600000000000105', 'N105', '刘伟', 5, 1, '1991-04-11', '2017-06-05', '110105199104112319', '13724403795',
        'n105@cslk-his.local', '19580003', '呼吸内科', '106', '15', NULL, '本科', NULL, 0, '0.00', 1, 'admin', 'system',
        0, NULL, '1', '1');
INSERT INTO sys_employee (id, emp_code, emp_name, emp_type, gender, birth_date, hire_date, id_card, phone, email,
                          dept_id, dept_name, title, position, specialty, education, avatar, is_expert, expert_price,
                          status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('896600000000000106', 'N106', '张小红', 5, 2, '1995-07-05', '2004-07-06', '110105199507052621', '13524540974',
        'n106@cslk-his.local', '19580003', '呼吸内科', '105', '15', NULL, '大专', NULL, 0, '0.00', 1, 'admin', 'system',
        0, NULL, '1', '1');
INSERT INTO sys_employee (id, emp_code, emp_name, emp_type, gender, birth_date, hire_date, id_card, phone, email,
                          dept_id, dept_name, title, position, specialty, education, avatar, is_expert, expert_price,
                          status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('896600000000000107', 'N107', '赵敏', 5, 2, '1992-11-18', '2007-08-07', '110105199211182929', '13524678153',
        'n107@cslk-his.local', '19580003', '呼吸内科', '514', '13', NULL, '本科', NULL, 0, '0.00', 1, 'admin', 'system',
        0, NULL, '1', '1');
INSERT INTO sys_employee (id, emp_code, emp_name, emp_type, gender, birth_date, hire_date, id_card, phone, email,
                          dept_id, dept_name, title, position, specialty, education, avatar, is_expert, expert_price,
                          status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('896600000000000108', 'N108', '孙倩', 5, 2, '1996-01-26', '2019-09-02', '110105199601263222', '13524815332',
        'n108@cslk-his.local', '19580003', '呼吸内科', '105', '15', NULL, '大专', NULL, 0, '0.00', 1, 'admin', 'system',
        0, NULL, '1', '1');
INSERT INTO sys_employee (id, emp_code, emp_name, emp_type, gender, birth_date, hire_date, id_card, phone, email,
                          dept_id, dept_name, title, position, specialty, education, avatar, is_expert, expert_price,
                          status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('896600000000000111', 'N111', '孙丽华', 5, 2, '1981-05-09', '2006-03-01', '320106198105091127', '13525226869',
        'n111@cslk-his.local', '19580004', '消化内科', '515', '9', NULL, '大专', NULL, 0, '0.00', 1, 'admin', 'system',
        0, NULL, '1', '1');
INSERT INTO sys_employee (id, emp_code, emp_name, emp_type, gender, birth_date, hire_date, id_card, phone, email,
                          dept_id, dept_name, title, position, specialty, education, avatar, is_expert, expert_price,
                          status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('896600000000000112', 'N112', '钱雅琴', 5, 2, '1994-08-12', '2009-04-02', '320106199408121420', '13525364048',
        'n112@cslk-his.local', '19580004', '消化内科', '203', '15', NULL, '本科', NULL, 0, '0.00', 1, 'admin', 'system',
        0, NULL, '1', '1');
INSERT INTO sys_employee (id, emp_code, emp_name, emp_type, gender, birth_date, hire_date, id_card, phone, email,
                          dept_id, dept_name, title, position, specialty, education, avatar, is_expert, expert_price,
                          status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('896600000000000113', 'N113', '冯健', 5, 1, '1989-02-26', '2012-05-03', '320106198902261715', '13725501227',
        'n113@cslk-his.local', '19580004', '消化内科', '106', '15', NULL, '中专', NULL, 0, '0.00', 1, 'admin', 'system',
        0, NULL, '1', '1');
INSERT INTO sys_employee (id, emp_code, emp_name, emp_type, gender, birth_date, hire_date, id_card, phone, email,
                          dept_id, dept_name, title, position, specialty, education, avatar, is_expert, expert_price,
                          status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('896600000000000114', 'N114', '卫兰', 5, 2, '1996-10-07', '2015-06-04', '320106199610072028', '13525638406',
        'n114@cslk-his.local', '19580004', '消化内科', '105', '15', NULL, '本科', NULL, 0, '0.00', 1, 'admin', 'system',
        0, NULL, '1', '1');
INSERT INTO sys_employee (id, emp_code, emp_name, emp_type, gender, birth_date, hire_date, id_card, phone, email,
                          dept_id, dept_name, title, position, specialty, education, avatar, is_expert, expert_price,
                          status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('896600000000000115', 'N115', '蒋雯', 5, 2, '1991-01-14', '2018-07-05', '320106199101142323', '13525775585',
        'n115@cslk-his.local', '19580004', '消化内科', '106', '15', NULL, '大专', NULL, 0, '0.00', 1, 'admin', 'system',
        0, NULL, '1', '1');
INSERT INTO sys_employee (id, emp_code, emp_name, emp_type, gender, birth_date, hire_date, id_card, phone, email,
                          dept_id, dept_name, title, position, specialty, education, avatar, is_expert, expert_price,
                          status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('896600000000000116', 'N116', '沈明珠', 5, 2, '1988-06-30', '2005-08-06', '320106198806302628', '13525912764',
        'n116@cslk-his.local', '19580004', '消化内科', '105', '15', NULL, '本科', NULL, 0, '0.00', 1, 'admin', 'system',
        0, NULL, '1', '1');
INSERT INTO sys_employee (id, emp_code, emp_name, emp_type, gender, birth_date, hire_date, id_card, phone, email,
                          dept_id, dept_name, title, position, specialty, education, avatar, is_expert, expert_price,
                          status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('896600000000000117', 'N117', '韩梅', 5, 2, '1993-03-22', '2008-09-07', '32010619930322292X', '13526049943',
        'n117@cslk-his.local', '19580004', '消化内科', '514', '13', NULL, '大专', NULL, 0, '0.00', 1, 'admin', 'system',
        0, NULL, '1', '1');
INSERT INTO sys_employee (id, emp_code, emp_name, emp_type, gender, birth_date, hire_date, id_card, phone, email,
                          dept_id, dept_name, title, position, specialty, education, avatar, is_expert, expert_price,
                          status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('896600000000000118', 'N118', '杨紫涵', 5, 2, '1997-12-03', '2020-10-01', '320106199712033222', '13526187122',
        'n118@cslk-his.local', '19580004', '消化内科', '105', '15', NULL, '本科', NULL, 0, '0.00', 1, 'admin', 'system',
        0, NULL, '1', '1');
INSERT INTO sys_employee (id, emp_code, emp_name, emp_type, gender, birth_date, hire_date, id_card, phone, email,
                          dept_id, dept_name, title, position, specialty, education, avatar, is_expert, expert_price,
                          status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('896600000000000121', 'N121', '曹玉梅', 5, 2, '1980-07-16', '2007-04-01', '44030519800716112X', '13526598659',
        'n121@cslk-his.local', '19580005', '心血管内科', '515', '9', NULL, '本科', NULL, 0, '0.00', 1, 'admin',
        'system', 0, NULL, '1', '1');
INSERT INTO sys_employee (id, emp_code, emp_name, emp_type, gender, birth_date, hire_date, id_card, phone, email,
                          dept_id, dept_name, title, position, specialty, education, avatar, is_expert, expert_price,
                          status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('896600000000000122', 'N122', '彭思琪', 5, 2, '1995-09-09', '2010-05-02', '440305199509091429', '13526735838',
        'n122@cslk-his.local', '19580005', '心血管内科', '203', '15', NULL, '中专', NULL, 0, '0.00', 1, 'admin',
        'system', 0, NULL, '1', '1');
INSERT INTO sys_employee (id, emp_code, emp_name, emp_type, gender, birth_date, hire_date, id_card, phone, email,
                          dept_id, dept_name, title, position, specialty, education, avatar, is_expert, expert_price,
                          status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('896600000000000123', 'N123', '萧海燕', 5, 2, '1990-12-04', '2013-06-03', '440305199012041725', '13526873017',
        'n123@cslk-his.local', '19580005', '心血管内科', '106', '15', NULL, '本科', NULL, 0, '0.00', 1, 'admin',
        'system', 0, NULL, '1', '1');
INSERT INTO sys_employee (id, emp_code, emp_name, emp_type, gender, birth_date, hire_date, id_card, phone, email,
                          dept_id, dept_name, title, position, specialty, education, avatar, is_expert, expert_price,
                          status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('896600000000000124', 'N124', '袁春丽', 5, 2, '1992-04-21', '2016-07-04', '440305199204212023', '13527010196',
        'n124@cslk-his.local', '19580005', '心血管内科', '105', '15', NULL, '大专', NULL, 0, '0.00', 1, 'admin',
        'system', 0, NULL, '1', '1');
INSERT INTO sys_employee (id, emp_code, emp_name, emp_type, gender, birth_date, hire_date, id_card, phone, email,
                          dept_id, dept_name, title, position, specialty, education, avatar, is_expert, expert_price,
                          status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('896600000000000125', 'N125', '陆晓峰', 5, 1, '1986-08-08', '2019-08-05', '440305198608082312', '13727147375',
        'n125@cslk-his.local', '19580005', '心血管内科', '106', '15', NULL, '本科', NULL, 0, '0.00', 1, 'admin',
        'system', 0, NULL, '1', '1');
INSERT INTO sys_employee (id, emp_code, emp_name, emp_type, gender, birth_date, hire_date, id_card, phone, email,
                          dept_id, dept_name, title, position, specialty, education, avatar, is_expert, expert_price,
                          status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('896600000000000126', 'N126', '俞静文', 5, 2, '1997-02-17', '2006-09-06', '440305199702172626', '13527284554',
        'n126@cslk-his.local', '19580005', '心血管内科', '105', '15', NULL, '大专', NULL, 0, '0.00', 1, 'admin',
        'system', 0, NULL, '1', '1');
INSERT INTO sys_employee (id, emp_code, emp_name, emp_type, gender, birth_date, hire_date, id_card, phone, email,
                          dept_id, dept_name, title, position, specialty, education, avatar, is_expert, expert_price,
                          status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('896600000000000127', 'N127', '顾惜', 5, 2, '1994-05-11', '2009-10-07', '440305199405112926', '13527421733',
        'n127@cslk-his.local', '19580005', '心血管内科', '514', '13', NULL, '本科', NULL, 0, '0.00', 1, 'admin',
        'system', 0, NULL, '1', '1');
INSERT INTO sys_employee (id, emp_code, emp_name, emp_type, gender, birth_date, hire_date, id_card, phone, email,
                          dept_id, dept_name, title, position, specialty, education, avatar, is_expert, expert_price,
                          status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('896600000000000128', 'N128', '时雨', 5, 2, '1993-07-28', '2017-11-01', '44030519930728322X', '13527558912',
        'n128@cslk-his.local', '19580005', '心血管内科', '106', '15', NULL, '大专', NULL, 0, '0.00', 1, 'admin',
        'system', 0, NULL, '1', '1');
INSERT INTO sys_employee (id, emp_code, emp_name, emp_type, gender, birth_date, hire_date, id_card, phone, email,
                          dept_id, dept_name, title, position, specialty, education, avatar, is_expert, expert_price,
                          status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('896800000000000011', 'D16801', '苏建华', 1, 1, '1978-05-16', '2003-07-01', '440305197805162317', '13712098001',
        'd16801@cslk-his.local', '19580003', '呼吸内科', '401', '1', NULL, '本科', NULL, 0, '0.00', 1, 'admin', 'admin',
        0, NULL, '1', '1');
INSERT INTO sys_employee (id, emp_code, emp_name, emp_type, gender, birth_date, hire_date, id_card, phone, email,
                          dept_id, dept_name, title, position, specialty, education, avatar, is_expert, expert_price,
                          status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('896800000000000012', 'D16802', '吴亦航', 1, 1, '1982-09-27', '2008-07-02', '440305198209273373', '13712098002',
        'd16802@cslk-his.local', '19580004', '消化内科', '301', '31', NULL, '硕士', NULL, 0, '0.00', 1, 'admin',
        'system', 0, NULL, '1', '1');
INSERT INTO sys_employee (id, emp_code, emp_name, emp_type, gender, birth_date, hire_date, id_card, phone, email,
                          dept_id, dept_name, title, position, specialty, education, avatar, is_expert, expert_price,
                          status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('896800000000000013', 'D16803', '郑明远', 1, 1, '1985-11-03', '2011-07-05', '440305198511032941', '13712098003',
        'd16803@cslk-his.local', '19580005', '心血管内科', '201', '31', NULL, '硕士', NULL, 0, '0.00', 1, 'admin',
        'system', 0, NULL, '1', '1');
INSERT INTO sys_employee (id, emp_code, emp_name, emp_type, gender, birth_date, hire_date, id_card, phone, email,
                          dept_id, dept_name, title, position, specialty, education, avatar, is_expert, expert_price,
                          status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('896809280000000101', 'Y0901', '韩雨薇', 1, 2, '1988-04-16', '2010-07-01', '110105198804162425', '13528010901',
        'y0901@cslk-his.local', '19580093', '营养科', '509', '34', '', '本科', NULL, 0, '0.00', 1, 'admin', 'system', 0,
        NULL, '1', '1');
INSERT INTO sys_employee (id, emp_code, emp_name, emp_type, gender, birth_date, hire_date, id_card, phone, email,
                          dept_id, dept_name, title, position, specialty, education, avatar, is_expert, expert_price,
                          status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('896809280000000102', 'Y0902', '郭建宁', 1, 1, '1991-07-22', '2015-09-01', '320106199107221239', '13728010902',
        'y0902@cslk-his.local', '19580093', '营养科', '508', '34', NULL, '硕士', NULL, 0, '0.00', 1, 'admin', 'system',
        0, NULL, '1', '1');
INSERT INTO sys_employee (id, emp_code, emp_name, emp_type, gender, birth_date, hire_date, id_card, phone, email,
                          dept_id, dept_name, title, position, specialty, education, avatar, is_expert, expert_price,
                          status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('2098255864065458177', 'E2026091100001', '任永星', 1, 1, '1997-09-18', '2026-09-11', '430726199709180511',
        '18888888888', '1888888@qq.com', '19580086', '全科医学科', '101', '2',
        '专业特专业特专业特专业特专业特专业特专业特', '1', NULL, 0, '0.00', 1, 'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_employee (id, emp_code, emp_name, emp_type, gender, birth_date, hire_date, id_card, phone, email,
                          dept_id, dept_name, title, position, specialty, education, avatar, is_expert, expert_price,
                          status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('2104147889868505089', 'E2026092700001', '字段变更探针账号', 1, 1, NULL, NULL, NULL, NULL, NULL, NULL, NULL,
        NULL, NULL, NULL, NULL, NULL, 0, '0.00', 1, 'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_employee (id, emp_code, emp_name, emp_type, gender, birth_date, hire_date, id_card, phone, email,
                          dept_id, dept_name, title, position, specialty, education, avatar, is_expert, expert_price,
                          status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('2104148167783088130', 'E2026092700002', '字段变更探针账号2', 1, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL,
        NULL, NULL, NULL, NULL, NULL, 0, '0.00', 1, 'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_employee (id, emp_code, emp_name, emp_type, gender, birth_date, hire_date, id_card, phone, email,
                          dept_id, dept_name, title, position, specialty, education, avatar, is_expert, expert_price,
                          status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('2104148441314623489', 'E2026092700003', '字段变更探针账号2', 1, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL,
        NULL, NULL, NULL, NULL, NULL, 0, '0.00', 1, 'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_employee (id, emp_code, emp_name, emp_type, gender, birth_date, hire_date, id_card, phone, email,
                          dept_id, dept_name, title, position, specialty, education, avatar, is_expert, expert_price,
                          status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('2104148493349158914', 'E2026092700004', '字段变更探针账号2', 1, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL,
        NULL, NULL, NULL, NULL, NULL, 0, '0.00', 1, 'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_employee (id, emp_code, emp_name, emp_type, gender, birth_date, hire_date, id_card, phone, email,
                          dept_id, dept_name, title, position, specialty, education, avatar, is_expert, expert_price,
                          status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('2104150910287605761', 'E2026092700005', '字段变更探针账号2', 1, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL,
        NULL, NULL, NULL, NULL, NULL, 0, '0.00', 1, 'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_employee (id, emp_code, emp_name, emp_type, gender, birth_date, hire_date, id_card, phone, email,
                          dept_id, dept_name, title, position, specialty, education, avatar, is_expert, expert_price,
                          status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('8900000000000023001', 'FIXB001', '压测医生甲', 1, 1, NULL, NULL, NULL, NULL, NULL, '8900000000000020001',
        '看板压测科（夹具）', '201', NULL, NULL, NULL, NULL, 0, '0.00', 0, 'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_employee (id, emp_code, emp_name, emp_type, gender, birth_date, hire_date, id_card, phone, email,
                          dept_id, dept_name, title, position, specialty, education, avatar, is_expert, expert_price,
                          status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('8900000000000023002', 'FIXB002', '压测医生乙', 1, 1, NULL, NULL, NULL, NULL, NULL, '8900000000000020001',
        '看板压测科（夹具）', '201', NULL, NULL, NULL, NULL, 0, '0.00', 0, 'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_employee (id, emp_code, emp_name, emp_type, gender, birth_date, hire_date, id_card, phone, email,
                          dept_id, dept_name, title, position, specialty, education, avatar, is_expert, expert_price,
                          status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('8900000000000023003', 'FIXB003', '压测医生丙', 1, 1, NULL, NULL, NULL, NULL, NULL, '8900000000000020001',
        '看板压测科（夹具）', '201', NULL, NULL, NULL, NULL, 0, '0.00', 0, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_employee (id, emp_code, emp_name, emp_type, gender, birth_date, hire_date, id_card, phone, email,
                          dept_id, dept_name, title, position, specialty, education, avatar, is_expert, expert_price,
                          status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('8900000000000023004', 'FIXB004', '压测医生丁', 1, 1, NULL, NULL, NULL, NULL, NULL, '8900000000000020001',
        '看板压测科（夹具）', '201', NULL, NULL, NULL, NULL, 0, '0.00', 0, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_employee (id, emp_code, emp_name, emp_type, gender, birth_date, hire_date, id_card, phone, email,
                          dept_id, dept_name, title, position, specialty, education, avatar, is_expert, expert_price,
                          status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('8900000000000030001', 'WB_SCOPE_PROBE', '权限验证探针医生', 1, 1, NULL, NULL, NULL, NULL, NULL, '19580003',
        '呼吸内科', NULL, NULL, NULL, NULL, NULL, 0, '0.00', 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_employee (id, emp_code, emp_name, emp_type, gender, birth_date, hire_date, id_card, phone, email,
                          dept_id, dept_name, title, position, specialty, education, avatar, is_expert, expert_price,
                          status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('8900000000000040001', 'DOC001', '医生甲', 1, 1, '1975-03-12', '2010-07-01', NULL, '13800000001', '',
        '19580086', '全科医学科', '401', '31', '呼吸系统常见病、慢阻肺', '', NULL, 1, '50.00', 1, 'admin', 'admin', 0,
        NULL, '1', '1');
INSERT INTO sys_employee (id, emp_code, emp_name, emp_type, gender, birth_date, hire_date, id_card, phone, email,
                          dept_id, dept_name, title, position, specialty, education, avatar, is_expert, expert_price,
                          status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('8900000000000040002', 'DOC002', '医生乙', 1, 2, '1980-09-08', '2013-07-01', NULL, '13800000002', NULL,
        '19580086', '全科医学科', '301', '31', '内分泌、糖尿病管理', NULL, NULL, 0, '0.00', 1, 'admin', 'admin', 0, NULL,
        '1', '1');
INSERT INTO sys_employee (id, emp_code, emp_name, emp_type, gender, birth_date, hire_date, id_card, phone, email,
                          dept_id, dept_name, title, position, specialty, education, avatar, is_expert, expert_price,
                          status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('8900000000000040003', 'DOC003', '医生丙', 1, 1, '1985-05-20', '2016-07-01', NULL, '13800000003', NULL,
        '19580086', '全科医学科', '201', '31', '心血管疾病、高血压', NULL, NULL, 0, '0.00', 1, 'admin', 'admin', 0, NULL,
        '1', '1');
INSERT INTO sys_employee (id, emp_code, emp_name, emp_type, gender, birth_date, hire_date, id_card, phone, email,
                          dept_id, dept_name, title, position, specialty, education, avatar, is_expert, expert_price,
                          status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('8900000000000040004', 'DOC004', '医生丁', 1, 2, '1988-11-02', '2019-07-01', NULL, '13800000004', NULL,
        '19580086', '全科医学科', '201', '31', '消化系统疾病', NULL, NULL, 0, '0.00', 1, 'admin', 'admin', 0, NULL, '1',
        '1');
INSERT INTO sys_employee (id, emp_code, emp_name, emp_type, gender, birth_date, hire_date, id_card, phone, email,
                          dept_id, dept_name, title, position, specialty, education, avatar, is_expert, expert_price,
                          status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('8900000000000040005', 'DOC005', '医生戊', 1, 1, '1992-02-18', '2022-07-01', NULL, '13800000005', NULL,
        '19580086', '全科医学科', '102', '31', '全科常见病、健康体检', NULL, NULL, 0, '0.00', 1, 'admin', 'admin', 0,
        NULL, '1', '1');
INSERT INTO sys_employee (id, emp_code, emp_name, emp_type, gender, birth_date, hire_date, id_card, phone, email,
                          dept_id, dept_name, title, position, specialty, education, avatar, is_expert, expert_price,
                          status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('8900000000000053001', 'PHAR001', '药师-周敏', 4, 2, NULL, NULL, NULL, NULL, NULL, '19580089', '药剂科', '202',
        '20', NULL, NULL, NULL, 0, '0.00', 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_employee (id, emp_code, emp_name, emp_type, gender, birth_date, hire_date, id_card, phone, email,
                          dept_id, dept_name, title, position, specialty, education, avatar, is_expert, expert_price,
                          status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('8900000000000053002', 'PHAR002', '药师-吴丽', 4, 2, NULL, NULL, NULL, NULL, NULL, '19580089', '药剂科', '104',
        '20', NULL, NULL, NULL, 0, '0.00', 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_employee (id, emp_code, emp_name, emp_type, gender, birth_date, hire_date, id_card, phone, email,
                          dept_id, dept_name, title, position, specialty, education, avatar, is_expert, expert_price,
                          status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('8900000000000070001', 'CS001', '客服-张静', 6, 0, NULL, '2026-10-03', NULL, '13800007001', NULL, '19580095',
        '客户服务中心', NULL, '客服专员', NULL, NULL, NULL, 0, '0.00', 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_employee (id, emp_code, emp_name, emp_type, gender, birth_date, hire_date, id_card, phone, email,
                          dept_id, dept_name, title, position, specialty, education, avatar, is_expert, expert_price,
                          status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('8900000000000070002', 'CS002', '客服-李娜', 6, 0, NULL, '2026-10-03', NULL, '13800007002', NULL, '19580095',
        '客户服务中心', NULL, '客服专员', NULL, NULL, NULL, 0, '0.00', 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_employee (id, emp_code, emp_name, emp_type, gender, birth_date, hire_date, id_card, phone, email,
                          dept_id, dept_name, title, position, specialty, education, avatar, is_expert, expert_price,
                          status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('8950000000000060001', 'VDP0001', '数据权限验证医生', 1, 1, NULL, NULL, NULL, NULL, NULL, '19580003', NULL,
        NULL, NULL, NULL, NULL, NULL, 0, '0.00', 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_employee (id, emp_code, emp_name, emp_type, gender, birth_date, hire_date, id_card, phone, email,
                          dept_id, dept_name, title, position, specialty, education, avatar, is_expert, expert_price,
                          status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('8950000000000060002', 'VDP0002', '数据权限验证护士', 1, 1, NULL, NULL, NULL, NULL, NULL, '19580003', NULL,
        NULL, NULL, NULL, NULL, NULL, 0, '0.00', 1, 'admin', 'admin', 0, NULL, '1', '1');
COMMIT;
SET
FOREIGN_KEY_CHECKS=1;

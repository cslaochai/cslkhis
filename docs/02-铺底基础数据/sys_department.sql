SET NAMES utf8mb4;
SET
FOREIGN_KEY_CHECKS=0;
START TRANSACTION;
INSERT INTO sys_department (id, dept_code, dept_name, dept_type, parent_id, sort_order, dept_icon, dept_desc,
                            contact_phone, location, dept_leader_id, is_open, status, create_by, update_by, del_flag,
                            remark, create_by_id, update_by_id)
VALUES ('1958001', '1001', '医院信息系统', '4', '0', 1, 'icon-neike', '综合内科诊疗中心', '010-88880001', '门诊楼3楼',
        NULL, 1, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_department (id, dept_code, dept_name, dept_type, parent_id, sort_order, dept_icon, dept_desc,
                            contact_phone, location, dept_leader_id, is_open, status, create_by, update_by, del_flag,
                            remark, create_by_id, update_by_id)
VALUES ('19580002', '100001', '内科系统', '4', '1958001', 1, 'icon-neike', '综合内科诊疗中心', '010-88880001',
        '门诊楼3楼', NULL, 1, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_department (id, dept_code, dept_name, dept_type, parent_id, sort_order, dept_icon, dept_desc,
                            contact_phone, location, dept_leader_id, is_open, status, create_by, update_by, del_flag,
                            remark, create_by_id, update_by_id)
VALUES ('19580003', '10000101', '呼吸内科', '1', '19580002', 1, 'icon-huxi', '呼吸道疾病诊疗', '010-88880101',
        '门诊楼3楼301', NULL, 1, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_department (id, dept_code, dept_name, dept_type, parent_id, sort_order, dept_icon, dept_desc,
                            contact_phone, location, dept_leader_id, is_open, status, create_by, update_by, del_flag,
                            remark, create_by_id, update_by_id)
VALUES ('19580004', '10000102', '消化内科', '1', '19580002', 2, 'icon-xiaohua', '胃肠道及肝胆疾病', '010-88880102',
        '门诊楼3楼302', NULL, 1, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_department (id, dept_code, dept_name, dept_type, parent_id, sort_order, dept_icon, dept_desc,
                            contact_phone, location, dept_leader_id, is_open, status, create_by, update_by, del_flag,
                            remark, create_by_id, update_by_id)
VALUES ('19580005', '10000103', '心血管内科', '1', '19580002', 3, 'icon-xinxueguan', '心脑血管疾病诊疗', '010-88880103',
        '门诊楼3楼303', NULL, 1, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_department (id, dept_code, dept_name, dept_type, parent_id, sort_order, dept_icon, dept_desc,
                            contact_phone, location, dept_leader_id, is_open, status, create_by, update_by, del_flag,
                            remark, create_by_id, update_by_id)
VALUES ('19580006', '10000104', '神经内科', '1', '19580002', 4, 'icon-shenjing', '神经系统疾病诊疗', '010-88880104',
        '门诊楼3楼304', NULL, 1, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_department (id, dept_code, dept_name, dept_type, parent_id, sort_order, dept_icon, dept_desc,
                            contact_phone, location, dept_leader_id, is_open, status, create_by, update_by, del_flag,
                            remark, create_by_id, update_by_id)
VALUES ('19580007', '10000105', '内分泌科', '1', '19580002', 5, 'icon-neifenmi', '糖尿病及甲状腺疾病', '010-88880105',
        '门诊楼3楼305', NULL, 1, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_department (id, dept_code, dept_name, dept_type, parent_id, sort_order, dept_icon, dept_desc,
                            contact_phone, location, dept_leader_id, is_open, status, create_by, update_by, del_flag,
                            remark, create_by_id, update_by_id)
VALUES ('19580008', '10000106', '肾内科', '1', '19580002', 6, 'icon-shenbing', '肾脏疾病及透析', '010-88880106',
        '住院部5楼', NULL, 1, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_department (id, dept_code, dept_name, dept_type, parent_id, sort_order, dept_icon, dept_desc,
                            contact_phone, location, dept_leader_id, is_open, status, create_by, update_by, del_flag,
                            remark, create_by_id, update_by_id)
VALUES ('19580009', '10000107', '血液内科', '1', '19580002', 7, 'icon-xueye', '血液系统疾病', '010-88880107',
        '住院部5楼', NULL, 1, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_department (id, dept_code, dept_name, dept_type, parent_id, sort_order, dept_icon, dept_desc,
                            contact_phone, location, dept_leader_id, is_open, status, create_by, update_by, del_flag,
                            remark, create_by_id, update_by_id)
VALUES ('19580010', '10000108', '风湿免疫科', '1', '19580002', 8, 'icon-fengshi', '风湿及自身免疫疾病', '010-88880108',
        '门诊楼3楼308', NULL, 1, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_department (id, dept_code, dept_name, dept_type, parent_id, sort_order, dept_icon, dept_desc,
                            contact_phone, location, dept_leader_id, is_open, status, create_by, update_by, del_flag,
                            remark, create_by_id, update_by_id)
VALUES ('19580011', '10000109', '老年病科', '1', '19580002', 9, 'icon-laonian', '老年综合评估与治疗', '010-88880109',
        '住院部6楼', NULL, 1, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_department (id, dept_code, dept_name, dept_type, parent_id, sort_order, dept_icon, dept_desc,
                            contact_phone, location, dept_leader_id, is_open, status, create_by, update_by, del_flag,
                            remark, create_by_id, update_by_id)
VALUES ('19580012', '100002', '外科系统', '4', '1958001', 2, 'icon-waike', '综合外科诊疗中心', '010-88880002',
        '门诊楼4楼', NULL, 1, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_department (id, dept_code, dept_name, dept_type, parent_id, sort_order, dept_icon, dept_desc,
                            contact_phone, location, dept_leader_id, is_open, status, create_by, update_by, del_flag,
                            remark, create_by_id, update_by_id)
VALUES ('19580013', '10000201', '普通外科', '1', '19580012', 1, 'icon-puwai', '胃肠及甲状腺乳腺外科', '010-88880201',
        '门诊楼4楼401', NULL, 1, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_department (id, dept_code, dept_name, dept_type, parent_id, sort_order, dept_icon, dept_desc,
                            contact_phone, location, dept_leader_id, is_open, status, create_by, update_by, del_flag,
                            remark, create_by_id, update_by_id)
VALUES ('19580014', '10000202', '骨科', '1', '19580012', 2, 'icon-guke', '骨关节及脊柱创伤', '010-88880202',
        '门诊楼4楼402', NULL, 1, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_department (id, dept_code, dept_name, dept_type, parent_id, sort_order, dept_icon, dept_desc,
                            contact_phone, location, dept_leader_id, is_open, status, create_by, update_by, del_flag,
                            remark, create_by_id, update_by_id)
VALUES ('19580015', '10000203', '神经外科', '1', '19580012', 3, 'icon-shenjingwaike', '颅脑及脊髓外科', '010-88880203',
        '门诊楼4楼403', NULL, 1, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_department (id, dept_code, dept_name, dept_type, parent_id, sort_order, dept_icon, dept_desc,
                            contact_phone, location, dept_leader_id, is_open, status, create_by, update_by, del_flag,
                            remark, create_by_id, update_by_id)
VALUES ('19580016', '10000204', '心胸外科', '1', '19580012', 4, 'icon-xinxiong', '心脏及胸腔外科手术', '010-88880204',
        '门诊楼4楼404', NULL, 1, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_department (id, dept_code, dept_name, dept_type, parent_id, sort_order, dept_icon, dept_desc,
                            contact_phone, location, dept_leader_id, is_open, status, create_by, update_by, del_flag,
                            remark, create_by_id, update_by_id)
VALUES ('19580017', '10000205', '泌尿外科', '1', '19580012', 5, 'icon-miniao', '泌尿系统及结石', '010-88880205',
        '门诊楼4楼405', NULL, 1, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_department (id, dept_code, dept_name, dept_type, parent_id, sort_order, dept_icon, dept_desc,
                            contact_phone, location, dept_leader_id, is_open, status, create_by, update_by, del_flag,
                            remark, create_by_id, update_by_id)
VALUES ('19580018', '10000206', '烧伤整形外科', '1', '19580012', 6, 'icon-shaoshang', '烧伤及创面修复', '010-88880206',
        '门诊楼4楼406', NULL, 1, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_department (id, dept_code, dept_name, dept_type, parent_id, sort_order, dept_icon, dept_desc,
                            contact_phone, location, dept_leader_id, is_open, status, create_by, update_by, del_flag,
                            remark, create_by_id, update_by_id)
VALUES ('19580019', '10000207', '血管外科', '1', '19580012', 7, 'icon-xueguanwaike', '动静脉血管疾病', '010-88880207',
        '门诊楼4楼407', NULL, 1, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_department (id, dept_code, dept_name, dept_type, parent_id, sort_order, dept_icon, dept_desc,
                            contact_phone, location, dept_leader_id, is_open, status, create_by, update_by, del_flag,
                            remark, create_by_id, update_by_id)
VALUES ('19580020', '10000208', '肝胆外科', '1', '19580012', 8, 'icon-gandan', '肝胆胰脾外科', '010-88880208',
        '门诊楼4楼408', NULL, 1, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_department (id, dept_code, dept_name, dept_type, parent_id, sort_order, dept_icon, dept_desc,
                            contact_phone, location, dept_leader_id, is_open, status, create_by, update_by, del_flag,
                            remark, create_by_id, update_by_id)
VALUES ('19580021', '100003', '妇产科系统', '4', '1958001', 3, 'icon-fuchan', '妇女儿童诊疗中心', '010-88880003',
        '门诊楼5楼', NULL, 1, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_department (id, dept_code, dept_name, dept_type, parent_id, sort_order, dept_icon, dept_desc,
                            contact_phone, location, dept_leader_id, is_open, status, create_by, update_by, del_flag,
                            remark, create_by_id, update_by_id)
VALUES ('19580022', '10000301', '妇科', '1', '19580021', 1, 'icon-fuke', '女性生殖系统疾病', '010-88880301',
        '门诊楼5楼501', NULL, 1, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_department (id, dept_code, dept_name, dept_type, parent_id, sort_order, dept_icon, dept_desc,
                            contact_phone, location, dept_leader_id, is_open, status, create_by, update_by, del_flag,
                            remark, create_by_id, update_by_id)
VALUES ('19580023', '10000302', '产科', '1', '19580021', 2, 'icon-chanke', '孕期保健及分娩', '010-88880302',
        '门诊楼5楼502', NULL, 1, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_department (id, dept_code, dept_name, dept_type, parent_id, sort_order, dept_icon, dept_desc,
                            contact_phone, location, dept_leader_id, is_open, status, create_by, update_by, del_flag,
                            remark, create_by_id, update_by_id)
VALUES ('19580024', '10000303', '生殖医学中心', '1', '19580021', 3, 'icon-shengzhi', '不孕不育及试管婴儿',
        '010-88880303', '门诊楼5楼503', NULL, 1, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_department (id, dept_code, dept_name, dept_type, parent_id, sort_order, dept_icon, dept_desc,
                            contact_phone, location, dept_leader_id, is_open, status, create_by, update_by, del_flag,
                            remark, create_by_id, update_by_id)
VALUES ('19580025', '100004', '儿科系统', '4', '1958001', 4, 'icon-erke', '儿童综合诊疗中心', '010-88880004',
        '门诊楼6楼', NULL, 1, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_department (id, dept_code, dept_name, dept_type, parent_id, sort_order, dept_icon, dept_desc,
                            contact_phone, location, dept_leader_id, is_open, status, create_by, update_by, del_flag,
                            remark, create_by_id, update_by_id)
VALUES ('19580026', '10000401', '小儿内科', '1', '19580025', 1, 'icon-xiaoerneike', '儿童常见病诊疗', '010-88880401',
        '门诊楼6楼601', NULL, 1, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_department (id, dept_code, dept_name, dept_type, parent_id, sort_order, dept_icon, dept_desc,
                            contact_phone, location, dept_leader_id, is_open, status, create_by, update_by, del_flag,
                            remark, create_by_id, update_by_id)
VALUES ('19580027', '10000402', '小儿外科', '1', '19580025', 2, 'icon-xiaoerwaike', '儿童外科手术', '010-88880402',
        '门诊楼6楼602', NULL, 1, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_department (id, dept_code, dept_name, dept_type, parent_id, sort_order, dept_icon, dept_desc,
                            contact_phone, location, dept_leader_id, is_open, status, create_by, update_by, del_flag,
                            remark, create_by_id, update_by_id)
VALUES ('19580028', '10000403', '新生儿科', '1', '19580025', 3, 'icon-xinshenger', '新生儿重症监护', '010-88880403',
        '住院部7楼', NULL, 1, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_department (id, dept_code, dept_name, dept_type, parent_id, sort_order, dept_icon, dept_desc,
                            contact_phone, location, dept_leader_id, is_open, status, create_by, update_by, del_flag,
                            remark, create_by_id, update_by_id)
VALUES ('19580029', '100005', '五官及皮肤科', '1', '1958001', 5, 'icon-wuguan', '五官及皮肤专科', '010-88880005',
        '门诊楼2楼', NULL, 1, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_department (id, dept_code, dept_name, dept_type, parent_id, sort_order, dept_icon, dept_desc,
                            contact_phone, location, dept_leader_id, is_open, status, create_by, update_by, del_flag,
                            remark, create_by_id, update_by_id)
VALUES ('19580030', '10000501', '眼科', '1', '19580029', 1, 'icon-yanke', '眼部疾病诊疗', '010-88880501',
        '门诊楼2楼201', NULL, 1, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_department (id, dept_code, dept_name, dept_type, parent_id, sort_order, dept_icon, dept_desc,
                            contact_phone, location, dept_leader_id, is_open, status, create_by, update_by, del_flag,
                            remark, create_by_id, update_by_id)
VALUES ('19580031', '10000502', '耳鼻喉科', '1', '19580029', 2, 'icon-erbihou', '耳鼻咽喉疾病', '010-88880502',
        '门诊楼2楼202', NULL, 1, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_department (id, dept_code, dept_name, dept_type, parent_id, sort_order, dept_icon, dept_desc,
                            contact_phone, location, dept_leader_id, is_open, status, create_by, update_by, del_flag,
                            remark, create_by_id, update_by_id)
VALUES ('19580032', '10000503', '口腔科', '1', '19580029', 3, 'icon-kouqiang', '牙齿及口腔疾病', '010-88880503',
        '门诊楼2楼203', NULL, 1, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_department (id, dept_code, dept_name, dept_type, parent_id, sort_order, dept_icon, dept_desc,
                            contact_phone, location, dept_leader_id, is_open, status, create_by, update_by, del_flag,
                            remark, create_by_id, update_by_id)
VALUES ('19580033', '10000504', '皮肤科', '1', '19580029', 4, 'icon-pifu', '皮肤及性病诊疗', '010-88880504',
        '门诊楼2楼204', NULL, 1, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_department (id, dept_code, dept_name, dept_type, parent_id, sort_order, dept_icon, dept_desc,
                            contact_phone, location, dept_leader_id, is_open, status, create_by, update_by, del_flag,
                            remark, create_by_id, update_by_id)
VALUES ('19580034', '100006', '医技科室', '2', '1958001', 6, 'icon-yiji', '辅助检查及治疗中心', '010-88880006',
        '医技楼1楼', NULL, 1, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_department (id, dept_code, dept_name, dept_type, parent_id, sort_order, dept_icon, dept_desc,
                            contact_phone, location, dept_leader_id, is_open, status, create_by, update_by, del_flag,
                            remark, create_by_id, update_by_id)
VALUES ('19580035', '10000601', '医学检验科', '2', '19580034', 1, 'icon-jianyan', '血液及体液检验', '010-88880601',
        '医技楼1楼101', '897001009', 1, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_department (id, dept_code, dept_name, dept_type, parent_id, sort_order, dept_icon, dept_desc,
                            contact_phone, location, dept_leader_id, is_open, status, create_by, update_by, del_flag,
                            remark, create_by_id, update_by_id)
VALUES ('19580036', '10000602', '放射科', '2', '19580034', 2, 'icon-fangshe', 'X光/CT/核磁共振', '010-88880602',
        '医技楼2楼', NULL, 1, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_department (id, dept_code, dept_name, dept_type, parent_id, sort_order, dept_icon, dept_desc,
                            contact_phone, location, dept_leader_id, is_open, status, create_by, update_by, del_flag,
                            remark, create_by_id, update_by_id)
VALUES ('19580037', '10000603', '超声医学科', '2', '19580034', 3, 'icon-chaosheng', 'B超及彩超检查', '010-88880603',
        '医技楼1楼103', '897001010', 1, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_department (id, dept_code, dept_name, dept_type, parent_id, sort_order, dept_icon, dept_desc,
                            contact_phone, location, dept_leader_id, is_open, status, create_by, update_by, del_flag,
                            remark, create_by_id, update_by_id)
VALUES ('19580038', '10000604', '病理科', '2', '19580034', 4, 'icon-bingli', '组织病理学诊断', '010-88880604',
        '医技楼3楼', NULL, 1, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_department (id, dept_code, dept_name, dept_type, parent_id, sort_order, dept_icon, dept_desc,
                            contact_phone, location, dept_leader_id, is_open, status, create_by, update_by, del_flag,
                            remark, create_by_id, update_by_id)
VALUES ('19580039', '10000605', '麻醉科', '2', '19580034', 5, 'icon-mazui', '手术麻醉及镇痛', '010-88880605', '手术区',
        NULL, 1, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_department (id, dept_code, dept_name, dept_type, parent_id, sort_order, dept_icon, dept_desc,
                            contact_phone, location, dept_leader_id, is_open, status, create_by, update_by, del_flag,
                            remark, create_by_id, update_by_id)
VALUES ('19580040', '10000606', '输血科', '2', '19580034', 6, 'icon-shuxue', '临床用血管理', '010-88880606',
        '医技楼1楼106', NULL, 1, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_department (id, dept_code, dept_name, dept_type, parent_id, sort_order, dept_icon, dept_desc,
                            contact_phone, location, dept_leader_id, is_open, status, create_by, update_by, del_flag,
                            remark, create_by_id, update_by_id)
VALUES ('19580041', '10000607', '康复医学科', '2', '19580034', 7, 'icon-kangfu', '物理及康复治疗', '010-88880607',
        '康复楼1楼', NULL, 1, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_department (id, dept_code, dept_name, dept_type, parent_id, sort_order, dept_icon, dept_desc,
                            contact_phone, location, dept_leader_id, is_open, status, create_by, update_by, del_flag,
                            remark, create_by_id, update_by_id)
VALUES ('19580042', '100007', '药剂科', '3', '1958001', 7, 'icon-yaofang', '药品调剂及管理', '010-88880007',
        '门诊楼1楼', NULL, 1, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_department (id, dept_code, dept_name, dept_type, parent_id, sort_order, dept_icon, dept_desc,
                            contact_phone, location, dept_leader_id, is_open, status, create_by, update_by, del_flag,
                            remark, create_by_id, update_by_id)
VALUES ('19580043', '10000701', '门诊西药房', '3', '19580042', 1, 'icon-xiyao', '门诊处方发药', '010-88880701',
        '门诊楼1楼101', NULL, 1, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_department (id, dept_code, dept_name, dept_type, parent_id, sort_order, dept_icon, dept_desc,
                            contact_phone, location, dept_leader_id, is_open, status, create_by, update_by, del_flag,
                            remark, create_by_id, update_by_id)
VALUES ('19580044', '10000702', '门诊中药房', '3', '19580042', 2, 'icon-zhongyao', '中药饮片及颗粒', '010-88880702',
        '门诊楼1楼102', NULL, 1, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_department (id, dept_code, dept_name, dept_type, parent_id, sort_order, dept_icon, dept_desc,
                            contact_phone, location, dept_leader_id, is_open, status, create_by, update_by, del_flag,
                            remark, create_by_id, update_by_id)
VALUES ('19580045', '10000703', '住院药房', '3', '19580042', 3, 'icon-zhuyuanyaofang', '住院患者摆药', '010-88880703',
        '住院部1楼', NULL, 1, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_department (id, dept_code, dept_name, dept_type, parent_id, sort_order, dept_icon, dept_desc,
                            contact_phone, location, dept_leader_id, is_open, status, create_by, update_by, del_flag,
                            remark, create_by_id, update_by_id)
VALUES ('19580046', '10000704', '静脉配液中心', '3', '19580042', 4, 'icon-peiyao', '集中静脉药物配置', '010-88880704',
        '住院部1楼', NULL, 1, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_department (id, dept_code, dept_name, dept_type, parent_id, sort_order, dept_icon, dept_desc,
                            contact_phone, location, dept_leader_id, is_open, status, create_by, update_by, del_flag,
                            remark, create_by_id, update_by_id)
VALUES ('19580047', '100008', '急危重症中心', '1', '1958001', 8, 'icon-jizhen', '急诊及重症医学', '010-88880008',
        '急诊楼', NULL, 1, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_department (id, dept_code, dept_name, dept_type, parent_id, sort_order, dept_icon, dept_desc,
                            contact_phone, location, dept_leader_id, is_open, status, create_by, update_by, del_flag,
                            remark, create_by_id, update_by_id)
VALUES ('19580048', '10000801', '急诊内科', '1', '19580047', 1, 'icon-jizhen-neike', '24小时急诊内科', '010-88880801',
        '急诊楼1楼', NULL, 1, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_department (id, dept_code, dept_name, dept_type, parent_id, sort_order, dept_icon, dept_desc,
                            contact_phone, location, dept_leader_id, is_open, status, create_by, update_by, del_flag,
                            remark, create_by_id, update_by_id)
VALUES ('19580049', '10000802', '急诊外科', '1', '19580047', 2, 'icon-jizhen-waike', '24小时急诊外科', '010-88880802',
        '急诊楼1楼', NULL, 1, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_department (id, dept_code, dept_name, dept_type, parent_id, sort_order, dept_icon, dept_desc,
                            contact_phone, location, dept_leader_id, is_open, status, create_by, update_by, del_flag,
                            remark, create_by_id, update_by_id)
VALUES ('19580050', '10000803', '重症医学科(ICU)', '1', '19580047', 3, 'icon-icu', '重症监护治疗', '010-88880803',
        '住院部8楼', NULL, 1, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_department (id, dept_code, dept_name, dept_type, parent_id, sort_order, dept_icon, dept_desc,
                            contact_phone, location, dept_leader_id, is_open, status, create_by, update_by, del_flag,
                            remark, create_by_id, update_by_id)
VALUES ('19580051', '10000804', '急诊儿科', '1', '19580047', 4, 'icon-jizhen-erke', '24小时儿童急诊', '010-88880804',
        '急诊楼2楼', NULL, 1, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_department (id, dept_code, dept_name, dept_type, parent_id, sort_order, dept_icon, dept_desc,
                            contact_phone, location, dept_leader_id, is_open, status, create_by, update_by, del_flag,
                            remark, create_by_id, update_by_id)
VALUES ('19580052', '100009', '中医科系统', '1', '1958001', 9, 'icon-zhongyi', '传统中医药诊疗中心', '010-88880009',
        '门诊楼7楼', NULL, 1, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_department (id, dept_code, dept_name, dept_type, parent_id, sort_order, dept_icon, dept_desc,
                            contact_phone, location, dept_leader_id, is_open, status, create_by, update_by, del_flag,
                            remark, create_by_id, update_by_id)
VALUES ('19580053', '10000901', '中医内科', '1', '19580052', 1, 'icon-zhongyi-neike', '中医辨证论治内科疾病',
        '010-88880901', '门诊楼7楼701', NULL, 1, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_department (id, dept_code, dept_name, dept_type, parent_id, sort_order, dept_icon, dept_desc,
                            contact_phone, location, dept_leader_id, is_open, status, create_by, update_by, del_flag,
                            remark, create_by_id, update_by_id)
VALUES ('19580054', '10000902', '中医骨伤科', '1', '19580052', 2, 'icon-zhongyi-gushang', '中医正骨及推拿理疗',
        '010-88880902', '门诊楼7楼702', NULL, 1, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_department (id, dept_code, dept_name, dept_type, parent_id, sort_order, dept_icon, dept_desc,
                            contact_phone, location, dept_leader_id, is_open, status, create_by, update_by, del_flag,
                            remark, create_by_id, update_by_id)
VALUES ('19580055', '10000903', '针灸推拿科', '1', '19580052', 3, 'icon-zhenjiu', '针灸及经络理疗', '010-88880903',
        '门诊楼7楼703', NULL, 1, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_department (id, dept_code, dept_name, dept_type, parent_id, sort_order, dept_icon, dept_desc,
                            contact_phone, location, dept_leader_id, is_open, status, create_by, update_by, del_flag,
                            remark, create_by_id, update_by_id)
VALUES ('19580056', '10000904', '中医妇科', '1', '19580052', 4, 'icon-zhongyi-fuke', '中医妇科调理', '010-88880904',
        '门诊楼7楼704', NULL, 1, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_department (id, dept_code, dept_name, dept_type, parent_id, sort_order, dept_icon, dept_desc,
                            contact_phone, location, dept_leader_id, is_open, status, create_by, update_by, del_flag,
                            remark, create_by_id, update_by_id)
VALUES ('19580057', '10000905', '中医儿科', '1', '19580052', 5, 'icon-zhongyi-erke', '小儿推拿及中医调理',
        '010-88880905', '门诊楼7楼705', NULL, 1, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_department (id, dept_code, dept_name, dept_type, parent_id, sort_order, dept_icon, dept_desc,
                            contact_phone, location, dept_leader_id, is_open, status, create_by, update_by, del_flag,
                            remark, create_by_id, update_by_id)
VALUES ('19580058', '100010', '肿瘤科系统', '4', '1958001', 10, 'icon-zhongliu', '肿瘤综合诊疗中心', '010-88880010',
        '肿瘤中心楼', NULL, 1, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_department (id, dept_code, dept_name, dept_type, parent_id, sort_order, dept_icon, dept_desc,
                            contact_phone, location, dept_leader_id, is_open, status, create_by, update_by, del_flag,
                            remark, create_by_id, update_by_id)
VALUES ('19580059', '10001001', '肿瘤内科', '4', '19580058', 1, 'icon-zhongliu-neike', '肿瘤化疗及靶向治疗',
        '010-88881001', '肿瘤中心楼2楼', NULL, 1, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_department (id, dept_code, dept_name, dept_type, parent_id, sort_order, dept_icon, dept_desc,
                            contact_phone, location, dept_leader_id, is_open, status, create_by, update_by, del_flag,
                            remark, create_by_id, update_by_id)
VALUES ('19580060', '10001002', '肿瘤外科', '4', '19580058', 2, 'icon-zhongliu-waike', '肿瘤切除及微创手术',
        '010-88881002', '肿瘤中心楼3楼', NULL, 1, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_department (id, dept_code, dept_name, dept_type, parent_id, sort_order, dept_icon, dept_desc,
                            contact_phone, location, dept_leader_id, is_open, status, create_by, update_by, del_flag,
                            remark, create_by_id, update_by_id)
VALUES ('19580061', '10001003', '放射治疗科', '2', '19580058', 3, 'icon-fangliao', '直线加速器及放疗', '010-88881003',
        '肿瘤中心楼B1', NULL, 1, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_department (id, dept_code, dept_name, dept_type, parent_id, sort_order, dept_icon, dept_desc,
                            contact_phone, location, dept_leader_id, is_open, status, create_by, update_by, del_flag,
                            remark, create_by_id, update_by_id)
VALUES ('19580062', '10001004', '安宁疗护病房', '4', '19580058', 4, 'icon-anning', '临终关怀及姑息治疗', '010-88881004',
        '肿瘤中心楼5楼', NULL, 1, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_department (id, dept_code, dept_name, dept_type, parent_id, sort_order, dept_icon, dept_desc,
                            contact_phone, location, dept_leader_id, is_open, status, create_by, update_by, del_flag,
                            remark, create_by_id, update_by_id)
VALUES ('19580063', '100011', '精神心理科', '1', '1958001', 11, 'icon-xinli', '精神及心理健康中心', '010-88880011',
        '门诊楼8楼', NULL, 1, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_department (id, dept_code, dept_name, dept_type, parent_id, sort_order, dept_icon, dept_desc,
                            contact_phone, location, dept_leader_id, is_open, status, create_by, update_by, del_flag,
                            remark, create_by_id, update_by_id)
VALUES ('19580064', '10001101', '精神科门诊', '1', '19580063', 1, 'icon-jingshen', '精神疾病诊疗', '010-88881101',
        '门诊楼8楼801', NULL, 1, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_department (id, dept_code, dept_name, dept_type, parent_id, sort_order, dept_icon, dept_desc,
                            contact_phone, location, dept_leader_id, is_open, status, create_by, update_by, del_flag,
                            remark, create_by_id, update_by_id)
VALUES ('19580065', '10001102', '临床心理科', '1', '19580063', 2, 'icon-xinli-zixun', '心理咨询及测评', '010-88881102',
        '门诊楼8楼802', NULL, 1, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_department (id, dept_code, dept_name, dept_type, parent_id, sort_order, dept_icon, dept_desc,
                            contact_phone, location, dept_leader_id, is_open, status, create_by, update_by, del_flag,
                            remark, create_by_id, update_by_id)
VALUES ('19580066', '10001103', '睡眠医学中心', '1', '19580063', 3, 'icon-shuimian', '睡眠障碍及多导睡眠监测',
        '010-88881103', '门诊楼8楼803', NULL, 1, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_department (id, dept_code, dept_name, dept_type, parent_id, sort_order, dept_icon, dept_desc,
                            contact_phone, location, dept_leader_id, is_open, status, create_by, update_by, del_flag,
                            remark, create_by_id, update_by_id)
VALUES ('19580067', '100012', '感染疾病科', '1', '1958001', 12, 'icon-ganran', '传染及感染性疾病诊疗', '010-88880012',
        '感染楼', NULL, 1, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_department (id, dept_code, dept_name, dept_type, parent_id, sort_order, dept_icon, dept_desc,
                            contact_phone, location, dept_leader_id, is_open, status, create_by, update_by, del_flag,
                            remark, create_by_id, update_by_id)
VALUES ('19580068', '10001201', '发热门诊', '1', '19580067', 1, 'icon-fare', '24小时发热筛查', '010-88881201',
        '感染楼1楼', NULL, 1, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_department (id, dept_code, dept_name, dept_type, parent_id, sort_order, dept_icon, dept_desc,
                            contact_phone, location, dept_leader_id, is_open, status, create_by, update_by, del_flag,
                            remark, create_by_id, update_by_id)
VALUES ('19580069', '10001202', '肠道门诊', '1', '19580067', 2, 'icon-changdao', '腹泻及肠道传染病', '010-88881202',
        '感染楼1楼', NULL, 1, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_department (id, dept_code, dept_name, dept_type, parent_id, sort_order, dept_icon, dept_desc,
                            contact_phone, location, dept_leader_id, is_open, status, create_by, update_by, del_flag,
                            remark, create_by_id, update_by_id)
VALUES ('19580070', '10001203', '肝病科', '1', '19580067', 3, 'icon-ganbing', '病毒性肝炎及肝病', '010-88881203',
        '感染楼2楼', NULL, 1, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_department (id, dept_code, dept_name, dept_type, parent_id, sort_order, dept_icon, dept_desc,
                            contact_phone, location, dept_leader_id, is_open, status, create_by, update_by, del_flag,
                            remark, create_by_id, update_by_id)
VALUES ('19580071', '100013', '健康管理中心', '1', '1958001', 13, 'icon-tijian', '健康体检及预防保健', '010-88880013',
        '体检楼', NULL, 1, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_department (id, dept_code, dept_name, dept_type, parent_id, sort_order, dept_icon, dept_desc,
                            contact_phone, location, dept_leader_id, is_open, status, create_by, update_by, del_flag,
                            remark, create_by_id, update_by_id)
VALUES ('19580072', '10001301', '普通体检科', '1', '19580071', 1, 'icon-putong-tijian', '常规入职及年度体检',
        '010-88881301', '体检楼1楼', NULL, 1, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_department (id, dept_code, dept_name, dept_type, parent_id, sort_order, dept_icon, dept_desc,
                            contact_phone, location, dept_leader_id, is_open, status, create_by, update_by, del_flag,
                            remark, create_by_id, update_by_id)
VALUES ('19580073', '10001302', 'VIP体检中心', '1', '19580071', 2, 'icon-vip-tijian', '高端定制体检服务',
        '010-88881302', '体检楼2楼', NULL, 1, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_department (id, dept_code, dept_name, dept_type, parent_id, sort_order, dept_icon, dept_desc,
                            contact_phone, location, dept_leader_id, is_open, status, create_by, update_by, del_flag,
                            remark, create_by_id, update_by_id)
VALUES ('19580074', '10001303', '预防保健科', '1', '19580071', 3, 'icon-yufang', '疫苗接种及慢病管理', '010-88881303',
        '体检楼1楼', NULL, 1, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_department (id, dept_code, dept_name, dept_type, parent_id, sort_order, dept_icon, dept_desc,
                            contact_phone, location, dept_leader_id, is_open, status, create_by, update_by, del_flag,
                            remark, create_by_id, update_by_id)
VALUES ('19580075', '100014', '行政后勤部', '5', '1958001', 14, 'icon-houqin', '医院行政及后勤保障', '010-88880014',
        '行政楼', NULL, 1, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_department (id, dept_code, dept_name, dept_type, parent_id, sort_order, dept_icon, dept_desc,
                            contact_phone, location, dept_leader_id, is_open, status, create_by, update_by, del_flag,
                            remark, create_by_id, update_by_id)
VALUES ('19580076', '10001401', '院办', '5', '19580075', 1, 'icon-yuanban', '医院综合行政办公', '010-88881401',
        '行政楼2楼', NULL, 1, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_department (id, dept_code, dept_name, dept_type, parent_id, sort_order, dept_icon, dept_desc,
                            contact_phone, location, dept_leader_id, is_open, status, create_by, update_by, del_flag,
                            remark, create_by_id, update_by_id)
VALUES ('19580077', '10001402', '医务科', '5', '19580075', 2, 'icon-yiwu', '医疗质量与安全管理', '010-88881402',
        '行政楼2楼', NULL, 1, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_department (id, dept_code, dept_name, dept_type, parent_id, sort_order, dept_icon, dept_desc,
                            contact_phone, location, dept_leader_id, is_open, status, create_by, update_by, del_flag,
                            remark, create_by_id, update_by_id)
VALUES ('19580078', '10001403', '护理部', '5', '19580075', 3, 'icon-huli', '全院护理质量管理', '010-88881403',
        '行政楼2楼', NULL, 1, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_department (id, dept_code, dept_name, dept_type, parent_id, sort_order, dept_icon, dept_desc,
                            contact_phone, location, dept_leader_id, is_open, status, create_by, update_by, del_flag,
                            remark, create_by_id, update_by_id)
VALUES ('19580079', '10001404', '财务科', '5', '19580075', 4, 'icon-caiwu', '医院财务及收费管理', '010-88881404',
        '行政楼3楼', NULL, 1, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_department (id, dept_code, dept_name, dept_type, parent_id, sort_order, dept_icon, dept_desc,
                            contact_phone, location, dept_leader_id, is_open, status, create_by, update_by, del_flag,
                            remark, create_by_id, update_by_id)
VALUES ('19580080', '10001405', '医保科', '5', '19580075', 5, 'icon-caiwu', '医保政策及结算管理', '010-88881405',
        '行政楼3楼', NULL, 1, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_department (id, dept_code, dept_name, dept_type, parent_id, sort_order, dept_icon, dept_desc,
                            contact_phone, location, dept_leader_id, is_open, status, create_by, update_by, del_flag,
                            remark, create_by_id, update_by_id)
VALUES ('19580081', '10001406', '信息科', '5', '19580075', 6, 'icon-caiwu', 'HIS系统及网络维护', '010-88881406',
        '行政楼4楼', NULL, 1, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_department (id, dept_code, dept_name, dept_type, parent_id, sort_order, dept_icon, dept_desc,
                            contact_phone, location, dept_leader_id, is_open, status, create_by, update_by, del_flag,
                            remark, create_by_id, update_by_id)
VALUES ('19580082', '10001407', '设备科', '5', '19580075', 7, 'icon-caiwu', '医疗器械采购与维修', '010-88881407',
        '行政楼4楼', NULL, 1, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_department (id, dept_code, dept_name, dept_type, parent_id, sort_order, dept_icon, dept_desc,
                            contact_phone, location, dept_leader_id, is_open, status, create_by, update_by, del_flag,
                            remark, create_by_id, update_by_id)
VALUES ('19580083', '10001408', '保卫科', '5', '19580075', 8, 'icon-caiwu', '医院安全及消防管理', '010-88881408',
        '行政楼1楼', NULL, 1, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_department (id, dept_code, dept_name, dept_type, parent_id, sort_order, dept_icon, dept_desc,
                            contact_phone, location, dept_leader_id, is_open, status, create_by, update_by, del_flag,
                            remark, create_by_id, update_by_id)
VALUES ('19580084', '10001409', '总务科', '5', '19580075', 9, 'icon-caiwu', '水电维修及物资供应', '010-88881409',
        '行政楼1楼', NULL, 1, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_department (id, dept_code, dept_name, dept_type, parent_id, sort_order, dept_icon, dept_desc,
                            contact_phone, location, dept_leader_id, is_open, status, create_by, update_by, del_flag,
                            remark, create_by_id, update_by_id)
VALUES ('19580085', '10001410', '病案室', '5', '19580075', 10, 'icon-caiwu', '病历归档及复印', '010-88881410',
        '门诊楼1楼', NULL, 1, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_department (id, dept_code, dept_name, dept_type, parent_id, sort_order, dept_icon, dept_desc,
                            contact_phone, location, dept_leader_id, is_open, status, create_by, update_by, del_flag,
                            remark, create_by_id, update_by_id)
VALUES ('19580086', '10000110', '全科医学科', '1', '19580002', 10, 'icon-quanke', '常见病首诊及慢病管理',
        '010-88880110', '门诊楼3楼310', NULL, 1, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_department (id, dept_code, dept_name, dept_type, parent_id, sort_order, dept_icon, dept_desc,
                            contact_phone, location, dept_leader_id, is_open, status, create_by, update_by, del_flag,
                            remark, create_by_id, update_by_id)
VALUES ('19580087', '10000209', '甲乳外科', '1', '19580012', 9, 'icon-caiwu', '甲状腺及乳腺外科', '010-88880209',
        '门诊楼4楼409', NULL, 1, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_department (id, dept_code, dept_name, dept_type, parent_id, sort_order, dept_icon, dept_desc,
                            contact_phone, location, dept_leader_id, is_open, status, create_by, update_by, del_flag,
                            remark, create_by_id, update_by_id)
VALUES ('19580088', '10000210', '肛肠外科', '1', '19580012', 10, 'icon-caiwu', '痔疮及肠道良性疾病', '010-88880210',
        '门诊楼4楼410', NULL, 1, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_department (id, dept_code, dept_name, dept_type, parent_id, sort_order, dept_icon, dept_desc,
                            contact_phone, location, dept_leader_id, is_open, status, create_by, update_by, del_flag,
                            remark, create_by_id, update_by_id)
VALUES ('19580089', '10000304', '产前诊断中心', '1', '19580021', 4, 'icon-caiwu', '优生优育及羊水穿刺', '010-88880304',
        '门诊楼5楼504', NULL, 1, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_department (id, dept_code, dept_name, dept_type, parent_id, sort_order, dept_icon, dept_desc,
                            contact_phone, location, dept_leader_id, is_open, status, create_by, update_by, del_flag,
                            remark, create_by_id, update_by_id)
VALUES ('19580090', '10000404', '儿童保健科', '1', '19580025', 4, 'icon-caiwu', '儿童生长发育评估', '010-88880404',
        '门诊楼6楼604', NULL, 1, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_department (id, dept_code, dept_name, dept_type, parent_id, sort_order, dept_icon, dept_desc,
                            contact_phone, location, dept_leader_id, is_open, status, create_by, update_by, del_flag,
                            remark, create_by_id, update_by_id)
VALUES ('19580091', '10000505', '医疗美容科', '1', '19580029', 5, 'icon-caiwu', '激光美容及整形', '010-88880505',
        '门诊楼2楼205', NULL, 1, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_department (id, dept_code, dept_name, dept_type, parent_id, sort_order, dept_icon, dept_desc,
                            contact_phone, location, dept_leader_id, is_open, status, create_by, update_by, del_flag,
                            remark, create_by_id, update_by_id)
VALUES ('19580092', '10000608', '核医学科', '2', '19580034', 8, 'icon-caiwu', 'PET-CT及同位素治疗', '010-88880608',
        '医技楼B1', NULL, 1, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_department (id, dept_code, dept_name, dept_type, parent_id, sort_order, dept_icon, dept_desc,
                            contact_phone, location, dept_leader_id, is_open, status, create_by, update_by, del_flag,
                            remark, create_by_id, update_by_id)
VALUES ('19580093', '10000609', '营养科', '2', '19580034', 9, 'icon-caiwu', '临床营养指导及配餐', '010-88880609',
        '住院部1楼', NULL, 1, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_department (id, dept_code, dept_name, dept_type, parent_id, sort_order, dept_icon, dept_desc,
                            contact_phone, location, dept_leader_id, is_open, status, create_by, update_by, del_flag,
                            remark, create_by_id, update_by_id)
VALUES ('19580094', '10000610', '消毒供应中心', '2', '19580034', 10, 'icon-caiwu', '医疗器械清洗灭菌', '010-88880610',
        '后勤楼1楼', NULL, 1, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_department (id, dept_code, dept_name, dept_type, parent_id, sort_order, dept_icon, dept_desc,
                            contact_phone, location, dept_leader_id, is_open, status, create_by, update_by, del_flag,
                            remark, create_by_id, update_by_id)
VALUES ('19580095', '10001411', '客户服务中心', '5', '19580075', 11, NULL, '患者转人工工单受理与患者服务咨询', NULL,
        NULL, NULL, 1, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_department (id, dept_code, dept_name, dept_type, parent_id, sort_order, dept_icon, dept_desc,
                            contact_phone, location, dept_leader_id, is_open, status, create_by, update_by, del_flag,
                            remark, create_by_id, update_by_id)
VALUES ('8900000000000020001', 'FIX-BOARD', '看板压测科（夹具）', '1', '0', 99, NULL, NULL, NULL, NULL, NULL, 1, 1,
        'admin', 'admin', 1, NULL, '1', '1');
COMMIT;
SET
FOREIGN_KEY_CHECKS=1;

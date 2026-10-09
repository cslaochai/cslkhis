SET NAMES utf8mb4;
SET
FOREIGN_KEY_CHECKS=0;
START TRANSACTION;
INSERT INTO biz_pathway (id, pathway_code, pathway_name, dept_id, dept_name, diagnosis, version, total_days, status,
                         publish_by, publish_time, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('2103376643757195266', 'CPFX001', 'UI验证-全髋置换路径', NULL, NULL, NULL, 'V1', 2, 2, '超级管理员',
        '2026-09-25 14:50:47', 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO biz_pathway (id, pathway_code, pathway_name, dept_id, dept_name, diagnosis, version, total_days, status,
                         publish_by, publish_time, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('2103762476871999490', 'PROBE-X', 'probe', NULL, NULL, NULL, 'V1', 1, 1, NULL, NULL, 'admin', 'admin', 1, NULL,
        '1', '1');
INSERT INTO biz_pathway (id, pathway_code, pathway_name, dept_id, dept_name, diagnosis, version, total_days, status,
                         publish_by, publish_time, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('2103767370186145794', 'LP-SMOKE-000', '铺底冒烟', NULL, NULL, NULL, 'V1', 1, 3, '超级管理员',
        '2026-09-26 16:43:21', 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO biz_pathway (id, pathway_code, pathway_name, dept_id, dept_name, diagnosis, version, total_days, status,
                         publish_by, publish_time, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('2103767370781736961', 'LP-RX-001', '社区获得性肺炎临床路径', '19580003', '呼吸内科',
        '社区获得性肺炎（轻中度，无并发症）', 'V1', 5, 2, '超级管理员', '2026-09-26 16:43:22', 'admin', 'admin', 0, NULL,
        '1', '1');
INSERT INTO biz_pathway (id, pathway_code, pathway_name, dept_id, dept_name, diagnosis, version, total_days, status,
                         publish_by, publish_time, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('2103767371167612930', 'LP-ZD-001', '腹腔镜胆囊切除术临床路径', '19580013', '普通外科',
        '胆囊结石伴胆囊炎，择期手术', 'V1', 4, 2, '超级管理员', '2026-09-26 16:43:22', 'admin', 'admin', 0, NULL, '1',
        '1');
INSERT INTO biz_pathway (id, pathway_code, pathway_name, dept_id, dept_name, diagnosis, version, total_days, status,
                         publish_by, publish_time, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('2103767371561877505', 'LP-GG-001', '人工全髋关节置换术临床路径', '19580014', '骨科',
        '单侧髋关节骨性关节炎，行初次全髋置换', 'V2', 7, 2, '超级管理员', '2026-09-26 16:43:22', 'admin', 'admin', 0,
        NULL, '1', '1');
INSERT INTO biz_pathway (id, pathway_code, pathway_name, dept_id, dept_name, diagnosis, version, total_days, status,
                         publish_by, publish_time, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('2103767371951947777', 'LP-FK-001', '经阴道自然分娩临床路径', '19580023', '产科', '足月单胎头位，无阴道分娩禁忌',
        'V1', 4, 2, '超级管理员', '2026-09-26 16:43:22', 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO biz_pathway (id, pathway_code, pathway_name, dept_id, dept_name, diagnosis, version, total_days, status,
                         publish_by, publish_time, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('2103767372279103490', 'LP-XH-001', '慢性胃炎（胃镜检查）临床路径', '19580004', '消化内科',
        '上腹痛待查，拟胃镜明确，Hp 检测', 'V1', 3, 2, '超级管理员', '2026-09-26 16:43:22', 'admin', 'admin', 0, NULL,
        '1', '1');
INSERT INTO biz_pathway (id, pathway_code, pathway_name, dept_id, dept_name, diagnosis, version, total_days, status,
                         publish_by, publish_time, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('2103767372597870593', 'LP-NAO-001', '急性脑梗死临床路径', '19580006', '神经内科',
        '急性脑梗死（发病14天内），无溶栓禁忌评估', 'V1', 7, 2, '超级管理员', '2026-09-26 16:43:22', 'admin', 'admin', 0,
        NULL, '1', '1');
INSERT INTO biz_pathway (id, pathway_code, pathway_name, dept_id, dept_name, diagnosis, version, total_days, status,
                         publish_by, publish_time, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('2103767372979552258', 'LP-MN-001', '泌尿系结石（输尿管镜）临床路径', '19580017', '泌尿外科',
        '输尿管结石 ≤1.5cm，拟输尿管镜碎石', 'V1', 5, 2, '超级管理员', '2026-09-26 16:43:22', 'admin', 'admin', 0, NULL,
        '1', '1');
INSERT INTO biz_pathway (id, pathway_code, pathway_name, dept_id, dept_name, diagnosis, version, total_days, status,
                         publish_by, publish_time, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('2103767373243793409', 'LP-XXG-001', '不稳定型心绞痛临床路径', '19580005', '心血管内科',
        '不稳定型心绞痛（低中危），保守治疗', 'V1', 5, 2, '超级管理员', '2026-09-26 16:43:22', 'admin', 'admin', 0, NULL,
        '1', '1');
INSERT INTO biz_pathway (id, pathway_code, pathway_name, dept_id, dept_name, diagnosis, version, total_days, status,
                         publish_by, publish_time, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('2103767373499645953', 'LP-FY-001', '急性阑尾炎（腹腔镜阑尾切除）临床路径', '19580013', '普通外科',
        '急性阑尾炎（单纯型/轻型），48小时内手术', 'V1', 4, 2, '超级管理员', '2026-09-26 16:43:22', 'admin', 'admin', 0,
        NULL, '1', '1');
INSERT INTO biz_pathway (id, pathway_code, pathway_name, dept_id, dept_name, diagnosis, version, total_days, status,
                         publish_by, publish_time, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('2103767373830995970', 'LP-HSN-001', '腹股沟疝（无张力修补）临床路径', '19580013', '普通外科',
        '单侧腹股沟斜疝/直疝，择期无张力修补', 'V1', 4, 2, '超级管理员', '2026-09-26 16:43:22', 'admin', 'admin', 0,
        NULL, '1', '1');
INSERT INTO biz_pathway (id, pathway_code, pathway_name, dept_id, dept_name, diagnosis, version, total_days, status,
                         publish_by, publish_time, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('2103767374091042818', 'LP-YB-001', '腰椎间盘突出症（保守治疗）临床路径', '19580014', '骨科',
        '腰椎间盘突出症（无马尾综合征），保守治疗', 'V1', 7, 2, '超级管理员', '2026-09-26 16:43:22', 'admin', 'admin', 0,
        NULL, '1', '1');
INSERT INTO biz_pathway (id, pathway_code, pathway_name, dept_id, dept_name, diagnosis, version, total_days, status,
                         publish_by, publish_time, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('2103767374351089666', 'LP-NTD-001', '2型糖尿病（血糖管理）临床路径', '19580007', '内分泌科',
        '2型糖尿病，初诊或血糖控制不佳住院调糖', 'V1', 5, 2, '超级管理员', '2026-09-26 16:43:22', 'admin', 'admin', 0,
        NULL, '1', '1');
INSERT INTO biz_pathway (id, pathway_code, pathway_name, dept_id, dept_name, diagnosis, version, total_days, status,
                         publish_by, publish_time, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('2103767374669856769', 'LP-MZNZ-001', '老年性白内障（超声乳化）临床路径', '19580030', '眼科',
        '年龄相关性白内障，晶状体混浊影响视力', 'V1', 4, 2, '超级管理员', '2026-09-26 16:43:22', 'admin', 'admin', 0,
        NULL, '1', '1');
INSERT INTO biz_pathway (id, pathway_code, pathway_name, dept_id, dept_name, diagnosis, version, total_days, status,
                         publish_by, publish_time, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('2103767374997012481', 'LP-BS-001', '慢性扁桃体炎（扁桃体切除）临床路径', '19580031', '耳鼻喉科',
        '慢性扁桃体炎反复发作，择期手术', 'V1', 5, 2, '超级管理员', '2026-09-26 16:43:23', 'admin', 'admin', 0, NULL,
        '1', '1');
INSERT INTO biz_pathway (id, pathway_code, pathway_name, dept_id, dept_name, diagnosis, version, total_days, status,
                         publish_by, publish_time, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('2103767375261253634', 'LP-XL-001', '支气管哮喘急性发作临床路径', '19580003', '呼吸内科',
        '哮喘急性发作（轻中度）——草稿，步骤待科室讨论', 'V1', 1, 1, NULL, NULL, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO biz_pathway (id, pathway_code, pathway_name, dept_id, dept_name, diagnosis, version, total_days, status,
                         publish_by, publish_time, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('2103767375391277057', 'LP-MZ-001', '慢性阻塞性肺疾病临床路径', '19580003', '呼吸内科',
        'AECOPD（旧版，已并入呼吸科统一路径）', 'V1', 2, 3, '超级管理员', '2026-09-26 16:43:23', 'admin', 'admin', 0, NULL,
        '1', '1');
COMMIT;
SET
FOREIGN_KEY_CHECKS=1;

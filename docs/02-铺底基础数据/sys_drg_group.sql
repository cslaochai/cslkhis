SET NAMES utf8mb4;
SET
FOREIGN_KEY_CHECKS=0;
START TRANSACTION;
INSERT INTO sys_drg_group (id, drg_code, drg_name, mdc_code, adrg_code, weight, pay_standard, source, version, status,
                           create_by, update_by, del_flag, remark, create_by_id, update_by_id, group_type, cc_mcc_flag,
                           gender_limit, age_tier, pre_group_flag, surgery_attr, base_disease_flag, diag_match,
                           oper_match)
VALUES ('9001', 'ES31', '呼吸系统感染/炎症，伴严重并发症或合并症', 'E', 'ES3', '1.6000', '12000.00', 'CHS-DRG 1.1（模拟）',
        '1.1', 1, 'admin', 'admin', 0, NULL, '1', '1', NULL, NULL, 0, 0, 0, 0, 0, NULL, NULL);
INSERT INTO sys_drg_group (id, drg_code, drg_name, mdc_code, adrg_code, weight, pay_standard, source, version, status,
                           create_by, update_by, del_flag, remark, create_by_id, update_by_id, group_type, cc_mcc_flag,
                           gender_limit, age_tier, pre_group_flag, surgery_attr, base_disease_flag, diag_match,
                           oper_match)
VALUES ('9002', 'ES33', '呼吸系统感染/炎症，伴一般并发症或合并症', 'E', 'ES3', '0.9000', '6800.00', 'CHS-DRG 1.1（模拟）',
        '1.1', 1, 'admin', 'admin', 0, NULL, '1', '1', NULL, NULL, 0, 0, 0, 0, 0, NULL, NULL);
INSERT INTO sys_drg_group (id, drg_code, drg_name, mdc_code, adrg_code, weight, pay_standard, source, version, status,
                           create_by, update_by, del_flag, remark, create_by_id, update_by_id, group_type, cc_mcc_flag,
                           gender_limit, age_tier, pre_group_flag, surgery_attr, base_disease_flag, diag_match,
                           oper_match)
VALUES ('9003', 'ES35', '呼吸系统感染/炎症，不伴并发症或合并症', 'E', 'ES3', '0.7000', '5200.00', 'CHS-DRG 1.1（模拟）',
        '1.1', 1, 'admin', 'admin', 0, NULL, '1', '1', NULL, NULL, 0, 0, 0, 0, 0, NULL, NULL);
INSERT INTO sys_drg_group (id, drg_code, drg_name, mdc_code, adrg_code, weight, pay_standard, source, version, status,
                           create_by, update_by, del_flag, remark, create_by_id, update_by_id, group_type, cc_mcc_flag,
                           gender_limit, age_tier, pre_group_flag, surgery_attr, base_disease_flag, diag_match,
                           oper_match)
VALUES ('9004', 'ET41', '慢性气道阻塞疾病', 'E', 'ET4', '0.8000', '6000.00', 'CHS-DRG 1.1（模拟）', '1.1', 1, 'admin',
        'admin', 0, NULL, '1', '1', NULL, NULL, 0, 0, 0, 0, 0, NULL, NULL);
INSERT INTO sys_drg_group (id, drg_code, drg_name, mdc_code, adrg_code, weight, pay_standard, source, version, status,
                           create_by, update_by, del_flag, remark, create_by_id, update_by_id, group_type, cc_mcc_flag,
                           gender_limit, age_tier, pre_group_flag, surgery_attr, base_disease_flag, diag_match,
                           oper_match)
VALUES ('9005', 'FS21', '心力衰竭，伴严重并发症或合并症', 'F', 'FS2', '1.1000', '8500.00', 'CHS-DRG 1.1（模拟）', '1.1', 1,
        'admin', 'admin', 0, NULL, '1', '1', NULL, NULL, 0, 0, 0, 0, 0, NULL, NULL);
INSERT INTO sys_drg_group (id, drg_code, drg_name, mdc_code, adrg_code, weight, pay_standard, source, version, status,
                           create_by, update_by, del_flag, remark, create_by_id, update_by_id, group_type, cc_mcc_flag,
                           gender_limit, age_tier, pre_group_flag, surgery_attr, base_disease_flag, diag_match,
                           oper_match)
VALUES ('9006', 'FS23', '心力衰竭，不伴并发症或合并症', 'F', 'FS2', '0.7500', '5800.00', 'CHS-DRG 1.1（模拟）', '1.1', 1,
        'admin', 'admin', 0, NULL, '1', '1', NULL, NULL, 0, 0, 0, 0, 0, NULL, NULL);
INSERT INTO sys_drg_group (id, drg_code, drg_name, mdc_code, adrg_code, weight, pay_standard, source, version, status,
                           create_by, update_by, del_flag, remark, create_by_id, update_by_id, group_type, cc_mcc_flag,
                           gender_limit, age_tier, pre_group_flag, surgery_attr, base_disease_flag, diag_match,
                           oper_match)
VALUES ('9007', 'GT21', '胃炎/消化功能紊乱', 'G', 'GT2', '0.6000', '4500.00', 'CHS-DRG 1.1（模拟）', '1.1', 1, 'admin',
        'admin', 0, NULL, '1', '1', NULL, NULL, 0, 0, 0, 0, 0, NULL, NULL);
INSERT INTO sys_drg_group (id, drg_code, drg_name, mdc_code, adrg_code, weight, pay_standard, source, version, status,
                           create_by, update_by, del_flag, remark, create_by_id, update_by_id, group_type, cc_mcc_flag,
                           gender_limit, age_tier, pre_group_flag, surgery_attr, base_disease_flag, diag_match,
                           oper_match)
VALUES ('9008', 'GE21', '肝胆胰系统疾患（非手术）', 'G', 'GE2', '0.9000', '7000.00', 'CHS-DRG 1.1（模拟）', '1.1', 1,
        'admin', 'admin', 0, NULL, '1', '1', NULL, NULL, 0, 0, 0, 0, 0, NULL, NULL);
INSERT INTO sys_drg_group (id, drg_code, drg_name, mdc_code, adrg_code, weight, pay_standard, source, version, status,
                           create_by, update_by, del_flag, remark, create_by_id, update_by_id, group_type, cc_mcc_flag,
                           gender_limit, age_tier, pre_group_flag, surgery_attr, base_disease_flag, diag_match,
                           oper_match)
VALUES ('9009', 'IE19', '内分泌代谢疾患（糖尿病等）', 'I', 'IE1', '0.6500', '4800.00', 'CHS-DRG 1.1（模拟）', '1.1', 1,
        'admin', 'admin', 0, NULL, '1', '1', NULL, NULL, 0, 0, 0, 0, 0, NULL, NULL);
INSERT INTO sys_drg_group (id, drg_code, drg_name, mdc_code, adrg_code, weight, pay_standard, source, version, status,
                           create_by, update_by, del_flag, remark, create_by_id, update_by_id, group_type, cc_mcc_flag,
                           gender_limit, age_tier, pre_group_flag, surgery_attr, base_disease_flag, diag_match,
                           oper_match)
VALUES ('9010', 'GC19', '胆囊切除手术', 'G', 'GC1', '0.9500', '8800.00', 'CHS-DRG 1.1（模拟）', '1.1', 1, 'admin',
        'admin', 0, NULL, '1', '1', NULL, NULL, 0, 0, 0, 0, 0, NULL, NULL);
INSERT INTO sys_drg_group (id, drg_code, drg_name, mdc_code, adrg_code, weight, pay_standard, source, version, status,
                           create_by, update_by, del_flag, remark, create_by_id, update_by_id, group_type, cc_mcc_flag,
                           gender_limit, age_tier, pre_group_flag, surgery_attr, base_disease_flag, diag_match,
                           oper_match)
VALUES ('9011', 'JJ29', '呼吸系统其他手术', 'E', 'JJ2', '1.2000', '10500.00', 'CHS-DRG 1.1（模拟）', '1.1', 1, 'admin',
        'admin', 0, NULL, '1', '1', NULL, NULL, 0, 0, 0, 0, 0, NULL, NULL);
INSERT INTO sys_drg_group (id, drg_code, drg_name, mdc_code, adrg_code, weight, pay_standard, source, version, status,
                           create_by, update_by, del_flag, remark, create_by_id, update_by_id, group_type, cc_mcc_flag,
                           gender_limit, age_tier, pre_group_flag, surgery_attr, base_disease_flag, diag_match,
                           oper_match)
VALUES ('9012', 'KE19', '消化系统其他手术', 'G', 'KE1', '1.0000', '9000.00', 'CHS-DRG 1.1（模拟）', '1.1', 1, 'admin',
        'admin', 0, NULL, '1', '1', NULL, NULL, 0, 0, 0, 0, 0, NULL, NULL);
INSERT INTO sys_drg_group (id, drg_code, drg_name, mdc_code, adrg_code, weight, pay_standard, source, version, status,
                           create_by, update_by, del_flag, remark, create_by_id, update_by_id, group_type, cc_mcc_flag,
                           gender_limit, age_tier, pre_group_flag, surgery_attr, base_disease_flag, diag_match,
                           oper_match)
VALUES ('9013', 'NB13', '脑血管病，伴一般并发症或合并症', 'N', 'NB1', '0.9500', '7200.00', 'CHS-DRG 1.1（模拟）', '1.1', 1,
        'admin', 'admin', 0, NULL, '1', '1', NULL, NULL, 0, 0, 0, 0, 0, NULL, NULL);
INSERT INTO sys_drg_group (id, drg_code, drg_name, mdc_code, adrg_code, weight, pay_standard, source, version, status,
                           create_by, update_by, del_flag, remark, create_by_id, update_by_id, group_type, cc_mcc_flag,
                           gender_limit, age_tier, pre_group_flag, surgery_attr, base_disease_flag, diag_match,
                           oper_match)
VALUES ('9014', 'RE19', '肾/尿路感染（非手术）', 'R', 'RE1', '0.7000', '5400.00', 'CHS-DRG 1.1（模拟）', '1.1', 1, 'admin',
        'admin', 0, NULL, '1', '1', NULL, NULL, 0, 0, 0, 0, 0, NULL, NULL);
INSERT INTO sys_drg_group (id, drg_code, drg_name, mdc_code, adrg_code, weight, pay_standard, source, version, status,
                           create_by, update_by, del_flag, remark, create_by_id, update_by_id, group_type, cc_mcc_flag,
                           gender_limit, age_tier, pre_group_flag, surgery_attr, base_disease_flag, diag_match,
                           oper_match)
VALUES ('9015', 'FR21', '急性心肌梗死，伴严重并发症或合并症', 'F', 'FR2', '1.9000', '15000.00', 'CHS-DRG 1.1（模拟）',
        '1.1', 1, 'admin', 'admin', 0, NULL, '1', '1', NULL, NULL, 0, 0, 0, 0, 0, NULL, NULL);
INSERT INTO sys_drg_group (id, drg_code, drg_name, mdc_code, adrg_code, weight, pay_standard, source, version, status,
                           create_by, update_by, del_flag, remark, create_by_id, update_by_id, group_type, cc_mcc_flag,
                           gender_limit, age_tier, pre_group_flag, surgery_attr, base_disease_flag, diag_match,
                           oper_match)
VALUES ('9016', 'FR23', '急性心肌梗死，不伴并发症或合并症', 'F', 'FR2', '0.9500', '7600.00', 'CHS-DRG 1.1（模拟）', '1.1',
        1, 'admin', 'admin', 0, NULL, '1', '1', NULL, NULL, 0, 0, 0, 0, 0, NULL, NULL);
INSERT INTO sys_drg_group (id, drg_code, drg_name, mdc_code, adrg_code, weight, pay_standard, source, version, status,
                           create_by, update_by, del_flag, remark, create_by_id, update_by_id, group_type, cc_mcc_flag,
                           gender_limit, age_tier, pre_group_flag, surgery_attr, base_disease_flag, diag_match,
                           oper_match)
VALUES ('9017', 'GS21', '上消化道出血，伴严重并发症或合并症', 'G', 'GS2', '1.3000', '9800.00', 'CHS-DRG 1.1（模拟）',
        '1.1', 1, 'admin', 'admin', 0, NULL, '1', '1', NULL, NULL, 0, 0, 0, 0, 0, NULL, NULL);
INSERT INTO sys_drg_group (id, drg_code, drg_name, mdc_code, adrg_code, weight, pay_standard, source, version, status,
                           create_by, update_by, del_flag, remark, create_by_id, update_by_id, group_type, cc_mcc_flag,
                           gender_limit, age_tier, pre_group_flag, surgery_attr, base_disease_flag, diag_match,
                           oper_match)
VALUES ('9018', 'GS23', '上消化道出血，不伴并发症或合并症', 'G', 'GS2', '0.7500', '5600.00', 'CHS-DRG 1.1（模拟）', '1.1',
        1, 'admin', 'admin', 0, NULL, '1', '1', NULL, NULL, 0, 0, 0, 0, 0, NULL, NULL);
INSERT INTO sys_drg_group (id, drg_code, drg_name, mdc_code, adrg_code, weight, pay_standard, source, version, status,
                           create_by, update_by, del_flag, remark, create_by_id, update_by_id, group_type, cc_mcc_flag,
                           gender_limit, age_tier, pre_group_flag, surgery_attr, base_disease_flag, diag_match,
                           oper_match)
VALUES ('9019', 'BU19', '脑血管病恢复期康复（非手术）', 'N', 'BU1', '0.8000', '6200.00', 'CHS-DRG 1.1（模拟）', '1.1', 1,
        'admin', 'admin', 0, NULL, '1', '1', NULL, NULL, 0, 0, 0, 0, 0, NULL, NULL);
INSERT INTO sys_drg_group (id, drg_code, drg_name, mdc_code, adrg_code, weight, pay_standard, source, version, status,
                           create_by, update_by, del_flag, remark, create_by_id, update_by_id, group_type, cc_mcc_flag,
                           gender_limit, age_tier, pre_group_flag, surgery_attr, base_disease_flag, diag_match,
                           oper_match)
VALUES ('9020', 'RV19', '肾功能衰竭（非手术）', 'R', 'RV1', '1.4000', '11000.00', 'CHS-DRG 1.1（模拟）', '1.1', 1, 'admin',
        'admin', 0, NULL, '1', '1', NULL, NULL, 0, 0, 0, 0, 0, NULL, NULL);
INSERT INTO sys_drg_group (id, drg_code, drg_name, mdc_code, adrg_code, weight, pay_standard, source, version, status,
                           create_by, update_by, del_flag, remark, create_by_id, update_by_id, group_type, cc_mcc_flag,
                           gender_limit, age_tier, pre_group_flag, surgery_attr, base_disease_flag, diag_match,
                           oper_match)
VALUES ('9021', 'UJ19', '泌尿系结石手术治疗', 'R', 'UJ1', '0.8500', '7000.00', 'CHS-DRG 1.1（模拟）', '1.1', 1, 'admin',
        'admin', 0, NULL, '1', '1', NULL, NULL, 0, 0, 0, 0, 0, NULL, NULL);
INSERT INTO sys_drg_group (id, drg_code, drg_name, mdc_code, adrg_code, weight, pay_standard, source, version, status,
                           create_by, update_by, del_flag, remark, create_by_id, update_by_id, group_type, cc_mcc_flag,
                           gender_limit, age_tier, pre_group_flag, surgery_attr, base_disease_flag, diag_match,
                           oper_match)
VALUES ('9022', 'KT19', '甲状腺手术治疗', 'I', 'KT1', '0.9000', '7600.00', 'CHS-DRG 1.1（模拟）', '1.1', 1, 'admin',
        'admin', 0, NULL, '1', '1', NULL, NULL, 0, 0, 0, 0, 0, NULL, NULL);
INSERT INTO sys_drg_group (id, drg_code, drg_name, mdc_code, adrg_code, weight, pay_standard, source, version, status,
                           create_by, update_by, del_flag, remark, create_by_id, update_by_id, group_type, cc_mcc_flag,
                           gender_limit, age_tier, pre_group_flag, surgery_attr, base_disease_flag, diag_match,
                           oper_match)
VALUES ('9023', 'XS19', '感染及寄生虫疾患（非手术）', 'A', 'XS1', '0.7000', '5400.00', 'CHS-DRG 1.1（模拟）', '1.1', 1,
        'admin', 'admin', 0, NULL, '1', '1', NULL, NULL, 0, 0, 0, 0, 0, NULL, NULL);
INSERT INTO sys_drg_group (id, drg_code, drg_name, mdc_code, adrg_code, weight, pay_standard, source, version, status,
                           create_by, update_by, del_flag, remark, create_by_id, update_by_id, group_type, cc_mcc_flag,
                           gender_limit, age_tier, pre_group_flag, surgery_attr, base_disease_flag, diag_match,
                           oper_match)
VALUES ('9024', 'ND19', '子宫附件手术治疗', 'N', 'ND1', '0.9000', '7400.00', 'CHS-DRG 1.1（模拟）', '1.1', 1, 'admin',
        'admin', 0, NULL, '1', '1', NULL, NULL, 0, 0, 0, 0, 0, NULL, NULL);
INSERT INTO sys_drg_group (id, drg_code, drg_name, mdc_code, adrg_code, weight, pay_standard, source, version, status,
                           create_by, update_by, del_flag, remark, create_by_id, update_by_id, group_type, cc_mcc_flag,
                           gender_limit, age_tier, pre_group_flag, surgery_attr, base_disease_flag, diag_match,
                           oper_match)
VALUES ('9025', 'OR19', '剖宫产分娩', 'O', 'OR1', '0.7000', '5200.00', 'CHS-DRG 1.1（模拟）', '1.1', 1, 'admin', 'admin',
        0, NULL, '1', '1', NULL, NULL, 0, 0, 0, 0, 0, NULL, NULL);
INSERT INTO sys_drg_group (id, drg_code, drg_name, mdc_code, adrg_code, weight, pay_standard, source, version, status,
                           create_by, update_by, del_flag, remark, create_by_id, update_by_id, group_type, cc_mcc_flag,
                           gender_limit, age_tier, pre_group_flag, surgery_attr, base_disease_flag, diag_match,
                           oper_match)
VALUES ('9026', 'PJ19', '新生儿疾患（非手术）', 'P', 'PJ1', '0.8000', '6400.00', 'CHS-DRG 1.1（模拟）', '1.1', 1, 'admin',
        'admin', 0, NULL, '1', '1', NULL, NULL, 0, 0, 0, 0, 0, NULL, NULL);
INSERT INTO sys_drg_group (id, drg_code, drg_name, mdc_code, adrg_code, weight, pay_standard, source, version, status,
                           create_by, update_by, del_flag, remark, create_by_id, update_by_id, group_type, cc_mcc_flag,
                           gender_limit, age_tier, pre_group_flag, surgery_attr, base_disease_flag, diag_match,
                           oper_match)
VALUES ('9027', 'MJ19', '精神疾患（非手术）', 'M', 'MJ1', '0.6000', '4500.00', 'CHS-DRG 1.1（模拟）', '1.1', 1, 'admin',
        'admin', 0, NULL, '1', '1', NULL, NULL, 0, 0, 0, 0, 0, NULL, NULL);
INSERT INTO sys_drg_group (id, drg_code, drg_name, mdc_code, adrg_code, weight, pay_standard, source, version, status,
                           create_by, update_by, del_flag, remark, create_by_id, update_by_id, group_type, cc_mcc_flag,
                           gender_limit, age_tier, pre_group_flag, surgery_attr, base_disease_flag, diag_match,
                           oper_match)
VALUES ('9028', 'XJ19', '损伤中毒（非手术）', 'X', 'XJ1', '0.7500', '5800.00', 'CHS-DRG 1.1（模拟）', '1.1', 1, 'admin',
        'admin', 0, NULL, '1', '1', NULL, NULL, 0, 0, 0, 0, 0, NULL, NULL);
COMMIT;
SET
FOREIGN_KEY_CHECKS=1;

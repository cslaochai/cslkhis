-- =====================================================================
-- 258 患者费别归一 + 挂号单结算方式改两档
-- ---------------------------------------------------------------------
-- 背景：
--   患者主档原先同时存在 patient_type（五分，1自费/2职工/3居民/4公费/5其他）
--   与 medical_insurance_type（费别字符串）两套口径，挂号单 settlement_type 又
--   是五分险种，三者重复且漂移。本次按方案 A 收口：
--     1) 患者主档只保留费别 medical_insurance_type（标准 5 项，中文）+ 医保号
--        medical_insurance_no，物理删除 patient_type 列；
--     2) 挂号单 settlement_type 由五分改为两档（1-全自费 / 2-医保联网），
--        与收费账单 settlement_mode、字典 his_settlement_mode 同口径；
--     3) 报销口径：费别为医保类（职工/居民/公费）+ 有医保号 + 本单联网 → 走统筹。
--
-- 注意（执行顺序）：
--   本脚本含 DROP COLUMN biz_patient.patient_type。必须先把后端发布到
--   已删除 patientType 字段的新代码（实体/DTO/VO 不再引用该列），再执行本脚本；
--   否则仍在运行的旧进程会因 SELECT patient_type 报 Unknown column。
--   执行后清空字典缓存（Redis 中 his:dict:* 等），或直接重启后端。
--
-- 不动的口径：
--   sys_insurance_policy.settlement_type（2职工/3居民/4公费）是险种 key，
--   字典 his_medical_insurance_type_code 与政策表五分保持不变，本脚本不触碰。
-- =====================================================================

-- 一、患者主档：费别 medical_insurance_type 数据归一 ---------------------------------
-- 1) 先把已经填了的长名/别名标准化为标准 5 项（费别才是报销口径，优先于旧 patient_type）
UPDATE biz_patient SET medical_insurance_type = '城镇职工医保'
 WHERE medical_insurance_type LIKE '%职工%';
UPDATE biz_patient SET medical_insurance_type = '城乡居民医保'
 WHERE medical_insurance_type LIKE '%居民%'
    OR medical_insurance_type LIKE '%农村%'
    OR medical_insurance_type LIKE '%新农合%';
UPDATE biz_patient SET medical_insurance_type = '公费医疗'
 WHERE medical_insurance_type LIKE '%公费%';
UPDATE biz_patient SET medical_insurance_type = '商业医疗保险'
 WHERE medical_insurance_type LIKE '%商业%';

-- 2) 标准化后仍为空/空串的，再按旧 patient_type 回填（patient_type 仅作兜底，不再优先）
UPDATE biz_patient SET medical_insurance_type = '城镇职工医保'
 WHERE (medical_insurance_type IS NULL OR medical_insurance_type = '') AND patient_type = 2;
UPDATE biz_patient SET medical_insurance_type = '城乡居民医保'
 WHERE (medical_insurance_type IS NULL OR medical_insurance_type = '') AND patient_type = 3;
UPDATE biz_patient SET medical_insurance_type = '公费医疗'
 WHERE (medical_insurance_type IS NULL OR medical_insurance_type = '') AND patient_type = 4;
-- patient_type=1 自费 / 5 其他 / NULL，以及任何未识别的特殊险种，一律归为自费
UPDATE biz_patient SET medical_insurance_type = '自费'
 WHERE medical_insurance_type IS NULL OR medical_insurance_type = '';

-- 3) 自费患者不应留医保号（历史脏数据清掉，避免被误判成医保患者）
UPDATE biz_patient SET medical_insurance_no = NULL
 WHERE medical_insurance_type = '自费';

-- 4) 统一费别列注释（列名不变，仅改注释）
ALTER TABLE biz_patient
    MODIFY COLUMN medical_insurance_type varchar(50)
        COMMENT '费别（自费/城镇职工医保/城乡居民医保/公费医疗/商业医疗保险）',
    MODIFY COLUMN medical_insurance_no varchar(50)
        COMMENT '医保卡号/医保编号（费别为医保类时填写）';

-- 5) 物理删除冗余的患者类型列（新代码已不再引用）
ALTER TABLE biz_patient DROP COLUMN patient_type;

-- 二、挂号单：结算方式五分归并为两档 -----------------------------------------------
-- 2职工/3居民/4公费/5商业 → 统一 2（医保联网）；1 全自费保持
UPDATE biz_appoint_info SET settlement_type = 2
 WHERE settlement_type IN (2, 3, 4, 5);
-- 历史脏值（NULL/0 等）归为 1 全自费
UPDATE biz_appoint_info SET settlement_type = 1
 WHERE settlement_type IS NULL OR settlement_type NOT IN (1, 2);

ALTER TABLE biz_appoint_info
    MODIFY COLUMN settlement_type tinyint
        COMMENT '结算方式（1-全自费 2-医保联网）',
    MODIFY COLUMN medical_insurance_type varchar(50)
        COMMENT '费别快照（取自患者主档：自费/城镇职工医保/城乡居民医保/公费医疗/商业医疗保险）',
    MODIFY COLUMN medical_insurance_no varchar(50)
        COMMENT '医保卡号（快照自患者主档）';

-- 三、字典 ------------------------------------------------------------------------
-- 3.1 费别字典 medical_insurance_type 重整为标准 5 项（1001职工 / 1002居民 / 1004公费 / 1005商业 / 1011自费）
UPDATE sys_dict_data SET dict_label = '城镇职工医保', dict_value = '城镇职工医保', dict_sort = 2,
                         update_by = 'admin', update_time = NOW()
 WHERE id = 1001 AND dict_type = 'medical_insurance_type';
UPDATE sys_dict_data SET dict_label = '城乡居民医保', dict_value = '城乡居民医保', dict_sort = 3,
                         update_by = 'admin', update_time = NOW()
 WHERE id = 1002 AND dict_type = 'medical_insurance_type';
-- 1003 新农合已并入城乡居民医保，停用（保留历史值不删，避免回显成未知）
UPDATE sys_dict_data SET status = 0, update_by = 'admin', update_time = NOW()
 WHERE id = 1003 AND dict_type = 'medical_insurance_type';
UPDATE sys_dict_data SET dict_sort = 4, update_by = 'admin', update_time = NOW()
 WHERE id = 1004 AND dict_type = 'medical_insurance_type';
UPDATE sys_dict_data SET dict_sort = 5, update_by = 'admin', update_time = NOW()
 WHERE id = 1005 AND dict_type = 'medical_insurance_type';
-- 1006-1010 离休/老工人/伤残军人/生育/工伤等特殊保障不在标准 5 项，下拉停用
UPDATE sys_dict_data SET status = 0, update_by = 'admin', update_time = NOW()
 WHERE dict_type = 'medical_insurance_type' AND id IN (1006, 1007, 1008, 1009, 1010);
-- 新增标准项「自费」（id 段避开 1001-1010）
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class,
                           is_default, status, create_by, update_by, del_flag, remark, dict_source,
                           create_by_id, update_by_id, create_time, update_time)
SELECT 1011, 'medical_insurance_type', '自费', '自费', 1, 'info', 'info', 1, 1,
       'admin', 'admin', 0, '全自费（默认费别）', 1, 1, 1, NOW(), NOW()
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type = 'medical_insurance_type' AND dict_value = '自费');

-- 3.2 停用旧的五分字典：患者类型 his_patient_type、挂号结算方式 his_settlement_type_regist
UPDATE sys_dict_data SET status = 0, update_by = 'admin', update_time = NOW()
 WHERE dict_type = 'his_patient_type';
UPDATE sys_dict_data SET status = 0, update_by = 'admin', update_time = NOW()
 WHERE dict_type = 'his_settlement_type_regist';

-- 3.3 两档结算字典 his_settlement_mode 标签改标准口径
UPDATE sys_dict_data SET dict_label = '全自费', update_by = 'admin', update_time = NOW()
 WHERE id = 891000000000000032 AND dict_type = 'his_settlement_mode';
UPDATE sys_dict_data SET dict_label = '医保联网', update_by = 'admin', update_time = NOW()
 WHERE id = 891000000000000033 AND dict_type = 'his_settlement_mode';

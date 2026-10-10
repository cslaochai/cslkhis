-- ============================================================
-- FAQ 分类字典（受控字典，替代原 sys_faq.category_code 自由文本编码）
-- 2026-10-10：原分类是自由录入文本，已出现口径分裂
--   - INSURANCE 既叫「医保相关」又叫「医保报销」
--   - FACILITY 与 SERVICE 都叫「便民服务」却是不同编码
--   - REPORT 既有「报告查询」又有「检验检查」
-- 收口为字典后，前端下拉只能选字典项，杜绝拼错/大小写撞车。
-- 存量 FAQ 的非字典分类（SEQ_VERIFY 等验证数据）保留不动，
-- 仅对上面三类口径分裂做统一归并。
-- ============================================================
SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;
START TRANSACTION;

-- 字典类型
INSERT IGNORE INTO sys_dict_type (id, dict_type, dict_name, status, create_by, update_by, del_flag, remark, dict_source,
                                 create_by_id, update_by_id)
VALUES ('880000000000000901', 'his_faq_category', '常见问题分类', 1, 'admin', 'admin', 0,
        '患者端常见问题（sys_faq）的分类受控字典，dict_value=分类编码、dict_label=分类名称', 1, '1', '1');

-- 字典数据（11 个真实业务分类）
INSERT IGNORE INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default,
                                 status, create_by, update_by, del_flag, remark, dict_source, create_by_id,
                                 update_by_id)
VALUES
    ('9900101', 'his_faq_category', '挂号预约', 'APPOINT', 1, NULL, 'primary', 0, 1, 'admin', 'admin', 0, NULL, 1, '1', '1'),
    ('9900102', 'his_faq_category', '缴费退款', 'PAY', 2, NULL, 'primary', 0, 1, 'admin', 'admin', 0, NULL, 1, '1', '1'),
    ('9900103', 'his_faq_category', '报告查询', 'REPORT', 3, NULL, 'primary', 0, 1, 'admin', 'admin', 0, NULL, 1, '1', '1'),
    ('9900104', 'his_faq_category', '住院服务', 'INPATIENT', 4, NULL, 'primary', 0, 1, 'admin', 'admin', 0, NULL, 1, '1', '1'),
    ('9900105', 'his_faq_category', '就诊流程', 'PROCESS', 5, NULL, 'primary', 0, 1, 'admin', 'admin', 0, NULL, 1, '1', '1'),
    ('9900106', 'his_faq_category', '医保相关', 'INSURANCE', 6, NULL, 'primary', 0, 1, 'admin', 'admin', 0, NULL, 1, '1', '1'),
    ('9900107', 'his_faq_category', '账号与就诊人', 'ACCOUNT', 7, NULL, 'primary', 0, 1, 'admin', 'admin', 0, NULL, 1, '1', '1'),
    ('9900108', 'his_faq_category', '便民服务', 'FACILITY', 8, NULL, 'primary', 0, 1, 'admin', 'admin', 0, NULL, 1, '1', '1'),
    ('9900109', 'his_faq_category', '健康体检', 'CHECKUP', 9, NULL, 'primary', 0, 1, 'admin', 'admin', 0, NULL, 1, '1', '1'),
    ('9900110', 'his_faq_category', '费用票据', 'BILLING', 10, NULL, 'primary', 0, 1, 'admin', 'admin', 0, NULL, 1, '1', '1'),
    ('9900111', 'his_faq_category', '慢病管理', 'CHRONIC', 11, NULL, 'primary', 0, 1, 'admin', 'admin', 0, NULL, 1, '1', '1');

-- 存量口径归并（与字典对齐，消除自由文本分裂；幂等）
-- 1) INSURANCE：原「医保报销」统一为「医保相关」
UPDATE sys_faq
SET category_name = '医保相关'
WHERE category_code = 'INSURANCE'
  AND category_name <> '医保相关';
-- 2) SERVICE（便民服务）并入 FACILITY（便民服务），编码统一
UPDATE sys_faq
SET category_code = 'FACILITY'
WHERE category_code = 'SERVICE';
-- 3) REPORT：原「检验检查」统一为「报告查询」
UPDATE sys_faq
SET category_name = '报告查询'
WHERE category_code = 'REPORT'
  AND category_name <> '报告查询';

COMMIT;
SET FOREIGN_KEY_CHECKS = 1;

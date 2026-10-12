SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ---- 加列 ----
ALTER TABLE biz_drg_mdc   ADD COLUMN create_by_id bigint COMMENT '创建人ID';
ALTER TABLE biz_drg_mdc   ADD COLUMN update_by_id bigint COMMENT '更新人ID';

ALTER TABLE biz_drg_adrg  ADD COLUMN create_by_id bigint COMMENT '创建人ID';
ALTER TABLE biz_drg_adrg  ADD COLUMN update_by_id bigint COMMENT '更新人ID';

ALTER TABLE biz_drg_set   ADD COLUMN create_by_id bigint COMMENT '创建人ID';
ALTER TABLE biz_drg_set   ADD COLUMN update_by_id bigint COMMENT '更新人ID';

ALTER TABLE biz_drg_ccmcc ADD COLUMN create_by_id bigint COMMENT '创建人ID';
ALTER TABLE biz_drg_ccmcc ADD COLUMN update_by_id bigint COMMENT '创建人ID';

ALTER TABLE biz_drg_mdc   DROP INDEX uk_mdc_code,       ADD UNIQUE KEY uk_mdc_code       (mdc_code, version, del_flag);
ALTER TABLE biz_drg_adrg  DROP INDEX uk_adrg_code,      ADD UNIQUE KEY uk_adrg_code      (adrg_code, version, del_flag);
ALTER TABLE biz_drg_set   DROP INDEX uk_set_icd,        ADD UNIQUE KEY uk_set_icd        (set_code, icd_code, version, del_flag);
ALTER TABLE biz_drg_group DROP INDEX uk_drg_code,       ADD UNIQUE KEY uk_drg_code       (drg_code, version, del_flag);
ALTER TABLE biz_drg_ccmcc DROP INDEX uk_ccmcc_code_ver, ADD UNIQUE KEY uk_ccmcc_code_ver (icd_code, version, del_flag);

-- ---- 历史数据补 1 ----
UPDATE biz_drg_mdc   SET create_by_id = 1, update_by_id = 1 WHERE create_by_id IS NULL OR update_by_id IS NULL;
UPDATE biz_drg_adrg  SET create_by_id = 1, update_by_id = 1 WHERE create_by_id IS NULL OR update_by_id IS NULL;
UPDATE biz_drg_set   SET create_by_id = 1, update_by_id = 1 WHERE create_by_id IS NULL OR update_by_id IS NULL;
UPDATE biz_drg_ccmcc SET create_by_id = 1, update_by_id = 1 WHERE create_by_id IS NULL OR update_by_id IS NULL;

SET FOREIGN_KEY_CHECKS = 1;
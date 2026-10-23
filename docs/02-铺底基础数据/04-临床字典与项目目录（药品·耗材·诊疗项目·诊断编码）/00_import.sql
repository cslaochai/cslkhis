-- 04-临床字典与项目目录（药品·耗材·诊疗项目·诊断编码） 铺底导入（依赖顺序见文件名，同域内互不依赖）
-- 用法：本目录下执行  mysql --default-character-set=utf8mb4 库名 < 00_import.sql
SET NAMES utf8mb4;

SOURCE ./biz_dip_aux.sql;
SOURCE ./biz_dip_disease.sql;
SOURCE ./biz_shift.sql;
SOURCE ./sys_checkup_package.sql;
SOURCE ./sys_checkup_package_item.sql;
SOURCE ./sys_consumable.sql;
SOURCE ./sys_diagnosis.sql;
SOURCE ./sys_drug.sql;
SOURCE ./sys_drug_dose_limit.sql;
SOURCE ./sys_drug_interaction.sql;
SOURCE ./sys_drug_price_history.sql;
SOURCE ./sys_icd10.sql;
SOURCE ./sys_icd9cm3.sql;
SOURCE ./sys_infectious_disease.sql;
SOURCE ./sys_inspection_item.sql;
SOURCE ./sys_laboratory_item.sql;
SOURCE ./sys_laboratory_item_detail.sql;
SOURCE ./sys_price_change_history.sql;
SOURCE ./sys_single_disease.sql;
SOURCE ./sys_supplier.sql;
SOURCE ./sys_treatment_item.sql;

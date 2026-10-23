-- 13-检查影像与报告（PACS·预约·心电·病理·内镜） 铺底导入（依赖顺序见文件名，同域内互不依赖）
-- 用法：本目录下执行  mysql --default-character-set=utf8mb4 库名 < 00_import.sql
SET NAMES utf8mb4;

SOURCE ./biz_ecg_template.sql;
SOURCE ./biz_exam_device.sql;
SOURCE ./biz_exam_device_item.sql;
SOURCE ./biz_exam_slot.sql;
SOURCE ./biz_film_spec.sql;
SOURCE ./biz_inspection_template.sql;
SOURCE ./biz_radio_report_template.sql;
SOURCE ./sys_imaging_plain_item.sql;

-- 03-空间与设备主数据（病区·床位·诊室·手术间·设备） 铺底导入（依赖顺序见文件名，同域内互不依赖）
-- 用法：本目录下执行  mysql --default-character-set=utf8mb4 库名 < 00_import.sql
SET NAMES utf8mb4;

SOURCE ./biz_infusion_seat.sql;
SOURCE ./sys_bed.sql;
SOURCE ./sys_clinic_room.sql;
SOURCE ./sys_equipment.sql;
SOURCE ./sys_operation_room.sql;
SOURCE ./sys_ward.sql;

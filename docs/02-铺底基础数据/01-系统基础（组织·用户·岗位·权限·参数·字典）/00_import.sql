-- 01-系统基础（组织·用户·岗位·权限·参数·字典） 铺底导入（依赖顺序见文件名，同域内互不依赖）
-- 用法：本目录下执行  mysql --default-character-set=utf8mb4 库名 < 00_import.sql
SET NAMES utf8mb4;

SOURCE ./sys_config.sql;
SOURCE ./sys_department.sql;
SOURCE ./sys_dict_data.sql;
SOURCE ./sys_dict_type.sql;
SOURCE ./sys_employee.sql;
SOURCE ./sys_employee_post.sql;
SOURCE ./sys_employee_qualification.sql;
SOURCE ./sys_employee_tech_auth.sql;
SOURCE ./sys_menu.sql;
SOURCE ./sys_role.sql;
SOURCE ./sys_role_menu.sql;
SOURCE ./sys_sign_cert.sql;
SOURCE ./sys_tsa_server.sql;
SOURCE ./sys_user.sql;

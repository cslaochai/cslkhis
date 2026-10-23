-- 铺底基础数据全量导入：按领域子目录依次执行
-- 用法：cd 到本目录后  mysql --default-character-set=utf8mb4 -u.. -p.. 库名 < 00_import_all.sql
-- 前置：先跑 ../01-初始化DDL/*.sql 建表
SET NAMES utf8mb4;

SOURCE ./01-系统基础（组织·用户·岗位·权限·参数·字典）/00_import.sql;
SOURCE ./03-空间与设备主数据（病区·床位·诊室·手术间·设备）/00_import.sql;
SOURCE ./04-临床字典与项目目录（药品·耗材·诊疗项目·诊断编码）/00_import.sql;
SOURCE ./05-医保（目录·对照·备案·结算·审核）/00_import.sql;
SOURCE ./07-预约挂号·排班·分诊·叫号/00_import.sql;
SOURCE ./08-门诊病历与处方/00_import.sql;
SOURCE ./09-住院与医嘱（入出转·会诊·床位调度）/00_import.sql;
SOURCE ./10-护理（文书·评估·排班·质控）/00_import.sql;
SOURCE ./12-检验LIS（申请·结果·质控·室间质评）/00_import.sql;
SOURCE ./13-检查影像与报告（PACS·预约·心电·病理·内镜）/00_import.sql;
SOURCE ./14-手术麻醉与日间手术/00_import.sql;
SOURCE ./15-输血与血库/00_import.sql;
SOURCE ./17-药房药库（采购·库存·发药·调拨·盘点·静配）/00_import.sql;
SOURCE ./18-耗材与消毒供应（CSSD）/00_import.sql;
SOURCE ./19-收费四层（记账·结算·支付·票据）/00_import.sql;
SOURCE ./21-专项质控与专科管理（路径·抗菌药物·VTE·营养·体检·透析·监护）/00_import.sql;
SOURCE ./22-互联网医院·随访·满意度·消息与工作台/00_import.sql;

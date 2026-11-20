-- sys_charge_policy  铺底数据：2 行（按主键序 自 dev 库 2026-11-20 导出）
SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;
START TRANSACTION;
INSERT INTO `sys_charge_policy` (`id`, `policy_code`, `policy_name`, `item_code`, `item_name`, `unit`, `catalog_type`, `yb_note`, `status`, `remark`, `create_by`, `create_time`, `update_by`, `update_time`, `create_by_id`, `update_by_id`)
VALUES
(9130000000000000001, 'OPD_CONSULT', '门诊诊查费(普通门诊)', 'ZCF', '门诊诊查费(普通门诊)', '次', 3, '主项 011102020010000；加收子码 001/002/003', 1, '湘医保发〔2025〕58号附件1：挂号费并入门诊诊查费，按医师职级四档', 'admin', '2026-10-28 15:10:31', 'admin', '2026-10-28 15:10:31', 1, 1),
(9130000000000000002, 'EMG_CONSULT', '急诊诊查费', 'JZCF', '急诊诊查费', '次', 3, '110200003「急诊诊察费」', 1, '国家价格项目110200003，与门诊分立定价；知名专家档急诊不适用；先救治后付费', 'admin', '2026-10-28 15:10:31', 'admin', '2026-10-28 15:10:31', 1, 1);
COMMIT;

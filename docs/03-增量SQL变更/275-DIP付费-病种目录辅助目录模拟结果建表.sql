-- =============================================================
-- 275-DIP付费改造：病种目录 · 辅助目录 · 模拟结果建表
-- 说明：DIP（按病种分值付费）数据层落地，与现有 DRG 3.0（sys_drg_*）平行，两套互不混装。
--   1) biz_dip_disease    病种目录（核心病种 + 综合病种）        对应 sys_drg_group 的角色
--   2) biz_dip_aux        辅助目录（病例组合系数）               对应 sys_drg_ccmcc 的角色
--   3) biz_dip_sim_result DIP 分组模拟结果                      对应 biz_drg_sim_result
--   4) sys_config 新增系统参数：
--      medpay.mode        住院结算主付费方式（DRG / DIP，一主一备，统筹区选定后切换）
--      dip.default_version DIP 病种库默认版本（占位 2.0，按统筹区下发实际版本调整）
--      dip.point_value     DIP 结算点值（元/分值，统筹区公布，未配置不计算结算金额）
--   5) 菜单与授权：DIP 分组模拟（菜单 908，权限 report:dip:list，授权角色 1/17/18，镜像 907）
-- 命名约定：按需求，DIP 相关表统一以 biz_dip 开头（与 sys_drg_* 的 sys 前缀不同，DIP 线全部走 biz 前缀）。
-- 唯一键均含 version + del_flag：多版本病种库可并存、软删后可重建同名行不冲突。
-- id 段：27500000000000001xx（病种）/ 27500000000000002xx（辅助目录）/
--        27500000000000003xx（模拟结果）/ 27500000000000000xx（系统参数），与雪花 id、既有铺底文件错开。
-- =============================================================

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS=0;
START TRANSACTION;

-- -------------------------------------------------------------
-- 1. biz_dip_disease  DIP 病种目录（核心病种 + 综合病种）
--    核心病种：主诊断（贯标码）+ 手术操作（贯标码）组合精确匹配；
--    综合病种：无法进入核心病种的病例，按主诊断归组（一般为前缀匹配）。
--    基准分值 base_score 与 DRG weight 同口径：官方/统筹区方案包未下发时保持 NULL，
--    模拟页渲染为「—」，盈亏算不出，不编造数值。
-- -------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `biz_dip_disease`
(
    `id`             bigint        NOT NULL COMMENT '主键ID',
    `disease_code`   varchar(32)   NOT NULL COMMENT '病种编码（核心病种=统筹区/医保局下发病种库编码；综合病种=按主诊断归组的目录码）',
    `disease_name`   varchar(200)  NOT NULL COMMENT '病种名称',
    `disease_type`   tinyint       NOT NULL COMMENT '病种类型（1-核心病种 2-综合病种）',
    `main_diag_code` varchar(32)   COMMENT '主诊断编码（医保版贯标码；核心病种=完整贯标码，综合病种=归组用的目录码/前缀码）',
    `main_diag_name` varchar(200)  COMMENT '主诊断名称（快照）',
    `oper_code`      varchar(32)   COMMENT '手术操作编码（医保版贯标码；核心病种的手术操作，可空=内科/保守治疗病种）',
    `oper_name`      varchar(200)  COMMENT '手术操作名称（快照）',
    `diag_match`     tinyint       NOT NULL DEFAULT '1' COMMENT '主诊断匹配方式（1-整码 2-前缀；综合病种一般为前缀归组）',
    `base_score`     decimal(10,4) COMMENT '病种基准分值（统筹区下发；未下发为 NULL，同 DRG weight 口径）',
    `source`         varchar(64)   COMMENT '来源（如：国家DIP病种库2.0 / 统筹区本地化病种库 / 院内自建）',
    `version`        varchar(32)   NOT NULL COMMENT '病种库版本（与系统参数 dip.default_version 对应）',
    `status`         tinyint       NOT NULL COMMENT '状态（0-停用 1-启用）',
    `create_by`      varchar(64)   NOT NULL COMMENT '创建人',
    `create_by_id`   bigint        COMMENT '创建人ID',
    `create_time`    datetime      NOT NULL COMMENT '创建时间',
    `update_by`      varchar(64)   NOT NULL COMMENT '更新人',
    `update_by_id`   bigint        COMMENT '更新人ID',
    `update_time`    datetime      NOT NULL COMMENT '更新时间',
    `del_flag`       tinyint       NOT NULL DEFAULT '0' COMMENT '删除标志（0-正常 1-删除）',
    `remark`         varchar(500)  COMMENT '备注',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_disease` (`disease_code`, `version`, `del_flag`),
    KEY `idx_diag_oper` (`main_diag_code`, `oper_code`, `version`, `status`),
    KEY `idx_type` (`disease_type`, `version`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = 'DIP 病种目录（核心病种+综合病种）';

-- -------------------------------------------------------------
-- 2. biz_dip_aux  DIP 辅助目录（病例组合系数）
--    命中辅助目录后：结算分值 = 基准分值 × 病例组合系数。
--    辅助目录针对核心病种，同一病种下按病例属性（年龄/合并症/肿瘤分级）给不同系数；
--    disease_code 为空表示该规则适用于全部病种。
-- -------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `biz_dip_aux`
(
    `id`           bigint        NOT NULL COMMENT '主键ID',
    `disease_code` varchar(32)   COMMENT '绑定病种编码（空=适用于全部病种；非空=仅该病种生效）',
    `aux_type`     tinyint       NOT NULL COMMENT '辅助目录类型（1-年龄 2-合并症并发症 3-肿瘤严重程度/恶性程度 4-其他病例属性）',
    `aux_code`     varchar(32)   COMMENT '属性条件编码（合并症/肿瘤用 ICD 码；年龄型为空）',
    `aux_name`     varchar(200)  COMMENT '属性条件名称（快照）',
    `age_min`      int           COMMENT '年龄下限（含，岁；仅年龄型辅助目录使用）',
    `age_max`      int           COMMENT '年龄上限（含，岁；仅年龄型辅助目录使用）',
    `coeff`        decimal(6,4)  NOT NULL COMMENT '病例组合系数（分值调整倍率，如 1.0500 / 0.9000）',
    `source`       varchar(64)   COMMENT '来源',
    `version`      varchar(32)   NOT NULL COMMENT '病种库版本',
    `status`       tinyint       NOT NULL COMMENT '状态（0-停用 1-启用）',
    `create_by`    varchar(64)   NOT NULL COMMENT '创建人',
    `create_by_id` bigint        COMMENT '创建人ID',
    `create_time`  datetime      NOT NULL COMMENT '创建时间',
    `update_by`    varchar(64)   NOT NULL COMMENT '更新人',
    `update_by_id` bigint        COMMENT '更新人ID',
    `update_time`  datetime      NOT NULL COMMENT '更新时间',
    `del_flag`     tinyint       NOT NULL DEFAULT '0' COMMENT '删除标志（0-正常 1-删除）',
    `remark`       varchar(500)  COMMENT '备注',
    PRIMARY KEY (`id`),
    KEY `idx_disease` (`disease_code`, `version`, `status`),
    KEY `idx_aux_type` (`aux_type`, `version`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = 'DIP 辅助目录（病例组合系数）';

-- -------------------------------------------------------------
-- 3. biz_dip_sim_result  DIP 分组模拟结果
--    每首页一条，重跑覆盖（含软删行复活，唯一键同 biz_drg_sim_result）。
--    结算分值 = 基准分值 × 病例组合系数；预测结算金额 = 结算分值 × 点值；
--    盈亏 = 结算金额 - 实际费用。点值为统筹区公布快照，未下发时相关金额为 NULL。
-- -------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `biz_dip_sim_result`
(
    `id`             bigint        NOT NULL COMMENT '主键ID',
    `summary_id`     bigint        NOT NULL COMMENT '病案首页ID',
    `patient_name`   varchar(50)   COMMENT '患者姓名（快照）',
    `main_diag_code` varchar(32)   COMMENT '分组时使用的主诊断编码（快照）',
    `main_diag_name` varchar(200)  COMMENT '主诊断名称（快照）',
    `oper_code`      varchar(32)   COMMENT '分组时使用的手术操作编码（快照，取主要操作）',
    `oper_name`      varchar(200)  COMMENT '手术操作名称（快照）',
    `disease_type`   tinyint       COMMENT '入组病种类型（1-核心病种 2-综合病种）',
    `dip_code`       varchar(32)   NOT NULL COMMENT '病种编码',
    `dip_name`       varchar(200)  COMMENT '病种名称',
    `version`        varchar(32)   NOT NULL DEFAULT '2.0' COMMENT '病种库版本（默认 2.0，占位，按统筹区实际版本调整）',
    `base_score`     decimal(10,4) COMMENT '基准分值',
    `coeff`          decimal(6,4)  COMMENT '病例组合系数（辅助目录命中系数，未命中为 1.0000）',
    `score`          decimal(10,4) COMMENT '结算分值 = 基准分值 × 病例组合系数',
    `point_value`    decimal(10,6) COMMENT '点值（元/分值，统筹区公布，快照）',
    `settle_amount`  decimal(10,2) COMMENT '预测结算金额 = 结算分值 × 点值',
    `actual_amount`  decimal(10,2) COMMENT '实际住院费用',
    `profit_amount`  decimal(10,2) COMMENT '盈亏 = 结算金额 - 实际费用',
    `sim_status`     tinyint       NOT NULL COMMENT '结果（1-已入组 2-未入组）',
    `rule_note`      varchar(300)  COMMENT '命中路径说明',
    `create_by`      varchar(64)   NOT NULL COMMENT '创建人',
    `create_by_id`   bigint        COMMENT '创建人ID',
    `create_time`    datetime      NOT NULL COMMENT '创建时间',
    `update_by`      varchar(64)   NOT NULL COMMENT '更新人',
    `update_by_id`   bigint        COMMENT '更新人ID',
    `update_time`    datetime      NOT NULL COMMENT '更新时间',
    `del_flag`       tinyint       NOT NULL DEFAULT '0' COMMENT '删除标志（0-正常 1-删除）',
    `remark`         varchar(500)  COMMENT '备注',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_summary` (`summary_id`, `version`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = 'DIP 分组模拟结果';

-- -------------------------------------------------------------
-- 4. sys_config 系统参数（DRG/DIP 切换入口，守卫按 config_key 判重，可重复执行）
--    medpay.mode：住院结算主付费方式，DRG / DIP 二选一（一主一备）；
--    dip.default_version：DIP 病种库默认版本，占位 2.0，按统筹区下发实际版本调整。
--    切换的运行时机制复用 DRG 现有模式：sys_config 落库 + Redis 缓存 + 运行接口
--    （DRG 为 POST /api/report/drg/version，DIP 落地后镜像实现）。
-- -------------------------------------------------------------
INSERT INTO sys_config (config_id, config_name, config_key, config_value, config_type, is_system, remark,
                        create_by, update_by, create_by_id, update_by_id, create_time, update_time)
SELECT '2750000000000000001', '住院结算主付费方式', 'medpay.mode', 'DRG', 1, 0,
       '住院结算按病种付费方式：DRG / DIP 二选一（一主一备，按统筹区选定切换；当前仓库 DRG 已实现，DIP 落地后切换生效）',
       'admin', 'admin', '1', '1', NOW(), NOW()
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM sys_config WHERE config_key = 'medpay.mode');

INSERT INTO sys_config (config_id, config_name, config_key, config_value, config_type, is_system, remark,
                        create_by, update_by, create_by_id, update_by_id, create_time, update_time)
SELECT '2750000000000000002', 'DIP 病种库默认版本', 'dip.default_version', '2.0', 1, 0,
       '占位值：按目标统筹区下发的 DIP 病种库实际版本调整（国家 DIP 病种库 2.0 版 2024-07 发布）',
       'admin', 'admin', '1', '1', NOW(), NOW()
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM sys_config WHERE config_key = 'dip.default_version');

INSERT INTO sys_config (config_id, config_name, config_key, config_value, config_type, is_system, remark,
                        create_by, update_by, create_by_id, update_by_id, create_time, update_time)
SELECT '2750000000000000003', 'DIP 结算点值', 'dip.point_value', NULL, 1, 0,
       '点值（元/分值，统筹区公布）；未配置时结算金额与盈亏不计算。示例格式：0.800000',
       'admin', 'admin', '1', '1', NOW(), NOW()
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM sys_config WHERE config_key = 'dip.point_value');

-- -------------------------------------------------------------
-- 5. 菜单与授权：DIP 分组模拟（镜像 907 DRG 模拟器；菜单 907 授权角色 1/17/18，DIP 同口径）
--    注意：原计划用 id='908'，但仓库内 908 已被「绩效与成本核算」占用（menu_key=finance.performance），
--    本脚本改为 id='910'（2026-10-19 278 号修正确认）；若执行环境已有 908 冲突，请改用 278 脚本。
-- -------------------------------------------------------------
INSERT INTO sys_menu (id, menu_name, parent_id, sort_order, menu_type, path, component, menu_key, icon, permission,
                      is_frame, is_cache, is_visible, status, create_by, update_by, del_flag, remark, create_by_id,
                      update_by_id, create_time, update_time)
SELECT '910', 'DIP 分组模拟', '1200', 21, 2, '/dipsim', 'drg/DipSimView', 'report.dipsim', 'icon-suanfa',
       'report:dip:list', 0, 0, 1, 1,
       'admin', 'admin', 0, NULL, '1', '1', NOW(), NOW()
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE id = '910' OR menu_key = 'report.dipsim' OR permission = 'report:dip:list');

INSERT INTO sys_role_menu (id, role_id, menu_id, create_by, update_by, create_by_id, update_by_id, create_time, update_time)
SELECT '2750000000000000901', '1', '910', 'admin', 'admin', '1', '1', NOW(), NOW()
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM sys_role_menu WHERE role_id = '1' AND menu_id = '910');

INSERT INTO sys_role_menu (id, role_id, menu_id, create_by, update_by, create_by_id, update_by_id, create_time, update_time)
SELECT '2750000000000000902', '17', '910', 'admin', 'admin', '1', '1', NOW(), NOW()
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM sys_role_menu WHERE role_id = '17' AND menu_id = '910');

INSERT INTO sys_role_menu (id, role_id, menu_id, create_by, update_by, create_by_id, update_by_id, create_time, update_time)
SELECT '2750000000000000903', '18', '910', 'admin', 'admin', '1', '1', NOW(), NOW()
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM sys_role_menu WHERE role_id = '18' AND menu_id = '910');

COMMIT;

-- 回查（人工核对用，勿删）：
-- SELECT COUNT(*) FROM biz_dip_disease WHERE del_flag = 0;
-- SELECT COUNT(*) FROM biz_dip_aux WHERE del_flag = 0;
-- SELECT COUNT(*) FROM biz_dip_sim_result WHERE del_flag = 0;
-- SELECT config_id, config_key, config_value FROM sys_config WHERE config_key IN ('medpay.mode', 'dip.default_version', 'dip.point_value');
-- SELECT id, menu_name, permission FROM sys_menu WHERE id IN ('910');
-- SELECT role_id, menu_id FROM sys_role_menu WHERE menu_id = '910';

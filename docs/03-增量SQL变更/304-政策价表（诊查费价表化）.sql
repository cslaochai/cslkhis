-- ============================================================================
-- 304 - 政策价表（诊查费价格价表化）
--
-- 【为什么要有这两张表】
-- 诊查费类「政策价」原先硬编码在代码里：
--   · 门诊诊查费 9/22/31/70 → ClinicDiagnosisTierEnum.getAmount()
--   · 急诊诊查费 10/23/33   → EmergencyConsultFeePolicy 私有常量
-- 改价要发版。本轮把价格搬进数据库，价格管理页新增「政策价」页签可调价，
-- 运行时取价一律走价表（ChargeFacadeService.policyPrice）。
--
-- 【两个政策项目】
--   OPD_CONSULT 门诊诊查费(普通门诊)：湘医保发〔2025〕58号附件1，挂号费已并入，
--               四档 = 普通9 / 副主任22 / 主任31 / 知名专家70，
--               副主任及以上按「主项目 + 加收子码」结构（surcharge 列）。
--   EMG_CONSULT 急诊诊查费：国家价格项目 110200003「急诊诊察费」，与门诊是
--               两个独立项目，三档 = 普通10 / 副主任23 / 主任33（无加收子码，
--               知名专家档急诊不适用——名单制号源急诊没有）。
--
-- 【取价口径】
--   · 急诊：接诊/终态时实时查价表，价表缺行或停用 → 只 log 不记账不拦接诊。
--   · 门诊：号源发布时把价表金额快照到 biz_clinic_source.diagnosis_fee
--           （价格是号源快照，医生升职称后已排出的号不变）；
--           复诊拆「主项目+加收」两行时实时查价表，缺行回退枚举兜底价。
--   · 儿童加收 30% 是全国统一政策比率（湘医保发〔2025〕58号），不是院内可调价，
--     仍由 PediatricSurchargePolicy 表达，不入价表。
--
-- 【删除口径】
-- 两张表都有业务唯一键（policy_code / policy_code+tier_code），一律物理删除，
-- 不设 del_flag（逻辑删除占唯一键，「删了再建同码」撞 Duplicate entry）。
--
-- 【雪花号段】
-- 政策行用 913 段、价目行用 914 段，与 300 号 SQL 的 911/912 段及各表存量互不重叠。
-- ============================================================================

-- ---------------- 1. 政策定义 ----------------
CREATE TABLE IF NOT EXISTS sys_charge_policy
(
    id           BIGINT       NOT NULL COMMENT '主键ID',
    policy_code  VARCHAR(32)  NOT NULL COMMENT '政策编码（OPD_CONSULT-门诊诊查费 EMG_CONSULT-急诊诊查费）',
    policy_name  VARCHAR(64)  NOT NULL COMMENT '政策项目名称（物价目录名）',
    item_code    VARCHAR(32)  NOT NULL COMMENT '院内费用行项目编码（biz_fee_record.item_code，如 ZCF/JZCF）',
    item_name    VARCHAR(64)  NOT NULL COMMENT '院内费用行项目名称',
    unit         VARCHAR(16)  NOT NULL DEFAULT '次' COMMENT '计价单位',
    catalog_type TINYINT      NULL COMMENT '支付分类（FeeCatalogTypeEnum：3-丙类，湘医保发〔2025〕58号诊查类全自费）',
    yb_note      VARCHAR(64)  NULL COMMENT '国家医保编码说明（档位子码见价目行 yb_code）',
    status       TINYINT      NOT NULL DEFAULT 1 COMMENT '状态：0-停用 1-启用（停用后运行时按缺行降级）',
    remark       VARCHAR(255) NULL COMMENT '备注',
    create_by    VARCHAR(64)  NOT NULL DEFAULT '' COMMENT '创建人',
    create_time  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_by    VARCHAR(64)  NOT NULL DEFAULT '' COMMENT '更新人',
    update_time  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    create_by_id BIGINT       NOT NULL DEFAULT 0 COMMENT '创建人ID',
    update_by_id BIGINT       NOT NULL DEFAULT 0 COMMENT '更新人ID',
    PRIMARY KEY (id),
    UNIQUE KEY uk_policy_code (policy_code)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci COMMENT = '收费政策价定义（诊查费等政策类项目）';

-- ---------------- 2. 政策价目（按档位） ----------------
CREATE TABLE IF NOT EXISTS sys_charge_policy_price
(
    id          BIGINT        NOT NULL COMMENT '主键ID',
    policy_code VARCHAR(32)   NOT NULL COMMENT '政策编码（sys_charge_policy.policy_code）',
    tier_code   TINYINT       NOT NULL COMMENT '档位码（对齐 ClinicDiagnosisTierEnum：1-普通 2-副主任 3-主任 4-知名专家）',
    tier_label  VARCHAR(32)   NOT NULL COMMENT '档位名称',
    yb_code     VARCHAR(32)   NULL COMMENT '国家医保编码（门诊档位子码 011102020010000~003 / 急诊 110200003）',
    amount      DECIMAL(10,2) NOT NULL COMMENT '全额（元）',
    surcharge   DECIMAL(10,2) NULL COMMENT '加收额（元）：门诊副主任及以上拆「主项目+加收」两行用，无加收为 NULL',
    sort_order  INT           NOT NULL DEFAULT 0 COMMENT '排序号',
    status      TINYINT       NOT NULL DEFAULT 1 COMMENT '状态：0-停用 1-启用（停用后该档按缺行降级）',
    remark      VARCHAR(255)  NULL COMMENT '备注',
    create_by   VARCHAR(64)   NOT NULL DEFAULT '' COMMENT '创建人',
    create_time DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_by   VARCHAR(64)   NOT NULL DEFAULT '' COMMENT '更新人',
    update_time DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    create_by_id BIGINT       NOT NULL DEFAULT 0 COMMENT '创建人ID',
    update_by_id BIGINT       NOT NULL DEFAULT 0 COMMENT '更新人ID',
    PRIMARY KEY (id),
    UNIQUE KEY uk_policy_tier (policy_code, tier_code)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci COMMENT = '收费政策价目（政策×档位的价格行）';

-- ---------------- 3. 种子数据（与改造前代码内常量等值，行为无缝迁移） ----------------
INSERT INTO sys_charge_policy (id, policy_code, policy_name, item_code, item_name, unit, catalog_type, yb_note, status, remark, create_by, update_by, create_by_id, update_by_id)
VALUES
(9130000000000000001, 'OPD_CONSULT', '门诊诊查费(普通门诊)', 'ZCF', '门诊诊查费(普通门诊)', '次', 3,
 '主项 011102020010000；加收子码 001/002/003', 1,
 '湘医保发〔2025〕58号附件1：挂号费并入门诊诊查费，按医师职级四档', 'admin', 'admin', 1, 1),
(9130000000000000002, 'EMG_CONSULT', '急诊诊查费', 'JZCF', '急诊诊查费', '次', 3,
 '110200003「急诊诊察费」', 1,
 '国家价格项目110200003，与门诊分立定价；知名专家档急诊不适用；先救治后付费', 'admin', 'admin', 1, 1);

INSERT INTO sys_charge_policy_price (id, policy_code, tier_code, tier_label, yb_code, amount, surcharge, sort_order, status, remark, create_by, update_by, create_by_id, update_by_id)
VALUES
-- 门诊四档（9/22/31/70；副主任及以上带加收额 13/22/61）
(9140000000000000001, 'OPD_CONSULT', 1, '普通',          '011102020010000', 9.00,  NULL, 1, 1, '主治及以下', 'admin', 'admin', 1, 1),
(9140000000000000002, 'OPD_CONSULT', 2, '副主任医师',    '011102020010001', 22.00, 13.00, 2, 1, '主项9+加收13', 'admin', 'admin', 1, 1),
(9140000000000000003, 'OPD_CONSULT', 3, '主任医师',      '011102020010002', 31.00, 22.00, 3, 1, '主项9+加收22', 'admin', 'admin', 1, 1),
(9140000000000000004, 'OPD_CONSULT', 4, '知名专家',      '011102020010003', 70.00, 61.00, 4, 1, '名单制认定，号源上手工指定', 'admin', 'admin', 1, 1),
-- 急诊三档（10/23/33；无加收子码，全额一行）
(9140000000000000005, 'EMG_CONSULT', 1, '普通',          '110200003', 10.00, NULL, 1, 1, '主治及以下', 'admin', 'admin', 1, 1),
(9140000000000000006, 'EMG_CONSULT', 2, '副主任医师',    '110200003', 23.00, NULL, 2, 1, NULL, 'admin', 'admin', 1, 1),
(9140000000000000007, 'EMG_CONSULT', 3, '主任医师',      '110200003', 33.00, NULL, 3, 1, '知名专家档急诊不适用，401 按主任档', 'admin', 'admin', 1, 1);

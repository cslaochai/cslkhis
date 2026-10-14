-- =====================================================================
-- 医保结算经办（医保局端）模拟平台 建表脚本（MySQL，独立库 mss_insurance）
-- 启动幂等执行，全部 IF NOT EXISTS。库本身由 JDBC URL createDatabaseIfNotExist 自动创建。
-- =====================================================================

-- 参保人（医保侧参保库）
CREATE TABLE IF NOT EXISTS mss_insured_person (
    id               BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    id_card          VARCHAR(32)  DEFAULT NULL COMMENT '身份证号',
    insurance_no     VARCHAR(64)  DEFAULT NULL COMMENT '医保卡号/医保电子凭证号',
    name             VARCHAR(64)  DEFAULT NULL COMMENT '姓名',
    gender           VARCHAR(8)   DEFAULT NULL COMMENT '性别',
    insurance_type   VARCHAR(16)  DEFAULT NULL COMMENT '险种（2-城镇职工医保 3-城乡居民医保）',
    person_status    INT          DEFAULT 1 COMMENT '参保状态（1-正常参保 0-停保）',
    account_balance  DECIMAL(12,2) DEFAULT 0 COMMENT '个人账户余额',
    region           VARCHAR(64)  DEFAULT '长沙市' COMMENT '参保地',
    create_time      DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_person_idcard (id_card)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='参保人（医保侧参保库）';

-- 平台侧结算单（2304 接收后独立落库，即“对方账”）
CREATE TABLE IF NOT EXISTS mss_settlement (
    id               BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    trade_no         VARCHAR(64)  DEFAULT NULL COMMENT 'HIS 侧流水号',
    settlement_no    VARCHAR(64)  DEFAULT NULL COMMENT '医保结算清单号',
    orig_trade_no    VARCHAR(64)  DEFAULT NULL COMMENT '被撤销流水号（2305）',
    fix_medins_code  VARCHAR(32)  DEFAULT NULL COMMENT '定点机构编码',
    bill_no          VARCHAR(64)  DEFAULT NULL COMMENT 'HIS 账单号',
    encounter_type   VARCHAR(16)  DEFAULT NULL COMMENT '就诊类型（门诊/住院）',
    patient_name     VARCHAR(64)  DEFAULT NULL COMMENT '患者姓名',
    id_card          VARCHAR(32)  DEFAULT NULL COMMENT '身份证号',
    insurance_no     VARCHAR(64)  DEFAULT NULL COMMENT '医保卡号',
    insurance_type   VARCHAR(16)  DEFAULT NULL COMMENT '险种',
    total_amount     DECIMAL(12,2) DEFAULT 0 COMMENT '总费用',
    insurance_pay    DECIMAL(12,2) DEFAULT 0 COMMENT '院方申报统筹支付',
    personal_pay     DECIMAL(12,2) DEFAULT 0 COMMENT '院方申报个人账户支付',
    self_pay         DECIMAL(12,2) DEFAULT 0 COMMENT '院方申报现金自付',
    pool_confirm     DECIMAL(12,2) DEFAULT 0 COMMENT '平台核定统筹支付',
    status           INT          DEFAULT 1 COMMENT '状态（1-已接收有效 2-已撤销冲正）',
    bill_date        DATE         DEFAULT NULL COMMENT '账期日期',
    receipt_no       VARCHAR(64)  DEFAULT NULL COMMENT '医保端回执编号',
    warn_msg         VARCHAR(500) DEFAULT NULL COMMENT '平台告警（非阻断）',
    raw_payload      TEXT COMMENT '2304 原始报文',
    create_time      DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    cancel_time      DATETIME     DEFAULT NULL COMMENT '撤销时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_sett_trade (trade_no),
    UNIQUE KEY uk_sett_settlementno (settlement_no),
    KEY idx_sett_billdate (bill_date),
    KEY idx_sett_month (fix_medins_code, bill_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='平台侧结算单（对方账）';

-- 报文收发台账（2304/2305 全留痕）
CREATE TABLE IF NOT EXISTS mss_message (
    id               BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    msg_type         VARCHAR(16)  DEFAULT NULL COMMENT '报文类型（2304/2305）',
    trade_no         VARCHAR(64)  DEFAULT NULL COMMENT 'HIS 侧流水号',
    orig_trade_no    VARCHAR(64)  DEFAULT NULL COMMENT '被撤销流水号',
    success          INT          DEFAULT 1 COMMENT '是否成功（1-是 0-否）',
    receipt_no       VARCHAR(64)  DEFAULT NULL COMMENT '回执编号',
    err_msg          VARCHAR(500) DEFAULT NULL COMMENT '错误信息',
    payload          TEXT COMMENT '原始报文',
    create_time      DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (id),
    KEY idx_msg_trade (trade_no)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='报文收发台账';

-- 月度结算单（申报→审核→拨付→清算）
CREATE TABLE IF NOT EXISTS mss_monthly (
    id               BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    month            VARCHAR(7)   DEFAULT NULL COMMENT '账期（yyyy-MM）',
    fix_medins_code  VARCHAR(32)  DEFAULT NULL COMMENT '定点机构编码',
    declare_count    INT          DEFAULT 0 COMMENT '申报结算单笔数',
    declare_amount   DECIMAL(12,2) DEFAULT 0 COMMENT '申报统筹合计',
    deduct_amount    DECIMAL(12,2) DEFAULT 0 COMMENT '审核扣款/拒付合计',
    reserve_amount   DECIMAL(12,2) DEFAULT 0 COMMENT '质量保证金预留',
    payable_amount   DECIMAL(12,2) DEFAULT 0 COMMENT '本月应拨付',
    allocated_amount DECIMAL(12,2) DEFAULT 0 COMMENT '已拨付',
    status           INT          DEFAULT 1 COMMENT '状态（1-已申报 2-已审核 3-已拨付 4-已清算）',
    declare_time     DATETIME     DEFAULT NULL COMMENT '申报时间',
    audit_time       DATETIME     DEFAULT NULL COMMENT '审核时间',
    allocate_time    DATETIME     DEFAULT NULL COMMENT '拨付时间',
    settle_time      DATETIME     DEFAULT NULL COMMENT '年终清算时间',
    create_time      DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_monthly_month (month)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='月度结算单';

-- 月度扣款/拒付明细
CREATE TABLE IF NOT EXISTS mss_monthly_deduct (
    id               BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    month            VARCHAR(7)   DEFAULT NULL COMMENT '账期（yyyy-MM）',
    trade_no         VARCHAR(64)  DEFAULT NULL COMMENT 'HIS 侧流水号',
    settlement_no    VARCHAR(64)  DEFAULT NULL COMMENT '结算清单号',
    deduct_type      INT          DEFAULT 1 COMMENT '扣款类型（1-拒付 2-审核扣款）',
    deduct_amount    DECIMAL(12,2) DEFAULT 0 COMMENT '扣款金额',
    reason           VARCHAR(255) DEFAULT NULL COMMENT '扣款原因',
    create_time      DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (id),
    KEY idx_deduct_month (month)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='月度扣款/拒付明细';

-- 拨付单（月度拨付 / 质保金返还）
CREATE TABLE IF NOT EXISTS mss_allocation (
    id               BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    month            VARCHAR(7)   DEFAULT NULL COMMENT '账期（yyyy-MM）',
    alloc_no         VARCHAR(64)  DEFAULT NULL COMMENT '拨付单号',
    alloc_type       INT          DEFAULT 1 COMMENT '拨付类型（1-月度拨付 2-质保金返还/年清算）',
    amount           DECIMAL(12,2) DEFAULT 0 COMMENT '拨付金额',
    remark           VARCHAR(255) DEFAULT NULL COMMENT '备注',
    create_time      DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (id),
    KEY idx_alloc_month (month)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='医保拨付单';

-- =====================================================================
-- 经办业务（医保中心窗口 / 经办平台）表
-- =====================================================================

-- 医保三大目录（西药/中成药/中草药/诊疗项目/医用耗材）
CREATE TABLE IF NOT EXISTS mss_catalog (
    id               BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    yb_code          VARCHAR(32)  DEFAULT NULL COMMENT '医保编码',
    name             VARCHAR(128) DEFAULT NULL COMMENT '目录名称',
    category         VARCHAR(32)  DEFAULT NULL COMMENT '类别（西药/中成药/中草药/诊疗项目/医用耗材）',
    spec             VARCHAR(64)  DEFAULT NULL COMMENT '规格',
    unit             VARCHAR(16)  DEFAULT NULL COMMENT '单位',
    pay_class        VARCHAR(8)   DEFAULT NULL COMMENT '支付类别（甲/乙/丙）',
    self_ratio       DECIMAL(5,2) DEFAULT 0 COMMENT '乙类先行自付比例（0-1）',
    insurance_ratio  DECIMAL(5,2) DEFAULT 0 COMMENT '政策报销比例（0-1）',
    limit_price      DECIMAL(12,2) DEFAULT 0 COMMENT '医保限价',
    status           INT          DEFAULT 1 COMMENT '状态（1-启用 0-停用）',
    create_time      DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time      DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    KEY idx_cat_name (name),
    KEY idx_cat_cat (category)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='医保三大目录';

-- 院内项目 ↔ 医保目录对照
CREATE TABLE IF NOT EXISTS mss_item_mapping (
    id               BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    hosp_item_code   VARCHAR(32)  DEFAULT NULL COMMENT '院内项目编码',
    hosp_item_name   VARCHAR(128) DEFAULT NULL COMMENT '院内项目名称',
    category         VARCHAR(32)  DEFAULT NULL COMMENT '类别',
    yb_code          VARCHAR(32)  DEFAULT NULL COMMENT '对照医保编码',
    yb_name          VARCHAR(128) DEFAULT NULL COMMENT '对照医保名称',
    pay_class        VARCHAR(8)   DEFAULT NULL COMMENT '支付类别（甲/乙/丙）',
    mapped           TINYINT(1)   DEFAULT 0 COMMENT '是否已对照（1-是 0-否）',
    updated_at       DATETIME     DEFAULT NULL COMMENT '对照时间',
    create_time      DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (id),
    KEY idx_map_hosp (hosp_item_name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='院内项目医保对照';

-- 待结算业务（门诊就诊 / 出院登记）
CREATE TABLE IF NOT EXISTS mss_pending_settle (
    id               BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    biz_type         INT          DEFAULT 1 COMMENT '业务类型（1-门诊 2-住院）',
    biz_no           VARCHAR(32)  DEFAULT NULL COMMENT '业务号（门诊就诊号/住院号）',
    patient_name     VARCHAR(64)  DEFAULT NULL COMMENT '患者姓名',
    id_card          VARCHAR(32)  DEFAULT NULL COMMENT '身份证号',
    id_card_mask     VARCHAR(32)  DEFAULT NULL COMMENT '脱敏身份证号',
    insurance_type   VARCHAR(16)  DEFAULT NULL COMMENT '参保类型（职工医保/居民医保/异地就医）',
    dept             VARCHAR(64)  DEFAULT NULL COMMENT '科室',
    doctor           VARCHAR(64)  DEFAULT NULL COMMENT '医师',
    admit_days       INT          DEFAULT NULL COMMENT '住院天数',
    biz_time         DATETIME     DEFAULT NULL COMMENT '就诊/出院时间',
    status           INT          DEFAULT 1 COMMENT '状态（1-待结算 2-已结算）',
    create_time      DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_pending_biz (biz_no),
    KEY idx_pending_type (biz_type, status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='待结算业务';

-- 经办结算单（窗口即时结算产生）
CREATE TABLE IF NOT EXISTS mss_bill (
    id               BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    bill_no          VARCHAR(32)  DEFAULT NULL COMMENT '结算单号',
    pending_id       BIGINT       DEFAULT NULL COMMENT '来源待结算业务id',
    biz_no           VARCHAR(32)  DEFAULT NULL COMMENT '业务号（就诊号/住院号）',
    admit_days       INT          DEFAULT NULL COMMENT '住院天数',
    patient_name     VARCHAR(64)  DEFAULT NULL COMMENT '患者姓名',
    id_card          VARCHAR(32)  DEFAULT NULL COMMENT '身份证号',
    id_card_mask     VARCHAR(32)  DEFAULT NULL COMMENT '脱敏身份证号',
    insurance_type   VARCHAR(16)  DEFAULT NULL COMMENT '参保类型',
    settle_type      VARCHAR(8)   DEFAULT NULL COMMENT '结算类型（门诊/住院）',
    dept             VARCHAR(64)  DEFAULT NULL COMMENT '科室',
    doctor           VARCHAR(64)  DEFAULT NULL COMMENT '医师',
    settle_time      DATETIME     DEFAULT NULL COMMENT '结算时间',
    status           VARCHAR(16)  DEFAULT '已结算' COMMENT '状态（已结算/待审核/已退费/异常）',
    total_amount     DECIMAL(12,2) DEFAULT 0 COMMENT '费用总额',
    insurance_pay    DECIMAL(12,2) DEFAULT 0 COMMENT '统筹支付',
    personal_account DECIMAL(12,2) DEFAULT 0 COMMENT '个人账户支付',
    cash_pay         DECIMAL(12,2) DEFAULT 0 COMMENT '现金支付',
    self_pay         DECIMAL(12,2) DEFAULT 0 COMMENT '个人自付合计',
    deductible       DECIMAL(12,2) DEFAULT 0 COMMENT '起付线',
    base_pool        DECIMAL(12,2) DEFAULT 0 COMMENT '统筹计入基数',
    create_time      DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time      DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_bill_no (bill_no),
    KEY idx_bill_time (settle_time),
    KEY idx_bill_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='经办结算单';

-- 费用明细（待结算 / 结算单共用：owner_type 1-待结算 2-结算单）
CREATE TABLE IF NOT EXISTS mss_fee_item (
    id               BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    owner_type       INT          DEFAULT 1 COMMENT '归属类型（1-待结算 2-结算单）',
    owner_id         BIGINT       DEFAULT NULL COMMENT '归属单id',
    name             VARCHAR(128) DEFAULT NULL COMMENT '项目名称',
    category         VARCHAR(32)  DEFAULT NULL COMMENT '类别',
    spec             VARCHAR(64)  DEFAULT NULL COMMENT '规格',
    unit             VARCHAR(16)  DEFAULT NULL COMMENT '单位',
    yb_code          VARCHAR(32)  DEFAULT NULL COMMENT '医保编码',
    pay_class        VARCHAR(8)   DEFAULT NULL COMMENT '支付类别（甲/乙/丙）',
    self_ratio       DECIMAL(5,2) DEFAULT 0 COMMENT '乙类先行自付比例',
    insurance_ratio  DECIMAL(5,2) DEFAULT 0 COMMENT '政策报销比例',
    qty              INT          DEFAULT 1 COMMENT '数量',
    price            DECIMAL(12,2) DEFAULT 0 COMMENT '单价',
    amount           DECIMAL(12,2) DEFAULT 0 COMMENT '金额',
    reversed         TINYINT(1)   DEFAULT 0 COMMENT '是否已冲正（1-是 0-否）',
    create_time      DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (id),
    KEY idx_item_owner (owner_type, owner_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='费用明细';

-- 费用审核任务
CREATE TABLE IF NOT EXISTS mss_fee_audit (
    id               BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    bill_id          BIGINT       DEFAULT NULL COMMENT '结算单id',
    bill_no          VARCHAR(32)  DEFAULT NULL COMMENT '结算单号',
    patient_name     VARCHAR(64)  DEFAULT NULL COMMENT '患者姓名',
    item_name        VARCHAR(128) DEFAULT NULL COMMENT '费用项目',
    category         VARCHAR(32)  DEFAULT NULL COMMENT '类别',
    pay_class        VARCHAR(8)   DEFAULT NULL COMMENT '支付类别（甲/乙/丙）',
    amount           DECIMAL(12,2) DEFAULT 0 COMMENT '金额',
    reason           VARCHAR(255) DEFAULT NULL COMMENT '审核要点',
    status           VARCHAR(16)  DEFAULT '待审核' COMMENT '状态（待审核/已通过/已驳回）',
    create_time      DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time      DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    KEY idx_audit_bill (bill_id),
    KEY idx_audit_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='费用审核任务';

-- 异地就医备案
CREATE TABLE IF NOT EXISTS mss_filing (
    id               BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    name             VARCHAR(64)  DEFAULT NULL COMMENT '姓名',
    id_card_mask     VARCHAR(32)  DEFAULT NULL COMMENT '脱敏身份证号',
    insurance_area   VARCHAR(64)  DEFAULT NULL COMMENT '参保地',
    hospital         VARCHAR(128) DEFAULT NULL COMMENT '就医医院',
    reason           VARCHAR(32)  DEFAULT NULL COMMENT '备案原因',
    from_area        VARCHAR(64)  DEFAULT NULL COMMENT '就医地',
    valid_start      DATE         DEFAULT NULL COMMENT '生效日',
    valid_end        DATE         DEFAULT NULL COMMENT '失效日',
    status           VARCHAR(16)  DEFAULT '审核中' COMMENT '状态（已备案/审核中/已失效）',
    apply_time       DATE         DEFAULT NULL COMMENT '申请日期',
    create_time      DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time      DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    KEY idx_filing_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='异地就医备案';

-- 清算对账（按月）
CREATE TABLE IF NOT EXISTS mss_recon (
    id               BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    period           VARCHAR(8)   DEFAULT NULL COMMENT '清算账期（yyyy-MM）',
    bill_count       INT          DEFAULT 0 COMMENT '结算笔数',
    settle_amount    DECIMAL(14,2) DEFAULT 0 COMMENT '申报结算额',
    insurance_pay    DECIMAL(14,2) DEFAULT 0 COMMENT '统筹申报支付',
    received_from_fund DECIMAL(14,2) DEFAULT 0 COMMENT '基金实际到账',
    diff             DECIMAL(14,2) DEFAULT 0 COMMENT '差额',
    status           VARCHAR(16)  DEFAULT '待对账' COMMENT '状态（待对账/已对账/存在差异）',
    create_time      DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time      DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_recon_period (period)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='清算对账';

-- =====================================================================
-- 系统基础（仿 HIS 01-系统基础：组织·用户·岗位·权限·参数·字典）
-- 独立库 mss_insurance 内使用 mss_ 前缀；删除均走软删（del_flag），
-- 岗位/角色/菜单这类唯一键不含 del_flag 的关联表删除走物理删。
-- =====================================================================

-- 经办科室（平台内部科室，岗位 = 角色 × 科室 中的“科室”）
CREATE TABLE IF NOT EXISTS mss_department (
    id               BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    dept_code        VARCHAR(32)  NOT NULL COMMENT '科室编码（唯一）',
    dept_name        VARCHAR(100) NOT NULL COMMENT '科室名称',
    dept_type        TINYINT      DEFAULT 1 COMMENT '科室类型（1-前台经办 2-审核稽核 3-基金财务 4-综合管理）',
    parent_id        BIGINT       NOT NULL DEFAULT 0 COMMENT '父科室ID',
    sort_order       INT          DEFAULT 0 COMMENT '排序号',
    dept_leader_id   BIGINT       DEFAULT NULL COMMENT '科室负责人（mss_employee.id）',
    phone            VARCHAR(20)  DEFAULT NULL COMMENT '联系电话',
    email            VARCHAR(100) DEFAULT NULL COMMENT '邮箱',
    status           TINYINT      DEFAULT 1 COMMENT '状态（0-停用 1-启用）',
    create_by        VARCHAR(64)  NOT NULL DEFAULT 'system' COMMENT '创建人',
    create_by_id     BIGINT       DEFAULT NULL COMMENT '创建人ID',
    create_time      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_by        VARCHAR(64)  NOT NULL DEFAULT 'system' COMMENT '更新人',
    update_by_id     BIGINT       DEFAULT NULL COMMENT '更新人ID',
    update_time      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    del_flag         TINYINT      NOT NULL DEFAULT 0 COMMENT '删除标志（0-正常 1-删除）',
    remark           VARCHAR(500) DEFAULT NULL COMMENT '备注',
    PRIMARY KEY (id),
    UNIQUE KEY uk_dept_code (dept_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='经办科室';

-- 角色
CREATE TABLE IF NOT EXISTS mss_role (
    id           BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    role_code    VARCHAR(32)  NOT NULL COMMENT '角色编码（唯一）',
    role_name    VARCHAR(100) NOT NULL COMMENT '角色名称',
    role_type    TINYINT      DEFAULT 1 COMMENT '角色类型（1-系统角色 2-自定义角色）',
    data_scope   TINYINT      DEFAULT 1 COMMENT '数据权限范围（1-全部数据 2-自定义数据 3-本部门数据 5-仅本人数据）',
    sort_order   INT          DEFAULT 0 COMMENT '排序号',
    status       TINYINT      DEFAULT 1 COMMENT '状态（0-停用 1-启用）',
    create_by    VARCHAR(64)  NOT NULL DEFAULT 'system' COMMENT '创建人',
    create_by_id BIGINT       DEFAULT NULL COMMENT '创建人ID',
    create_time  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_by    VARCHAR(64)  NOT NULL DEFAULT 'system' COMMENT '更新人',
    update_by_id BIGINT       DEFAULT NULL COMMENT '更新人ID',
    update_time  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    del_flag     TINYINT      NOT NULL DEFAULT 0 COMMENT '删除标志（0-正常 1-删除）',
    remark       VARCHAR(500) DEFAULT NULL COMMENT '备注',
    PRIMARY KEY (id),
    UNIQUE KEY uk_role_code (role_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色';

-- 菜单
CREATE TABLE IF NOT EXISTS mss_menu (
    id           BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    menu_name    VARCHAR(100) NOT NULL COMMENT '菜单名称',
    parent_id    BIGINT       DEFAULT 0 COMMENT '父菜单ID',
    sort_order   INT          DEFAULT 0 COMMENT '排序号',
    menu_type    TINYINT      DEFAULT 2 COMMENT '菜单类型（1-目录 2-菜单 3-按钮）',
    path         VARCHAR(200) DEFAULT NULL COMMENT '路由地址',
    component    VARCHAR(200) DEFAULT NULL COMMENT '组件路径',
    menu_key     VARCHAR(100) DEFAULT NULL COMMENT '菜单标识（唯一）',
    icon         VARCHAR(100) DEFAULT NULL COMMENT '图标',
    permission   VARCHAR(200) DEFAULT NULL COMMENT '权限标识',
    is_frame     TINYINT      DEFAULT 0 COMMENT '是否外链（0-否 1-是）',
    is_cache     TINYINT      DEFAULT 0 COMMENT '是否缓存（0-否 1-是）',
    is_visible   TINYINT      DEFAULT 1 COMMENT '是否可见（0-隐藏 1-显示）',
    status       TINYINT      DEFAULT 1 COMMENT '状态（0-停用 1-启用）',
    create_by    VARCHAR(64)  NOT NULL DEFAULT 'system' COMMENT '创建人',
    create_by_id BIGINT       DEFAULT NULL COMMENT '创建人ID',
    create_time  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_by    VARCHAR(64)  NOT NULL DEFAULT 'system' COMMENT '更新人',
    update_by_id BIGINT       DEFAULT NULL COMMENT '更新人ID',
    update_time  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    del_flag     TINYINT      NOT NULL DEFAULT 0 COMMENT '删除标志（0-正常 1-删除）',
    remark       VARCHAR(500) DEFAULT NULL COMMENT '备注',
    PRIMARY KEY (id),
    UNIQUE KEY uk_menu_key (menu_key),
    KEY idx_parent_id (parent_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='菜单';

-- 角色菜单关联（本表无 del_flag，删除走物理删）
CREATE TABLE IF NOT EXISTS mss_role_menu (
    id           BIGINT      NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    role_id      BIGINT      NOT NULL COMMENT '角色ID',
    menu_id      BIGINT      NOT NULL COMMENT '菜单ID',
    create_by    VARCHAR(64) NOT NULL DEFAULT 'system' COMMENT '创建人',
    create_by_id BIGINT      DEFAULT NULL COMMENT '创建人ID',
    create_time  DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_by    VARCHAR(64) NOT NULL DEFAULT 'system' COMMENT '更新人',
    update_by_id BIGINT      DEFAULT NULL COMMENT '更新人ID',
    update_time  DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_role_menu (role_id, menu_id),
    KEY idx_menu_id (menu_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色菜单关联';

-- 用户
CREATE TABLE IF NOT EXISTS mss_user (
    id                   BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    user_name            VARCHAR(64)  NOT NULL COMMENT '用户名（唯一）',
    password             VARCHAR(200) NOT NULL COMMENT '密码（BCrypt）',
    real_name            VARCHAR(64)  DEFAULT NULL COMMENT '真实姓名',
    emp_id               BIGINT       DEFAULT NULL COMMENT '关联员工ID',
    user_type            TINYINT      DEFAULT 1 COMMENT '用户类型（1-系统用户 2-外部用户）',
    avatar               VARCHAR(200) DEFAULT NULL COMMENT '头像地址',
    last_login_time      DATETIME     DEFAULT NULL COMMENT '最后登录时间',
    last_login_ip        VARCHAR(50)  DEFAULT NULL COMMENT '最后登录IP',
    login_count          INT          DEFAULT 0 COMMENT '登录次数',
    password_update_time DATETIME     DEFAULT NULL COMMENT '密码更新时间',
    status               TINYINT      DEFAULT 1 COMMENT '状态（0-停用 1-启用 2-锁定）',
    create_by            VARCHAR(64)  NOT NULL DEFAULT 'system' COMMENT '创建人',
    create_by_id         BIGINT       DEFAULT NULL COMMENT '创建人ID',
    create_time          DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_by            VARCHAR(64)  NOT NULL DEFAULT 'system' COMMENT '更新人',
    update_by_id         BIGINT       DEFAULT NULL COMMENT '更新人ID',
    update_time          DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    del_flag             TINYINT      NOT NULL DEFAULT 0 COMMENT '删除标志（0-正常 1-删除）',
    remark               VARCHAR(500) DEFAULT NULL COMMENT '备注',
    PRIMARY KEY (id),
    UNIQUE KEY uk_user_name (user_name),
    KEY idx_emp_id (emp_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户';

-- 经办人员（员工）
CREATE TABLE IF NOT EXISTS mss_employee (
    id           BIGINT      NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    emp_code     VARCHAR(32) NOT NULL COMMENT '员工编号（唯一）',
    emp_name     VARCHAR(50) NOT NULL COMMENT '员工姓名',
    emp_type     TINYINT     NOT NULL DEFAULT 1 COMMENT '员工类型（1-经办员 2-审核员 3-管理员 4-其他）',
    gender       TINYINT     DEFAULT NULL COMMENT '性别（1-男 2-女 3-未知）',
    birth_date   DATE        DEFAULT NULL COMMENT '出生日期',
    hire_date    DATE        DEFAULT NULL COMMENT '入职日期',
    id_card      VARCHAR(18) DEFAULT NULL COMMENT '身份证号',
    phone        VARCHAR(20) DEFAULT NULL COMMENT '手机号码',
    email        VARCHAR(100) DEFAULT NULL COMMENT '电子邮箱',
    dept_id      BIGINT      DEFAULT NULL COMMENT '科室ID',
    dept_name    VARCHAR(200) DEFAULT NULL COMMENT '科室名称（快照）',
    title        VARCHAR(50) DEFAULT NULL COMMENT '职称',
    position     VARCHAR(50) DEFAULT NULL COMMENT '职位',
    education    VARCHAR(20) DEFAULT NULL COMMENT '学历',
    status       TINYINT     DEFAULT 1 COMMENT '状态（0-停用 1-启用）',
    create_by    VARCHAR(64) NOT NULL DEFAULT 'system' COMMENT '创建人',
    create_by_id BIGINT      DEFAULT NULL COMMENT '创建人ID',
    create_time  DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_by    VARCHAR(64) NOT NULL DEFAULT 'system' COMMENT '更新人',
    update_by_id BIGINT      DEFAULT NULL COMMENT '更新人ID',
    update_time  DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    del_flag     TINYINT     NOT NULL DEFAULT 0 COMMENT '删除标志（0-正常 1-删除）',
    remark       VARCHAR(500) DEFAULT NULL COMMENT '备注',
    PRIMARY KEY (id),
    UNIQUE KEY uk_emp_code (emp_code),
    KEY idx_dept_id (dept_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='经办人员';

-- 员工岗位（角色 × 科室；本表无 del_flag，删除走物理删）
CREATE TABLE IF NOT EXISTS mss_employee_post (
    id             BIGINT      NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    employee_id    BIGINT      NOT NULL COMMENT '员工ID',
    role_id        BIGINT      NOT NULL COMMENT '角色ID',
    dept_id        BIGINT      NOT NULL COMMENT '科室ID',
    is_primary     TINYINT     DEFAULT 0 COMMENT '是否主岗位（0-否 1-是）',
    effective_date DATE        DEFAULT NULL COMMENT '岗位生效日期',
    expire_date    DATE        DEFAULT NULL COMMENT '岗位失效日期',
    create_by      VARCHAR(64) NOT NULL DEFAULT 'system' COMMENT '创建人',
    create_by_id   BIGINT      DEFAULT NULL COMMENT '创建人ID',
    create_time    DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_by      VARCHAR(64) NOT NULL DEFAULT 'system' COMMENT '更新人',
    update_by_id   BIGINT      DEFAULT NULL COMMENT '更新人ID',
    update_time    DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_emp_role_dept (employee_id, role_id, dept_id),
    KEY idx_employee_id (employee_id),
    KEY idx_dept_id (dept_id),
    KEY idx_role_id (role_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='员工岗位（角色×科室）';

-- 员工资格证书（仿 HIS biz_employee_qualification，经办员工执业/职业证书登记）
CREATE TABLE IF NOT EXISTS mss_employee_qualification (
    id          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    employee_id BIGINT       NOT NULL COMMENT '员工ID',
    cert_type   VARCHAR(50)  NOT NULL COMMENT '证书类型（his_emp_cert_type 字典）',
    cert_no     VARCHAR(64)  NOT NULL COMMENT '证书编号',
    issue_org   VARCHAR(100) DEFAULT NULL COMMENT '发证机关（his_emp_cert_org 字典）',
    issue_date  DATE         DEFAULT NULL COMMENT '发证日期',
    valid_until DATE         DEFAULT NULL COMMENT '有效期至（空=长期有效）',
    remark      VARCHAR(200) DEFAULT NULL COMMENT '备注',
    create_by   VARCHAR(64)  NOT NULL DEFAULT 'system' COMMENT '创建人',
    create_by_id BIGINT      DEFAULT NULL COMMENT '创建人ID',
    create_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_by   VARCHAR(64)  NOT NULL DEFAULT 'system' COMMENT '更新人',
    update_by_id BIGINT      DEFAULT NULL COMMENT '更新人ID',
    update_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    del_flag    TINYINT      NOT NULL DEFAULT 0 COMMENT '删除标志（0-正常 1-删除）',
    PRIMARY KEY (id),
    KEY idx_employee_id (employee_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='员工资格证书';

-- 系统参数
CREATE TABLE IF NOT EXISTS mss_config (
    config_id    BIGINT      NOT NULL AUTO_INCREMENT COMMENT '配置ID',
    config_name  VARCHAR(64) NOT NULL COMMENT '配置名称',
    config_key   VARCHAR(64) NOT NULL COMMENT '配置键',
    config_value VARCHAR(500) DEFAULT NULL COMMENT '配置值',
    config_type  TINYINT     NOT NULL DEFAULT 1 COMMENT '类型（0-系统 1-业务）',
    is_system    TINYINT     NOT NULL DEFAULT 1 COMMENT '是否系统内置（0-否 1-是）',
    remark       VARCHAR(500) DEFAULT NULL COMMENT '备注',
    create_by    VARCHAR(64) NOT NULL DEFAULT 'system' COMMENT '创建人',
    create_by_id BIGINT      DEFAULT NULL COMMENT '创建人ID',
    create_time  DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_by    VARCHAR(64) NOT NULL DEFAULT 'system' COMMENT '更新人',
    update_by_id BIGINT      DEFAULT NULL COMMENT '更新人ID',
    update_time  DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (config_id),
    UNIQUE KEY uk_config_key (config_key)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统参数';

-- 字典类型
CREATE TABLE IF NOT EXISTS mss_dict_type (
    id           BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    dict_type    VARCHAR(100) NOT NULL COMMENT '字典类型（唯一）',
    dict_name    VARCHAR(100) NOT NULL COMMENT '字典名称',
    status       TINYINT      DEFAULT 1 COMMENT '状态（0-停用 1-启用）',
    dict_source  TINYINT      DEFAULT 1 COMMENT '来源（1-系统级 2-自定义）',
    create_by    VARCHAR(64)  NOT NULL DEFAULT 'system' COMMENT '创建人',
    create_by_id BIGINT       DEFAULT NULL COMMENT '创建人ID',
    create_time  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_by    VARCHAR(64)  NOT NULL DEFAULT 'system' COMMENT '更新人',
    update_by_id BIGINT       DEFAULT NULL COMMENT '更新人ID',
    update_time  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    del_flag     TINYINT      NOT NULL DEFAULT 0 COMMENT '删除标志（0-正常 1-删除）',
    remark       VARCHAR(500) DEFAULT NULL COMMENT '备注',
    PRIMARY KEY (id),
    UNIQUE KEY uk_dict_type (dict_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='字典类型';

-- 字典数据
CREATE TABLE IF NOT EXISTS mss_dict_data (
    id           BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    dict_type    VARCHAR(100) NOT NULL COMMENT '字典类型',
    dict_label   VARCHAR(100) NOT NULL COMMENT '字典标签',
    dict_value   VARCHAR(100) NOT NULL COMMENT '字典值',
    dict_sort    INT          DEFAULT 0 COMMENT '排序号',
    dict_class   VARCHAR(100) DEFAULT NULL COMMENT '样式属性',
    list_class   VARCHAR(100) DEFAULT NULL COMMENT '表格回显样式',
    is_default   TINYINT      DEFAULT 0 COMMENT '是否默认（0-否 1-是）',
    status       TINYINT      DEFAULT 1 COMMENT '状态（0-停用 1-启用）',
    dict_source  TINYINT      DEFAULT 1 COMMENT '来源（1-系统级 2-自定义）',
    create_by    VARCHAR(64)  NOT NULL DEFAULT 'system' COMMENT '创建人',
    create_by_id BIGINT       DEFAULT NULL COMMENT '创建人ID',
    create_time  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_by    VARCHAR(64)  NOT NULL DEFAULT 'system' COMMENT '更新人',
    update_by_id BIGINT       DEFAULT NULL COMMENT '更新人ID',
    update_time  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    del_flag     TINYINT      NOT NULL DEFAULT 0 COMMENT '删除标志（0-正常 1-删除）',
    remark       VARCHAR(500) DEFAULT NULL COMMENT '备注',
    PRIMARY KEY (id),
    KEY idx_dict_type (dict_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='字典数据';

-- =====================================================================
-- 基金监管（医保局端监管侧：规则引擎 / 疑点稽核 / 飞检 / 违规追回）
-- =====================================================================

-- 监管规则
CREATE TABLE IF NOT EXISTS mss_audit_rule (
    id            BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    rule_code     VARCHAR(32)  NOT NULL COMMENT '规则编码（唯一）',
    rule_name     VARCHAR(64)  NOT NULL COMMENT '规则名称',
    rule_type     VARCHAR(32)  NOT NULL COMMENT '规则类型（duplicate_charge-重复收费 over_limit_price-超限价收费 split_hospitalization-分解住院 bed_hanging-挂床住院 abnormal_cost-高额住院 payclass_c-丙类项目入统筹）',
    target        INT          DEFAULT 1 COMMENT '稽核对象（1-经办结算单 2-机构报盘）',
    severity      INT          DEFAULT 1 COMMENT '违规级别（1-一般 2-较重 3-严重）',
    params        VARCHAR(500) DEFAULT NULL COMMENT '规则参数（JSON）',
    description   VARCHAR(500) DEFAULT NULL COMMENT '规则说明',
    status        INT          DEFAULT 1 COMMENT '状态（1-启用 0-停用）',
    create_time   DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time   DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_rule_code (rule_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='监管规则';

-- 疑点记录（规则引擎扫描结算数据产出）
CREATE TABLE IF NOT EXISTS mss_suspicion (
    id               BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    suspicion_no     VARCHAR(32)  NOT NULL COMMENT '疑点单号（唯一）',
    rule_id          BIGINT       DEFAULT NULL COMMENT '命中规则id',
    rule_name        VARCHAR(64)  DEFAULT NULL COMMENT '命中规则名称',
    rule_type        VARCHAR(32)  DEFAULT NULL COMMENT '规则类型',
    biz_source       INT          DEFAULT 1 COMMENT '疑点来源（1-经办结算单 2-机构报盘）',
    biz_id           BIGINT       DEFAULT NULL COMMENT '来源单据id',
    biz_no           VARCHAR(64)  DEFAULT NULL COMMENT '来源单据号',
    patient_name     VARCHAR(64)  DEFAULT NULL COMMENT '患者姓名',
    id_card_mask     VARCHAR(32)  DEFAULT NULL COMMENT '脱敏身份证号',
    insurance_type   VARCHAR(16)  DEFAULT NULL COMMENT '参保类型',
    encounter_type   VARCHAR(16)  DEFAULT NULL COMMENT '就诊类型（门诊/住院）',
    dept             VARCHAR(64)  DEFAULT NULL COMMENT '科室',
    doctor           VARCHAR(64)  DEFAULT NULL COMMENT '医师',
    biz_date         DATE         DEFAULT NULL COMMENT '业务日期',
    involved_amount  DECIMAL(12,2) DEFAULT 0 COMMENT '疑点涉及金额',
    reason           VARCHAR(500) DEFAULT NULL COMMENT '疑点说明',
    status           VARCHAR(16)  DEFAULT '待核实' COMMENT '状态（待核实/已确认/已排除）',
    confirm_amount   DECIMAL(12,2) DEFAULT 0 COMMENT '确认违规金额',
    handle_user      VARCHAR(64)  DEFAULT NULL COMMENT '处置人',
    handle_time      DATETIME     DEFAULT NULL COMMENT '处置时间',
    remark           VARCHAR(500) DEFAULT NULL COMMENT '备注',
    create_time      DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time      DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_susp_no (suspicion_no),
    UNIQUE KEY uk_susp_biz_rule (biz_source, biz_id, rule_id),
    KEY idx_susp_rule (rule_id),
    KEY idx_susp_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='疑点记录';

-- 飞检任务
CREATE TABLE IF NOT EXISTS mss_inspection (
    id             BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    inspection_no  VARCHAR(32)  NOT NULL COMMENT '任务单号（唯一）',
    title          VARCHAR(128) DEFAULT NULL COMMENT '任务名称',
    inspect_type   VARCHAR(16)  DEFAULT NULL COMMENT '检查类型（专项检查/日常巡查/举报核查）',
    target_org     VARCHAR(128) DEFAULT NULL COMMENT '被查机构',
    plan_start     DATE         DEFAULT NULL COMMENT '计划开始日',
    plan_end       DATE         DEFAULT NULL COMMENT '计划结束日',
    leader         VARCHAR(64)  DEFAULT NULL COMMENT '带队人',
    members        VARCHAR(200) DEFAULT NULL COMMENT '检查组成员',
    scope_desc     VARCHAR(500) DEFAULT NULL COMMENT '检查范围',
    status         VARCHAR(16)  DEFAULT '草稿' COMMENT '状态（草稿/待开展/检查中/已完成）',
    result_summary VARCHAR(1000) DEFAULT NULL COMMENT '检查结果小结',
    create_time    DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time    DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_ins_no (inspection_no),
    KEY idx_ins_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='飞检任务';

-- 飞检发现问题明细
CREATE TABLE IF NOT EXISTS mss_inspection_item (
    id              BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    inspection_id   BIGINT       NOT NULL COMMENT '任务id',
    problem_type    VARCHAR(32)  DEFAULT NULL COMMENT '问题类型',
    problem_desc    VARCHAR(500) DEFAULT NULL COMMENT '问题描述',
    evidence        VARCHAR(500) DEFAULT NULL COMMENT '证据说明',
    involved_amount DECIMAL(12,2) DEFAULT 0 COMMENT '涉及金额',
    dispose         VARCHAR(16)  DEFAULT NULL COMMENT '处置方式（整改/追回/移交稽核）',
    create_time     DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (id),
    KEY idx_item_inspection (inspection_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='飞检发现问题明细';

-- 违规追回单
CREATE TABLE IF NOT EXISTS mss_recover (
    id               BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    recover_no       VARCHAR(32)  NOT NULL COMMENT '追回单号（唯一）',
    source_type      INT          DEFAULT 1 COMMENT '来源类型（1-疑点稽核 2-飞检）',
    source_no        VARCHAR(64)  DEFAULT NULL COMMENT '来源单号',
    target_org_code  VARCHAR(32)  DEFAULT NULL COMMENT '定点机构编码',
    target_org_name  VARCHAR(128) DEFAULT NULL COMMENT '定点机构名称',
    month            VARCHAR(7)   DEFAULT NULL COMMENT '所属账期（yyyy-MM）',
    recover_amount   DECIMAL(12,2) DEFAULT 0 COMMENT '追回金额',
    reason           VARCHAR(500) DEFAULT NULL COMMENT '追回原因',
    status           VARCHAR(16)  DEFAULT '待追回' COMMENT '状态（待追回/已追回/已核销）',
    recover_time     DATETIME     DEFAULT NULL COMMENT '追回时间',
    handler          VARCHAR(64)  DEFAULT NULL COMMENT '经办人',
    remark           VARCHAR(500) DEFAULT NULL COMMENT '备注',
    create_time      DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time      DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_recover_no (recover_no),
    KEY idx_recover_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='违规追回单';

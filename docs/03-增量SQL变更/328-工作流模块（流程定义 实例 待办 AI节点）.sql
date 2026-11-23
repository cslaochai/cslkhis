-- ============================================================================
-- 328-工作流模块（流程定义 / 实例 / 待办 / 轨迹 / AI 节点）
-- ============================================================================
-- 背景：新增 his-ms-workflow 模块，把 CA 系统那套「DB 驱动」审批引擎搬进 cslk，
--       并在原有 0-开始/1-审批/2-结束 之上加两类节点：
--         3-自动：调一次 AI 提示词模板（或后续任意服务），无人工待办，直接推进
--         4-网关：按流转条件分支；ai_action=ROUTE 时先做一次意图识别再分支
--       AI 侧不反向依赖：工作流通过 his-core-spi 的 FlowAiGatewayService 端口调 AI。
--
-- 表命名：配置类 sys_flow_*，运行类 biz_flow_*。
-- ⚠ 本文件所有表的唯一键都不含 del_flag，删除一律物理删（代码里已按此实现）。
--
-- 配套代码：source/back_end/his-ms-workflow
-- 权限码：workflow:definition:config（流程定义管理）、workflow:task:handle（待办处理）
--   —— 前端页面未落地前，这两行菜单先设为隐藏（is_visible=0），仅用于授权聚合。
--   上线页面时把 is_visible 改 1，并补 menu_type=1/2 的目录与页面行。
-- 执行后清权限缓存：redis-cli --scan --pattern 'his:perm:role:*' | xargs redis-cli DEL
-- ============================================================================

-- ---------------- 1. 流程定义 ----------------
CREATE TABLE IF NOT EXISTS sys_flow_definition (
    id          BIGINT       NOT NULL COMMENT '主键',
    flow_code   VARCHAR(64)  NOT NULL COMMENT '流程编码',
    flow_name   VARCHAR(128) NOT NULL COMMENT '流程名称',
    flow_desc   VARCHAR(500) DEFAULT NULL COMMENT '流程说明（意图路由给模型看的边界描述）',
    status      TINYINT      NOT NULL DEFAULT 0 COMMENT '状态（0-草稿 1-已发布 2-已停用）',
    version     INT          NOT NULL DEFAULT 1 COMMENT '版本号（每次发布递增）',
    model_json  TEXT         DEFAULT NULL COMMENT '整图快照 JSON（仅回看，不参与执行）',
    create_by   VARCHAR(64)  DEFAULT NULL COMMENT '创建人',
    create_by_id BIGINT      DEFAULT NULL COMMENT '创建人ID',
    create_time DATETIME     DEFAULT NULL COMMENT '创建时间',
    update_by   VARCHAR(64)  DEFAULT NULL COMMENT '更新人',
    update_by_id BIGINT      DEFAULT NULL COMMENT '更新人ID',
    update_time DATETIME     DEFAULT NULL COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_flow_code (flow_code)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '流程定义';

-- ---------------- 2. 流程节点 ----------------
CREATE TABLE IF NOT EXISTS sys_flow_node (
    id               BIGINT       NOT NULL COMMENT '主键',
    definition_id    BIGINT       NOT NULL COMMENT '流程定义ID',
    node_code        VARCHAR(64)  NOT NULL COMMENT '节点编码',
    node_name        VARCHAR(128) NOT NULL COMMENT '节点名称',
    node_type        TINYINT      NOT NULL COMMENT '节点类型（0-开始 1-审批 2-结束 3-自动 4-网关）',
    permission_flag  VARCHAR(255) DEFAULT NULL COMMENT '审批人表达式（role:XXX@@user:1001）',
    node_ratio       VARCHAR(16)  DEFAULT NULL COMMENT '签核比例（0-或签 100-会签 n-票签n%）',
    ai_action        VARCHAR(32)  DEFAULT NULL COMMENT 'AI动作（CAPABILITY-能力调用 ROUTE-意图路由）',
    ai_capability_key VARCHAR(64) DEFAULT NULL COMMENT '能力标识（空则用 flow_ai_node）',
    ai_template_name VARCHAR(128) DEFAULT NULL COMMENT '提示词模板编码',
    ai_input_json    TEXT         DEFAULT NULL COMMENT '节点固定输入 JSON',
    coordinate       VARCHAR(64)  DEFAULT NULL COMMENT '画布坐标',
    sort_no          INT          DEFAULT NULL COMMENT '展示顺序',
    PRIMARY KEY (id),
    UNIQUE KEY uk_def_node (definition_id, node_code)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '流程节点';

-- ---------------- 3. 流转线 ----------------
CREATE TABLE IF NOT EXISTS sys_flow_skip (
    id             BIGINT       NOT NULL COMMENT '主键',
    definition_id  BIGINT       NOT NULL COMMENT '流程定义ID',
    now_node_code  VARCHAR(64)  NOT NULL COMMENT '起点节点编码',
    next_node_code VARCHAR(64)  NOT NULL COMMENT '目标节点编码',
    skip_type      VARCHAR(16)  NOT NULL COMMENT '流转线类型（PASS-正常 COND-条件 REJECT-驳回）',
    condition_expr VARCHAR(500) DEFAULT NULL COMMENT '条件表达式（仅COND；DEFAULT=兜底）',
    sort_no        INT          DEFAULT NULL COMMENT '同层匹配顺序',
    PRIMARY KEY (id),
    KEY idx_def_now (definition_id, now_node_code)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '流程流转线';

-- ---------------- 4. 流程实例 ----------------
CREATE TABLE IF NOT EXISTS biz_flow_instance (
    id                 BIGINT      NOT NULL COMMENT '主键',
    definition_id      BIGINT      NOT NULL COMMENT '流程定义ID',
    flow_code          VARCHAR(64) NOT NULL COMMENT '流程编码',
    flow_name          VARCHAR(128) DEFAULT NULL COMMENT '流程名称',
    business_id        VARCHAR(64) DEFAULT NULL COMMENT '业务单据ID',
    biz_type           VARCHAR(64) DEFAULT NULL COMMENT '业务类型',
    started_by_user_id BIGINT      DEFAULT NULL COMMENT '发起人ID',
    started_by_name    VARCHAR(64) DEFAULT NULL COMMENT '发起人姓名',
    node_code          VARCHAR(64) DEFAULT NULL COMMENT '当前节点编码',
    node_name          VARCHAR(128) DEFAULT NULL COMMENT '当前节点名称',
    node_type          TINYINT     DEFAULT NULL COMMENT '当前节点类型',
    flow_status        VARCHAR(16) NOT NULL COMMENT '实例状态（RUNNING/APPROVED/REJECTED/CANCELED）',
    activity_status    TINYINT     NOT NULL DEFAULT 1 COMMENT '活动标记（1-活动 0-已结束）',
    variable_json      MEDIUMTEXT  DEFAULT NULL COMMENT '流程变量JSON',
    create_time        DATETIME    DEFAULT NULL COMMENT '创建时间',
    update_time        DATETIME    DEFAULT NULL COMMENT '更新时间',
    PRIMARY KEY (id),
    KEY idx_business (business_id),
    KEY idx_status (flow_status, activity_status)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '流程实例';

-- ---------------- 5. 审批待办 ----------------
CREATE TABLE IF NOT EXISTS biz_flow_task (
    id              BIGINT      NOT NULL COMMENT '主键',
    definition_id   BIGINT      NOT NULL COMMENT '流程定义ID',
    instance_id     BIGINT      NOT NULL COMMENT '流程实例ID',
    flow_code       VARCHAR(64) DEFAULT NULL COMMENT '流程编码',
    flow_name       VARCHAR(128) DEFAULT NULL COMMENT '流程名称',
    business_id     VARCHAR(64) DEFAULT NULL COMMENT '业务单据ID',
    node_code       VARCHAR(64) NOT NULL COMMENT '节点编码',
    node_name       VARCHAR(128) DEFAULT NULL COMMENT '节点名称',
    node_type       TINYINT     DEFAULT NULL COMMENT '节点类型',
    task_status     VARCHAR(16) NOT NULL COMMENT '待办状态（PENDING/PASS/REJECT/CANCELED）',
    assignee_expr   VARCHAR(255) DEFAULT NULL COMMENT '候选人表达式快照',
    assignee_user_id BIGINT     DEFAULT NULL COMMENT '实际处理人ID',
    assignee_name   VARCHAR(64) DEFAULT NULL COMMENT '实际处理人姓名',
    create_time     DATETIME    DEFAULT NULL COMMENT '创建时间',
    update_time     DATETIME    DEFAULT NULL COMMENT '更新时间',
    PRIMARY KEY (id),
    KEY idx_instance_node (instance_id, node_code),
    KEY idx_status (task_status)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '流程待办';

-- ---------------- 6. 流转轨迹 ----------------
CREATE TABLE IF NOT EXISTS biz_flow_his_task (
    id            BIGINT       NOT NULL COMMENT '主键',
    definition_id BIGINT       NOT NULL COMMENT '流程定义ID',
    instance_id   BIGINT       NOT NULL COMMENT '流程实例ID',
    task_id       BIGINT       DEFAULT NULL COMMENT '待办ID',
    business_id   VARCHAR(64)  DEFAULT NULL COMMENT '业务单据ID',
    flow_code     VARCHAR(64)  DEFAULT NULL COMMENT '流程编码',
    node_code     VARCHAR(64)  DEFAULT NULL COMMENT '节点编码',
    node_name     VARCHAR(128) DEFAULT NULL COMMENT '节点名称',
    node_type     TINYINT      DEFAULT NULL COMMENT '节点类型',
    action        VARCHAR(16)  NOT NULL COMMENT '动作（START/PASS/REJECT/CANCELED/TASK/AUTO/APPROVED/REJECTED）',
    approver_id   BIGINT       DEFAULT NULL COMMENT '操作人ID',
    approver_name VARCHAR(64)  DEFAULT NULL COMMENT '操作人姓名',
    message       VARCHAR(255) DEFAULT NULL COMMENT '意见/摘要',
    create_time   DATETIME     DEFAULT NULL COMMENT '创建时间',
    PRIMARY KEY (id),
    KEY idx_instance (instance_id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '流程流转轨迹';

-- ============================================================================
-- 7. 菜单铺底（顶级目录「工作流」+ 页面「流程定义」+ 两个按钮码），只授系统管理员 role_id=1
-- ============================================================================
-- 背景（2026-11-14 补）：第一版只建了 menu_type=3 的隐藏按钮，侧边栏没有入口；
--   第二版补了目录/页面，但工作流排在「患者中心」前面、待办又单开一页 —— 按用户口径收敛为：
--     · 目录「工作流」排在「系统管理」之后（sort 155，系统管理 150）
--     · 只保留「流程定义」一个页面
--     · 「我的待办」不单开页，并入门户→消息待办（sys_menu 102 / /messages）的同页签，
--       按钮码 29642 改挂 102，页签只对持有 workflow:task:handle 的岗位出现
--   sys_menu 可见性 = sys_role_menu 是否授本角色（/system/menu/userMenus 按当前角色过滤），
--   router/guard.js 又按「是不是 menu_type=2 的 path」收敛，所以目录 + 页面两行都必须有。
-- id 段 29651/29652（目录/页面）、29641/29642（按钮）。
-- ⚠ 29642 的 parent 是门户的 102（sql/100 建的「消息待办」页），不是本文件的菜单；
--   若某环境没有 102，这一步会留孤儿按钮（权限码仍生效，只是挂不到页面下）。
-- ⚠ 执行后清角色权限缓存（新增了 workflow:definition:config / workflow:task:handle）：
--     redis-cli --scan --pattern 'his:perm:role:*' 逐键 DEL；或重新登录。
-- 回滚：
--   DELETE FROM sys_role_menu WHERE menu_id IN (29641, 29642, 29651, 29652);
--   DELETE FROM sys_menu WHERE id IN (29641, 29642, 29651, 29652);
-- ============================================================================

-- 7.1 顶级目录「工作流」（排在「系统管理」之后）
INSERT INTO sys_menu (id, parent_id, menu_name, sort_order, menu_type, path, component, menu_key, icon,
                      permission, is_frame, is_cache, is_visible, status, create_by, create_time, del_flag, remark)
SELECT 29651, 0, '工作流', 155, 1, '/workflow-group', NULL, 'workflow', 'Share',
       NULL, 0, 1, 1, 1, 'sql328', NOW(), 0, '工作流入口（审批流 / AI 集成流）'
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE id = 29651)
  AND NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_key = 'workflow')
  AND NOT EXISTS (SELECT 1 FROM sys_menu WHERE path = '/workflow-group');

-- 已执行过旧版时把排序挪到位（不改码值/父级，只调位置）
UPDATE sys_menu SET sort_order = 155, update_time = NOW() WHERE id = 29651 AND sort_order <> 155;

-- 7.2 页面「流程定义」
-- ⚠ 页面的 permission 留空：权限码只落在 menu_type=3 的按钮行（v-perm 与 hasAuthority 都读 sys_menu.permission，
--   页面行再挂一遍同一码会让「目录/页面/按钮」三处各持一份，改码时必漂移）
INSERT INTO sys_menu (id, parent_id, menu_name, sort_order, menu_type, path, component, menu_key, icon,
                      permission, is_frame, is_cache, is_visible, status, create_by, create_time, del_flag, remark)
SELECT 29652, 29651, '流程定义', 1, 2, '/workflow/definition', 'workflow/FlowDefinitionView', 'workflow.definition', 'Share',
       NULL, 0, 1, 1, 1, 'sql328', NOW(), 0, '流程定义列表：节点/流转线/AI 节点配置与发布'
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE id = 29652)
  AND NOT EXISTS (SELECT 1 FROM sys_menu WHERE path = '/workflow/definition');

-- 7.3 撤掉旧版单开的「我的待办」页（29653）：并入门户→消息待办的同页签
DELETE FROM sys_role_menu WHERE menu_id = 29653;
DELETE FROM sys_menu WHERE id = 29653;

-- 7.3b 旧版把权限码挂在了页面行上（menu_type=2），这里收敛到按钮行，页面行清空
UPDATE sys_menu SET permission = NULL, update_time = NOW()
WHERE id = 29652 AND permission IS NOT NULL;

-- 7.4 按钮码：29641 挂流程定义页、29642 挂门户的消息待办页（102）
INSERT INTO sys_menu (id, parent_id, menu_name, sort_order, menu_type, path, component, menu_key, icon,
                      permission, is_frame, is_cache, is_visible, status, create_by, create_time, del_flag)
SELECT 29641, 29652, '工作流-流程定义配置', 1, 3, NULL, NULL, 'workflow.definition.config', NULL,
       'workflow:definition:config', 0, 1, 0, 1, 'sql328', NOW(), 0
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE id = 29641)
  AND NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_type = 3 AND permission = 'workflow:definition:config');

INSERT INTO sys_menu (id, parent_id, menu_name, sort_order, menu_type, path, component, menu_key, icon,
                      permission, is_frame, is_cache, is_visible, status, create_by, create_time, del_flag)
SELECT 29642, 102, '工作流-待办处理', 1, 3, NULL, NULL, 'workflow.task.handle', NULL,
       'workflow:task:handle', 0, 1, 0, 1, 'sql328', NOW(), 0
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE id = 29642)
  AND NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_type = 3 AND permission = 'workflow:task:handle');

-- 旧版已执行过本文件时，按钮的父级可能还挂在 0 / 29653，这里对齐（不改 permission，避免权限码漂移）
UPDATE sys_menu SET parent_id = 29652, update_time = NOW() WHERE id = 29641 AND parent_id <> 29652;
UPDATE sys_menu SET parent_id = 102, update_time = NOW() WHERE id = 29642 AND parent_id <> 102;

-- 7.5 只授超管（role 1）；其余角色按岗位需要自行在「角色管理→菜单权限」里勾
INSERT INTO sys_role_menu (id, role_id, menu_id, create_by, create_time, update_by, update_time, create_by_id, update_by_id)
SELECT 892632810000000003, 1, 29651, 'sql328', NOW(), 'sql328', NOW(), 1, 1
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM sys_role_menu WHERE role_id = 1 AND menu_id = 29651);

INSERT INTO sys_role_menu (id, role_id, menu_id, create_by, create_time, update_by, update_time, create_by_id, update_by_id)
SELECT 892632810000000004, 1, 29652, 'sql328', NOW(), 'sql328', NOW(), 1, 1
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM sys_role_menu WHERE role_id = 1 AND menu_id = 29652);

INSERT INTO sys_role_menu (id, role_id, menu_id, create_by, create_time, update_by, update_time, create_by_id, update_by_id)
SELECT 892632810000000001, 1, 29641, 'sql328', NOW(), 'sql328', NOW(), 1, 1
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM sys_role_menu WHERE role_id = 1 AND menu_id = 29641);

INSERT INTO sys_role_menu (id, role_id, menu_id, create_by, create_time, update_by, update_time, create_by_id, update_by_id)
SELECT 892632810000000002, 1, 29642, 'sql328', NOW(), 'sql328', NOW(), 1, 1
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM sys_role_menu WHERE role_id = 1 AND menu_id = 29642);

-- ============================================================================
-- 8. 提示词模板（AI 节点全靠模板，不再为每个节点写 Java）
-- ============================================================================
INSERT INTO sys_ai_prompt_template (id, template_code, template_name, version, content, is_active, remark,
                                    create_by, create_by_id, create_time, update_by, update_by_id, update_time)
SELECT 296420000000000001, 'flow-intent-route', '工作流-意图路由', '1.0',
       '你是医院小程序的入口分流器。根据患者的一句话，从候选流程里选一条。

候选流程（一行一条：编码 | 名称 | 说明）：
{{options}}

规则：
1. 只能返回候选清单里的编码，不许自己编造；
2. 拿不准就返回空字符串 flowCode，让患者补充描述，不许猜；
3. 只输出 JSON，不要解释：{"flowCode":"编码","confidence":0.0~1.0,"reason":"一句话理由"}',
       1, '工作流统一入口的意图识别', 'sql328', 1, NOW(), 'sql328', 1, NOW()
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM sys_ai_prompt_template WHERE template_code = 'flow-intent-route' AND is_active = 1);

INSERT INTO sys_ai_prompt_template (id, template_code, template_name, version, content, is_active, remark,
                                    create_by, create_by_id, create_time, update_by, update_by_id, update_time)
SELECT 296420000000000002, 'flow-answer-general', '工作流-患者咨询通用回答', '1.0',
       '你是医院小程序里的导诊助手，用大白话回答患者的问题。

纪律：
1. 不做诊断、不给用药剂量、不下分级结论；
2. 需要判断的事（是不是重病、要不要停药、能不能手术）一律让患者到门诊当面看；
3. 拿不准就说“这个问题需要医生当面看”，不要编。

节点输入（含 _text 患者原话）：
{{inputJson}}

只输出 JSON：{"answer":"给患者看的话（不超过120字）","suggestAction":"建议动作"}',
       1, '患者咨询兜底分支', 'sql328', 1, NOW(), 'sql328', 1, NOW()
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM sys_ai_prompt_template WHERE template_code = 'flow-answer-general' AND is_active = 1);

INSERT INTO sys_ai_prompt_template (id, template_code, template_name, version, content, is_active, remark,
                                    create_by, create_by_id, create_time, update_by, update_by_id, update_time)
SELECT 296420000000000003, 'flow-answer-report', '工作流-报告解读分支', '1.0',
       '你是医院小程序里的报告解读助手。患者问的是检查/检验报告怎么看。

纪律：
1. 只解释“这项查什么、你的值/描述是什么意思”，不诊断、不定级；
2. 没有拿到具体数值或报告原文时，不要假设结果，直接说明需要上传报告或到门诊看；
3. 危急值/危急描述一律要求立即就医。

节点输入：
{{inputJson}}

只输出 JSON：{"answer":"给患者看的话（不超过150字）","suggestAction":"建议动作"}',
       1, '患者咨询-报告解读分支', 'sql328', 1, NOW(), 'sql328', 1, NOW()
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM sys_ai_prompt_template WHERE template_code = 'flow-answer-report' AND is_active = 1);

INSERT INTO sys_ai_prompt_template (id, template_code, template_name, version, content, is_active, remark,
                                    create_by, create_by_id, create_time, update_by, update_by_id, update_time)
SELECT 296420000000000004, 'flow-answer-registration', '工作流-挂号指引分支', '1.0',
       '你是医院小程序里的挂号指引助手。患者问的是怎么挂号、挂哪个科、带什么材料。

纪律：
1. 只讲流程与材料，不判断病情、不推荐具体诊断；
2. 说不准挂哪个科时，引导患者用小程序里的“智能导诊”或到门诊服务台；
3. 医保报销口径不臆测，让患者看参保地政策或问窗口。

节点输入：
{{inputJson}}

只输出 JSON：{"answer":"给患者看的话（不超过150字）","suggestAction":"建议动作"}',
       1, '患者咨询-挂号指引分支', 'sql328', 1, NOW(), 'sql328', 1, NOW()
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM sys_ai_prompt_template WHERE template_code = 'flow-answer-registration' AND is_active = 1);

INSERT INTO sys_ai_prompt_template (id, template_code, template_name, version, content, is_active, remark,
                                    create_by, create_by_id, create_time, update_by, update_by_id, update_time)
SELECT 296420000000000005, 'flow-qc-inner-check', '工作流-病历内涵预检', '1.0',
       '你是病历内涵质控助手。读节点输入里的病历文本，找出“规则查不出、但人一眼能看出不对劲”的地方。

只做提示，不做判定：
1. 不计算分数、不定严重度、不写“违规成立”；
2. 每条问题必须指出原文位置或原句；
3. 没有问题就返回空数组，不要硬凑。

节点输入：
{{inputJson}}

只输出 JSON：{"issues":[{"point":"问题点","evidence":"原文依据","suggest":"建议补充/修改方向"}]}',
       1, '审批流示例：AI 预检 + 人工复核', 'sql328', 1, NOW(), 'sql328', 1, NOW()
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM sys_ai_prompt_template WHERE template_code = 'flow-qc-inner-check' AND is_active = 1);

-- ============================================================================
-- 9. 示例流程一：患者咨询分流（START → AI 网关 → 三分支 → END）
-- ============================================================================
INSERT INTO sys_flow_definition (id, flow_code, flow_name, flow_desc, status, version, model_json,
                                 create_by, create_by_id, create_time, update_by, update_by_id, update_time)
SELECT 296430000000000001, 'PATIENT_CONSULT', '患者咨询分流', 
       '患者在小程序里问健康/就医问题的通用入口。凡是问“怎么办/挂什么科/报告怎么看”且没有具体业务单据的，都走这条。',
       1, 1, '{"nodes":["START","ROUTE","ANSWER_REPORT","ANSWER_REGISTRATION","ANSWER_GENERAL","END"]}',
       'sql328', 1, NOW(), 'sql328', 1, NOW()
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM sys_flow_definition WHERE flow_code = 'PATIENT_CONSULT');

INSERT INTO sys_flow_node (id, definition_id, node_code, node_name, node_type, permission_flag, node_ratio,
                           ai_action, ai_capability_key, ai_template_name, ai_input_json, coordinate, sort_no)
SELECT 296431000000000001, 296430000000000001, 'START', '开始', 0, NULL, NULL, NULL, NULL, NULL, NULL, '0,0', 1 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM sys_flow_node WHERE definition_id = 296430000000000001 AND node_code = 'START');

INSERT INTO sys_flow_node (id, definition_id, node_code, node_name, node_type, permission_flag, node_ratio,
                           ai_action, ai_capability_key, ai_template_name, ai_input_json, coordinate, sort_no)
SELECT 296431000000000002, 296430000000000001, 'ROUTE', '意图识别', 4, NULL, NULL,
       'ROUTE', 'flow_intent_route', 'flow-intent-route', NULL, '200,0', 2 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM sys_flow_node WHERE definition_id = 296430000000000001 AND node_code = 'ROUTE');

INSERT INTO sys_flow_node (id, definition_id, node_code, node_name, node_type, permission_flag, node_ratio,
                           ai_action, ai_capability_key, ai_template_name, ai_input_json, coordinate, sort_no)
SELECT 296431000000000003, 296430000000000001, 'ANSWER_REPORT', '报告解读回答', 3, NULL, NULL,
       'CAPABILITY', 'flow_ai_node', 'flow-answer-report', NULL, '400,0', 3 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM sys_flow_node WHERE definition_id = 296430000000000001 AND node_code = 'ANSWER_REPORT');

INSERT INTO sys_flow_node (id, definition_id, node_code, node_name, node_type, permission_flag, node_ratio,
                           ai_action, ai_capability_key, ai_template_name, ai_input_json, coordinate, sort_no)
SELECT 296431000000000004, 296430000000000001, 'ANSWER_REGISTRATION', '挂号指引回答', 3, NULL, NULL,
       'CAPABILITY', 'flow_ai_node', 'flow-answer-registration', NULL, '400,100', 4 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM sys_flow_node WHERE definition_id = 296430000000000001 AND node_code = 'ANSWER_REGISTRATION');

INSERT INTO sys_flow_node (id, definition_id, node_code, node_name, node_type, permission_flag, node_ratio,
                           ai_action, ai_capability_key, ai_template_name, ai_input_json, coordinate, sort_no)
SELECT 296431000000000005, 296430000000000001, 'ANSWER_GENERAL', '通用回答', 3, NULL, NULL,
       'CAPABILITY', 'flow_ai_node', 'flow-answer-general', NULL, '400,200', 5 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM sys_flow_node WHERE definition_id = 296430000000000001 AND node_code = 'ANSWER_GENERAL');

INSERT INTO sys_flow_node (id, definition_id, node_code, node_name, node_type, permission_flag, node_ratio,
                           ai_action, ai_capability_key, ai_template_name, ai_input_json, coordinate, sort_no)
SELECT 296431000000000006, 296430000000000001, 'END', '结束', 2, NULL, NULL, NULL, NULL, NULL, NULL, '600,100', 6 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM sys_flow_node WHERE definition_id = 296430000000000001 AND node_code = 'END');

INSERT INTO sys_flow_skip (id, definition_id, now_node_code, next_node_code, skip_type, condition_expr, sort_no)
SELECT 296432000000000001, 296430000000000001, 'START', 'ROUTE', 'PASS', NULL, 1 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM sys_flow_skip WHERE definition_id = 296430000000000001 AND now_node_code = 'START' AND next_node_code = 'ROUTE');

INSERT INTO sys_flow_skip (id, definition_id, now_node_code, next_node_code, skip_type, condition_expr, sort_no)
SELECT 296432000000000002, 296430000000000001, 'ROUTE', 'ANSWER_REPORT', 'COND',
       'ROUTE.flowCode == ''ANSWER_REPORT''', 1 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM sys_flow_skip WHERE definition_id = 296430000000000001 AND now_node_code = 'ROUTE' AND next_node_code = 'ANSWER_REPORT');

INSERT INTO sys_flow_skip (id, definition_id, now_node_code, next_node_code, skip_type, condition_expr, sort_no)
SELECT 296432000000000003, 296430000000000001, 'ROUTE', 'ANSWER_REGISTRATION', 'COND',
       'ROUTE.flowCode == ''ANSWER_REGISTRATION''', 2 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM sys_flow_skip WHERE definition_id = 296430000000000001 AND now_node_code = 'ROUTE' AND next_node_code = 'ANSWER_REGISTRATION');

INSERT INTO sys_flow_skip (id, definition_id, now_node_code, next_node_code, skip_type, condition_expr, sort_no)
SELECT 296432000000000004, 296430000000000001, 'ROUTE', 'ANSWER_GENERAL', 'COND', 'DEFAULT', 3 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM sys_flow_skip WHERE definition_id = 296430000000000001 AND now_node_code = 'ROUTE' AND next_node_code = 'ANSWER_GENERAL');

INSERT INTO sys_flow_skip (id, definition_id, now_node_code, next_node_code, skip_type, condition_expr, sort_no)
SELECT 296432000000000005, 296430000000000001, 'ANSWER_REPORT', 'END', 'PASS', NULL, 1 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM sys_flow_skip WHERE definition_id = 296430000000000001 AND now_node_code = 'ANSWER_REPORT');

INSERT INTO sys_flow_skip (id, definition_id, now_node_code, next_node_code, skip_type, condition_expr, sort_no)
SELECT 296432000000000006, 296430000000000001, 'ANSWER_REGISTRATION', 'END', 'PASS', NULL, 1 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM sys_flow_skip WHERE definition_id = 296430000000000001 AND now_node_code = 'ANSWER_REGISTRATION');

INSERT INTO sys_flow_skip (id, definition_id, now_node_code, next_node_code, skip_type, condition_expr, sort_no)
SELECT 296432000000000007, 296430000000000001, 'ANSWER_GENERAL', 'END', 'PASS', NULL, 1 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM sys_flow_skip WHERE definition_id = 296430000000000001 AND now_node_code = 'ANSWER_GENERAL');

-- ============================================================================
-- 10. 示例流程二：病历内涵复核（START → AI 预检 → 科室质控员审批 → END）
-- ============================================================================
INSERT INTO sys_flow_definition (id, flow_code, flow_name, flow_desc, status, version, model_json,
                                 create_by, create_by_id, create_time, update_by, update_by_id, update_time)
SELECT 296430000000000002, 'EMR_QC_REVIEW', '病历内涵复核',
       '规则质控命中或病历提交前的内涵复核：AI 先出问题清单，再由科室质控员确认整改。只提示不计分。',
       1, 1, '{"nodes":["START","AI_CHECK","REVIEW","END"]}',
       'sql328', 1, NOW(), 'sql328', 1, NOW()
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM sys_flow_definition WHERE flow_code = 'EMR_QC_REVIEW');

INSERT INTO sys_flow_node (id, definition_id, node_code, node_name, node_type, permission_flag, node_ratio,
                           ai_action, ai_capability_key, ai_template_name, ai_input_json, coordinate, sort_no)
SELECT 296431000000000011, 296430000000000002, 'START', '开始', 0, NULL, NULL, NULL, NULL, NULL, NULL, '0,0', 1 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM sys_flow_node WHERE definition_id = 296430000000000002 AND node_code = 'START');

INSERT INTO sys_flow_node (id, definition_id, node_code, node_name, node_type, permission_flag, node_ratio,
                           ai_action, ai_capability_key, ai_template_name, ai_input_json, coordinate, sort_no)
SELECT 296431000000000012, 296430000000000002, 'AI_CHECK', '内涵预检', 3, NULL, NULL,
       'CAPABILITY', 'flow_ai_node', 'flow-qc-inner-check', NULL, '200,0', 2 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM sys_flow_node WHERE definition_id = 296430000000000002 AND node_code = 'AI_CHECK');

INSERT INTO sys_flow_node (id, definition_id, node_code, node_name, node_type, permission_flag, node_ratio,
                           ai_action, ai_capability_key, ai_template_name, ai_input_json, coordinate, sort_no)
SELECT 296431000000000013, 296430000000000002, 'REVIEW', '科室质控员确认', 1, 'role:DOCTOR', '0',
       NULL, NULL, NULL, NULL, '400,0', 3 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM sys_flow_node WHERE definition_id = 296430000000000002 AND node_code = 'REVIEW');

INSERT INTO sys_flow_node (id, definition_id, node_code, node_name, node_type, permission_flag, node_ratio,
                           ai_action, ai_capability_key, ai_template_name, ai_input_json, coordinate, sort_no)
SELECT 296431000000000014, 296430000000000002, 'END', '结束', 2, NULL, NULL, NULL, NULL, NULL, NULL, '600,0', 4 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM sys_flow_node WHERE definition_id = 296430000000000002 AND node_code = 'END');

INSERT INTO sys_flow_skip (id, definition_id, now_node_code, next_node_code, skip_type, condition_expr, sort_no)
SELECT 296432000000000011, 296430000000000002, 'START', 'AI_CHECK', 'PASS', NULL, 1 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM sys_flow_skip WHERE definition_id = 296430000000000002 AND now_node_code = 'START');

INSERT INTO sys_flow_skip (id, definition_id, now_node_code, next_node_code, skip_type, condition_expr, sort_no)
SELECT 296432000000000012, 296430000000000002, 'AI_CHECK', 'REVIEW', 'PASS', NULL, 1 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM sys_flow_skip WHERE definition_id = 296430000000000002 AND now_node_code = 'AI_CHECK');

INSERT INTO sys_flow_skip (id, definition_id, now_node_code, next_node_code, skip_type, condition_expr, sort_no)
SELECT 296432000000000013, 296430000000000002, 'REVIEW', 'END', 'PASS', NULL, 1 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM sys_flow_skip WHERE definition_id = 296430000000000002 AND now_node_code = 'REVIEW');

-- ============================================================================
-- 11. 回查
-- ============================================================================
SELECT flow_code, flow_name, status, version FROM sys_flow_definition ORDER BY flow_code;
SELECT definition_id, COUNT(*) AS node_count FROM sys_flow_node GROUP BY definition_id;
SELECT definition_id, COUNT(*) AS skip_count FROM sys_flow_skip GROUP BY definition_id;
SELECT template_code, template_name, is_active FROM sys_ai_prompt_template WHERE template_code LIKE 'flow-%';

-- 菜单期望：4 行（1 目录 + 1 页面 + 2 按钮），目录 sort=155 排在系统管理(150)之后，
-- 29642 的 parent 应为门户的 102（消息待办页）
SELECT id, parent_id, menu_name, menu_type, sort_order, path, menu_key, permission, is_visible
FROM sys_menu
WHERE id IN (29641, 29642, 29651, 29652)
ORDER BY id;

-- 期望：只有 role_id=1 的 4 行
SELECT rm.role_id, r.role_name, rm.menu_id, m.menu_name, m.menu_type, m.permission
FROM sys_role_menu rm
         LEFT JOIN sys_role r ON r.id = rm.role_id
         LEFT JOIN sys_menu m ON m.id = rm.menu_id
WHERE rm.menu_id IN (29641, 29642, 29651, 29652)
ORDER BY rm.menu_id, rm.role_id;


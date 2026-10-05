-- =============================================================================
-- 226 · 病历草稿 AI 留痕（草稿 → 终稿 diff）
--
-- 【这一刀是什么】
-- docs/AI能力施工手册.md G-10：医生点「填入草稿」后，AI 原稿与医生终稿之间的差异
-- 此前无任何记录，未来 SFT 微调的训练原料（数据飞轮）正在流失。
-- 现在病历保存时若携带 AI 草稿原文，落一行 diff（his-emr 拥有：写入方是病历保存流，
-- 业务模块不得依赖 his-ai，diff 表与写入都归病历域）。
--
-- 【口径】
-- · 纯留痕表，只增不改不删；查询入口 /emr/draftDiff/listPage（AI 管理台「草稿留痕」签页，
--   权限码沿用 ai:admin:list，不新增菜单）。
-- · 同一病历多次「填草稿→保存」各留一行（每次终审都是一条独立样本），不做唯一键。
-- · draft/final 文本各截断 2000 字，diff 分段 JSON 截断 8000 字（MEDIUMTEXT 上限内）。
-- =============================================================================

CREATE TABLE IF NOT EXISTS biz_ai_draft_diff (
  id           BIGINT        NOT NULL                COMMENT '主键ID（雪花）',
  record_id    BIGINT        NOT NULL                COMMENT '病历ID',
  regist_id    BIGINT        DEFAULT NULL            COMMENT '挂号ID',
  patient_id   BIGINT        DEFAULT NULL            COMMENT '患者ID',
  patient_no   VARCHAR(50)   DEFAULT ''              COMMENT '患者号',
  patient_name VARCHAR(50)   DEFAULT ''              COMMENT '患者姓名',
  dept_id      BIGINT        DEFAULT NULL            COMMENT '接诊科室ID',
  dept_name    VARCHAR(50)   DEFAULT ''              COMMENT '接诊科室名称',
  doctor_id    BIGINT        DEFAULT NULL            COMMENT '终审医生ID',
  doctor_name  VARCHAR(50)   DEFAULT ''              COMMENT '终审医生姓名',
  draft_text   TEXT                                  COMMENT 'AI草稿原文（截断2000字）',
  final_text   TEXT                                  COMMENT '医生终稿（截断2000字）',
  diff_json    MEDIUMTEXT                            COMMENT '差异分段JSON（0-相同 1-删 2-增）',
  changed      TINYINT       DEFAULT 1               COMMENT '是否修改（1-有修改 0-未修改）',
  create_by    VARCHAR(64)   DEFAULT ''              COMMENT '创建人',
  create_time  DATETIME      DEFAULT NULL            COMMENT '创建时间',
  update_by    VARCHAR(64)   DEFAULT ''              COMMENT '更新人',
  update_time  DATETIME      DEFAULT NULL            COMMENT '更新时间',
  del_flag     TINYINT       DEFAULT 0               COMMENT '删除标志（0-正常 1-删除）',
  remark       VARCHAR(500)  DEFAULT ''              COMMENT '备注',
  PRIMARY KEY (id),
  KEY idx_record (record_id),
  KEY idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='病历草稿AI留痕（草稿与终稿差异，SFT训练原料）';

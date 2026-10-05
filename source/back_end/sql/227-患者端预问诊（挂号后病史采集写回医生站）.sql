-- =============================================================================
-- 227 · 患者端预问诊（挂号后、就诊前采集病史，写回医生站）
--
-- 【这一刀是什么】
-- docs/AI能力施工手册.md G-05：患者挂号成功后在小程序填预问诊问卷（量表先行，
-- 模型只把结构化答案凝成一段就诊用病史摘要，规则模板兜底），医生站接诊时直接看到
-- 报告，可一键参考填入现病史。
--
-- 【口径】
-- · 一次挂号一份问卷（regist_id 唯一，重复提交覆盖更新）。
-- · 题目结构由后端 PrevisitQuestionnaireSupport 给出（版本化在代码里，随接口下发），
--   不在前端写死；answers_json 只存「题目+作答」的回显数据，不存题目版本。
-- · summary_source（1-模型 2-规则）：模型不可用时摘要由规则模板拼接，功能不缺位。
-- =============================================================================

CREATE TABLE IF NOT EXISTS biz_previsit_record (
  id             BIGINT       NOT NULL                COMMENT '主键ID（雪花）',
  regist_id      BIGINT       NOT NULL                COMMENT '挂号ID（一次挂号一份问卷）',
  patient_id     BIGINT       NOT NULL                COMMENT '患者ID',
  patient_no     VARCHAR(50)  DEFAULT ''              COMMENT '患者号',
  patient_name   VARCHAR(50)  DEFAULT ''              COMMENT '患者姓名',
  dept_id        BIGINT       DEFAULT NULL            COMMENT '就诊科室ID',
  dept_name      VARCHAR(50)  DEFAULT ''              COMMENT '就诊科室名称',
  main_symptom   VARCHAR(50)  DEFAULT ''              COMMENT '主症状',
  answers_json   TEXT                                 COMMENT '问答明细JSON（题目与作答回显）',
  free_text      TEXT                                 COMMENT '患者补充描述',
  summary_ai     TEXT                                 COMMENT '病史摘要（模型凝练或规则模板）',
  summary_source TINYINT      DEFAULT NULL            COMMENT '摘要来源（1-模型 2-规则）',
  create_by      VARCHAR(64)  DEFAULT ''              COMMENT '创建人',
  create_time    DATETIME     DEFAULT NULL            COMMENT '创建时间',
  update_by      VARCHAR(64)  DEFAULT ''              COMMENT '更新人',
  update_time    DATETIME     DEFAULT NULL            COMMENT '更新时间',
  del_flag       TINYINT      DEFAULT 0               COMMENT '删除标志（0-正常 1-删除）',
  remark         VARCHAR(500) DEFAULT ''              COMMENT '备注',
  PRIMARY KEY (id),
  UNIQUE KEY uk_regist (regist_id, del_flag)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='患者端预问诊记录（挂号后病史采集）';

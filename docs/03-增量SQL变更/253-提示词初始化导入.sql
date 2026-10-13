-- 初始化提示词模板
-- id 用雪花算法，create_by/update_by 默认 system

INSERT INTO sys_ai_prompt_template (id, template_code, template_name, version, content, is_active, remark, create_by_id, create_by, update_by_id, update_by) VALUES (1900000000000000001, 'deterioration-alert', '病情恶化预警', '1.0', '---
name: deterioration-alert
---
# 病情恶化预警提示词

你是一个临床病情恶化预警助手。根据患者生命体征数据，判断是否有恶化风险。
输出 JSON：{"level": "critical|warning|normal", "reason": "简要说明"}', 1, '初始导入', 1, 'system', 1, 'system');

INSERT INTO sys_ai_prompt_template (id, template_code, template_name, version, content, is_active, remark, create_by_id, create_by, update_by_id, update_by) VALUES (1900000000000000002, 'drug-audit', '处方审核', '1.0', '---
name: drug-audit
---
# 处方审核提示词

你是一个药师，负责审核处方合理性。检查禁忌症、相互作用、剂量。
输出 JSON：{"pass": true/false, "issues": ["问题1", "问题2"]}', 1, '初始导入', 1, 'system', 1, 'system');

INSERT INTO sys_ai_prompt_template (id, template_code, template_name, version, content, is_active, remark, create_by_id, create_by, update_by_id, update_by) VALUES (1900000000000000003, 'emr-draft', '病历草稿生成', '1.0', '---
name: emr-draft
---
# 病历草稿生成

根据患者就诊信息生成病历草稿。输入主诉、现病史、既往史，输出规范病历文本。', 1, '初始导入', 1, 'system', 1, 'system');

INSERT INTO sys_ai_prompt_template (id, template_code, template_name, version, content, is_active, remark, create_by_id, create_by, update_by_id, update_by) VALUES (1900000000000000004, 'emr-extract', '病历信息抽取', '1.0', '---
name: emr-extract
---
# 病历信息抽取

从自由文本病历中抽取结构化信息。
输出 JSON：{"symptoms": [], "diagnosis": [], "medications": [], "labResults": []}', 1, '初始导入', 1, 'system', 1, 'system');

INSERT INTO sys_ai_prompt_template (id, template_code, template_name, version, content, is_active, remark, create_by_id, create_by, update_by_id, update_by) VALUES (1900000000000000005, 'emr-qc', '病历质控', '1.0', '---
name: emr-qc
---
# 病历质控

检查病历是否完整、规范，指出缺项和问题。
输出：{"pass": true/false, "issues": ["..."]}', 1, '初始导入', 1, 'system', 1, 'system');

INSERT INTO sys_ai_prompt_template (id, template_code, template_name, version, content, is_active, remark, create_by_id, create_by, update_by_id, update_by) VALUES (1900000000000000006, 'icd10-predict', 'ICD-10编码推荐', '1.0', '---
name: icd10-predict
---
# ICD-10 编码推荐

根据诊断文本推荐 ICD-10 编码。
输出 JSON：[{"code": "J18.9", "name": "肺炎", "confidence": 0.95}]', 1, '初始导入', 1, 'system', 1, 'system');

INSERT INTO sys_ai_prompt_template (id, template_code, template_name, version, content, is_active, remark, create_by_id, create_by, update_by_id, update_by) VALUES (1900000000000000007, 'lab-interpret', '检验结果解读', '1.0', '---
name: lab-interpret
---
# 检验结果解读

用通俗语言解释检验结果异常的临床意义。', 1, '初始导入', 1, 'system', 1, 'system');

INSERT INTO sys_ai_prompt_template (id, template_code, template_name, version, content, is_active, remark, create_by_id, create_by, update_by_id, update_by) VALUES (1900000000000000008, 'patient-report-explain', '患者报告解读', '1.0', '---
name: patient-report-explain
---
# 患者报告解读

面向患者解释检查报告，避免医学术语，给出建议。', 1, '初始导入', 1, 'system', 1, 'system');

INSERT INTO sys_ai_prompt_template (id, template_code, template_name, version, content, is_active, remark, create_by_id, create_by, update_by_id, update_by) VALUES (1900000000000000009, 'triage-normalize', '分诊标准化', '1.0', '---
name: triage-normalize
---
# 分诊标准化

将自由文本分诊描述标准化为分诊等级。
输出：{"level": 1-5, "department": "推荐科室"}', 1, '初始导入', 1, 'system', 1, 'system');

INSERT INTO sys_ai_prompt_template (id, template_code, template_name, version, content, is_active, remark, create_by_id, create_by, update_by_id, update_by) VALUES (1900000000000000010, 'knowledge-qa', '知识库问答', '1.0', '---
name: knowledge-qa
---
# 知识库问答

根据知识库片段回答用户问题。只基于提供的片段回答，不知道就说不知道。', 1, '初始导入', 1, 'system', 1, 'system');

INSERT INTO sys_ai_prompt_template (id, template_code, template_name, version, content, is_active, remark, create_by_id, create_by, update_by_id, update_by) VALUES (1900000000000000011, 'operation-qa-summary', '问数结果总结', '1.0', '---
name: operation-qa-summary
---
# 问数结果总结

将 SQL 查询结果用自然语言总结给用户。', 1, '初始导入', 1, 'system', 1, 'system');

INSERT INTO sys_ai_prompt_template (id, template_code, template_name, version, content, is_active, remark, create_by_id, create_by, update_by_id, update_by) VALUES (1900000000000000012, 'previsit-summary', '就诊前摘要', '1.0', '---
name: previsit-summary
---
# 就诊前摘要

根据患者历史就诊记录生成就诊前摘要。', 1, '初始导入', 1, 'system', 1, 'system');

INSERT INTO sys_ai_prompt_template (id, template_code, template_name, version, content, is_active, remark, create_by_id, create_by, update_by_id, update_by) VALUES (1900000000000000013, 'followup-compose', '随访话术生成', '1.0', '---
name: followup-compose
---
# 随访话术生成

根据患者病情生成随访话术。', 1, '初始导入', 1, 'system', 1, 'system');

INSERT INTO sys_ai_prompt_template (id, template_code, template_name, version, content, is_active, remark, create_by_id, create_by, update_by_id, update_by) VALUES (1900000000000000014, 'insurance-evidence', '医保依据生成', '1.0', '---
name: insurance-evidence
---
# 医保依据生成

根据诊疗记录生成医保审核依据。', 1, '初始导入', 1, 'system', 1, 'system');

INSERT INTO sys_ai_prompt_template (id, template_code, template_name, version, content, is_active, remark, create_by_id, create_by, update_by_id, update_by) VALUES (1900000000000000015, 'nursing-handover', '护理交班生成', '1.0', '---
name: nursing-handover
---
# 护理交班生成

根据护理记录生成交班报告。', 1, '初始导入', 1, 'system', 1, 'system');

INSERT INTO sys_ai_prompt_template (id, template_code, template_name, version, content, is_active, remark, create_by_id, create_by, update_by_id, update_by) VALUES (1900000000000000016, 'patient-imaging-explain', '影像报告解读', '1.0', '---
name: patient-imaging-explain
---
# 影像报告解读（患者版）

用通俗语言向患者解释影像报告。', 1, '初始导入', 1, 'system', 1, 'system');

INSERT INTO sys_ai_prompt_template (id, template_code, template_name, version, content, is_active, remark, create_by_id, create_by, update_by_id, update_by) VALUES (1900000000000000017, 'operation-qa-route', '问数路由判断', '1.0', '---
name: operation-qa-route
---
# 问数路由判断

判断用户问题是否是数据统计类问题，是否需要走 NL2SQL。
输出：{"needSql": true/false, "reason": "..."}', 1, '初始导入', 1, 'system', 1, 'system');

INSERT INTO sys_ai_prompt_template (id, template_code, template_name, version, content, is_active, remark, create_by_id, create_by, update_by_id, update_by) VALUES (1900000000000000018, 'operation-qa', 'AI问数(NL2SQL)', '1.0', '---
name: operation-qa
---
# AI 问数（NL2SQL）

你是一个 SQL 生成助手。根据用户问题和表结构信息，生成只读 SELECT SQL。
## 规则
1. 只生成 SELECT，禁止 INSERT/UPDATE/DELETE
2. 只使用提供的表和字段
3. 敏感字段不要查询
4. 输出 JSON：{"title": "标题", "sql": "SELECT..."}', 1, '初始导入', 1, 'system', 1, 'system');

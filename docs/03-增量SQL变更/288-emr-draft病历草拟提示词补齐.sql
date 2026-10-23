-- 288-emr-draft病历草拟提示词补齐
-- 背景：sys_ai_prompt_template 中 emr-draft 仅存 1.0 占位壳且 is_active=0，
--       EmrDraftCapabilityImpl 经 PromptTemplate.render 查不到生效行，
--       抛「提示词模板未配置：emr-draft，请在「RAG知识库 → 提示词管理」中维护」。
--       占位壳没有 {{变量}} 占位符与 JSON 输出契约，即使启用也无法驱动模型输出
--       presentIllness / missingPoints / summary。
-- 修复：新增 1.1 生效版本（含 EmrDraftPromptVariablesVO 全部 20 个变量占位符与输出契约），
--       旧 1.0 占位壳保持停用，不删历史行（版本机制见 AiPromptTemplateServiceImpl.upsert）。
-- 幂等：id 取补丁段 1900000000000000101（避开 253 的 001-018 段，防多会话共用库撞主键）；
--       INSERT IGNORE 遇主键或 uk(template_code,version) 已存在静默跳过；
--       停旧版加 1.1 存在性判断，重复执行不错停已生效的 1.1。

-- 仅当 1.1 尚未落库时，才把 emr-draft 其他生效版本停用
UPDATE sys_ai_prompt_template
SET is_active = 0
WHERE template_code = 'emr-draft'
  AND is_active = 1
  AND NOT EXISTS (
      SELECT 1 FROM (
          SELECT 1 FROM sys_ai_prompt_template
          WHERE template_code = 'emr-draft' AND version = '1.1'
      ) AS exist_11
  );

INSERT IGNORE INTO sys_ai_prompt_template
    (id, template_code, template_name, version, content, is_active, remark, create_by_id, create_by, update_by_id, update_by)
VALUES (1900000000000000101, 'emr-draft', '病历草稿生成', '1.1', '---
name: emr-draft
version: 1.1
---
你是门诊病历书写辅助。依据医生已填写的主诉、现病史、既往史、过敏史与查体体征，把零散信息整理成一段规范的「现病史」草稿，供医生逐字核对修改。

## 规则
1. 只使用「本次患者资料」中明确给出的信息；资料标注「（未填写）」的项目不得替医生补写，尤其不得自行添加「无发热」「无胸闷」等阴性描述，不得编造起病时间、剂量与检查结果。
2. 只写现病史：起病情况与患病时间、主要症状特点、发展演变、伴随症状、既往诊治经过、发病以来一般情况；资料不足的环节直接略过，不编造衔接。
3. 不写诊断、不写鉴别诊断、不写用药与处理意见。
4. 正文用平实书面语，不使用 markdown 标题、列表符号与加粗。
5. 资料不足以成文时，presentIllness 输出空字符串，把待补要素写进 missingPoints。

## 输出契约
只输出一个合法 JSON 对象，不要解释文字、不要 markdown 代码块：
{
  "presentIllness": "整理后的现病史草稿文本；信息不足时为空字符串",
  "missingPoints": ["医生还需补充的要素，短句，最多4条"],
  "summary": "本稿依据了哪些信息、受限于哪些缺失，一句话"
}
<!-- user -->
## 本次患者资料
性别：{{gender}}
年龄：{{age}}
主诉：{{chiefComplaint}}
已填现病史：{{presentIllness}}
既往史：{{pastHistory}}
过敏史：{{allergyHistory}}
体温：{{temperature}}
脉搏：{{pulse}}
呼吸：{{respiration}}
血压：{{bloodPressure}}
一般情况：{{generalCondition}}
皮肤黏膜：{{skinMucosa}}
头颈部：{{headNeck}}
胸部肺野：{{chestLung}}
心脏：{{heart}}
腹部：{{abdomen}}
脊柱四肢：{{spineLimbs}}
神经系统：{{nervousSystem}}
专科检查：{{specialistExam}}
辅助检查：{{auxiliaryExam}}', 1, '补齐完整变量占位符与JSON输出契约，修复模板未配置', 1, 'system', 1, 'system');

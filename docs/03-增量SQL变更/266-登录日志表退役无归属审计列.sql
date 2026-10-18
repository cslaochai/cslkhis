-- 266-登录日志表退役无归属意义的审计列
-- 背景：sys_login_log 是只追加的登录流水，代码里从不 update（实测 dev 库 2260 行 update_time 全部等于
--       create_time），"创建人/更新人"对它没有业务含义 —— 真正的主体是 user_name/user_id 两列，
--       create_by 2260 行里 990 行就等于 user_name（失败登录那批 user_id 为 NULL，create_by 却是登录账号）。
--       remark 三列里 2260 行只有 3 行有值，且 SysLoginLogServiceImpl 从不写它。
-- 依据：AGENTS 第 4 节"只追加的流水表不挂审计四件套"（参照 sys_knowledge_query_log 的实体写法：
--       不 extends BaseEntity，自持 id + createTime）。
-- 影响面：SysLoginLog 实体去掉 remark 字段与 BaseEntity 继承；SysLoginLogServiceImpl 去掉 setCreateBy。
--       登录日志查询（SysLogServiceImpl#loginLogListPage）只读 del_flag/login_* /user_*，未引用上述列。
-- 顺序：必须先发布后端（SysLoginLog 不再映射这 5 列）再执行本文件，否则 MP 写库直接 Unknown column。

ALTER TABLE `sys_login_log`
    DROP COLUMN `create_by`,
    DROP COLUMN `create_by_id`,
    DROP COLUMN `update_by`,
    DROP COLUMN `update_by_id`,
    DROP COLUMN `remark`;
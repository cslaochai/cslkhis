-- ============================================================
-- 增量变更：sys_message 创建人/更新人允许为空
-- 根因：定时任务（危急值升级/急诊等待/检查爽约/转诊/等床等 8 条链路）在
--       无登录态线程插入站内信，MetaObjectHandler.insertFill 取不到当前用户时
--       跳过 create_by/update_by/create_by_id/update_by_id 填充，而这四列
--       在库中均为 NOT NULL 且无默认值，MySQL 严格模式报
--       Field 'create_by'/'create_by_id' doesn't have a default value（ERROR 1364）。
--       系统消息"无操作人"本就是合法态，此处对齐。
--       注：DDL 文档中 create_by_id/update_by_id 一直为可空，实际库建库时
--       为 NOT NULL（列序也与文档不同，两者长期不同源）；本次一并修正。
-- 目标库：hn_biz_his  @ 192.168.88.132:3306
-- 影响列数：4（仅元数据变更，不触数据）
-- 说明：可逆；如需回滚按列 ALTER TABLE ... MODIFY ... NOT NULL（需先清空存量 NULL 行）
-- ============================================================

-- sys_message.create_by  (varchar(64) NO -> YES)
ALTER TABLE `sys_message` MODIFY COLUMN `create_by` varchar(64) NULL COMMENT '创建人';

-- sys_message.update_by  (varchar(64) NO -> YES)
ALTER TABLE `sys_message` MODIFY COLUMN `update_by` varchar(64) NULL COMMENT '更新人';

-- sys_message.create_by_id  (bigint NO -> YES)
ALTER TABLE `sys_message` MODIFY COLUMN `create_by_id` bigint NULL COMMENT '创建人ID';

-- sys_message.update_by_id  (bigint NO -> YES)
ALTER TABLE `sys_message` MODIFY COLUMN `update_by_id` bigint NULL COMMENT '更新人ID';

-- 403 回滚：撤体检登记表归属科室列与索引（体检列表回到不按科室收口的原状）
ALTER TABLE `biz_checkup_record`
  DROP KEY `idx_dept`,
  DROP COLUMN `dept_id`;

-- 执行后清 Redis：his:perm:role:*、his:user:identity:*

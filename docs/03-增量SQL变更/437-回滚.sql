-- ============================================================================
-- 437 回滚：撤掉供应商经营范围列与送药机器人厂商绑定
-- 只回滚 437 自己写入的东西，不动既有主档的任何业务字段
-- ============================================================================

-- 1) 机器人档案的厂商指针清空（brand/model 是 431 之前就有的，不回滚）
UPDATE sys_equipment
   SET supplier_id = NULL, supplier_name = NULL, update_by = 'sql437-rollback', update_time = NOW()
 WHERE del_flag = 0
   AND category = 8
   AND supplier_name = '上海擎朗智能科技有限公司';

-- 2) 删机器人厂商档案（先确认没有被任何单据引用）
DELETE FROM sys_supplier
 WHERE supplier_code = 'SUP016'
   AND supplier_id = 4370000000000000001;

-- 3) 删经营范围列与索引
ALTER TABLE sys_supplier
    DROP INDEX idx_supplier_type,
    DROP COLUMN supplier_type;

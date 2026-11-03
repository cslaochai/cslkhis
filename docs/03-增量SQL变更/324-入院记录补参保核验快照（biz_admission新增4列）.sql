-- ============================================================
-- 324 入院记录补参保核验快照（biz_admission）
-- 目标库：hn_biz_his  @ 192.168.88.132:3306
--
-- 背景：参保核验此前只在结算那一刻做，挂号/入院都不查；入院补查之后，
-- 必须把结果落在入院记录上，后续统计才能区分自费与医保，结算时也能直接复用快照。
--
-- 口径：一律静默降级放行，不拦入院。
--   0-未核验（主档无医保号也无身份证）
--   1-参保正常
--   2-查无参保
--   3-停保/参保状态异常（含身份不符）
--   4-核验失败（医保平台不可用/异常）
-- 险种、核验时间、说明为快照列，未核验/失败时可为 NULL（可空列不写 DEFAULT）。
--
-- 代码侧：his-core-spi 的 ChargeFacadeService#checkInsured → his-ms-charge 实现，
-- 入院登记 InpatientServiceImpl#admit 调用并写入本表。
--
-- 可重复执行：MySQL 8.0 不支持 ADD COLUMN IF NOT EXISTS，二次执行会报「列已存在」，属预期。
-- ============================================================
SET NAMES utf8mb4;

ALTER TABLE `biz_admission`
  ADD COLUMN `insurance_check_status` tinyint NOT NULL DEFAULT '0'
    COMMENT '入院参保核验结果（0-未核验 1-参保正常 2-查无参保 3-停保/异常 4-核验失败）' AFTER `admit_status`,
  ADD COLUMN `insurance_type` varchar(32)
    COMMENT '险种快照（核验时医保平台返回；未参保/核验失败为 NULL）' AFTER `insurance_check_status`,
  ADD COLUMN `insurance_checked_at` datetime
    COMMENT '参保核验时间（未发起核验为 NULL）' AFTER `insurance_type`,
  ADD COLUMN `insurance_check_msg` varchar(200)
    COMMENT '核验说明（未参保/停保/平台不可用的原因）' AFTER `insurance_checked_at`;

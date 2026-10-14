-- =====================================================================
-- 263-手术排班护士跟台与人力冲突校验.sql
-- 背景：手术申请单缺"巡回/洗手护士"（手术人力维度为空，排台总表看不到谁跟台，
--       AI 排班也没有护士×时段×手术间的历史数据）；排台只拦"同手术间时段重叠"，
--       同一主刀/麻醉医师在同一时段跨手术间分身两台不拦。
-- 本文件：biz_operation_apply 加巡回/洗手护士四列（幂等，information_schema 判列）。
-- 回查：SHOW COLUMNS FROM biz_operation_apply 应见 circulating_nurse_id 等四列
-- =====================================================================

SET @has_circ_id = (SELECT COUNT(*) FROM information_schema.COLUMNS
                    WHERE TABLE_SCHEMA='hn_biz_his' AND TABLE_NAME='biz_operation_apply' AND COLUMN_NAME='circulating_nurse_id');
SET @sql1 = IF(@has_circ_id = 0, 'ALTER TABLE biz_operation_apply ADD COLUMN circulating_nurse_id BIGINT NULL COMMENT ''巡回护士ID（员工ID）''', 'SELECT 1');
PREPARE st1 FROM @sql1; EXECUTE st1; DEALLOCATE PREPARE st1;

SET @has_circ_name = (SELECT COUNT(*) FROM information_schema.COLUMNS
                      WHERE TABLE_SCHEMA='hn_biz_his' AND TABLE_NAME='biz_operation_apply' AND COLUMN_NAME='circulating_nurse_name');
SET @sql2 = IF(@has_circ_name = 0, 'ALTER TABLE biz_operation_apply ADD COLUMN circulating_nurse_name VARCHAR(64) NULL COMMENT ''巡回护士姓名''', 'SELECT 1');
PREPARE st2 FROM @sql2; EXECUTE st2; DEALLOCATE PREPARE st2;

SET @has_scrub_id = (SELECT COUNT(*) FROM information_schema.COLUMNS
                     WHERE TABLE_SCHEMA='hn_biz_his' AND TABLE_NAME='biz_operation_apply' AND COLUMN_NAME='scrub_nurse_id');
SET @sql3 = IF(@has_scrub_id = 0, 'ALTER TABLE biz_operation_apply ADD COLUMN scrub_nurse_id BIGINT NULL COMMENT ''洗手护士ID（员工ID）''', 'SELECT 1');
PREPARE st3 FROM @sql3; EXECUTE st3; DEALLOCATE PREPARE st3;

SET @has_scrub_name = (SELECT COUNT(*) FROM information_schema.COLUMNS
                       WHERE TABLE_SCHEMA='hn_biz_his' AND TABLE_NAME='biz_operation_apply' AND COLUMN_NAME='scrub_nurse_name');
SET @sql4 = IF(@has_scrub_name = 0, 'ALTER TABLE biz_operation_apply ADD COLUMN scrub_nurse_name VARCHAR(64) NULL COMMENT ''洗手护士姓名''', 'SELECT 1');
PREPARE st4 FROM @sql4; EXECUTE st4; DEALLOCATE PREPARE st4;

-- 回查
SELECT COLUMN_NAME, COLUMN_TYPE, COLUMN_COMMENT FROM information_schema.COLUMNS
WHERE TABLE_SCHEMA='hn_biz_his' AND TABLE_NAME='biz_operation_apply'
  AND COLUMN_NAME IN ('circulating_nurse_id','circulating_nurse_name','scrub_nurse_id','scrub_nurse_name');

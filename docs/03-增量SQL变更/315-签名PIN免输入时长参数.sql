-- ============================================================================
-- 315-软签新增「签名PIN免输入时长」参数（sign.pin.ttl_minutes，默认 30 分钟）
-- ============================================================================
-- 背景：软签每次都要输 PIN，医生一天几十份病历就要几十次确认，实际是把人推向
--   「把 PIN 贴在屏幕上」这种更坏的做法。本次给签名 PIN 加免输入窗口：
--   一次校验通过后，窗口内继续签名不再要 PIN；每次签名自动续期，超时后重新输入。
--   窗口时长按 sys_config 配置（与 sign.cert.valid_days 等同一组，代码缺省 30 分钟），
--   配 0 = 关掉免输入，每次签名都必须输 PIN。
--   窗口键绑「员工 + 证书」，换发或吊销证书后旧窗口即失效（不落库，只在 Redis 计时）。
--
-- 幂等：按 config_key 判存在（唯一键 uk_config_key），已存在则 0 行生效、不覆盖运维改过的值。
-- ⚠ 该参数由「系统管理→参数配置→电子签名」页维护，改完即时生效（每次签名现读配置）。
-- 回滚：DELETE FROM sys_config WHERE config_key = 'sign.pin.ttl_minutes';
-- ============================================================================

INSERT INTO sys_config (config_id, config_name, config_key, config_value, config_type, is_system, remark,
                        create_by, create_time, update_by, update_time, create_by_id, update_by_id)
SELECT 9000000000000000004, '签名PIN免输入时长（分钟）', 'sign.pin.ttl_minutes', '30', 1, 0,
       '软签 PIN 一次校验通过后的免输入时长；0 表示每次签名都要重新输入',
       'sql315', NOW(), 'sql315', NOW(), 1, 1
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM sys_config WHERE config_key = 'sign.pin.ttl_minutes');

-- ---------------- 回查（期望一行：sign.pin.ttl_minutes / 30） ----------------
SELECT config_id, config_name, config_key, config_value
FROM sys_config
WHERE config_key LIKE 'sign.%'
ORDER BY config_key;

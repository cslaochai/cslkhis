-- 307: 电子签名证书台账补「软签 PIN」与「CA 申请单」列
-- 为什么要这几列：
--   1. 证书签发改为**线下人工**（HIS 与 CA 认证中心的交互全程人工，不再调接口）：
--      管理员在 HIS 生成密钥对 → 拿「CA 签发申请单」（Subject DN + 公钥）线下送 CA 人工签发
--      → CA 返回证书 PEM → 管理员回填。ca_subject_dn 存申请单上的主体 DN，供人工核对与留痕。
--   2. 无 UKey 硬件时用 PIN 代替芯片内 PIN 做「签名人专有」的软确认：
--      每次签名都要签名人本人输入 PIN，PIN 校验通过才解密私钥。
--      明文 PIN 绝不落库——只存 PBKDF2 派生哈希（pin_hash）与派生盐（pin_iterations 记录轮次）。
--   3. cert_status 新增 0-待CA签发：已生成密钥对但证书尚未回填，该状态不可用于签名。
--
-- 存量数据说明：老证书没有 PIN（pin_hash 为空），签名时会被拒绝——这是预期的，
-- 管理员需为这些员工重新走「生成密钥对 + 线下送 CA + 回填」流程后即可正常签名。
ALTER TABLE `sys_sign_cert`
  ADD COLUMN `pin_hash` varchar(128) DEFAULT NULL COMMENT '签名PIN哈希（PBKDF2派生，明文不入库）' AFTER `sign_count`,
  ADD COLUMN `pin_salt` varchar(64) DEFAULT NULL COMMENT 'PIN哈希派生盐（Base64）' AFTER `pin_hash`,
  ADD COLUMN `pin_iterations` int DEFAULT NULL COMMENT 'PIN哈希派生迭代次数' AFTER `pin_salt`,
  ADD COLUMN `ca_subject_dn` varchar(255) DEFAULT NULL COMMENT 'CA签发申请单主体DN（线下送CA时用）' AFTER `pin_iterations`;

-- cert_status 取值扩展：0-待CA签发 1-有效 2-已吊销（原 DDL 的注释同步更新）
ALTER TABLE `sys_sign_cert`
  MODIFY COLUMN `cert_status` tinyint NOT NULL DEFAULT '1' COMMENT '证书状态（0-待CA签发 1-有效 2-已吊销）';

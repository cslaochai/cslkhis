-- 267-参保台账表退役（医保参保只走远程）
-- 背景：biz_insured_person 是医保局参保库在院内的镜像台账，只被 mock 通道的 checkInsured 读。
--       真实参保状态只有一个权威来源 = 医保结算经办平台（back_end_mss /insure/person/query）；
--       院内台账必然与医保局漂移（停保、销户、异地参保都不会回写），拿它判"能不能报销"
--       迟早和医保清算对不上。口径改为：结算那一刻远程查一次，查不到参保就按自费原价收，不拦单。
-- 影响面：BizInsuredPerson 实体与 BizInsuredPersonMapper 已删除；
--       InsuranceChannelServiceImpl（mock 通道）不再持有 Mapper，checkInsured 恒返回"未参保"。
-- 顺序：先发布后端（不再映射该表）再执行本文件。
--
-- dev 库该表在本次改造前无数据（台账从未作为业务入口，仅骨架），DROP 不丢业务数据。

DROP TABLE IF EXISTS `biz_insured_person`;
-- 292 处方模板增加处方类型列
-- 背景：医生工作台「常用药品」快捷 chips 需要按处方类型 tab（西药/中成药/中药饮片）过滤。
--      旧模板 prescription_type 留 NULL（前端视为不限制 tab，避免历史模板在任一 tab 下消失）。
ALTER TABLE `biz_rx_template`
    ADD COLUMN `prescription_type` tinyint NULL COMMENT '处方类型（1-西药 2-中成药 3-中药饮片）' AFTER `template_name`;

-- 293 班次字典扩展展示与工时津贴字段
-- 背景：标准HIS班次字典除起止时间/适用域外，还需排班图短码/缩写/颜色，以及工时与夜班津贴判定参数。
--      本次只加列不改口径，历史行按默认值落库（计工时=1、计夜班津贴=0）。
ALTER TABLE `biz_shift`
    ADD COLUMN `shift_code`           VARCHAR(16) NULL     COMMENT '班次业务短码（AM/PM/N/E/R，对接考勤机/薪资用）' AFTER `shift_name`,
    ADD COLUMN `short_name`            VARCHAR(4)  NULL     COMMENT '排班图显示缩写（如 夜/休/听）'                 AFTER `shift_code`,
    ADD COLUMN `color`                 VARCHAR(7)  NULL     COMMENT '排班图颜色HEX（如 #F56C6C）'                  AFTER `short_name`,
    ADD COLUMN `count_work_hours`      TINYINT     NOT NULL DEFAULT 1 COMMENT '是否计工时（1-是 0-否）'              AFTER `late_grace_minutes`,
    ADD COLUMN `count_night_allowance` TINYINT     NOT NULL DEFAULT 0 COMMENT '是否计夜班津贴（1-是 0-否）'          AFTER `count_work_hours`;

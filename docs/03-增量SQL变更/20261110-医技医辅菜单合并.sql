-- ============================================================
-- 20261110-医技医辅菜单合并
-- 日期：2026-11-10
-- 目的：把 11 个过度拆分的医技医辅菜单收成 5 个入口 + 页内 tab，
--       被合掉的菜单只隐藏（is_visible=0），del_flag 保持 0、权限码与角色授权不动，
--       因此前端 v-perm 与服务端 @PreAuthorize 都不受影响。
-- 前置（务必先满足，否则点进去 404）：
--   source/front/src/views/module-center/ 下 5 个容器页已上线：
--     ExamCenterView / LisCenterView / TransfusionCenterView / DialysisCenterView / ExamAppointmentCenterView
--   且 router/index.js 中 5 条静态路由（inspection-workstation / laboratory-workstation /
--     transfusion / dialysis / exam-appointment）的 component 已指向上述容器。
-- 依据：联网核对主流 HIS/PACS/LIS 厂商（九阵 AHIS、嘉讯、CNWAYLab 等）模块组织方式，
--       与本地《招标需求-广西》《HIS-功能-价格表.pdf》口径一致：
--       放射/超声/内镜/心电/胶片收进统一 PACS；标本/质控/间质评收进统一 LIS；
--       血库并入输血；机位并入血透；病理厂商普遍单独成系统（保留）。
-- 回滚：见文件末尾「回滚段」。注意回滚还需把 router/index.js 5 条静态路由 component 改回原 View。
-- ============================================================

-- A 检查工作站（PACS）：401 保留为统一入口，模态收为页内 tab
UPDATE sys_menu SET component = 'module-center/ExamCenterView', update_time = NOW() WHERE id = 401;
UPDATE sys_menu SET is_visible = 0, update_time = NOW() WHERE id IN (414, 409, 408, 417, 415);

-- B 检验工作站（LIS）：402 保留为统一入口，标本/质控/间质评收为页内 tab
UPDATE sys_menu SET component = 'module-center/LisCenterView', update_time = NOW() WHERE id = 402;
UPDATE sys_menu SET is_visible = 0, update_time = NOW() WHERE id IN (403, 410, 416);

-- C 输血管理：406 保留，血库收为页内 tab
UPDATE sys_menu SET component = 'module-center/TransfusionCenterView', update_time = NOW() WHERE id = 406;
UPDATE sys_menu SET is_visible = 0, update_time = NOW() WHERE id = 411;

-- D 血液净化中心：413 保留，机位收为页内 tab
UPDATE sys_menu SET component = 'module-center/DialysisCenterView', update_time = NOW() WHERE id = 413;
UPDATE sys_menu SET is_visible = 0, update_time = NOW() WHERE id = 2931;

-- E 检查预约中心：412 保留，设备与号源收为页内 tab
UPDATE sys_menu SET component = 'module-center/ExamAppointmentCenterView', update_time = NOW() WHERE id = 412;
UPDATE sys_menu SET is_visible = 0, update_time = NOW() WHERE id = 2925;

-- F 被隐藏菜单加标注，避免在「角色管理-菜单权限」树里和合并后的入口重名
UPDATE sys_menu SET menu_name = '放射诊断工作站（已并入检查工作站）', update_time = NOW() WHERE id = 414;
UPDATE sys_menu SET menu_name = '超声工作站（已并入检查工作站）', update_time = NOW() WHERE id = 409;
UPDATE sys_menu SET menu_name = '内镜工作站（已并入检查工作站）', update_time = NOW() WHERE id = 408;
UPDATE sys_menu SET menu_name = '心电工作站（已并入检查工作站）', update_time = NOW() WHERE id = 417;
UPDATE sys_menu SET menu_name = '胶片量方与发放（已并入检查工作站）', update_time = NOW() WHERE id = 415;
UPDATE sys_menu SET menu_name = '标本管理（已并入检验工作站）', update_time = NOW() WHERE id = 403;
UPDATE sys_menu SET menu_name = '检验质控（已并入检验工作站）', update_time = NOW() WHERE id = 410;
UPDATE sys_menu SET menu_name = '室间质评（已并入检验工作站）', update_time = NOW() WHERE id = 416;
UPDATE sys_menu SET menu_name = '血库管理（已并入输血管理）', update_time = NOW() WHERE id = 411;
UPDATE sys_menu SET menu_name = '血液净化机位管理（已并入血液净化中心）', update_time = NOW() WHERE id = 2931;
UPDATE sys_menu SET menu_name = '检查设备与号源（已并入检查预约中心）', update_time = NOW() WHERE id = 2925;

-- G 保留不动：407 病理工作站（厂商单独成系统）、404 检查项目字典、405 检验项目字典（配置类，暂不合并）

-- ============================================================
-- 回滚段：恢复被合掉菜单的可见性 + 原名 + 原 component
-- 注意：本段同时需把 router/index.js 的 5 条静态路由 component 改回原 View，
--       否则存活入口仍会渲染容器页（容器页本身无害，只是回到多菜单前的样式）。
-- ============================================================
-- UPDATE sys_menu SET is_visible = 1, menu_name = '放射诊断工作站', component = 'lab/RadioDiagnosisView', update_time = NOW() WHERE id = 414;
-- UPDATE sys_menu SET is_visible = 1, menu_name = '超声工作站', component = 'ultrasound/UltrasoundView', update_time = NOW() WHERE id = 409;
-- UPDATE sys_menu SET is_visible = 1, menu_name = '内镜工作站', component = 'endoscopy/EndoscopyView', update_time = NOW() WHERE id = 408;
-- UPDATE sys_menu SET is_visible = 1, menu_name = '心电工作站', component = 'lab/EcgWorkstationView', update_time = NOW() WHERE id = 417;
-- UPDATE sys_menu SET is_visible = 1, menu_name = '胶片量方与发放', component = 'lab/ExamFilmView', update_time = NOW() WHERE id = 415;
-- UPDATE sys_menu SET is_visible = 1, menu_name = '标本管理', component = 'specimen/SpecimenView', update_time = NOW() WHERE id = 403;
-- UPDATE sys_menu SET is_visible = 1, menu_name = '检验质控', component = 'lisqc/LisQcView', update_time = NOW() WHERE id = 410;
-- UPDATE sys_menu SET is_visible = 0, menu_name = '室间质评', component = 'lisqc/LisEqaView', update_time = NOW() WHERE id = 416;  -- 416 合入前本就是 is_visible=0
-- UPDATE sys_menu SET is_visible = 1, menu_name = '血库管理', component = 'bloodbank/BloodBankView', update_time = NOW() WHERE id = 411;
-- UPDATE sys_menu SET is_visible = 1, menu_name = '血液净化机位管理', component = 'dialysis/DialysisMachineView', update_time = NOW() WHERE id = 2931;
-- UPDATE sys_menu SET is_visible = 1, menu_name = '检查设备与号源', component = 'exam-appointment/ExamDeviceSlotView', update_time = NOW() WHERE id = 2925;
-- UPDATE sys_menu SET component = 'lab/InspectionView', update_time = NOW() WHERE id = 401;
-- UPDATE sys_menu SET component = 'lab/LaboratoryView', update_time = NOW() WHERE id = 402;
-- UPDATE sys_menu SET component = 'transfusion/TransfusionView', update_time = NOW() WHERE id = 406;
-- UPDATE sys_menu SET component = 'exam-appointment/ExamAppointmentView', update_time = NOW() WHERE id = 412;
-- UPDATE sys_menu SET component = 'dialysis/DialysisView', update_time = NOW() WHERE id = 413;

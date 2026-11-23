-- 343 隐藏页转功能按钮（宿主页下）+ 角色补页
-- 日期：2026-11-20
-- 背景：18 个「已并入」隐藏页被后端 @PreAuthorize 和前端 v-perm 当作功能权限码使用，
--       不能直接删授权。正确做法是把它们从「页面(menu_type=2)」降为「按钮(menu_type=3)」，
--       挂在宿主页下：侧栏不再出现，权限码照常下发，前后端鉴权零改动。
-- 效果：页面 181 -> 163，按钮 348 -> 366；18 条授权原样保留（变成按钮授权）。
-- 另：信息科管理员/科室排班员/放射诊断医师/麻醉医师/院办值班管理员 补页；医生摘手术器械清点。

UPDATE sys_menu SET menu_type=3, parent_id=402, path=NULL, component=NULL, menu_name='标本管理', is_visible=1, update_time=NOW() WHERE id=403;
UPDATE sys_menu SET menu_type=3, parent_id=401, path=NULL, component=NULL, menu_name='内镜工作站', is_visible=1, update_time=NOW() WHERE id=408;
UPDATE sys_menu SET menu_type=3, parent_id=401, path=NULL, component=NULL, menu_name='超声工作站', is_visible=1, update_time=NOW() WHERE id=409;
UPDATE sys_menu SET menu_type=3, parent_id=402, path=NULL, component=NULL, menu_name='检验质控', is_visible=1, update_time=NOW() WHERE id=410;
UPDATE sys_menu SET menu_type=3, parent_id=406, path=NULL, component=NULL, menu_name='血库管理', is_visible=1, update_time=NOW() WHERE id=411;
UPDATE sys_menu SET menu_type=3, parent_id=401, path=NULL, component=NULL, menu_name='放射诊断工作站', is_visible=1, update_time=NOW() WHERE id=414;
UPDATE sys_menu SET menu_type=3, parent_id=401, path=NULL, component=NULL, menu_name='胶片量方与发放', is_visible=1, update_time=NOW() WHERE id=415;
UPDATE sys_menu SET menu_type=3, parent_id=402, path=NULL, component=NULL, menu_name='室间质评', is_visible=1, update_time=NOW() WHERE id=416;
UPDATE sys_menu SET menu_type=3, parent_id=401, path=NULL, component=NULL, menu_name='心电工作站', is_visible=1, update_time=NOW() WHERE id=417;
UPDATE sys_menu SET menu_type=3, parent_id=602, path=NULL, component=NULL, menu_name='病历审核', is_visible=1, update_time=NOW() WHERE id=603;
UPDATE sys_menu SET menu_type=3, parent_id=604, path=NULL, component=NULL, menu_name='全院数据质量监测', is_visible=1, update_time=NOW() WHERE id=605;
UPDATE sys_menu SET menu_type=3, parent_id=609, path=NULL, component=NULL, menu_name='病案借阅复印', is_visible=1, update_time=NOW() WHERE id=611;
UPDATE sys_menu SET menu_type=3, parent_id=609, path=NULL, component=NULL, menu_name='编码任务池', is_visible=1, update_time=NOW() WHERE id=612;
UPDATE sys_menu SET menu_type=3, parent_id=610, path=NULL, component=NULL, menu_name='医疗纠纷与投诉', is_visible=1, update_time=NOW() WHERE id=615;
UPDATE sys_menu SET menu_type=3, parent_id=613, path=NULL, component=NULL, menu_name='公卫上报-其他上报', is_visible=1, update_time=NOW() WHERE id=618;
UPDATE sys_menu SET menu_type=3, parent_id=607, path=NULL, component=NULL, menu_name='单病种质控', is_visible=1, update_time=NOW() WHERE id=2183;
UPDATE sys_menu SET menu_type=3, parent_id=412, path=NULL, component=NULL, menu_name='检查设备与号源', is_visible=1, update_time=NOW() WHERE id=2925;
UPDATE sys_menu SET menu_type=3, parent_id=413, path=NULL, component=NULL, menu_name='血液净化机位管理', is_visible=1, update_time=NOW() WHERE id=2931;

INSERT INTO sys_role_menu (id, role_id, menu_id, create_by, create_time, update_by, update_time, create_by_id, update_by_id) VALUES
 (9210000000000000675, 33, 1100, 'menu-govern-343', NOW(), 'menu-govern-343', NOW(), 0, 0),
 (9210000000000000676, 33, 1101, 'menu-govern-343', NOW(), 'menu-govern-343', NOW(), 0, 0),
 (9210000000000000677, 33, 1102, 'menu-govern-343', NOW(), 'menu-govern-343', NOW(), 0, 0),
 (9210000000000000678, 33, 1103, 'menu-govern-343', NOW(), 'menu-govern-343', NOW(), 0, 0),
 (9210000000000000679, 33, 1104, 'menu-govern-343', NOW(), 'menu-govern-343', NOW(), 0, 0),
 (9210000000000000680, 33, 1105, 'menu-govern-343', NOW(), 'menu-govern-343', NOW(), 0, 0),
 (9210000000000000681, 33, 1106, 'menu-govern-343', NOW(), 'menu-govern-343', NOW(), 0, 0),
 (9210000000000000682, 33, 1108, 'menu-govern-343', NOW(), 'menu-govern-343', NOW(), 0, 0),
 (9210000000000000683, 33, 1109, 'menu-govern-343', NOW(), 'menu-govern-343', NOW(), 0, 0),
 (9210000000000000684, 33, 2942, 'menu-govern-343', NOW(), 'menu-govern-343', NOW(), 0, 0),
 (9210000000000000685, 33, 2946, 'menu-govern-343', NOW(), 'menu-govern-343', NOW(), 0, 0),
 (9210000000000000686, 33, 29652, 'menu-govern-343', NOW(), 'menu-govern-343', NOW(), 0, 0),
 (9210000000000000688, 33, 29626, 'menu-govern-343', NOW(), 'menu-govern-343', NOW(), 0, 0),
 (9210000000000000689, 28, 2938, 'menu-govern-343', NOW(), 'menu-govern-343', NOW(), 0, 0),
 (9210000000000000690, 28, 29609, 'menu-govern-343', NOW(), 'menu-govern-343', NOW(), 0, 0),
 (9210000000000000691, 20, 608, 'menu-govern-343', NOW(), 'menu-govern-343', NOW(), 0, 0),
 (9210000000000000692, 20, 404, 'menu-govern-343', NOW(), 'menu-govern-343', NOW(), 0, 0),
 (9210000000000000693, 31, 608, 'menu-govern-343', NOW(), 'menu-govern-343', NOW(), 0, 0),
 (9210000000000000694, 36, 2920, 'menu-govern-343', NOW(), 'menu-govern-343', NOW(), 0, 0),
 (9210000000000000695, 36, 2936, 'menu-govern-343', NOW(), 'menu-govern-343', NOW(), 0, 0);

DELETE FROM sys_role_menu WHERE role_id=2 AND menu_id=2924;

-- 343 回滚：按钮恢复为隐藏页 + 删除补页授权 + 医生恢复器械清点
UPDATE sys_menu SET menu_type=2, parent_id=400, path='/specimen', component='specimen/SpecimenView', menu_name='标本管理（已并入检验工作站）', is_visible=0, update_time=NOW() WHERE id=403;
UPDATE sys_menu SET menu_type=2, parent_id=400, path='/endoscopy', component='endoscopy/EndoscopyView', menu_name='内镜工作站（已并入检查工作站）', is_visible=0, update_time=NOW() WHERE id=408;
UPDATE sys_menu SET menu_type=2, parent_id=400, path='/ultrasound', component='ultrasound/UltrasoundView', menu_name='超声工作站（已并入检查工作站）', is_visible=0, update_time=NOW() WHERE id=409;
UPDATE sys_menu SET menu_type=2, parent_id=400, path='/lis-qc', component='lisqc/LisQcView', menu_name='检验质控（已并入检验工作站）', is_visible=0, update_time=NOW() WHERE id=410;
UPDATE sys_menu SET menu_type=2, parent_id=400, path='/blood-bank', component='bloodbank/BloodBankView', menu_name='血库管理（已并入输血管理）', is_visible=0, update_time=NOW() WHERE id=411;
UPDATE sys_menu SET menu_type=2, parent_id=400, path='/radio-diagnosis', component='lab/RadioDiagnosisView', menu_name='放射诊断工作站（已并入检查工作站）', is_visible=0, update_time=NOW() WHERE id=414;
UPDATE sys_menu SET menu_type=2, parent_id=400, path='/exam-film', component='lab/ExamFilmView', menu_name='胶片量方与发放（已并入检查工作站）', is_visible=0, update_time=NOW() WHERE id=415;
UPDATE sys_menu SET menu_type=2, parent_id=400, path='/lis-eqa', component='lisqc/LisEqaView', menu_name='室间质评（已并入检验工作站）', is_visible=0, update_time=NOW() WHERE id=416;
UPDATE sys_menu SET menu_type=2, parent_id=400, path='/ecg', component='lab/EcgWorkstationView', menu_name='心电工作站（已并入检查工作站）', is_visible=0, update_time=NOW() WHERE id=417;
UPDATE sys_menu SET menu_type=2, parent_id=600, path='/medical-review', component='medical-record/MedicalReviewView', menu_name='病历审核（已并入院历查阅与审核）', is_visible=0, update_time=NOW() WHERE id=603;
UPDATE sys_menu SET menu_type=2, parent_id=600, path='/data-quality', component='quality/DataQualityView', menu_name='全院数据质量监测（已并入院历质控）', is_visible=0, update_time=NOW() WHERE id=605;
UPDATE sys_menu SET menu_type=2, parent_id=600, path='/archive-borrow', component='medical-record/ArchiveBorrowView', menu_name='病案借阅复印（已并入病案管理）', is_visible=0, update_time=NOW() WHERE id=611;
UPDATE sys_menu SET menu_type=2, parent_id=600, path='/coding-task', component='medical-record/CodingTaskView', menu_name='编码任务池（已并入病案管理）', is_visible=0, update_time=NOW() WHERE id=612;
UPDATE sys_menu SET menu_type=2, parent_id=29700, path='/dispute', component='dispute/DisputeView', menu_name='医疗纠纷与投诉（已并入医疗安全）', is_visible=0, update_time=NOW() WHERE id=615;
UPDATE sys_menu SET menu_type=2, parent_id=29701, path='/public-health', component='public-health/PublicHealthReportView', menu_name='公卫上报-其他上报（已并入公卫上报）', is_visible=0, update_time=NOW() WHERE id=618;
UPDATE sys_menu SET menu_type=2, parent_id=29700, path='/singleDisease', component='quality/SingleDiseaseView', menu_name='单病种质控（已并入专项质控）', is_visible=0, update_time=NOW() WHERE id=2183;
UPDATE sys_menu SET menu_type=2, parent_id=400, path='/exam-device-slot', component='exam-appointment/ExamDeviceSlotView', menu_name='检查设备与号源（已并入检查预约中心）', is_visible=0, update_time=NOW() WHERE id=2925;
UPDATE sys_menu SET menu_type=2, parent_id=400, path='/dialysis-machine', component='dialysis/DialysisMachineView', menu_name='血液净化机位管理（已并入血液净化中心）', is_visible=0, update_time=NOW() WHERE id=2931;
DELETE FROM sys_role_menu WHERE create_by='menu-govern-343';
INSERT INTO sys_role_menu (id, role_id, menu_id, create_by, create_time, update_by, update_time, create_by_id, update_by_id) VALUES (9210000000000000699, 2, 2924, 'rollback-343', NOW(), 'rollback-343', NOW(), 0, 0);

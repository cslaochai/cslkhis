-- 352 角色描述补全与纠偏
-- 日期：2026-11-20
-- 模板：岗位定位 + 核心职责 + 数据/权限边界。
-- 修正：1-25 原为空；28/29 带「演示角色」字样；27 混入变更日期；31/32 写的是排台技术口径而非职责；
--       33 只写 CA 证书、26 只写人力标准、25 只写排班号源，均严重窄化；30 把流程规则当描述；34/35 混入操作细节。

UPDATE sys_role SET remark='全院信息系统最高权限账号：负责用户、角色、菜单、参数、字典与安全审计；仅限信息科指定人员，不承担业务操作。' WHERE id=1; -- 系统管理员
UPDATE sys_role SET remark='门诊与住院诊疗主岗：负责接诊、开立医嘱与书写病历、会诊转科、出院随访与线上问诊；患者范围按所在科室收口。' WHERE id=2; -- 医生
UPDATE sys_role SET remark='病区与门诊护理执行岗：负责医嘱执行、输液与血液净化、入出院转科办理、病情观察与护理记录；按所在病区收口。' WHERE id=3; -- 护士
UPDATE sys_role SET remark='收费窗口岗：负责挂号收费、住院预交与结算、发票与退费受理、财务日结；不修改价格与医保政策。' WHERE id=4; -- 收费员
UPDATE sys_role SET remark='药房调剂岗：负责门急诊与住院发药、摆药、静配、盘点与药品追溯；麻精药品按专册双人管理。' WHERE id=5; -- 药剂师
UPDATE sys_role SET remark='医技检查执行岗：负责检验、检查、病理、输血、透析等医技工作站操作与报告回填；诊断审核签名由相应医师完成。' WHERE id=6; -- 医技人员
UPDATE sys_role SET remark='门诊服务台岗：负责挂号预约、患者建档与标签、就诊引导、收费协助；不接触病历与医嘱。' WHERE id=7; -- 前台导诊
UPDATE sys_role SET remark='急诊诊疗岗：负责急诊接诊、抢救记录、急诊值班与听班响应；患者范围限急诊在诊患者。' WHERE id=8; -- 急诊医生
UPDATE sys_role SET remark='急诊预检分诊岗：负责预检分诊、分级判定、急诊挂号与就诊秩序、门诊日志；不执行医嘱。' WHERE id=9; -- 分诊护士
UPDATE sys_role SET remark='病区护理管理岗：负责病区排班、护理质控、床位与危重患者管理、护理安全事件处置；管辖本科室护理单元。' WHERE id=10; -- 护士长
UPDATE sys_role SET remark='检验执行岗：负责标本接收与检验、室内质控、输血配血、危急值上报；报告审核由上级技师完成。' WHERE id=11; -- 检验技师
UPDATE sys_role SET remark='检查执行岗：负责影像、超声、心电、内镜登记，设备与号源、检查预约、胶片与报告回填；诊断结论由诊断医师出具。' WHERE id=12; -- 检查技师
UPDATE sys_role SET remark='临床药学服务岗：负责处方审核与点评、抗菌药物分级与使用监测、围手术期用药、合理用药咨询；不参与发药。' WHERE id=13; -- 临床药师
UPDATE sys_role SET remark='病案管理岗：负责病历回收审核、疾病与手术编码、病案首页质控、患者主索引维护；不修改临床记录。' WHERE id=14; -- 病案编码员
UPDATE sys_role SET remark='病案质控岗：负责运行与终末病历质控、专项质控、评分与整改跟踪；覆盖全院出院病历。' WHERE id=15; -- 病案质控员
UPDATE sys_role SET remark='医保业务岗：负责医保结算与清单上传、目录对照、慢特病备案、扣款飞检处理、合规台账与财务日结；不修改临床医嘱。' WHERE id=16; -- 医保结算员
UPDATE sys_role SET remark='运营监督岗：负责 BI 与运营报表、DRG/DIP 分组模拟、绩效成本核算、资金与支付对账、病案统计上报；只读不改业务数据。' WHERE id=17; -- 审计统计员
UPDATE sys_role SET remark='院级管理岗：查看全院运营、质量、绩效与排班总览，审批重大事项；只读报表，不操作临床与收费。' WHERE id=18; -- 院领导
UPDATE sys_role SET remark='患者自助端身份：查看本人健康档案、检验白话解释、满意度评价与常见问题；仅限本人数据。' WHERE id=19; -- 患者
UPDATE sys_role SET remark='影像诊断岗：负责放射报告书写、审核与发布，危急值处置，检查项目字典维护；不承担检查执行。' WHERE id=20; -- 放射诊断医师
UPDATE sys_role SET remark='临床营养岗：负责营养风险筛查、膳食医嘱、营养会诊、订餐配送与营养指标监测；覆盖住院患者。' WHERE id=21; -- 营养师
UPDATE sys_role SET remark='公共卫生岗：负责慢病建档与长处方、随访回访、传染病与院感上报、死亡与肿瘤报卡；覆盖全院上报。' WHERE id=22; -- 公卫医师
UPDATE sys_role SET remark='行政后勤岗：负责满意度、工单受理、总值班日志与值班点位/类型维护、排班总览；不接触临床数据。' WHERE id=23; -- 行政人员
UPDATE sys_role SET remark='患者服务岗：负责满意度回访、咨询工单、体检预约与线上问诊受理；不查看完整病历。' WHERE id=24; -- 客服专员
UPDATE sys_role SET remark='门诊管理职能岗：负责号源与门诊排班审核、诊室管理、双向转诊、体检业务与满意度；管辖全院门诊。' WHERE id=25; -- 门诊办
UPDATE sys_role SET remark='医疗管理职能岗：负责会诊转诊、日间手术准入、死亡证明、病历与专项质控、技术授权、医师排班与值班、签名管理；全院医疗业务监管。' WHERE id=26; -- 医务科
UPDATE sys_role SET remark='科室医疗负责人：负责本科室查房与病历质控、会诊转诊、日间手术、危重与死亡病例、科室排班与听班；管辖本科室。' WHERE id=27; -- 科主任
UPDATE sys_role SET remark='科室排班执行岗：负责门诊排班编制、排班字典与历史查询、排班总览；审核权归门诊办。' WHERE id=28; -- 科室排班员
UPDATE sys_role SET remark='全院护理管理岗：负责护理排班统筹、护理质量与安全、全院总值班；管辖全院护理单元。' WHERE id=29; -- 护理部主任
UPDATE sys_role SET remark='护理片区管理岗：负责片区护理排班审核、跨病区人力调配、听班与质量督查；管辖所属片区。' WHERE id=30; -- 科护士长
UPDATE sys_role SET remark='手术麻醉岗：负责术前访视与麻醉方案、术中麻醉记录与复苏、术后镇痛与危急值处置；仅限手术患者。' WHERE id=31; -- 麻醉医师
UPDATE sys_role SET remark='手术室护理岗：负责手术排台配合、器械清点与交接、无菌物品管理、日间手术准入核对；仅限手术区域。' WHERE id=32; -- 手术室护士
UPDATE sys_role SET remark='信息系统运维岗：负责用户角色菜单、参数字典、机构与主数据映射、日志审计、知识库与流程配置、CA 证书台账；不办理业务。' WHERE id=33; -- 信息科管理员
UPDATE sys_role SET remark='护理人力调配岗：负责护士岗位归属与生效日期维护、跨科室护理排班；不改变科室编制。' WHERE id=34; -- 护理调配员
UPDATE sys_role SET remark='医务人力调配岗：负责医师岗位归属与生效日期维护、跨科室门诊与急诊排班；不改变科室编制。' WHERE id=35; -- 医务调配员
UPDATE sys_role SET remark='院办值班管理岗：负责行政总值班与院领导带班排班、值班点位与类型维护、交班本管理；全院发布审核。' WHERE id=36; -- 院办值班管理员
UPDATE sys_role SET remark='药库管理岗：负责药品采购与供应商、入库验收、调拨退货、盘点与追溯；麻精药品按专册管理。' WHERE id=37; -- 药库管理员
UPDATE sys_role SET remark='物资仓储岗：负责物资耗材入库出库、库存盘点、科室领用与耗材字典维护；不含设备与消毒供应。' WHERE id=38; -- 物资库管员
UPDATE sys_role SET remark='设备与消毒供应岗：负责医疗设备台账与维保、CSSD 器械包与灭菌、医废处置与追溯；不含药品与耗材采购。' WHERE id=39; -- 设备与供应室管理员
UPDATE sys_role SET remark='医疗服务价格岗：负责收费项目与价格维护、复诊收费策略、价格公示与调价报备；不执行收费与退费。' WHERE id=40; -- 物价员
UPDATE sys_role SET remark='统计上报岗：负责运营统计报表编制、病案与卫生统计上报、数据质量核对；只读业务数据。' WHERE id=41; -- 统计上报员

-- 回滚（执行前原值）
-- UPDATE sys_role SET remark=NULL WHERE id=1;
-- UPDATE sys_role SET remark=NULL WHERE id=2;
-- UPDATE sys_role SET remark=NULL WHERE id=3;
-- UPDATE sys_role SET remark=NULL WHERE id=4;
-- UPDATE sys_role SET remark=NULL WHERE id=5;
-- UPDATE sys_role SET remark=NULL WHERE id=6;
-- UPDATE sys_role SET remark=NULL WHERE id=7;
-- UPDATE sys_role SET remark=NULL WHERE id=8;
-- UPDATE sys_role SET remark=NULL WHERE id=9;
-- UPDATE sys_role SET remark=NULL WHERE id=10;
-- UPDATE sys_role SET remark=NULL WHERE id=11;
-- UPDATE sys_role SET remark=NULL WHERE id=12;
-- UPDATE sys_role SET remark=NULL WHERE id=13;
-- UPDATE sys_role SET remark=NULL WHERE id=14;
-- UPDATE sys_role SET remark=NULL WHERE id=15;
-- UPDATE sys_role SET remark=NULL WHERE id=16;
-- UPDATE sys_role SET remark=NULL WHERE id=17;
-- UPDATE sys_role SET remark=NULL WHERE id=18;
-- UPDATE sys_role SET remark=NULL WHERE id=19;
-- UPDATE sys_role SET remark=NULL WHERE id=20;
-- UPDATE sys_role SET remark=NULL WHERE id=21;
-- UPDATE sys_role SET remark=NULL WHERE id=22;
-- UPDATE sys_role SET remark=NULL WHERE id=23;
-- UPDATE sys_role SET remark=NULL WHERE id=24;
-- UPDATE sys_role SET remark='门诊排班/号源的业务主管岗（院级职能处室），data_scope=1 全院' WHERE id=25;
-- UPDATE sys_role SET remark='全院岗位出勤/岗位人力标准的主管岗，data_scope=1 全院' WHERE id=26;
-- UPDATE sys_role SET remark='本科室岗位出勤与急诊排班负责人，data_scope=3 本部门；门诊排班归科室排班员(28)，20261027 收回门诊排班域' WHERE id=27;
-- UPDATE sys_role SET remark='排班域演示角色·科室排班员' WHERE id=28;
-- UPDATE sys_role SET remark='排班域演示角色·护理部主任' WHERE id=29;
-- UPDATE sys_role SET remark='护理片区（大科）级护理排班审核人，编制与审核分离' WHERE id=30;
-- UPDATE sys_role SET remark='手术排台「麻醉医师」候选口径：排台时按此角色取人，不再全院 535 人' WHERE id=31;
-- UPDATE sys_role SET remark='手术排台「巡回/器械护士」候选口径：排台时按此角色取人' WHERE id=32;
-- UPDATE sys_role SET remark='信息科固定岗：签名证书台账查询与办理（CA在线申请/回填）；吊销处置归医务科（分权）' WHERE id=33;
-- UPDATE sys_role SET remark='护理部人力调配岗：改护士岗位归属（含生效/失效日期）+ 跨科室排班（不改编制）' WHERE id=34;
-- UPDATE sys_role SET remark='医务科人力调配岗：改医师岗位归属（含生效/失效日期）+ 跨科室排门诊/急诊班（不改编制）' WHERE id=35;
-- UPDATE sys_role SET remark='院办岗：行政总值班与院领导带班的排班、全流程发布审核、值班类型维护' WHERE id=36;
-- UPDATE sys_role SET remark='药库采购/入库/调拨/退货/盘点/追溯' WHERE id=37;
-- UPDATE sys_role SET remark='物资耗材/耗材字典/库存盘点/科室领用' WHERE id=38;
-- UPDATE sys_role SET remark='设备管理/CSSD/医废/器械包模板' WHERE id=39;
-- UPDATE sys_role SET remark='医疗服务价格维护与收费策略' WHERE id=40;
-- UPDATE sys_role SET remark='病案统计与运营报表上报' WHERE id=41;

SET NAMES utf8mb4;
SET
FOREIGN_KEY_CHECKS=0;
START TRANSACTION;
INSERT INTO sys_faq (id, faq_no, category_code, category_name, question, answer, keywords, hot_flag, view_count,
                     helpful_count, useless_count, status, sort_order, create_by, update_by, del_flag, remark,
                     create_by_id, update_by_id)
VALUES ('21690101', 'FAQ0001', 'APPOINT', '挂号预约', '怎么在网上预约挂号',
        '在小程序首页点「预约挂号」，依次选择科室、医生和就诊时间，确认后即完成预约。到院当天请到对应楼层分诊台签到候诊。',
        '挂号、约号、预约、网上挂号、挂哪个科、看门诊', 0, 2, 2, 0, 1, 101, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_faq (id, faq_no, category_code, category_name, question, answer, keywords, hot_flag, view_count,
                     helpful_count, useless_count, status, sort_order, create_by, update_by, del_flag, remark,
                     create_by_id, update_by_id)
VALUES ('21690102', 'FAQ0002', 'APPOINT', '挂号预约', '预约成功后怎么退号',
        '进入「我的预约」，找到该条记录点「退号」。已支付的挂号费按原支付渠道退回，到账时间以支付渠道为准。',
        '退号、取消预约、取消挂号、不去了、预约', 0, 0, 0, 0, 1, 102, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_faq (id, faq_no, category_code, category_name, question, answer, keywords, hot_flag, view_count,
                     helpful_count, useless_count, status, sort_order, create_by, update_by, del_flag, remark,
                     create_by_id, update_by_id)
VALUES ('21690103', 'FAQ0003', 'APPOINT', '挂号预约', '可以提前几天预约挂号',
        '可预约的日期范围以小程序号源列表实际展示为准，列表里能选到哪天就说明那天放号了。',
        '提前几天、放号、约不到、没号、预约、挂号', 0, 0, 0, 0, 1, 103, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_faq (id, faq_no, category_code, category_name, question, answer, keywords, hot_flag, view_count,
                     helpful_count, useless_count, status, sort_order, create_by, update_by, del_flag, remark,
                     create_by_id, update_by_id)
VALUES ('21690104', 'FAQ0004', 'APPOINT', '挂号预约', '没带身份证能挂号吗',
        '首次就诊必须带本人有效身份证件（身份证、户口本、护照等）建档。已建档的患者可用就诊卡或电子就诊码挂号。',
        '没带身份证、忘带身份证、没带卡、挂号', 0, 0, 0, 0, 1, 104, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_faq (id, faq_no, category_code, category_name, question, answer, keywords, hot_flag, view_count,
                     helpful_count, useless_count, status, sort_order, create_by, update_by, del_flag, remark,
                     create_by_id, update_by_id)
VALUES ('21690105', 'FAQ0005', 'APPOINT', '挂号预约', '怎么给小孩挂号',
        '先在「就诊人」里为孩子建档（可用监护人手机号建档），建档后用孩子的就诊人身份挂号，科室请选择儿科或相应专科。',
        '小孩挂号、儿童挂号、宝宝挂号、新生儿、挂号', 0, 0, 0, 0, 1, 105, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_faq (id, faq_no, category_code, category_name, question, answer, keywords, hot_flag, view_count,
                     helpful_count, useless_count, status, sort_order, create_by, update_by, del_flag, remark,
                     create_by_id, update_by_id)
VALUES ('21690106', 'FAQ0006', 'APPOINT', '挂号预约', '怎么给家里老人挂号',
        '在「就诊人」中绑定老人为家人就诊人后，切换到该就诊人再挂号即可。老人本人手机号收不到验证码时，可用其建档预留手机号。',
        '帮家人挂号、老人挂号、代挂号、帮父母挂号、挂号', 0, 0, 0, 0, 1, 106, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_faq (id, faq_no, category_code, category_name, question, answer, keywords, hot_flag, view_count,
                     helpful_count, useless_count, status, sort_order, create_by, update_by, del_flag, remark,
                     create_by_id, update_by_id)
VALUES ('21690107', 'FAQ0007', 'APPOINT', '挂号预约', '专家号怎么挂',
        '号源列表里标注「专家」的医生即为专家号，与普通号同一入口预约。专家号源较少，建议尽早预约。',
        '专家号、主任医师、教授号、名医', 0, 0, 0, 0, 1, 107, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_faq (id, faq_no, category_code, category_name, question, answer, keywords, hot_flag, view_count,
                     helpful_count, useless_count, status, sort_order, create_by, update_by, del_flag, remark,
                     create_by_id, update_by_id)
VALUES ('21690108', 'FAQ0008', 'APPOINT', '挂号预约', '挂号费能退吗',
        '未就诊且未超时的预约可退号，挂号费原路退回。已签到就诊的原则上不退，如有特殊情况请到收费窗口咨询。',
        '退挂号费、挂号费退、退费、挂号', 0, 0, 0, 0, 1, 108, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_faq (id, faq_no, category_code, category_name, question, answer, keywords, hot_flag, view_count,
                     helpful_count, useless_count, status, sort_order, create_by, update_by, del_flag, remark,
                     create_by_id, update_by_id)
VALUES ('21690109', 'FAQ0009', 'APPOINT', '挂号预约', '预约了没去会怎么样',
        '预约后未按时到院签到，该号会作废，号源释放给其他患者。多次违约可能影响后续预约，请以医院现场公示的规则为准。',
        '爽约、没去、过期、号作废、预约', 0, 0, 0, 0, 1, 109, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_faq (id, faq_no, category_code, category_name, question, answer, keywords, hot_flag, view_count,
                     helpful_count, useless_count, status, sort_order, create_by, update_by, del_flag, remark,
                     create_by_id, update_by_id)
VALUES ('21690110', 'FAQ0010', 'APPOINT', '挂号预约', '当天还能现场挂号吗',
        '可以。门诊当天一般保留部分现场号，请在挂号窗口或自助机办理，号源以现场剩余为准。',
        '现场挂号、当天挂号、没预约、直接去、挂号', 0, 0, 0, 0, 1, 110, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_faq (id, faq_no, category_code, category_name, question, answer, keywords, hot_flag, view_count,
                     helpful_count, useless_count, status, sort_order, create_by, update_by, del_flag, remark,
                     create_by_id, update_by_id)
VALUES ('21690201', 'FAQ0011', 'PAY', '缴费退款', '门诊缴费怎么交',
        '医生开单后，进入「门诊缴费」查看待缴费用，核对明细后在线支付即可。也可在收费窗口或自助机缴费。',
        '缴费、交钱、付款、门诊缴费、怎么付', 0, 0, 0, 0, 1, 201, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_faq (id, faq_no, category_code, category_name, question, answer, keywords, hot_flag, view_count,
                     helpful_count, useless_count, status, sort_order, create_by, update_by, del_flag, remark,
                     create_by_id, update_by_id)
VALUES ('21690202', 'FAQ0012', 'PAY', '缴费退款', '支付成功了但显示未缴费怎么办',
        '请先下拉刷新页面确认状态。若已扣款仍显示未缴，请勿重复支付，携带支付凭证到收费窗口核实处理。',
        '扣钱了、已支付、重复扣款、支付失败、缴费', 0, 0, 0, 0, 1, 202, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_faq (id, faq_no, category_code, category_name, question, answer, keywords, hot_flag, view_count,
                     helpful_count, useless_count, status, sort_order, create_by, update_by, del_flag, remark,
                     create_by_id, update_by_id)
VALUES ('21690203', 'FAQ0013', 'PAY', '缴费退款', '怎么申请退费',
        '需由开单医生在系统中发起退费申请，再到收费窗口办理退款，款项按原支付渠道退回。已取药、已执行的检查项目一般不退。',
        '退费、退款、退钱、不想做了', 0, 0, 0, 0, 1, 203, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_faq (id, faq_no, category_code, category_name, question, answer, keywords, hot_flag, view_count,
                     helpful_count, useless_count, status, sort_order, create_by, update_by, del_flag, remark,
                     create_by_id, update_by_id)
VALUES ('21690204', 'FAQ0014', 'PAY', '缴费退款', '发票或电子票据在哪里开',
        '缴费完成后可在「我的支付单」中申请电子票据，也可凭就诊卡到收费窗口打印。具体开票规则以医院现场公示为准。',
        '发票、票据、电子发票、报销凭证、在哪', 0, 0, 0, 0, 1, 204, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_faq (id, faq_no, category_code, category_name, question, answer, keywords, hot_flag, view_count,
                     helpful_count, useless_count, status, sort_order, create_by, update_by, del_flag, remark,
                     create_by_id, update_by_id)
VALUES ('21690205', 'FAQ0015', 'PAY', '缴费退款', '费用明细看不懂，为什么自己还要交钱',
        '费用明细中「医保统筹」是医保支付部分，「个人自付」是需要您自己承担的部分。甲乙类目录、起付线和封顶线都会影响自付金额，如需逐项解释请到收费窗口咨询。',
        '自付、自己交钱、为什么这么贵、费用明细、看不懂', 0, 0, 0, 0, 1, 205, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_faq (id, faq_no, category_code, category_name, question, answer, keywords, hot_flag, view_count,
                     helpful_count, useless_count, status, sort_order, create_by, update_by, del_flag, remark,
                     create_by_id, update_by_id)
VALUES ('21690206', 'FAQ0016', 'PAY', '缴费退款', '住院押金怎么交和退',
        '住院押金可在「住院押金」中在线充值。出院结算时系统自动从押金中扣除应付金额，余额按原渠道退回。',
        '押金、预交金、住院交钱、押金退', 0, 0, 0, 0, 1, 206, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_faq (id, faq_no, category_code, category_name, question, answer, keywords, hot_flag, view_count,
                     helpful_count, useless_count, status, sort_order, create_by, update_by, del_flag, remark,
                     create_by_id, update_by_id)
VALUES ('21690207', 'FAQ0017', 'PAY', '缴费退款', '微信支付能用医保报销吗',
        '在线支付时医保结算需在医院收费窗口或自助机办理，具体能否线上医保结算以医院开通情况为准，请咨询收费窗口。',
        '微信医保、线上医保、医保支付', 0, 0, 0, 0, 1, 207, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_faq (id, faq_no, category_code, category_name, question, answer, keywords, hot_flag, view_count,
                     helpful_count, useless_count, status, sort_order, create_by, update_by, del_flag, remark,
                     create_by_id, update_by_id)
VALUES ('21690208', 'FAQ0018', 'PAY', '缴费退款', '缴费后多久能取药',
        '缴费成功后凭处方到药房窗口取药，取药排队情况以药房现场叫号为准。', '取药、拿药、多久拿药、药房、缴费', 0, 0, 0, 0, 1,
        208, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_faq (id, faq_no, category_code, category_name, question, answer, keywords, hot_flag, view_count,
                     helpful_count, useless_count, status, sort_order, create_by, update_by, del_flag, remark,
                     create_by_id, update_by_id)
VALUES ('21690301', 'FAQ0019', 'REPORT', '报告查询', '报告多久能出来',
        '不同项目出报告时间不同，一般常规检验当天可出，特殊项目可能需要数个工作日。报告发布后小程序会出提示，以实际发布为准。',
        '报告多久、什么时候出、化验结果、报告没出来', 0, 0, 0, 0, 1, 301, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_faq (id, faq_no, category_code, category_name, question, answer, keywords, hot_flag, view_count,
                     helpful_count, useless_count, status, sort_order, create_by, update_by, del_flag, remark,
                     create_by_id, update_by_id)
VALUES ('21690302', 'FAQ0020', 'REPORT', '报告查询', '在哪里查看和打印报告',
        '在小程序「报告查询」中可查看已发布的报告。需要纸质报告的，请到门诊自助机或检验/检查科室窗口打印。',
        '查报告、打印报告、化验单、结果查询、在哪', 0, 0, 0, 0, 1, 302, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_faq (id, faq_no, category_code, category_name, question, answer, keywords, hot_flag, view_count,
                     helpful_count, useless_count, status, sort_order, create_by, update_by, del_flag, remark,
                     create_by_id, update_by_id)
VALUES ('21690303', 'FAQ0021', 'REPORT', '报告查询', '报告上的箭头和偏高偏低是什么意思',
        '报告结果后面的参考范围用于对照：高于或低于参考范围会标记箭头或「偏高/偏低」。这不等于确诊疾病，需要医生结合您的症状和其他检查综合判断，请携带报告复诊。',
        '箭头、偏高、偏低、异常、看不懂报告、什么意思', 0, 0, 0, 0, 1, 303, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_faq (id, faq_no, category_code, category_name, question, answer, keywords, hot_flag, view_count,
                     helpful_count, useless_count, status, sort_order, create_by, update_by, del_flag, remark,
                     create_by_id, update_by_id)
VALUES ('21690304', 'FAQ0022', 'REPORT', '报告查询', '检查报告和检验报告有什么不一样',
        '检验报告是血液、尿液等标本的化验结果；检查报告是 CT、B 超、心电等影像和功能检查的结果与结论。两者在小程序中分开列出。',
        '化验单、CT、B超、超声、影像', 0, 0, 0, 0, 1, 304, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_faq (id, faq_no, category_code, category_name, question, answer, keywords, hot_flag, view_count,
                     helpful_count, useless_count, status, sort_order, create_by, update_by, del_flag, remark,
                     create_by_id, update_by_id)
VALUES ('21690305', 'FAQ0023', 'REPORT', '报告查询', '报告可以代领或代打印吗',
        '可以。请携带就诊人本人身份证件和代领人身份证件，到相应科室窗口办理。', '代领、代打、帮家人取报告、别人代拿', 0, 0, 0,
        0, 1, 305, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_faq (id, faq_no, category_code, category_name, question, answer, keywords, hot_flag, view_count,
                     helpful_count, useless_count, status, sort_order, create_by, update_by, del_flag, remark,
                     create_by_id, update_by_id)
VALUES ('21690306', 'FAQ0024', 'REPORT', '报告查询', '报告弄丢了能补打吗',
        '可以。凭就诊卡或身份证件到自助机或相应科室窗口补打，报告在系统中长期保存。', '报告丢了、补打、重新打印', 0, 0, 0, 0,
        1, 306, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_faq (id, faq_no, category_code, category_name, question, answer, keywords, hot_flag, view_count,
                     helpful_count, useless_count, status, sort_order, create_by, update_by, del_flag, remark,
                     create_by_id, update_by_id)
VALUES ('21690307', 'FAQ0025', 'REPORT', '报告查询', '体检报告在哪里取',
        '体检报告请到体检中心领取，部分项目可在小程序报告查询中查看，以体检中心现场告知为准。',
        '体检报告、体检结果、入职体检、在哪', 0, 0, 0, 0, 1, 307, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_faq (id, faq_no, category_code, category_name, question, answer, keywords, hot_flag, view_count,
                     helpful_count, useless_count, status, sort_order, create_by, update_by, del_flag, remark,
                     create_by_id, update_by_id)
VALUES ('21690308', 'FAQ0026', 'REPORT', '报告查询', '报告有异常该怎么办',
        '请不要自行判断或用药。携带报告到开单科室复诊，由医生结合临床情况给出处理意见。若出现危急值，医院会按危急值流程主动联系您。',
        '报告异常、结果不好、有问题、危急值', 0, 0, 0, 0, 1, 308, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_faq (id, faq_no, category_code, category_name, question, answer, keywords, hot_flag, view_count,
                     helpful_count, useless_count, status, sort_order, create_by, update_by, del_flag, remark,
                     create_by_id, update_by_id)
VALUES ('21690401', 'FAQ0027', 'INPATIENT', '住院服务', '住院怎么办理，需要带什么',
        '凭医生开具的住院证到入院处办理，需携带本人身份证件、医保凭证、住院证及预交押金。具体清单以入院处现场告知为准。',
        '住院办理、入院、住院证、带什么', 0, 0, 0, 0, 1, 401, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_faq (id, faq_no, category_code, category_name, question, answer, keywords, hot_flag, view_count,
                     helpful_count, useless_count, status, sort_order, create_by, update_by, del_flag, remark,
                     create_by_id, update_by_id)
VALUES ('21690402', 'FAQ0028', 'INPATIENT', '住院服务', '住院押金不够了怎么办',
        '可在小程序「住院押金」中随时补充充值，也可到入院处或收费窗口缴纳。费用不足时护士站会提前通知。',
        '押金不够、欠费、补交押金、押金', 0, 0, 0, 0, 1, 402, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_faq (id, faq_no, category_code, category_name, question, answer, keywords, hot_flag, view_count,
                     helpful_count, useless_count, status, sort_order, create_by, update_by, del_flag, remark,
                     create_by_id, update_by_id)
VALUES ('21690403', 'FAQ0029', 'INPATIENT', '住院服务', '住院每天花了多少钱在哪看',
        '可在小程序「住院押金」中查看押金流水，每日费用明细请到护士站或自助机打印日清单。',
        '日清单、每日费用、花了多少、费用查询、在哪', 0, 0, 0, 0, 1, 403, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_faq (id, faq_no, category_code, category_name, question, answer, keywords, hot_flag, view_count,
                     helpful_count, useless_count, status, sort_order, create_by, update_by, del_flag, remark,
                     create_by_id, update_by_id)
VALUES ('21690404', 'FAQ0030', 'INPATIENT', '住院服务', '出院怎么结算',
        '出院时由病区完成出院登记后，到出院结算窗口办理结算，系统自动结算医保与自付部分，多退少补。',
        '出院结算、结账、出院手续、办出院', 0, 0, 0, 0, 1, 404, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_faq (id, faq_no, category_code, category_name, question, answer, keywords, hot_flag, view_count,
                     helpful_count, useless_count, status, sort_order, create_by, update_by, del_flag, remark,
                     create_by_id, update_by_id)
VALUES ('21690405', 'FAQ0031', 'INPATIENT', '住院服务', '家属能陪护和探视吗',
        '陪护与探视按病区管理规定执行，是否需要陪护由病区根据患者病情评估，请咨询所在病区护士站。',
        '陪护、探视、家属陪、看望', 0, 0, 0, 0, 1, 405, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_faq (id, faq_no, category_code, category_name, question, answer, keywords, hot_flag, view_count,
                     helpful_count, useless_count, status, sort_order, create_by, update_by, del_flag, remark,
                     create_by_id, update_by_id)
VALUES ('21690406', 'FAQ0032', 'INPATIENT', '住院服务', '住院期间吃饭怎么解决',
        '可按病区指引订购营养餐，也可由家属送餐。特殊饮食（糖尿病餐、流质等）需遵医嘱，请告知护士站。',
        '住院吃饭、订餐、送饭、营养餐', 0, 0, 0, 0, 1, 406, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_faq (id, faq_no, category_code, category_name, question, answer, keywords, hot_flag, view_count,
                     helpful_count, useless_count, status, sort_order, create_by, update_by, del_flag, remark,
                     create_by_id, update_by_id)
VALUES ('21690407', 'FAQ0033', 'INPATIENT', '住院服务', '出院后怎么复印病历',
        '出院后携带本人身份证件到病案室申请复印；代办理需同时提供代办人身份证件和授权材料。复印范围与时限以病案室规定为准。',
        '复印病历、病案、病历打印、出院资料', 0, 0, 0, 0, 1, 407, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_faq (id, faq_no, category_code, category_name, question, answer, keywords, hot_flag, view_count,
                     helpful_count, useless_count, status, sort_order, create_by, update_by, del_flag, remark,
                     create_by_id, update_by_id)
VALUES ('21690501', 'FAQ0034', 'PROCESS', '就诊流程', '第一次来院看病是什么流程',
        '先挂号（线上预约或窗口）→ 到院签到 → 分诊台候诊叫号 → 医生就诊 → 缴费 → 检查/取药 → 按医嘱复诊。',
        '第一次来、初诊、流程、怎么办、先看哪', 0, 0, 0, 0, 1, 501, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_faq (id, faq_no, category_code, category_name, question, answer, keywords, hot_flag, view_count,
                     helpful_count, useless_count, status, sort_order, create_by, update_by, del_flag, remark,
                     create_by_id, update_by_id)
VALUES ('21690502', 'FAQ0035', 'PROCESS', '就诊流程', '到院后要签到吗，怎么签到',
        '需要签到。在小程序「排队叫号」中点击签到，或在楼层自助机、分诊台签到，签到后按叫号顺序就诊。',
        '签到、叫号、排队、到院了', 0, 0, 0, 0, 1, 502, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_faq (id, faq_no, category_code, category_name, question, answer, keywords, hot_flag, view_count,
                     helpful_count, useless_count, status, sort_order, create_by, update_by, del_flag, remark,
                     create_by_id, update_by_id)
VALUES ('21690503', 'FAQ0036', 'PROCESS', '就诊流程', '抽血或做检查前要空腹吗',
        '抽血、腹部超声等部分项目需要空腹，具体要求以检查单上的准备说明为准。检查前请仔细阅读小程序或检查单上的注意事项。',
        '空腹、抽血、不能吃饭、检查前准备、要准备什么', 0, 0, 0, 0, 1, 503, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_faq (id, faq_no, category_code, category_name, question, answer, keywords, hot_flag, view_count,
                     helpful_count, useless_count, status, sort_order, create_by, update_by, del_flag, remark,
                     create_by_id, update_by_id)
VALUES ('21690504', 'FAQ0037', 'PROCESS', '就诊流程', '复诊怎么预约',
        '可在小程序「复诊预约」中选择以往就诊记录发起复诊，由医生接诊后按提示完成缴费与检查。',
        '复诊、再来看、第二次、复查、预约', 0, 0, 0, 0, 1, 504, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_faq (id, faq_no, category_code, category_name, question, answer, keywords, hot_flag, view_count,
                     helpful_count, useless_count, status, sort_order, create_by, update_by, del_flag, remark,
                     create_by_id, update_by_id)
VALUES ('21690505', 'FAQ0038', 'PROCESS', '就诊流程', '慢病患者怎么开长处方',
        '符合条件的慢病患者可由医生评估后开具长处方，具体病种与用量标准由医生按相关规定执行，请在就诊时向医生说明。',
        '长处方、慢病开药、长期拿药、慢性病', 0, 0, 0, 0, 1, 505, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_faq (id, faq_no, category_code, category_name, question, answer, keywords, hot_flag, view_count,
                     helpful_count, useless_count, status, sort_order, create_by, update_by, del_flag, remark,
                     create_by_id, update_by_id)
VALUES ('21690506', 'FAQ0039', 'PROCESS', '就诊流程', '需要转院或转诊怎么办',
        '由接诊医生评估后开具转诊意见，再到相关窗口办理手续。急诊转院由急诊科按流程处理。', '转院、转诊、转科、去别的医院', 0,
        0, 0, 0, 1, 506, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_faq (id, faq_no, category_code, category_name, question, answer, keywords, hot_flag, view_count,
                     helpful_count, useless_count, status, sort_order, create_by, update_by, del_flag, remark,
                     create_by_id, update_by_id)
VALUES ('21690507', 'FAQ0040', 'PROCESS', '就诊流程', '急诊怎么走',
        '急诊 24 小时接诊，请直接到急诊科，危重患者请走急诊绿色通道，先救治后办手续。', '急诊、夜里看病、突发、绿色通道', 0, 0,
        0, 0, 1, 507, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_faq (id, faq_no, category_code, category_name, question, answer, keywords, hot_flag, view_count,
                     helpful_count, useless_count, status, sort_order, create_by, update_by, del_flag, remark,
                     create_by_id, update_by_id)
VALUES ('21690601', 'FAQ0041', 'INSURANCE', '医保相关', '医保能报销多少',
        '具体报销比例、起付线和封顶线按参保地医保政策执行，不同参保类型（职工、居民、异地）差异较大，请以收费窗口结算结果或参保地医保部门答复为准。',
        '报销多少、医保比例、能报多少、统筹', 0, 0, 0, 0, 1, 601, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_faq (id, faq_no, category_code, category_name, question, answer, keywords, hot_flag, view_count,
                     helpful_count, useless_count, status, sort_order, create_by, update_by, del_flag, remark,
                     create_by_id, update_by_id)
VALUES ('21690602', 'FAQ0042', 'INSURANCE', '医保相关', '没带医保卡能报销吗',
        '可尝试使用医保电子凭证完成结算；无法使用电子凭证的，请到收费窗口咨询能否补办结算。',
        '没带医保卡、忘带卡、电子医保、医保码', 0, 0, 0, 0, 1, 602, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_faq (id, faq_no, category_code, category_name, question, answer, keywords, hot_flag, view_count,
                     helpful_count, useless_count, status, sort_order, create_by, update_by, del_flag, remark,
                     create_by_id, update_by_id)
VALUES ('21690603', 'FAQ0043', 'INSURANCE', '医保相关', '异地医保能不能用',
        '异地参保患者一般需先在参保地办理异地就医备案，备案成功后可在院直接结算。能否直接结算以结算时系统返回为准，请提前咨询参保地医保部门。',
        '异地医保、外地医保、备案、外地看病', 0, 0, 0, 0, 1, 603, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_faq (id, faq_no, category_code, category_name, question, answer, keywords, hot_flag, view_count,
                     helpful_count, useless_count, status, sort_order, create_by, update_by, del_flag, remark,
                     create_by_id, update_by_id)
VALUES ('21690604', 'FAQ0044', 'INSURANCE', '医保相关', '门诊慢特病怎么申请',
        '门诊慢特病需按参保地规定申请认定，认定后方可享受相应待遇。所需材料和流程请咨询医院医保办或参保地医保部门。',
        '慢病认定、特殊病种、门诊慢病、医保办', 0, 0, 0, 0, 1, 604, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_faq (id, faq_no, category_code, category_name, question, answer, keywords, hot_flag, view_count,
                     helpful_count, useless_count, status, sort_order, create_by, update_by, del_flag, remark,
                     create_by_id, update_by_id)
VALUES ('21690605', 'FAQ0045', 'INSURANCE', '医保相关', '医保报销需要哪些材料',
        '常见材料包括住院发票、费用明细清单、出院小结、身份证件和社保卡。具体以参保地医保部门要求为准。',
        '报销材料、需要什么、报销要带啥、出院小结', 0, 0, 0, 0, 1, 605, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_faq (id, faq_no, category_code, category_name, question, answer, keywords, hot_flag, view_count,
                     helpful_count, useless_count, status, sort_order, create_by, update_by, del_flag, remark,
                     create_by_id, update_by_id)
VALUES ('21690606', 'FAQ0046', 'INSURANCE', '医保相关', '什么是自费和医保目录',
        '医保目录内的项目可按规定纳入报销，目录外的项目需自费；目录内项目还可能分甲类和乙类，乙类通常需先自付一部分。具体以结算明细为准。',
        '自费、甲类、乙类、医保目录、统筹', 0, 0, 0, 0, 1, 606, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_faq (id, faq_no, category_code, category_name, question, answer, keywords, hot_flag, view_count,
                     helpful_count, useless_count, status, sort_order, create_by, update_by, del_flag, remark,
                     create_by_id, update_by_id)
VALUES ('21690701', 'FAQ0047', 'ACCOUNT', '账号与就诊人', '怎么添加家人就诊人',
        '进入「就诊人」点「添加就诊人」，可选择绑定已有档案或新建档案。新建需通过预留手机号验证。',
        '添加就诊人、加家人、绑定家人、多就诊人', 0, 0, 0, 0, 1, 701, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_faq (id, faq_no, category_code, category_name, question, answer, keywords, hot_flag, view_count,
                     helpful_count, useless_count, status, sort_order, create_by, update_by, del_flag, remark,
                     create_by_id, update_by_id)
VALUES ('21690702', 'FAQ0048', 'ACCOUNT', '账号与就诊人', '一个人能绑几个就诊人',
        '可绑定多名家人就诊人，绑定数量上限以系统设置为准。建议只绑定确实需要代为挂号缴费的亲属。',
        '能绑几个、最多几个、绑定上限', 0, 0, 0, 0, 1, 702, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_faq (id, faq_no, category_code, category_name, question, answer, keywords, hot_flag, view_count,
                     helpful_count, useless_count, status, sort_order, create_by, update_by, del_flag, remark,
                     create_by_id, update_by_id)
VALUES ('21690703', 'FAQ0049', 'ACCOUNT', '账号与就诊人', '换手机号了怎么办',
        '请携带本人身份证件到挂号窗口或门诊服务台办理手机号变更，变更后才能正常接收验证码和就诊通知。',
        '换手机号、改号码、号码变了、接收不到短信', 0, 0, 0, 0, 1, 703, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_faq (id, faq_no, category_code, category_name, question, answer, keywords, hot_flag, view_count,
                     helpful_count, useless_count, status, sort_order, create_by, update_by, del_flag, remark,
                     create_by_id, update_by_id)
VALUES ('21690704', 'FAQ0050', 'ACCOUNT', '账号与就诊人', '收不到验证码登录不上怎么办',
        '请先确认手机信号与短信拦截设置；仍收不到请确认当前手机号与建档预留手机号是否一致，不一致需先办理变更。',
        '验证码、收不到短信、登录不了、登录不上', 0, 0, 0, 0, 1, 704, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_faq (id, faq_no, category_code, category_name, question, answer, keywords, hot_flag, view_count,
                     helpful_count, useless_count, status, sort_order, create_by, update_by, del_flag, remark,
                     create_by_id, update_by_id)
VALUES ('21690705', 'FAQ0051', 'ACCOUNT', '账号与就诊人', '小孩没有手机号怎么建档',
        '可用监护人的手机号为其建档，建档时填写监护人联系方式即可，就诊人信息填写孩子本人的身份信息。',
        '小孩建档、没有手机号、儿童建档、新生儿建档', 0, 0, 0, 0, 1, 705, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_faq (id, faq_no, category_code, category_name, question, answer, keywords, hot_flag, view_count,
                     helpful_count, useless_count, status, sort_order, create_by, update_by, del_flag, remark,
                     create_by_id, update_by_id)
VALUES ('21690801', 'FAQ0052', 'FACILITY', '便民服务', '门诊上班时间',
        '门诊工作时间请以医院现场公示或小程序首页展示为准，急诊 24 小时接诊。', '几点上班、门诊时间、下班、周末上班吗', 0, 0,
        0, 0, 1, 801, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_faq (id, faq_no, category_code, category_name, question, answer, keywords, hot_flag, view_count,
                     helpful_count, useless_count, status, sort_order, create_by, update_by, del_flag, remark,
                     create_by_id, update_by_id)
VALUES ('21690802', 'FAQ0053', 'FACILITY', '便民服务', '医院地址和电话',
        '医院地址与咨询电话在小程序首页「联系医院」中展示，可直接点击查看或一键拨号。', '地址、电话、在哪、怎么去、联系方式', 0,
        0, 0, 0, 1, 802, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_faq (id, faq_no, category_code, category_name, question, answer, keywords, hot_flag, view_count,
                     helpful_count, useless_count, status, sort_order, create_by, update_by, del_flag, remark,
                     create_by_id, update_by_id)
VALUES ('21690803', 'FAQ0054', 'FACILITY', '便民服务', '开车来有地方停车吗',
        '院内一般设有停车场，收费标准与车位情况以现场公示为准，就诊高峰建议预留充足时间。', '停车、停车场、开车、车位', 0, 0,
        0, 0, 1, 803, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_faq (id, faq_no, category_code, category_name, question, answer, keywords, hot_flag, view_count,
                     helpful_count, useless_count, status, sort_order, create_by, update_by, del_flag, remark,
                     create_by_id, update_by_id)
VALUES ('21690804', 'FAQ0055', 'FACILITY', '便民服务', '行动不便有轮椅吗',
        '门诊服务台一般提供轮椅借用服务，可凭身份证件借用，也可向导诊人员寻求帮助。', '轮椅、行动不便、老人推车、借轮椅', 0,
        0, 0, 0, 1, 804, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_faq (id, faq_no, category_code, category_name, question, answer, keywords, hot_flag, view_count,
                     helpful_count, useless_count, status, sort_order, create_by, update_by, del_flag, remark,
                     create_by_id, update_by_id)
VALUES ('21690805', 'FAQ0056', 'FACILITY', '便民服务', '有意见或投诉怎么反馈',
        '可在小程序客服页提交留言，或到门诊服务台、医患沟通办公室现场反映，医院会按流程受理并反馈。',
        '投诉、意见、建议、反馈、态度不好', 0, 0, 0, 0, 1, 805, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_faq (id, faq_no, category_code, category_name, question, answer, keywords, hot_flag, view_count,
                     helpful_count, useless_count, status, sort_order, create_by, update_by, del_flag, remark,
                     create_by_id, update_by_id)
VALUES ('890000000000230057', 'FAQ0057', 'REPORT', '报告查询', '报告看不懂怎么办',
        '打开报告详情页，点「看不懂？用大白话讲给我听」，系统会用通俗的话逐项说明这项查什么、偏高偏低通常意味着什么。需要明确诊断的，请携带报告到门诊复诊，由医生结合您的情况判断。',
        '报告看不懂、看不明白、什么意思、箭头、大白话、解读、讲解、化验单看不懂', 1, 0, 0, 0, 1, 806, 'admin', 'admin', 0,
        NULL, '1', '1');
INSERT INTO sys_faq (id, faq_no, category_code, category_name, question, answer, keywords, hot_flag, view_count,
                     helpful_count, useless_count, status, sort_order, create_by, update_by, del_flag, remark,
                     create_by_id, update_by_id)
VALUES ('890000000000230058', 'FAQ0058', 'PAY', '缴费退款', '为什么这次自付这么多',
        '在待缴费列表里点「这笔钱怎么算的」，系统会按医保目录把费用拆成统筹支付、个人账户和自付三部分，并列出自付最多的项目。对拆分有疑问的，请到收费窗口或医保办核实。',
        '自付、为什么这么贵、费用怎么算、医保报了多少、统筹、自费、收费明细、花太多', 1, 0, 0, 0, 1, 706, 'admin', 'admin', 0,
        NULL, '1', '1');
INSERT INTO sys_faq (id, faq_no, category_code, category_name, question, answer, keywords, hot_flag, view_count,
                     helpful_count, useless_count, status, sort_order, create_by, update_by, del_flag, remark,
                     create_by_id, update_by_id)
VALUES ('2108171142387736578', 'FAQ0059', 'SEQ_VERIFY', '编号序列验证', 'Redis 全局序列取号验证（勿删，编号迁移留证）',
        '用于验证 MiniFaqServiceImpl 的取号已从 max()+1 迁到 redisSequenceService.nextGlobal(\"FAQ\", seed)。',
        '编号验证、Redis序列', 0, 0, 0, 0, 1, 9999, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_faq (id, faq_no, category_code, category_name, question, answer, keywords, hot_flag, view_count,
                     helpful_count, useless_count, status, sort_order, create_by, update_by, del_flag, remark,
                     create_by_id, update_by_id)
VALUES ('2108171142580674562', 'FAQ0060', 'SEQ_VERIFY', '编号序列验证', 'Redis 全局序列取号验证（勿删，编号迁移留证） #2',
        '用于验证 MiniFaqServiceImpl 的取号已从 max()+1 迁到 redisSequenceService.nextGlobal(\"FAQ\", seed)。',
        '编号验证、Redis序列', 0, 0, 0, 0, 1, 9999, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_faq (id, faq_no, category_code, category_name, question, answer, keywords, hot_flag, view_count,
                     helpful_count, useless_count, status, sort_order, create_by, update_by, del_flag, remark,
                     create_by_id, update_by_id)
VALUES ('2108171292136972290', 'FAQ0061', 'SEQ_VERIFY', '编号序列验证', 'Redis 全局序列取号验证（勿删，编号迁移留证）',
        '用于验证 MiniFaqServiceImpl 的取号已从 max()+1 迁到 redisSequenceService.nextGlobal(\"FAQ\", seed)。',
        '编号验证、Redis序列', 0, 0, 0, 0, 1, 9999, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_faq (id, faq_no, category_code, category_name, question, answer, keywords, hot_flag, view_count,
                     helpful_count, useless_count, status, sort_order, create_by, update_by, del_flag, remark,
                     create_by_id, update_by_id)
VALUES ('2108171292262801410', 'FAQ0062', 'SEQ_VERIFY', '编号序列验证', 'Redis 全局序列取号验证（勿删，编号迁移留证） #2',
        '用于验证 MiniFaqServiceImpl 的取号已从 max()+1 迁到 redisSequenceService.nextGlobal(\"FAQ\", seed)。',
        '编号验证、Redis序列', 0, 0, 0, 0, 1, 9999, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_faq (id, faq_no, category_code, category_name, question, answer, keywords, hot_flag, view_count,
                     helpful_count, useless_count, status, sort_order, create_by, update_by, del_flag, remark,
                     create_by_id, update_by_id)
VALUES ('2108173980904034306', 'FAQ202610080001', 'SEQ_VERIFY', '编号序列验证',
        '按天归零取号验证（勿删，编号迁移留证） #1',
        '验证 MiniFaqServiceImpl 的取号走 redisSequenceService.next(\"FAQ\")，号形如 FAQ+yyyyMMdd+4 位序号。',
        '编号验证、Redis序列', 0, 0, 0, 0, 1, 9999, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_faq (id, faq_no, category_code, category_name, question, answer, keywords, hot_flag, view_count,
                     helpful_count, useless_count, status, sort_order, create_by, update_by, del_flag, remark,
                     create_by_id, update_by_id)
VALUES ('2108173981101166593', 'FAQ202610080002', 'SEQ_VERIFY', '编号序列验证',
        '按天归零取号验证（勿删，编号迁移留证） #2',
        '验证 MiniFaqServiceImpl 的取号走 redisSequenceService.next(\"FAQ\")，号形如 FAQ+yyyyMMdd+4 位序号。',
        '编号验证、Redis序列', 0, 0, 0, 0, 1, 9999, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_faq (id, faq_no, category_code, category_name, question, answer, keywords, hot_flag, view_count,
                     helpful_count, useless_count, status, sort_order, create_by, update_by, del_flag, remark,
                     create_by_id, update_by_id)
VALUES ('2360000000000008001', 'FAQ202605080001', 'INSURANCE', '医保报销', '门诊看病医保能报多少',
        '不同参保类型和项目比例不同，甲类全额纳入统筹按比例报销，乙类需先自付一定比例。具体结算金额以收费窗口医保结算单为准，可携带医保凭证到一楼医保窗口咨询。',
        '医保、报销比例、统筹、乙类、自付', 1, 0, 0, 0, 1, 201, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_faq (id, faq_no, category_code, category_name, question, answer, keywords, hot_flag, view_count,
                     helpful_count, useless_count, status, sort_order, create_by, update_by, del_flag, remark,
                     create_by_id, update_by_id)
VALUES ('2360000000000008002', 'FAQ202605080002', 'INSURANCE', '医保报销', '住院押金（预交金）可以退吗',
        '出院结算后预交金多退少补，余额按原缴纳渠道退回，一般1—3个工作日到账，具体以支付渠道为准。',
        '押金、预交金、退款、出院结算', 0, 0, 0, 0, 1, 202, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_faq (id, faq_no, category_code, category_name, question, answer, keywords, hot_flag, view_count,
                     helpful_count, useless_count, status, sort_order, create_by, update_by, del_flag, remark,
                     create_by_id, update_by_id)
VALUES ('2360000000000008003', 'FAQ202605080003', 'INSURANCE', '医保报销', '外地参保能在你们医院直接结算吗',
        '已办理异地就医备案的参保人员可持医保电子凭证或社保卡在出院时直接结算。备案可通过参保地医保公众号或国家医保服务平台APP办理。',
        '异地就医、备案、直接结算、外地医保', 1, 0, 0, 0, 1, 203, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_faq (id, faq_no, category_code, category_name, question, answer, keywords, hot_flag, view_count,
                     helpful_count, useless_count, status, sort_order, create_by, update_by, del_flag, remark,
                     create_by_id, update_by_id)
VALUES ('2360000000000008004', 'FAQ202605080004', 'CHECKUP', '健康体检', '体检报告多久能出',
        '一般3—5个工作日出报告，公众号可查电子版；个别特殊项目（如病理）时间略长，报告完成后会有短信提醒。',
        '体检报告、多久、查询、电子报告', 1, 0, 0, 0, 1, 204, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_faq (id, faq_no, category_code, category_name, question, answer, keywords, hot_flag, view_count,
                     helpful_count, useless_count, status, sort_order, create_by, update_by, del_flag, remark,
                     create_by_id, update_by_id)
VALUES ('2360000000000008005', 'FAQ202605080005', 'CHECKUP', '健康体检', '入职体检需要带什么',
        '携带本人身份证，空腹前来（前一天晚20点后禁食）。如单位有指定体检套餐请告知前台，报告可按单位要求加盖体检专用章。',
        '入职体检、空腹、身份证、套餐', 0, 0, 0, 0, 1, 205, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_faq (id, faq_no, category_code, category_name, question, answer, keywords, hot_flag, view_count,
                     helpful_count, useless_count, status, sort_order, create_by, update_by, del_flag, remark,
                     create_by_id, update_by_id)
VALUES ('2360000000000008006', 'FAQ202605080006', 'INPATIENT', '住院服务', '住院需要带哪些东西',
        '身份证、医保凭证、既往病历和检查资料、生活用品。住院部提供热水与陪护床租借，贵重物品建议勿带入病房。',
        '住院、准备、物品、陪护', 0, 0, 0, 0, 1, 206, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_faq (id, faq_no, category_code, category_name, question, answer, keywords, hot_flag, view_count,
                     helpful_count, useless_count, status, sort_order, create_by, update_by, del_flag, remark,
                     create_by_id, update_by_id)
VALUES ('2360000000000008007', 'FAQ202605080007', 'INPATIENT', '住院服务', '探视时间是什么时候',
        '普通病区探视时间为每日15:00—20:00，每位患者同时段探视人数建议不超过2人；ICU按科室规定预约探视。',
        '探视、时间、ICU、家属', 0, 0, 0, 0, 1, 207, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_faq (id, faq_no, category_code, category_name, question, answer, keywords, hot_flag, view_count,
                     helpful_count, useless_count, status, sort_order, create_by, update_by, del_flag, remark,
                     create_by_id, update_by_id)
VALUES ('2360000000000008008', 'FAQ202605080008', 'INPATIENT', '住院服务', '出院小结和病历复印件怎么开',
        '出院小结在办理出院时随出院记录一并发放。病历复印请到病案室窗口申请，携带患者及代办人身份证，5个工作日后凭回执领取（可邮寄）。',
        '病历复印、病案室、出院小结、证明', 1, 0, 0, 0, 1, 208, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_faq (id, faq_no, category_code, category_name, question, answer, keywords, hot_flag, view_count,
                     helpful_count, useless_count, status, sort_order, create_by, update_by, del_flag, remark,
                     create_by_id, update_by_id)
VALUES ('2360000000000008009', 'FAQ202605080009', 'BILLING', '费用票据', '电子发票怎么获取',
        '缴费成功后可通过公众号「电子票据」栏目开具电子发票，发送至预留邮箱；纸质发票可在自助机或收费窗口补打。',
        '电子发票、票据、开票、打印', 1, 0, 0, 0, 1, 209, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_faq (id, faq_no, category_code, category_name, question, answer, keywords, hot_flag, view_count,
                     helpful_count, useless_count, status, sort_order, create_by, update_by, del_flag, remark,
                     create_by_id, update_by_id)
VALUES ('2360000000000008010', 'FAQ202605080010', 'BILLING', '费用票据', '检查缴费后不想做了能退吗',
        '未执行的检查检验项目可退费：请先到开单医生处开具退费单，再凭缴费凭证到一楼收费窗口办理；已执行项目原则上不退费。',
        '退费、退检查、不想做了、取消', 1, 0, 0, 0, 1, 210, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_faq (id, faq_no, category_code, category_name, question, answer, keywords, hot_flag, view_count,
                     helpful_count, useless_count, status, sort_order, create_by, update_by, del_flag, remark,
                     create_by_id, update_by_id)
VALUES ('2360000000000008011', 'FAQ202605080011', 'SERVICE', '便民服务', '医院有停车场吗 收费标准如何',
        '院内设地面与立体停车库，就诊车辆凭当日就诊凭证首小时免费，之后按当地物价标准计费，具体以入口公示牌为准。',
        '停车、停车场、收费、车位', 0, 0, 0, 0, 1, 211, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_faq (id, faq_no, category_code, category_name, question, answer, keywords, hot_flag, view_count,
                     helpful_count, useless_count, status, sort_order, create_by, update_by, del_flag, remark,
                     create_by_id, update_by_id)
VALUES ('2360000000000008012', 'FAQ202605080012', 'SERVICE', '便民服务', '有轮椅和平车可以借吗',
        '门诊一楼服务台凭有效证件免费借用轮椅，住院部各病区备有平车与轮椅，由护士站协助安排。', '轮椅、平车、借用、行动不便',
        0, 0, 0, 0, 1, 212, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_faq (id, faq_no, category_code, category_name, question, answer, keywords, hot_flag, view_count,
                     helpful_count, useless_count, status, sort_order, create_by, update_by, del_flag, remark,
                     create_by_id, update_by_id)
VALUES ('2360000000000008013', 'FAQ202605080013', 'SERVICE', '便民服务', '晚上和周末能看病吗',
        '急诊24小时开放。门诊周末及节假日安排以公众号每周公告为准，部分专科开设夜间门诊，可在预约挂号中查看「夜间门诊」标签。',
        '夜间门诊、周末、节假日、急诊', 1, 0, 0, 0, 1, 213, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_faq (id, faq_no, category_code, category_name, question, answer, keywords, hot_flag, view_count,
                     helpful_count, useless_count, status, sort_order, create_by, update_by, del_flag, remark,
                     create_by_id, update_by_id)
VALUES ('2360000000000008014', 'FAQ202605080014', 'CHRONIC', '慢病管理', '高血压糖尿病慢病卡怎么办',
        '需二级及以上医院出具诊断证明与相关报告，由主管医师协助填写申请表，交医保办审核后报参保地经办机构评审，通过后享受门诊慢特病待遇。',
        '慢病卡、慢特病、高血压、糖尿病、备案', 1, 0, 0, 0, 1, 214, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_faq (id, faq_no, category_code, category_name, question, answer, keywords, hot_flag, view_count,
                     helpful_count, useless_count, status, sort_order, create_by, update_by, del_flag, remark,
                     create_by_id, update_by_id)
VALUES ('2360000000000008015', 'FAQ202605080015', 'REPORT', '检验检查', '检查结果旁边的箭头是什么意思',
        '箭头表示该指标高于或低于参考区间，提示需要关注，但单一指标波动不一定代表疾病。患者端报告页对常见指标有白话解释，最终解读请以医生意见为准。',
        '箭头、偏高、偏低、参考区间、解读', 1, 0, 0, 0, 1, 215, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_faq (id, faq_no, category_code, category_name, question, answer, keywords, hot_flag, view_count,
                     helpful_count, useless_count, status, sort_order, create_by, update_by, del_flag, remark,
                     create_by_id, update_by_id)
VALUES ('2360000000000008016', 'FAQ202605080016', 'APPOINT', '挂号预约', '过号了还能看吗',
        '取号后超过预约时段未签到的，系统自动顺延3位或按现场排队处理；如当日号源已关闭，可与分诊台协商安排补号或重新预约。',
        '过号、迟到、顺延、重新排队', 0, 0, 0, 0, 1, 216, 'admin', 'admin', 0, NULL, '1', '1');
COMMIT;
SET
FOREIGN_KEY_CHECKS=1;

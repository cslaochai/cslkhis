SET NAMES utf8mb4;
SET
FOREIGN_KEY_CHECKS=0;
START TRANSACTION;
INSERT INTO sys_knowledge_doc (id, title, category, source_type, content, chunk_count, status, create_by, update_by,
                               del_flag, remark, create_by_id, update_by_id)
VALUES ('2106587567246921730', '就诊须知', '就诊须知', 1,
        '# 就诊须知（示例知识库条目）\n\n本院门诊时间为周一至周日 08:00—12:00、14:30—17:30，法定节假日门诊安排以公众号公告为准。急诊科 24 小时开放，急危重症请直接前往急诊科。\n\n首次就诊患者请先在一楼导诊台或自助机凭身份证、医保电子凭证完成建档领卡。复诊患者请携带就诊卡或医保电子凭证。\n\n挂号方式：微信公众号预约、现场自助机挂号、人工窗口挂号。预约号建议提前 30 分钟到院取号，过号需重新排队或顺延就诊。\n\n医保患者请就诊时主动出示医保电子凭证或实体社保卡，自费项目需签署知情同意。门诊费用支持医保个人账户、微信、支付宝、现金。\n\n退费流程：未执行的检查检验项目，凭缴费凭证及医生退费单到一楼收费窗口办理，已执行的检验项目原则上不退费。\n\n发热患者（体温 ≥ 37.3℃）请先到发热门诊预检分诊，不要直接进入普通门诊候诊区。\n\n请妥善保管个人财物，院内禁止吸烟，保持安静。\n',
        1, 0, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_knowledge_doc (id, title, category, source_type, content, chunk_count, status, create_by, update_by,
                               del_flag, remark, create_by_id, update_by_id)
VALUES ('2106587567381139458', '检查注意事项', '检查注意事项', 1,
        '# 检查注意事项（示例知识库条目）\n\n抽血化验：多数生化与免疫项目需空腹 8—12 小时，可饮少量白开水，请勿进食早餐、含糖饮料。采血前避免剧烈运动，静坐 10 分钟再采血。正在服用的药物请提前告知医生，部分项目需停药后复查。\n\n腹部 B 超（肝、胆、胰、脾）：检查前需空腹 8 小时，胆囊检查前 3 天清淡饮食、前 1 天晚餐后禁食；盆腔 B 超、前列腺 B 超需憋尿使膀胱适度充盈。\n\nCT 增强 / 磁共振增强：需经医生评估肾功能，增强前 4 小时避免大量进食；既往有碘对比剂过敏史、甲亢、严重肾衰者务必提前告知，禁忌者改平扫或其他检查。体内有心脏起搏器、金属植入物者一般禁止磁共振。\n\n胃镜 / 肠镜：胃镜需空腹 6 小时以上；肠镜需按医嘱提前口服泻药清肠，直至排出清水样便方可检查。检查当天需有家属陪同。\n\nX 线 / CT 孕妇慎做：育龄女性请主动告知是否怀孕，妊娠早期尽量不做腹部与盆腔放射检查。\n\n检查报告：影像与检验一般于检查后 30 分钟至次日出具，电子报告可在公众号查询，纸质报告到自助机打印。\n',
        1, 0, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_knowledge_doc (id, title, category, source_type, content, chunk_count, status, create_by, update_by,
                               del_flag, remark, create_by_id, update_by_id)
VALUES ('2106587567444054018', '科室介绍', '科室介绍', 1,
        '# 科室介绍（示例知识库条目）\n\n内科：位于门诊二楼东侧，设心血管、呼吸、消化、内分泌等专业诊室，诊治常见慢性病与内科系统疾病，工作日全天开诊。\n\n外科：位于门诊二楼西侧，设普外、骨科、泌尿等专业，处理外伤、肿物、骨折及需手术的疾病，急诊创伤在急诊科首诊。\n\n妇产科：位于门诊三楼，设妇科、产科与计划生育门诊，孕产期保健、妇科检查、孕期建档均在本科室。\n\n儿科：位于门诊一楼北侧，专门接诊 14 周岁以下儿童，设独立候诊与雾化治疗区。\n\n急诊科：位于门诊一楼西侧，24 小时开放，设抢救室、留观室与急诊检验，承担急危重症首诊。\n\n医学影像科：位于医技楼一楼，提供 DR、CT、磁共振（MRI）检查，CT 与 MRI 需先预约，急诊检查绿色通道优先。\n\n检验科：位于医技楼二楼，提供血尿便常规、生化、免疫等检验，抽血窗口工作日为 07:30 开始。\n',
        1, 0, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_knowledge_doc (id, title, category, source_type, content, chunk_count, status, create_by, update_by,
                               del_flag, remark, create_by_id, update_by_id)
VALUES ('2106587567540523010', '药品说明书示例', '药品说明书示例', 1,
        '# 药品说明书示例（示例知识库条目，非真实注册说明书）\n\n阿莫西林胶囊：属青霉素类抗菌药物。用于敏感菌所致呼吸道、尿路、皮肤软组织感染。成人常规口服一次 0.5g、每 6—8 小时一次，一日剂量不超过 4g。对青霉素过敏者禁用，用药前需询问过敏史，皮试阳性者不得使用。常见不良反应为腹泻、恶心、皮疹。\n\n布洛芬片：非甾体抗炎药（NSAIDs），用于缓解轻至中度疼痛（头痛、牙痛、痛经、关节痛）及普通感冒或流感所致发热。成人一次 0.2g—0.4g，若持续疼痛或发热可间隔 4—6 小时重复一次，24 小时内不超过 1.2g。活动性消化道溃疡、严重肝肾功能不全、对阿司匹林过敏者禁用。不可与其它含布洛芬的药品同服，避免空腹大量服用。\n\n以上为知识库示例条目，具体用药请遵医嘱，以实际药品说明书与药师指导为准。\n',
        1, 0, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_knowledge_doc (id, title, category, source_type, content, chunk_count, status, create_by, update_by,
                               del_flag, remark, create_by_id, update_by_id)
VALUES ('2360000000000004001', '住院须知', '住院服务', 1,
        '# 住院须知\n\n办理入院：持门诊/急诊医生开具的入院通知单，到住院部一楼入院处办理登记、缴纳预交金，领取腕带与床头卡。请携带身份证、医保凭证及既往检查资料。\n\n病区管理：住院期间请佩戴腕带，不得擅自离开病区；外出检查须告知责任护士。贵重物品请自行妥善保管或交家属带回。\n\n陪护与探视：每位患者原则上留陪护 1 人，探视时间为每日 15:00—20:00，ICU 病区按科室规定执行。\n\n饮食：医生开具膳食医嘱后由营养食堂统一配送；自带食物请先咨询医生或护士。\n\n出院办理：医生下达出院医嘱后，持出院记录到入院处结算费用、打印清单，凭结算单领取出院带药。',
        1, 0, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_knowledge_doc (id, title, category, source_type, content, chunk_count, status, create_by, update_by,
                               del_flag, remark, create_by_id, update_by_id)
VALUES ('2360000000000004002', '医保报销指南', '医保服务', 1,
        '# 医保报销指南\n\n参保类型：城镇职工医保、城乡居民医保、公费医疗。就诊时请主动出示医保电子凭证或社保卡。\n\n门诊报销：普通门诊统筹按比例报销，乙类药品先自付一定比例再纳入统筹；门诊慢特病需先在医保经办机构备案。\n\n住院报销：起付线以上、封顶线以内按政策比例报销，出院时在医院医保窗口直接结算（异地就医需提前备案）。\n\n不予报销范围：非疾病治疗项目（美容、矫形）、自费药品、第三方责任等。\n\n报销材料：发票原件、费用清单、出院记录、诊断证明。商业保险理赔请到病案室复印病历并盖章。',
        1, 0, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_knowledge_doc (id, title, category, source_type, content, chunk_count, status, create_by, update_by,
                               del_flag, remark, create_by_id, update_by_id)
VALUES ('2360000000000004003', '门诊慢特病待遇办理', '医保服务', 1,
        '# 门诊慢特病待遇办理\n\n适用病种：高血压、糖尿病、冠心病、脑血管病后遗症、慢性肾功能不全（透析）、恶性肿瘤门诊治疗、系统性红斑狼疮等。\n\n办理材料：二级及以上医院的出院记录或门诊诊断证明、相关检查检验报告、身份证与社保卡复印件。\n\n办理流程：主管医师填写《门诊慢特病待遇认定申请表》→ 医保办审核盖章 → 参保地医保经办机构评审 → 通过后自次月起享受门诊慢特病待遇。\n\n待遇标准：认定病种范围内的门诊费用按住院或门诊特定比例报销，具体比例以参保地政策为准。',
        1, 0, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_knowledge_doc (id, title, category, source_type, content, chunk_count, status, create_by, update_by,
                               del_flag, remark, create_by_id, update_by_id)
VALUES ('2360000000000004004', '体检须知', '健康体检', 1,
        '# 体检须知\n\n预约：通过公众号「健康体检」或电话预约，团体体检由单位统一安排。\n\n准备：体检前 3 天清淡饮食、忌酒；前 1 天晚 20:00 后禁食，保证充足睡眠。女性经期请避开尿常规、妇科检查；备孕或怀孕者禁做放射类项目。\n\n流程：体检当日晨起禁食禁水，携身份证到体检中心前台报到，按指引单顺序完成各科检查，空腹项目（采血、腹部彩超）优先。\n\n报告：一般 3—5 个工作日出报告，可在公众号查询电子报告，异常指标建议按报告提示专科门诊复查。',
        1, 0, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_knowledge_doc (id, title, category, source_type, content, chunk_count, status, create_by, update_by,
                               del_flag, remark, create_by_id, update_by_id)
VALUES ('2360000000000004005', '输液与注射注意事项', '门诊治疗', 1,
        '# 输液与注射注意事项\n\n输液前请告知医护人员：过敏史、是否正在使用抗凝药物、今日是否已进食（某些药物需餐后使用）。\n\n输液中若出现心慌、胸闷、皮疹、寒战等不适，请立即按床头呼叫铃，切勿自行调节滴速。\n\n皮试类药物（如青霉素类）需在治疗区观察 20 分钟无异常方可离院；72 小时内需连续使用同一批号药物请保留皮试记录。\n\n输液结束后按压针眼 5 分钟（勿揉），24 小时内避免针眼沾水。拔针后如出现局部肿胀，可用冷敷，24 小时后改热敷。',
        1, 0, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_knowledge_doc (id, title, category, source_type, content, chunk_count, status, create_by, update_by,
                               del_flag, remark, create_by_id, update_by_id)
VALUES ('2360000000000004006', '检验报告如何查询', '检验检查', 1,
        '# 检验报告如何查询\n\n电子报告：关注医院公众号，绑定就诊卡后在「报告查询」中查看检验与检查报告，一般采样后 30 分钟至 24 小时内出具。\n\n纸质报告：凭就诊卡到门诊大厅自助机打印，检验报告保留 30 天，请及时领取。\n\n危急值：检验发现危急值时会第一时间电话通知开单医生并同步病区，请保持登记电话畅通。\n\n报告解读：患者端报告页对常见指标提供白话解释，仅供参考；具体诊疗请以医生面诊意见为准。',
        1, 0, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_knowledge_doc (id, title, category, source_type, content, chunk_count, status, create_by, update_by,
                               del_flag, remark, create_by_id, update_by_id)
VALUES ('2360000000000004007', '投诉与建议渠道', '服务保障', 1,
        '# 投诉与建议渠道\n\n院内投诉：门诊一楼服务台、住院部一楼客服中心均可现场受理；工作时间 08:00—17:30。\n\n电话渠道：客服热线工作日 08:00—17:30 人工接听，其余时间语音留言，1 个工作日内回访。\n\n线上渠道：小程序「意见反馈」提交，可上传图片，客服将在 1 个工作日内回复处理进度。\n\n处理时限：一般投诉 3 个工作日内答复；涉及医疗质量的投诉转医务部专项处理，15 个工作日内答复。所有投诉信息严格保密。',
        1, 0, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_knowledge_doc (id, title, category, source_type, content, chunk_count, status, create_by, update_by,
                               del_flag, remark, create_by_id, update_by_id)
VALUES ('2360000000000004008', '出生医学证明办理', '妇幼服务', 1,
        '# 出生医学证明办理\n\n办理时间：新生儿分娩后即可申请，建议在出院前完成信息确认。\n\n所需材料：父母双方有效身份证原件及复印件、《出生医学证明首次签发登记表》（分娩机构领取填写）。\n\n办理地点：住院部三楼出生证明办理窗口，工作日 08:30—12:00、14:00—17:00。\n\n注意事项：新生儿姓名须用规范汉字，一经签发不可更改涂改；如信息有误需换发，请携带原证及父母身份证件办理。补办正、副页遗失的按签发机构补发流程办理。',
        1, 0, 'admin', 'admin', 0, NULL, '1', '1');
COMMIT;
SET
FOREIGN_KEY_CHECKS=1;

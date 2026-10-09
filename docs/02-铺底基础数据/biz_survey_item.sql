SET NAMES utf8mb4;
SET
FOREIGN_KEY_CHECKS=0;
START TRANSACTION;
INSERT INTO biz_survey_item (id, template_id, seq_no, dimension, question_type, title, required, weight, max_score,
                             create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('895000000000000911', '895000000000000901', 1, 1, 1, '挂号和预约是否方便、等待时间是否可接受', 1, '1.00', 5,
        'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO biz_survey_item (id, template_id, seq_no, dimension, question_type, title, required, weight, max_score,
                             create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('895000000000000912', '895000000000000901', 2, 2, 1, '主管医生是否把病情和治疗讲清楚', 1, '1.00', 5, 'admin',
        'admin', 0, NULL, '1', '1');
INSERT INTO biz_survey_item (id, template_id, seq_no, dimension, question_type, title, required, weight, max_score,
                             create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('895000000000000913', '895000000000000901', 3, 3, 1, '护士巡视、用药宣教是否到位', 1, '1.00', 5, 'admin',
        'admin', 0, NULL, '1', '1');
INSERT INTO biz_survey_item (id, template_id, seq_no, dimension, question_type, title, required, weight, max_score,
                             create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('895000000000000914', '895000000000000901', 4, 4, 1, '病区环境与标识指引是否满意', 1, '0.50', 5, 'admin',
        'admin', 0, NULL, '1', '1');
INSERT INTO biz_survey_item (id, template_id, seq_no, dimension, question_type, title, required, weight, max_score,
                             create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('895000000000000915', '895000000000000901', 5, 5, 1, '费用清单与价格是否清楚透明', 1, '1.00', 5, 'admin',
        'admin', 0, NULL, '1', '1');
INSERT INTO biz_survey_item (id, template_id, seq_no, dimension, question_type, title, required, weight, max_score,
                             create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('895000000000000916', '895000000000000901', 6, 6, 1, '总体治疗效果与住院安全感', 1, '2.00', 5, 'admin', 'admin',
        0, NULL, '1', '1');
INSERT INTO biz_survey_item (id, template_id, seq_no, dimension, question_type, title, required, weight, max_score,
                             create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('895000000000000917', '895000000000000901', 7, 7, 4, '您是否愿意向亲友推荐本院（0-10 分）', 0, '0.00', 10,
        'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO biz_survey_item (id, template_id, seq_no, dimension, question_type, title, required, weight, max_score,
                             create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('895000000000000918', '895000000000000901', 8, 7, 5, '您对我们还有什么意见或建议', 0, '0.00', 5, 'admin',
        'admin', 0, NULL, '1', '1');
COMMIT;
SET
FOREIGN_KEY_CHECKS=1;

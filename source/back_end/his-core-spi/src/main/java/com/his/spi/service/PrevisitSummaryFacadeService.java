package com.his.spi.service;

import com.his.spi.vo.PrevisitDetailVO;

/**
 * 预问诊端口：按挂号取患者自填的病史采集结果，并把凝练好的病史摘要写回。
 */
public interface PrevisitSummaryFacadeService {

    /**
     * 按挂号取该次就诊的预问诊记录。
     *
     * <p>用于医生站报告卡与病史摘要凝练前取回主症状、问答明细与患者补充描述。
     *
     * @param registId 挂号ID
     * @return 预问诊记录；未提交过返回 {@code null}
     */
    PrevisitDetailVO getByRegist(Long registId);

    /**
     * 写回病史摘要。
     *
     * <p>用于摘要环节生成完文案后落到预问诊记录上，医生站报告卡直接读该记录，不在页面上二次生成。
     *
     * @param registId 挂号ID
     * @param summary  摘要正文
     * @param source   摘要来源（1-模型 2-规则）
     */
    void saveSummary(Long registId, String summary, Integer source);
}

package com.his.clinical.service;

import com.his.common.enums.QcTriggerSourceEnum;
import com.his.clinical.entity.BizQualityControl;
import com.his.clinical.vo.QcResultVO;
import com.his.clinical.support.QcOperator;
import com.his.clinical.support.QcSnapshot;

/**
 * 病历质控落库服务：一次质控的质控单与其问题明细的写入收口。
 */
public interface QcStoreService {

    /**
     * 落一张质控单及其问题明细：单号由服务端生成，结论、得分、严重度上限取自规则引擎结果，问题明细超上限只截断不丢弃主单。
     * 供病案质控执行环节在规则引擎跑完拿到结果后写入质控记录时调用，病历保存链路的自动质控与手动「立即质控」走同一入口。
     * 主单与明细同事务整体成败；单号撞唯一索引时整事务回滚，由调用方在事务外重试——落库单独成一个能力即为此。
     *
     * @param qcType        本次质控类型，为空按综合质控落库
     * @param triggerSource 触发来源（人工 / 结诊自动 / 定时补扫），决定这张单能不能被算进自动质控覆盖率
     * @param operator      操作人快照，同时作为质控人与建单人（建单人ID也要它带，见实现类注释）
     */
    BizQualityControl save(QcSnapshot snapshot, QcResultVO result, Integer qcType,
                           QcTriggerSourceEnum triggerSource, QcOperator operator);
}

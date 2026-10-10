package com.his.patient.support;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.his.patient.entity.BizInpatientOperation;
import com.his.patient.mapper.BizInpatientOperationMapper;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

/**
 * 病案首页手术明细的序号重排。
 */
public final class SummaryOperationSeq {

    /**
     * 重排某次住院的全部手术明细序号：主要手术在前，其余按手术时间升序，
     * 时间相同按 ID 升序（保证结果稳定，可重复执行）。
     */
    public static void reseq(BizInpatientOperationMapper mapper, Long admissionId) {
        if (mapper == null || admissionId == null) {
            return;
        }
        List<BizInpatientOperation> rows = mapper.selectList(
                new LambdaQueryWrapper<BizInpatientOperation>()
                        .eq(BizInpatientOperation::getAdmissionId, admissionId));
        rows.sort(Comparator
                .comparing((BizInpatientOperation o) -> Objects.equals(1, o.getIsMain()) ? 0 : 1)
                .thenComparing(o -> o.getOperationDate() == null ? LocalDateTime.MAX : o.getOperationDate())
                .thenComparing(BizInpatientOperation::getId));
        int seq = 1;
        for (BizInpatientOperation o : rows) {
            if (!Objects.equals(seq, o.getSeqNo())) {
                // 只更新序号列（MyBatis-Plus 默认 NOT_NULL 策略，其余字段不会被覆盖成 null）
                BizInpatientOperation upd = new BizInpatientOperation();
                upd.setId(o.getId());
                upd.setSeqNo(seq);
                mapper.updateById(upd);
            }
            seq++;
        }
    }
}

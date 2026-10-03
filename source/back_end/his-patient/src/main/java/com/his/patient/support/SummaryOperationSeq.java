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
 *
 * <p>为什么需要它：首页手术明细的序号不是随便排的 —— <b>主要手术恒为第 1 条</b>
 * （国家首页口径，也是"主要手术"这个格子取数的地方）。
 * 而这张表的写入方有<b>两个</b>：
 * <ol>
 *   <li>手术闭环完成时回写（手术申请单.finish）；</li>
 *   <li>病案首页表单保存（{@code /patient/inpatient/summary/save}）。</li>
 * </ol>
 * 两边各自 delete+insert/insert 之后，序号必然对不上（闭环插了第 n+1 条，
 * 而表单又从 1 开始排）。所以两边都在写完之后调用这里统一重排 ——
 * <b>同一份数据有两个写入方，排序规则就必须只有一处实现</b>，否则迟早出现
 * "首页主要手术指向序号 2"这种没人能一眼看懂的错。
 */
public final class SummaryOperationSeq {

    private SummaryOperationSeq() {
    }

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

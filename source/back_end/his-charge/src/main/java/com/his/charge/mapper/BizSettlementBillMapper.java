package com.his.charge.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.his.charge.dto.PendingEncounterQueryPageDTO;
import com.his.charge.entity.BizSettlementBill;
import com.his.charge.vo.PendingEncounterVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * 结算账单 Mapper。
 *
 * <p>自定义 {@code @Select} 不受 {@code @TableLogic} 影响 → 显式写 {@code del_flag = 0}。
 */
@Mapper
public interface BizSettlementBillMapper extends BaseMapper<BizSettlementBill> {

    /**
     * 某次就诊下「未收讫」的账单张数（1-待支付 2-部分支付）。
     * 临床侧要问"这个人的费用结清了没有"，读这里，不再读收费单状态列。
     */
    @Select("""
            SELECT COUNT(*)
            FROM biz_settlement_bill b
            WHERE b.del_flag = 0 AND b.bill_status IN (1, 2)
              AND b.encounter_type = #{encounterType} AND b.encounter_id = #{encounterId}
            """)
    long countUnpaid(@Param("encounterType") Integer encounterType,
                     @Param("encounterId") Long encounterId);

    /**
     * 某次就诊未收讫账单的应缴差额合计（应付 - 已收）。
     */
    @Select("""
            SELECT IFNULL(SUM(b.payable_amount - b.paid_amount), 0)
            FROM biz_settlement_bill b
            WHERE b.del_flag = 0 AND b.bill_status IN (1, 2)
              AND b.encounter_type = #{encounterType} AND b.encounter_id = #{encounterId}
            """)
    BigDecimal sumUnpaidGap(@Param("encounterType") Integer encounterType,
                            @Param("encounterId") Long encounterId);

    /**
     * 收费台首屏：按<b>就诊</b>汇总的待收费榜。
     *
     * <p>两个金额必须分开列而不是相加：待出账的应收（L1）说明"还没结成账单"，
     * 未收齐的差额（L2）说明"账单开了钱没到"，收费员的操作完全不同（先结算 vs 先收款）。
     * 记账行只认 {@code fee_status = 1}：已锁进账单的行由账单侧统计，两边都算一次就翻倍。
     * 差额表达式与 {@link #sumUnpaidGap} 逐字一致，否则列表与详情两个页面两个数。
     *
     * <p>外层包子查询：MP 分页 count 的 join 优化会把无人引用的 LEFT JOIN 删掉，
     * 内层条件引用聚合列时就报 Unknown column。
     */
    @Select("""
            <script>
            SELECT * FROM (
              SELECT e.encounterType AS encounterType, e.encounterId AS encounterId, e.encounterNo AS encounterNo,
                     e.patientId AS patientId, e.patientNo AS patientNo, e.patientName AS patientName,
                     IFNULL(f.cnt, 0) AS pendingFeeCount, IFNULL(f.amt, 0) AS pendingFeeAmount,
                     IFNULL(b.cnt, 0) AS unpaidBillCount, IFNULL(b.gap, 0) AS unpaidBillAmount,
                     DATE_FORMAT(GREATEST(IFNULL(f.lastTime, '1970-01-01'), IFNULL(b.lastTime, '1970-01-01')),
                                 '%Y-%m-%d %H:%i:%s') AS lastTime
                FROM (
                      SELECT encounter_type AS encounterType, encounter_id AS encounterId,
                             MAX(encounter_no) AS encounterNo, MAX(patient_id) AS patientId,
                             MAX(patient_no) AS patientNo, MAX(patient_name) AS patientName
                        FROM biz_fee_record
                       WHERE del_flag = 0 AND fee_status = 1 AND encounter_type = #{q.encounterType}
                       GROUP BY encounter_type, encounter_id
                      UNION
                      SELECT encounter_type, encounter_id,
                             MAX(encounter_no), MAX(patient_id), MAX(patient_no), MAX(patient_name)
                        FROM biz_settlement_bill
                       WHERE del_flag = 0 AND bill_status IN (1, 2) AND encounter_type = #{q.encounterType}
                       GROUP BY encounter_type, encounter_id
                     ) e
                LEFT JOIN (SELECT encounter_id AS eid, COUNT(*) AS cnt, SUM(amount) AS amt, MAX(book_time) AS lastTime
                             FROM biz_fee_record
                            WHERE del_flag = 0 AND fee_status = 1 AND encounter_type = #{q.encounterType}
                            GROUP BY encounter_id) f ON f.eid = e.encounterId
                LEFT JOIN (SELECT encounter_id AS eid, COUNT(*) AS cnt,
                                   SUM(payable_amount - paid_amount) AS gap, MAX(bill_time) AS lastTime
                             FROM biz_settlement_bill
                            WHERE del_flag = 0 AND bill_status IN (1, 2) AND encounter_type = #{q.encounterType}
                            GROUP BY encounter_id) b ON b.eid = e.encounterId
            ) t
            WHERE (t.pendingFeeCount &gt; 0 OR t.unpaidBillCount &gt; 0)
            <if test="q.keyword != null and q.keyword != ''">
              AND (t.patientName LIKE CONCAT('%', #{q.keyword}, '%')
                   OR t.patientNo LIKE CONCAT('%', #{q.keyword}, '%')
                   OR t.encounterNo LIKE CONCAT('%', #{q.keyword}, '%'))
            </if>
            ORDER BY t.lastTime DESC, t.encounterId DESC
            </script>
            """)
    IPage<PendingEncounterVO> selectPendingEncounterPage(IPage<PendingEncounterVO> page,
                                                         @Param("q") PendingEncounterQueryPageDTO query);

    /**
     * 日结区间内的账单（含作废，作废也要出现在差异说明里）。
     */
    @Select("""
            SELECT b.*
            FROM biz_settlement_bill b
            WHERE b.del_flag = 0 AND b.bill_date >= #{beginDate} AND b.bill_date <= #{endDate}
            ORDER BY b.id
            """)
    List<BizSettlementBill> selectByBillDate(@Param("beginDate") LocalDate beginDate,
                                             @Param("endDate") LocalDate endDate);
}

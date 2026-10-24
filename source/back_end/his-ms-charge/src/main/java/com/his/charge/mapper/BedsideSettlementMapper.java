package com.his.charge.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.his.charge.vo.BedsideWardPatientVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 床旁结算 Mapper（病区榜）。
 *
 * <p>与欠费榜（{@code BizArrearsPolicyMapper.selectArrearsBoard}的关系：
 * 金额聚合口径逐项照抄，差别只在一线之隔——欠费榜按欠费额倒序列全院的欠费者，
 * 这里按床号列<b>本病区全部在院患者</b>（含不欠费的，床旁结算要能找到刚出院的那个人）。
 */
@Mapper
public interface BedsideSettlementMapper extends BaseMapper<Object> {

    /**
     * 病区在院患者榜（带钱况与结算单状态）。
     *
     * <p>科室范围由服务端按当前岗位科室算好传进来（{@code deptIds}），SQL 不再接受任何科室参数。
     * 已发生 = L1 应收净额（fee_status&lt;&gt;4 的正负行合计）；
     * 已收 = 净预交（无账单锚的 source_type=3 流水）+ 账单上直接收的钱（排除 pay_method=5 余额抵扣）；
     * 欠费 = max(0, 已发生 − 已收)。账单列 = 该入院最新一张未作废的出院结算单。
     */
    @Select("""
            <script>
            SELECT * FROM (
              SELECT a.admission_id AS admissionId, a.admission_no AS admissionNo,
                     a.patient_id AS patientId, p.patient_no AS patientNo,
                     p.patient_name AS patientName, p.gender AS gender, p.age AS age,
                     d.dept_name AS deptName, b.bed_no AS bedNo,
                     a.diagnosis AS diagnosis, a.admit_time AS admitTime,
                     IFNULL(fa.bal, 0) AS prepayBalance,
                     IFNULL(fr.tot, 0) AS chargedAmount,
                     GREATEST(IFNULL(fr.tot, 0) - IFNULL(pp.net, 0) - IFNULL(bc.net, 0), 0) AS arrearsAmount,
                     sb.id AS billId, sb.bill_no AS billNo,
                     sb.payable_amount AS billPayable, sb.paid_amount AS billPaid,
                     GREATEST(IFNULL(sb.payable_amount, 0) - IFNULL(sb.paid_amount, 0), 0) AS billUnpaid,
                     sb.bill_status AS billStatus
                FROM biz_admission a
                JOIN biz_patient p ON p.id = a.patient_id AND p.del_flag = 0
                LEFT JOIN sys_department d ON d.id = a.dept_id AND d.del_flag = 0
                LEFT JOIN sys_bed b ON b.bed_id = a.bed_id AND b.del_flag = 0
                LEFT JOIN (SELECT encounter_id, SUM(amount) tot FROM biz_fee_record
                            WHERE del_flag = 0 AND fee_status &lt;&gt; 4 AND encounter_type = 2
                            GROUP BY encounter_id) fr
                       ON fr.encounter_id = a.admission_id
                LEFT JOIN (SELECT encounter_id, SUM(amount) net FROM biz_payment_txn
                            WHERE del_flag = 0 AND txn_status = 1 AND bill_id IS NULL AND source_type = 3
                              AND encounter_type = 2 GROUP BY encounter_id) pp
                       ON pp.encounter_id = a.admission_id
                LEFT JOIN (SELECT encounter_id, SUM(amount) net FROM biz_payment_txn
                            WHERE del_flag = 0 AND txn_status = 1 AND bill_id IS NOT NULL AND pay_method &lt;&gt; 5
                              AND encounter_type = 2 GROUP BY encounter_id) bc
                       ON bc.encounter_id = a.admission_id
                LEFT JOIN (SELECT admission_id, SUM(amount) bal FROM biz_fund_account_txn
                            WHERE del_flag = 0 AND txn_status = 1 AND owner_type = 2
                            GROUP BY admission_id) fa
                       ON fa.admission_id = a.admission_id
                LEFT JOIN (SELECT s1.* FROM biz_settlement_bill s1
                             JOIN (SELECT encounter_id, MAX(id) AS mid FROM biz_settlement_bill
                                    WHERE del_flag = 0 AND encounter_type = 2 AND bill_type = 4
                                      AND bill_status &lt;&gt; 4
                                    GROUP BY encounter_id) s2 ON s2.mid = s1.id) sb
                       ON sb.encounter_id = a.admission_id
               WHERE a.del_flag = 0 AND a.admit_status = 1
                 <if test="deptIds != null and deptIds.size() > 0">
                 AND a.dept_id IN
                 <foreach item="did" collection="deptIds" open="(" separator="," close=")">#{did}</foreach>
                 </if>
            ) t
            <if test="keyword != null and keyword != ''">
            WHERE (t.patientName LIKE CONCAT('%', #{keyword}, '%') OR t.patientNo LIKE CONCAT('%', #{keyword}, '%'))
            </if>
            ORDER BY t.bedNo, t.admissionId
            </script>
            """)
    IPage<BedsideWardPatientVO> selectWardBoard(Page<BedsideWardPatientVO> page,
                                                @Param("deptIds") List<Long> deptIds,
                                                @Param("keyword") String keyword);
}

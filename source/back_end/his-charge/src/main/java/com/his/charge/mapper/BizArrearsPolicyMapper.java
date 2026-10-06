package com.his.charge.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.his.charge.entity.BizArrearsPolicy;
import com.his.charge.vo.ArrearsPatientVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 欠费管控 Mapper。
 */
@Mapper
public interface BizArrearsPolicyMapper extends BaseMapper<BizArrearsPolicy> {

    /**
     * 在院欠费患者榜。
     *
     * <p>口径必须与 {@code InpatientAccountService.arrearsView} 逐项对应（同一个患者两个页面两个欠费额
     * 就等于没有口径）：已发生 = L1 应收净额（{@code fee_status<>4} 的正负行合计）；
     * 已收 = 净预交（充值 − 柜面退款，无账单锚的 {@code source_type=3} 流水）
     * 加上账单上直接收的钱（排除 {@code pay_method=5} 余额抵扣，那笔就是预交金，算两遍会把欠 800 的人显示成不欠）；
     * 余额 = 住院资金账户净额（{@code owner_type=2}），它和"已收"不等价，因为抵扣后钱离开了账户。
     * 只列在院（admit_status=1）；欠费 = max(0, 已发生 − 已收) > 0，按欠费额倒序。
     */
    @Select("""
            <script>
            /* 外层包子查询：MP 分页 count 的 join 优化会删掉无 WHERE 引用的 LEFT JOIN，
               导致内层 HAVING 引用不到聚合列 —— 包一层后外层 FROM 是子查询，优化不会越进来 */
            SELECT * FROM (
              SELECT a.admission_id AS admissionId, a.admission_no AS admissionNo,
                     a.patient_id AS patientId, p.patient_no AS patientNo,
                     p.patient_name AS patientName,
                     d.dept_name AS deptName, a.diagnosis AS diagnosis,
                     a.admit_time AS admitTime,
                     IFNULL(fa.bal, 0) AS prepayBalance,
                     IFNULL(fr.tot, 0) AS chargedAmount,
                     GREATEST(IFNULL(fr.tot, 0) - IFNULL(pp.net, 0) - IFNULL(bc.net, 0), 0) AS arrearsAmount
                FROM biz_admission a
                JOIN biz_patient p ON p.id = a.patient_id AND p.del_flag = 0
                LEFT JOIN sys_department d ON d.id = a.dept_id AND d.del_flag = 0
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
               WHERE a.del_flag = 0 AND a.admit_status = 1
            ) t
            WHERE t.arrearsAmount &gt; 0
              <if test="keyword != null and keyword != ''">
              AND (t.patientName LIKE CONCAT('%', #{keyword}, '%') OR t.patientNo LIKE CONCAT('%', #{keyword}, '%'))
              </if>
            ORDER BY t.arrearsAmount DESC
            </script>
            """)
    IPage<ArrearsPatientVO> selectArrearsBoard(Page<ArrearsPatientVO> page, @Param("keyword") String keyword);
}

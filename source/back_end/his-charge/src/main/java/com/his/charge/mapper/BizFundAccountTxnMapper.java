package com.his.charge.mapper;

import com.his.charge.entity.BizFundAccountTxn;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import java.math.BigDecimal;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 资金账户流水 Mapper。
 *
 * <p>余额永远 SUM 流水，不读 {@code balance_after} 快照（那是审计用的，写错一次就永久错）。
 */
@Mapper
public interface BizFundAccountTxnMapper extends BaseMapper<BizFundAccountTxn> {

    @Select("""
            SELECT IFNULL(SUM(t.amount), 0)
            FROM biz_fund_account_txn t
            WHERE t.del_flag = 0 AND t.txn_status = 1 AND t.account_id = #{accountId}
            """)
    BigDecimal sumBalance(@Param("accountId") Long accountId);

    /**
     * 某次住院的预交金余额（欠费管控按入院算，不是按人算 ——
     * 同一个人可能同时有门诊余额和这次住院的预交金）。
     */
    @Select("""
            SELECT IFNULL(SUM(t.amount), 0)
            FROM biz_fund_account_txn t
            WHERE t.del_flag = 0 AND t.txn_status = 1 AND t.owner_type = 2 AND t.admission_id = #{admissionId}
            """)
    BigDecimal sumAdmissionBalance(@Param("admissionId") Long admissionId);

    @Select("""
            SELECT t.*
            FROM biz_fund_account_txn t
            WHERE t.del_flag = 0 AND t.account_id = #{accountId}
            ORDER BY t.txn_time DESC, t.id DESC
            """)
    List<BizFundAccountTxn> selectByAccount(@Param("accountId") Long accountId);
}

package com.his.charge.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.his.charge.entity.BizFundAccount;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.math.BigDecimal;

/**
 * 资金账户 Mapper。
 */
@Mapper
public interface BizFundAccountMapper extends BaseMapper<BizFundAccount> {

    /**
     * 加行锁取账户：余额扣减必须在锁内做「读余额 → 判够不够 → 写流水 → 更新缓存」，
     * 否则两个人同时刷同一个人的余额，两边都判够、扣完就是负数。
     */
    @Select("""
            SELECT a.*
            FROM biz_fund_account a
            WHERE a.del_flag = 0 AND a.owner_type = #{ownerType} AND a.owner_id = #{ownerId}
            FOR UPDATE
            """)
    BizFundAccount selectByOwnerForUpdate(@Param("ownerType") Integer ownerType,
                                          @Param("ownerId") Long ownerId);

    @Select("""
            SELECT a.*
            FROM biz_fund_account a
            WHERE a.del_flag = 0 AND a.owner_type = #{ownerType} AND a.owner_id = #{ownerId}
            """)
    BizFundAccount selectByOwner(@Param("ownerType") Integer ownerType,
                                 @Param("ownerId") Long ownerId);

    /**
     * 乐观锁更新余额：UPDATE ... SET balance = balance + ?, version = version + 1
     * WHERE id = ? AND version = ?。返回受影响行数，0 表示版本冲突。
     */
    @Update("UPDATE biz_fund_account " +
            " SET balance = balance + #{delta}, " +
            "     total_recharge = CASE WHEN #{delta} > 0 THEN total_recharge + #{delta} ELSE total_recharge END, " +
            "     total_consume = CASE WHEN #{delta} < 0 THEN total_consume - #{delta} ELSE total_consume END, " +
            "     last_txn_time = #{lastTxnTime}, " +
            "     version = version + 1 " +
            " WHERE id = #{id} AND version = #{version}")
    int updateBalanceOptimistic(@Param("id") Long id,
                                @Param("delta") BigDecimal delta,
                                @Param("version") Long version,
                                @Param("lastTxnTime") java.time.LocalDateTime lastTxnTime);
}

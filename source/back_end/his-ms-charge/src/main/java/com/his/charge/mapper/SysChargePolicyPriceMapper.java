package com.his.charge.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.his.charge.entity.SysChargePolicyPrice;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

import java.math.BigDecimal;

/**
 * 收费政策价目 Mapper
 */
@Mapper
public interface SysChargePolicyPriceMapper extends BaseMapper<SysChargePolicyPrice> {

    /**
     * 调价（价格管理页「政策价」）：全额必调，加收额可空（null = 不动加收列）。
     */
    @Update("<script>" +
            "UPDATE sys_charge_policy_price SET amount = #{amount}, " +
            "<if test='surcharge != null'>surcharge = #{surcharge}, </if>" +
            "update_time = NOW() WHERE id = #{id}" +
            "</script>")
    int updatePrice(@Param("id") Long id,
                    @Param("amount") BigDecimal amount,
                    @Param("surcharge") BigDecimal surcharge);
}

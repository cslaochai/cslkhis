package com.his.operation.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.his.operation.entity.BizOperationCount;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 手术器械清点主单 Mapper。
 *
 * <p>{@code countBlocking()} 是这条链<b>唯一真正的价值</b>：
 * 手术登记完成之前问一句"这台手术的器械清点到底是干净还是没做"，
 * 只要存在"三轮没走完"或"三轮走完但不一致"的清点单，就锁死 {@code finish()}。
 */
@Mapper
public interface BizOperationCountMapper extends BaseMapper<BizOperationCount> {

    /** 当天已生成的清点单号条数（单号序号用） */
    @Select("SELECT COUNT(*) FROM biz_operation_count WHERE del_flag = 0 AND count_no LIKE CONCAT(#{prefix}, '%')")
    long countByNoPrefix(@Param("prefix") String prefix);

    /**
     * 该手术是否存在“未完成/不一致”的清点单。
     *
     * <p>口径刻意<b>不</b>把「压根没有清点单」也算拦截：清点是可选登记的，
     * 把它当成每台手术的必填项，只会催生"为了过关而补填的假清点单"——
     * 那比没有更糟。但只要**登记了并且对不上**，就必须是硬闸门。<｜hy_place▁holder▁no▁813｜> —— 但只要**登记了并且对不上**，就必须是硬闸门。
     */
    @Select("""
            SELECT COUNT(*) FROM biz_operation_count
            WHERE del_flag = 0 AND apply_id = #{applyId}
              AND phase < 3
            """)
    long countUnfinished(@Param("applyId") Long applyId);

    @Select("""
            SELECT COUNT(*) FROM biz_operation_count
            WHERE del_flag = 0 AND apply_id = #{applyId}
              AND (final_result = 2 OR discrepancy_flag = 1 OR status = 2)
            """)
    long countDiscrepancy(@Param("applyId") Long applyId);
}

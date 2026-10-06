package com.his.charge.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.his.charge.entity.BizFeeRecord;
import com.his.charge.vo.FeeTypeSumVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.math.BigDecimal;
import java.util.List;

/**
 * 费用记账行 Mapper。
 *
 * <p>自定义 {@code @Select} 不受 {@code @TableLogic} 影响 → 显式写 {@code del_flag = 0}。
 */
@Mapper
public interface BizFeeRecordMapper extends BaseMapper<BizFeeRecord> {

    /**
     * 某次就诊「还没进账单」的应收合计（含红冲负行，净额才是真应收）。
     * 签到/发药这些临床门禁问的是"还有没收的钱"，答案来自这里，不来自支付状态列。
     */
    @Select("""
            SELECT IFNULL(SUM(r.amount), 0)
            FROM biz_fee_record r
            WHERE r.del_flag = 0 AND r.fee_status = 1
              AND r.encounter_type = #{encounterType} AND r.encounter_id = #{encounterId}
            """)
    BigDecimal sumPendingAmount(@Param("encounterType") Integer encounterType,
                                @Param("encounterId") Long encounterId);

    /**
     * 某次就诊未结算的记账行数（0=费用全部结清或已进账单）
     */
    @Select("""
            SELECT COUNT(*)
            FROM biz_fee_record r
            WHERE r.del_flag = 0 AND r.fee_status = 1
              AND r.encounter_type = #{encounterType} AND r.encounter_id = #{encounterId}
            """)
    long countPending(@Param("encounterType") Integer encounterType,
                      @Param("encounterId") Long encounterId);

    /**
     * 某次就诊按项目类型分组的应收净额（含红冲负行，排除已全额红冲的原行）。
     *
     * <p>与 {@link #sumPendingAmount} 的区别是不问结算状态：患者引导单、科室报表问的是
     * "这次就诊一共发生了多少钱"，钱收没收回、进没进账单都不改变这笔费用发生过的事实。
     */
    @Select("""
            SELECT r.item_type AS itemType, IFNULL(SUM(r.amount), 0) AS amount
            FROM biz_fee_record r
            WHERE r.del_flag = 0 AND r.fee_status <> 4
              AND r.encounter_type = #{encounterType} AND r.encounter_id = #{encounterId}
            GROUP BY r.item_type
            ORDER BY r.item_type
            """)
    List<FeeTypeSumVO> sumNetGroupByItemType(@Param("encounterType") Integer encounterType,
                                             @Param("encounterId") Long encounterId);

    /**
     * 某次就诊发生过的记账行明细（含红冲负行，排除已全额红冲的一对）。
     *
     * <p>日清单要的就是这张清单：不问结算状态（进没进账单、钱收没收都改变不了"这天发生过这笔费用"），
     * 但必须把负行一起带出来，否则红冲之后清单合计比实际应收大。
     */
    @Select("""
            SELECT r.*
            FROM biz_fee_record r
            WHERE r.del_flag = 0 AND r.fee_status <> 4
              AND r.encounter_type = #{encounterType} AND r.encounter_id = #{encounterId}
            ORDER BY r.book_time, r.id
            """)
    List<BizFeeRecord> selectNetByEncounter(@Param("encounterType") Integer encounterType,
                                            @Param("encounterId") Long encounterId);

    /**
     * 某次就诊的应收净额（同上口径，只合计不外泄明细）：账务概览与欠费榜的"已发生费用"。
     */
    @Select("""
            SELECT IFNULL(SUM(r.amount), 0)
            FROM biz_fee_record r
            WHERE r.del_flag = 0 AND r.fee_status <> 4
              AND r.encounter_type = #{encounterType} AND r.encounter_id = #{encounterId}
            """)
    BigDecimal sumNetByEncounter(@Param("encounterType") Integer encounterType,
                                 @Param("encounterId") Long encounterId);

    /**
     * 按来源单据查记账行：记账幂等用（同一张处方重复执行不能记两次费用）。
     */
    @Select("""
            SELECT r.*
            FROM biz_fee_record r
            WHERE r.del_flag = 0 AND r.source_type = #{sourceType} AND r.source_id = #{sourceId}
            ORDER BY r.id
            """)
    List<BizFeeRecord> selectBySource(@Param("sourceType") Integer sourceType,
                                      @Param("sourceId") Long sourceId);

    /**
     * 净应收（含红冲）：某来源单据实际记了多少费用。
     * 一张处方多次执行/多次冲减时，"这张单子还剩多少钱"必须现算，不能读某一行的 amount。
     */
    @Select("""
            SELECT IFNULL(SUM(r.amount), 0)
            FROM biz_fee_record r
            WHERE r.del_flag = 0 AND r.source_type = #{sourceType} AND r.source_id = #{sourceId}
              AND r.fee_status <> 4
            """)
    BigDecimal sumNetBySource(@Param("sourceType") Integer sourceType,
                              @Param("sourceId") Long sourceId);

    /**
     * 本行已被冲减的金额（负数或 0）。红冲前用它算"还能冲多少"，防止把一行冲成负应收。
     *
     * <p>不按 fee_status 过滤：整行冲完时原行与负行一起置 4，此时"已冲多少"仍是历史事实，
     * 详情页要能看出来 100 元是被 30+40+30 三次冲掉的，而不是凭空消失。
     */
    @Select("""
            SELECT IFNULL(SUM(r.amount), 0)
            FROM biz_fee_record r
            WHERE r.del_flag = 0 AND r.orig_fee_id = #{feeId}
            """)
    BigDecimal sumReversalAmount(@Param("feeId") Long feeId);

    /**
     * 本行已被冲减的数量（负数或 0），与 {@link #sumReversalAmount} 一起决定"还能冲多少"。
     */
    @Select("""
            SELECT IFNULL(SUM(r.quantity), 0)
            FROM biz_fee_record r
            WHERE r.del_flag = 0 AND r.orig_fee_id = #{feeId}
            """)
    BigDecimal sumReversalQuantity(@Param("feeId") Long feeId);

    /**
     * 冲本行的所有红冲行（详情回显）
     */
    @Select("""
            SELECT r.*
            FROM biz_fee_record r
            WHERE r.del_flag = 0 AND r.orig_fee_id = #{feeId}
            ORDER BY r.id
            """)
    List<BizFeeRecord> selectReversalRows(@Param("feeId") Long feeId);
}

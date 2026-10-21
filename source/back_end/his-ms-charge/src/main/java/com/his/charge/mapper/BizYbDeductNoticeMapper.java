package com.his.charge.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.his.charge.entity.BizYbDeductNotice;
import com.his.charge.vo.DeductSummaryVO;
import com.his.charge.vo.YbInspectionDeductCountVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.Collection;
import java.util.List;

/**
 * 扣款通知单 Mapper（状态机流转见 YbDeductNoticeServiceImpl）。
 */
@Mapper
public interface BizYbDeductNoticeMapper extends BaseMapper<BizYbDeductNotice> {

    /**
     * 按飞检批次聚合名下扣款单数与金额（批次列表的「几张单/多少钱」和作废闸门都来自这里）。
     * 列名与医保扣款通知单在 163 建表脚本里逐字对过。
     */
    @Select("<script>SELECT inspection_id AS inspectionId, COUNT(*) AS deductCount, "
            + "COALESCE(SUM(deduct_amount), 0) AS deductAmountSum "
            + "FROM biz_yb_deduct_notice WHERE del_flag = 0 AND inspection_id IN "
            + "<foreach collection='ids' item='i' open='(' separator=',' close=')'>#{i}</foreach> "
            + "GROUP BY inspection_id</script>")
    List<YbInspectionDeductCountVO> countByInspectionIds(@org.apache.ibatis.annotations.Param("ids") Collection<Long> ids);

    /**
     * 扣款台账汇总：一次 SQL 出六个数，页面顶部的卡片直接用它。
     * 超期口径与列表一致 —— handle_deadline 早于当天且状态为 1-待确认 / 2-申诉中。
     */
    @Select("SELECT "
            + "SUM(CASE WHEN deduct_status = 1 THEN 1 ELSE 0 END) AS pendingConfirmCount, "
            + "SUM(CASE WHEN deduct_status = 2 THEN 1 ELSE 0 END) AS appealingCount, "
            + "SUM(CASE WHEN deduct_status = 4 THEN 1 ELSE 0 END) AS waitPayCount, "
            + "SUM(CASE WHEN deduct_status = 5 THEN 1 ELSE 0 END) AS paidCount, "
            + "SUM(CASE WHEN deduct_status IN (1,2) AND handle_deadline < CURDATE() THEN 1 ELSE 0 END) AS overdueCount, "
            + "COALESCE(SUM(CASE WHEN deduct_status IN (1,2,4) THEN deduct_amount ELSE 0 END), 0) AS openAmountSum, "
            + "COALESCE(SUM(CASE WHEN deduct_status = 5 THEN paid_amount ELSE 0 END), 0) AS paidAmountSum "
            + "FROM biz_yb_deduct_notice WHERE del_flag = 0")
    DeductSummaryVO summary();
}


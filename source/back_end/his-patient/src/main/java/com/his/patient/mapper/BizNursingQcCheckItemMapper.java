package com.his.patient.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.his.patient.entity.BizNursingQcCheckItem;
import com.his.patient.vo.NurseQcVO;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 护理质量检查单明细 Mapper（sql/168）。
 *
 * <p>明细是检查表的正文：逐项抽查数/合格数/得分 + 存在问题/原因分析/整改措施（PDCA 后两环）。
 * 主表六个汇总数字全部由这里求和上来，读侧不再重复聚合。
 */
@Mapper
public interface BizNursingQcCheckItemMapper extends BaseMapper<BizNursingQcCheckItem> {

    /**
     * 一张单的明细（按目录顺序渲染）。
     * 单项合格率在 SQL 侧现算，因为列里没存它，而「哪一项最差」正是页面要排序标色的依据。
     */
    @Select("""
            SELECT id, check_id AS checkId, item_id AS itemId, item_code AS itemCode,
                   item_name AS itemName, category, checked_num AS checkedNum,
                   qualified_num AS qualifiedNum, full_score AS fullScore, score,
                   ROUND(qualified_num / NULLIF(checked_num, 0) * 100, 2) AS qualifiedRate,
                   problem, cause_analysis AS causeAnalysis, rectify_measure AS rectifyMeasure,
                   remark
              FROM biz_nursing_qc_check_item
             WHERE del_flag = 0 AND check_id = #{checkId}
             ORDER BY category, item_code, id
            """)
    List<NurseQcVO.CheckItemRow> selectItemsByCheckId(@Param("checkId") Long checkId);

    /**
     * 整单清明细（物理删）。
     *
     * <p>保存一张检查单是「先清这张单的明细再按提交重插」，而唯一键
     * {@code uk_check_item(check_id, item_id)} 不含 del_flag ⇒ 软删后第二步必撞键（AGENTS §3）。
     */
    @Delete("DELETE FROM biz_nursing_qc_check_item WHERE check_id = #{checkId}")
    int purgeByCheckId(@Param("checkId") Long checkId);
}

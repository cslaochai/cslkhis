package com.his.patient.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.his.patient.entity.SysNursingQcItem;
import com.his.patient.vo.NurseQcVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 护理质控检查项标准目录 Mapper（sql/168）。
 *
 * <p>本表是全院一份的打分依据，页面<b>只读</b>：目录要随评审细则改版走数据库变更（sql 文件），
 * 不做界面维护 —— 一旦允许在页面上改应得分，「每类合计 100」这条不变量就没有守门人了
 * （自检 T2 盯的是数据库里的合计，运行时改就绕过了它）。
 *
 * <p>只列 {@code status=1}：停用的项不再作为新检查单的候选，历史检查单靠自己的明细快照渲染。
 */
@Mapper
public interface SysNursingQcItemMapper extends BaseMapper<SysNursingQcItem> {

    String ITEM_COLUMNS = """
            id AS itemId, item_code AS itemCode, item_name AS itemName, category,
            indicator_code AS indicatorCode, standard, full_score AS fullScore,
            target_rate AS targetRate, key_flag AS keyFlag, sort_order AS sortOrder""";

    /** 某类别的启用检查项（检查单表单的行来源，也是保存时校验 itemId 合法性的唯一依据） */
    @Select("SELECT " + ITEM_COLUMNS + """
              FROM sys_nursing_qc_item
             WHERE del_flag = 0 AND status = 1 AND category = #{category}
             ORDER BY sort_order, id
            """)
    List<NurseQcVO.ItemDef> selectItemsByCategory(@Param("category") Integer category);

    /** 全部启用检查项（台账/看板按指标反查项目名用，一次捞完） */
    @Select("SELECT " + ITEM_COLUMNS + """
              FROM sys_nursing_qc_item
             WHERE del_flag = 0 AND status = 1
             ORDER BY category, sort_order, id
            """)
    List<NurseQcVO.ItemDef> selectAllEnabledItems();
}

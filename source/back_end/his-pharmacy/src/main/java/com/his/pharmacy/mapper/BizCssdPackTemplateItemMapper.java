package com.his.pharmacy.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.his.pharmacy.entity.BizCssdPackTemplateItem;
import com.his.pharmacy.vo.CssdPackTemplateItemVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * CSSD 器械包模板组成明细 Mapper。
 */
@Mapper
public interface BizCssdPackTemplateItemMapper extends BaseMapper<BizCssdPackTemplateItem> {

    /**
     * 启用模板下按名称去重的器械汇总（名称/单位/规格取任意一条），供模板编辑器下拉带出规格
     */
    @Select("SELECT item_name AS itemName, MAX(spec) AS spec, MAX(unit) AS unit " +
            "FROM biz_cssd_pack_template_item i " +
            "JOIN biz_cssd_pack_template t ON t.id = i.template_id AND t.del_flag = 0 " +
            "WHERE t.status = 1 " +
            "GROUP BY item_name ORDER BY item_name")
    List<CssdPackTemplateItemVO> selectDistinctItemSummary();
}

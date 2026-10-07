package com.his.ai.mapper;

import com.his.ai.vo.LabItemRefCountVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 检验结果表的项目名引用次数统计（白话词典覆盖率用）。
 *
 * <p>表属于 his-medicaltech，但统计口径服务于 his-ai 的词典维护页，
 * 故在此处自建只读查询：<b>只读his-ai 目录内可改</b>，
 * 不必为了一个统计口径去改业务模块的 Mapper。
 *
 * <p>SQL 与原 {@code QueryWrapper + selectMaps} 写法逐条等价：
 *同一张表、同一过滤条件、同一分组、同一别名；
 * {@code del_flag = 0} 是显式补上的 —— MP 的 {@code @TableLogic} 只对
 * {@code selectMaps/selectList} 这类内置方法自动追加，写成 {@code @Select}
 * 后逻辑删除<b>不会</b>自动加，漏掉就会把软删行算进分母。
 */
@Mapper
public interface LabResultRefCountMapper {

    /**
     * 按检验项目名统计出现次数（已排除空项目名与软删行）。
     *
     * <p>SQL 别名与 {@link LabItemRefCountVO} 字段名严格一致：
     * {@code laboratoryItemName} / {@code refCnt}。
     */
    @Select("SELECT laboratory_item_name AS laboratoryItemName, COUNT(*) AS refCnt "
            + "FROM biz_lab_result "
            + "WHERE del_flag = 0 AND laboratory_item_name IS NOT NULL AND laboratory_item_name <> '' "
            + "GROUP BY laboratory_item_name")
    List<LabItemRefCountVO> countGroupByItemName();
}
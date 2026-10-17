package com.his.ai.mapper;

import com.his.ai.vo.LabItemRefCountVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 检验结果表的项目名引用次数统计（白话词典覆盖率用）。
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
package com.his.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.his.system.entity.SysDrug;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 药品Mapper
 */
@Mapper
public interface SysDrugMapper extends BaseMapper<SysDrug> {

    /**
     * 按成分关键字统计在用药品的条数（药品名称或通用名包含即算）
     * <p>合理用药知识库用它区分「知识写错了」和「本院还没进这个药」，见 sql/130 自检 T5。
     * 裸 SQL 需自带 del_flag（@TableLogic 只作用于 Wrapper 查询）。
     */
    @Select("SELECT COUNT(*) FROM sys_drug WHERE del_flag = 0 AND status = 1"
            + " AND (drug_name LIKE CONCAT('%', #{keyword}, '%') OR generic_name LIKE CONCAT('%', #{keyword}, '%'))")
    int countByComponent(@Param("keyword") String keyword);
}

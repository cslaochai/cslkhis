package com.his.platform.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.his.platform.entity.SysDictData;
import com.his.platform.vo.OrderDictUsageCountVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 字典数据Mapper
 */
@Mapper
public interface SysDictDataMapper extends BaseMapper<SysDictData> {

    @Select("SELECT * FROM sys_dict_data WHERE dict_type = #{dictType} AND status = 1 AND del_flag = 0 ORDER BY dict_sort")
    List<SysDictData> selectByDictType(@Param("dictType") String dictType);

    /**
     * 某个字典值被多少条住院医嘱引用（医嘱字典列表上的「使用量」列）。
     *
     * <p>{@code ${column}} 是动态列名，只能由 {@code OrderDictTypes.orderColumn()} 传入
     * （route / frequency / dosage_unit 三选一），不接任何外部输入；
     * 这里必须显式写 {@code del_flag = 0} —— 自定义 @Select 不受 @TableLogic 影响。
     */
    @Select("SELECT ${column} AS dictValue, COUNT(*) AS cnt FROM biz_inpatient_order "
            + "WHERE del_flag = 0 AND ${column} IS NOT NULL AND ${column} <> '' GROUP BY ${column}")
    List<OrderDictUsageCountVO> countOrderUsage(@Param("column") String column);
}

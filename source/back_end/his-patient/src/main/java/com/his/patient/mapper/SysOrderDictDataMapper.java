package com.his.patient.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.his.patient.entity.SysOrderDictData;
import com.his.patient.vo.OrderDictUsageCountVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 医嘱基础字典（字典数据的窄口访问，只允许三种 dict_type，sql/142）。
 */
@Mapper
public interface SysOrderDictDataMapper extends BaseMapper<SysOrderDictData> {

    /**
     * 某个字典值被多少条医嘱引用（列表上的「使用量」列）。
     *
     * <p>{@code ${column}} 是动态列名，只能由 {@code OrderDictTypes.orderColumn()} 传入
     * （route / frequency / dosage_unit 三选一），不接任何外部输入；
     * 这里必须显式写 {@code del_flag = 0} —— 自定义 @Select 不受 @TableLogic 影响。
     *
     * <p>动态的只是「查哪一列」，返回结构是定长的（一个字典值 + 一个条数），
     * 所以用 {@link OrderDictUsageCountVO} 承接，不需要动态结构。
     */
    @Select("SELECT ${column} AS dictValue, COUNT(*) AS cnt FROM biz_inpatient_order "
            + "WHERE del_flag = 0 AND ${column} IS NOT NULL AND ${column} <> '' GROUP BY ${column}")
    List<OrderDictUsageCountVO> countOrderUsage(@Param("column") String column);
}

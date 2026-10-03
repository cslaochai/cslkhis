package com.his.patient.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.his.patient.entity.SysOrderDictData;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

/**
 * 医嘱基础字典（字典数据的窄口访问，只允许三种 dict_type，sql/142）。
 *
 * <p>跨模块写字典表用本地 Mapper 而不是 his-system 的 SysDictDataMapper：
 * 后者是通用字典的口子，让业务模块直接拿到它，权限边界就形同虚设
 * （谁能改医嘱途径，谁就能改「患者性别」「收费项目类别」）。
 * 这里只做单表读写，且每次调用前由服务层校验 dictType 在白名单内。
 */
@Mapper
public interface SysOrderDictDataMapper extends BaseMapper<SysOrderDictData> {

    /**
     * 某个字典值被多少条医嘱引用（列表上的「使用量」列）。
     *
     * <p>{@code ${column}} 是动态列名，只能由 {@code OrderDictTypes.orderColumn()} 传入
     * （route / frequency / dosage_unit 三选一），不接任何外部输入；
     * 这里必须显式写 {@code del_flag = 0} —— 自定义 @Select 不受 @TableLogic 影响。
     */
    @Select("SELECT ${column} AS v, COUNT(*) AS c FROM biz_inpatient_order "
            + "WHERE del_flag = 0 AND ${column} IS NOT NULL AND ${column} <> '' GROUP BY ${column}")
    List<Map<String, Object>> countOrderUsage(@Param("column") String column);
}

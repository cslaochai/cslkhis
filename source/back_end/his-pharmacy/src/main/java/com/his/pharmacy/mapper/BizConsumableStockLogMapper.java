package com.his.pharmacy.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.his.pharmacy.entity.BizConsumableStockLog;
import com.his.pharmacy.vo.BizConsumableStockLogVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 耗材出入库流水Mapper
 */
@Mapper
public interface BizConsumableStockLogMapper extends BaseMapper<BizConsumableStockLog> {

    /**
     * 流水分页（JOIN 字典带耗材名，名称模糊 + 变动类型过滤）
     */
    @Select("<script>" +
            "SELECT l.id, l.create_by, l.create_time, l.update_by, l.update_time, l.del_flag, l.remark, " +
            "       l.stock_id, l.consumable_id, c.consumable_name, l.batch_no, l.change_type, " +
            "       l.change_quantity, l.quantity_before, l.quantity_after, " +
            "       l.source_type, l.source_id, l.source_no, l.operator_name " +
            "FROM biz_consumable_stock_log l " +
            "LEFT JOIN sys_consumable c ON l.consumable_id = c.id " +
            "WHERE l.del_flag = 0 " +
            "<if test='keyword != null and keyword != \"\"'> AND c.consumable_name LIKE CONCAT('%', #{keyword}, '%') </if> " +
            "<if test='changeType != null'> AND l.change_type = #{changeType} </if> " +
            "ORDER BY l.create_time DESC, l.id DESC" +
            "</script>")
    Page<BizConsumableStockLogVO> selectLogPage(Page<BizConsumableStockLogVO> page,
                                                @Param("keyword") String keyword,
                                                @Param("changeType") Integer changeType);
}

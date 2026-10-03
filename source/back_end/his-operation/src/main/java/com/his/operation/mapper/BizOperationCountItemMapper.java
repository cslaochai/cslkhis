package com.his.operation.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.his.operation.entity.BizOperationCountItem;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 手术清点明细 Mapper。
 *
 * <p>行号序号编号由服务端按当前最大行号 +1 生成：
 * 清点单上的顺序是护士实际报数的顺序，打岔、回头改名都不改变它。
 */
@Mapper
public interface BizOperationCountItemMapper extends BaseMapper<BizOperationCountItem> {

    @Select("""
            SELECT * FROM biz_operation_count_item
            WHERE del_flag = 0 AND count_id = #{countId}
            ORDER BY seq_no ASC, id ASC
            """)
    List<BizOperationCountItem> selectByCount(@Param("countId") Long countId);

    @Select("SELECT COALESCE(MAX(seq_no), 0) FROM biz_operation_count_item WHERE count_id = #{countId}")
    int maxSeqNo(@Param("countId") Long countId);
}

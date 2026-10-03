package com.his.operation.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.his.operation.entity.BizDaySurgeryFollow;
import com.his.operation.vo.DaySurgeryFollowVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 日间手术随访台账 Mapper（追加式，只增不改）。
 */
@Mapper
public interface BizDaySurgeryFollowMapper extends BaseMapper<BizDaySurgeryFollow> {

    @Select("SELECT f.* FROM biz_day_surgery_follow f WHERE f.apply_id = #{applyId} AND f.del_flag = 0 " +
            " ORDER BY f.follow_time ASC, f.id ASC")
    List<DaySurgeryFollowVO> selectByApplyId(@Param("applyId") Long applyId);

    @Select("SELECT COUNT(*) FROM biz_day_surgery_follow WHERE apply_id = #{applyId} AND del_flag = 0")
    int countByApplyId(@Param("applyId") Long applyId);
}

package com.his.emr.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.his.emr.entity.BizDisputeFlow;
import com.his.emr.vo.DisputeFlowVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 纠纷/投诉处理跟踪台账 Mapper（追加式，只增不改）。
 */
@Mapper
public interface BizDisputeFlowMapper extends BaseMapper<BizDisputeFlow> {

    @Select("SELECT f.* FROM biz_dispute_flow f WHERE f.case_id = #{caseId} AND f.del_flag = 0 " +
            " ORDER BY f.operate_time ASC, f.id ASC")
    List<DisputeFlowVO> selectByCaseId(@Param("caseId") Long caseId);

    @Select("SELECT COUNT(*) FROM biz_dispute_flow WHERE case_id = #{caseId} AND del_flag = 0")
    int countByCaseId(@Param("caseId") Long caseId);
}

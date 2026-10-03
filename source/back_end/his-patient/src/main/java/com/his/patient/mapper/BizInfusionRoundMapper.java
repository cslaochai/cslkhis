package com.his.patient.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.his.patient.entity.BizInfusionRound;
import com.his.patient.vo.InfusionRoundVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 输液巡视记录 Mapper（巡视行只增不改，无 update 场景的裸 SQL）。
 */
@Mapper
public interface BizInfusionRoundMapper extends BaseMapper<BizInfusionRound> {

    /**
     * 按执行行取巡视记录（时间升序：巡视是时序观察，倒序读会诱导漏看前一次的异常）
     */
    @Select("""
            SELECT id, exec_id, round_time, drip_rate, remaining_volume, round_nurse_id, round_nurse_name, remark
              FROM biz_infusion_round
             WHERE del_flag = 0 AND exec_id = #{execId}
             ORDER BY round_time ASC, id ASC
            """)
    List<InfusionRoundVO> selectRoundsByExecId(@Param("execId") Long execId);

    @Select("SELECT COUNT(*) FROM biz_infusion_round WHERE del_flag = 0 AND exec_id = #{execId}")
    long countByExecId(@Param("execId") Long execId);
}

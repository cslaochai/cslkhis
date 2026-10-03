package com.his.emr.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.his.emr.entity.BizRecordQcFlow;
import com.his.emr.vo.RecordQcFlowVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 病历三级质控流转 Mapper
 */
@Mapper
public interface BizRecordQcFlowMapper extends BaseMapper<BizRecordQcFlow> {

    /**
     * 流转单分页
     *
     * ⚠ ORDER BY 必须补唯一二级键 id（同秒创建顺序不稳定 → 翻页重复+丢行，且不报错）
     */
    @Select("<script>" +
            "SELECT f.* FROM biz_record_qc_flow f " +
            "WHERE f.del_flag = 0 " +
            "<if test='flowNo != null and flowNo != \"\"'> AND f.flow_no LIKE CONCAT('%', #{flowNo}, '%') </if> " +
            "<if test='flowStatus != null'> AND f.flow_status = #{flowStatus} </if> " +
            "<if test='currentLevel != null'> AND f.current_level = #{currentLevel} </if> " +
            "<if test='recordSource != null and recordSource != \"\"'> AND f.record_source = #{recordSource} </if> " +
            "<if test='keyword != null and keyword != \"\"'> AND (f.flow_no LIKE CONCAT('%', #{keyword}, '%') " +
            "   OR f.patient_name LIKE CONCAT('%', #{keyword}, '%') OR f.dept_name LIKE CONCAT('%', #{keyword}, '%')) </if> " +
            "ORDER BY f.create_time DESC, f.id DESC" +
            "</script>")
    Page<RecordQcFlowVO> selectFlowPage(Page<RecordQcFlowVO> page,
                                        @Param("flowNo") String flowNo,
                                        @Param("flowStatus") Integer flowStatus,
                                        @Param("currentLevel") Integer currentLevel,
                                        @Param("recordSource") String recordSource,
                                        @Param("keyword") String keyword);

    /**
     * 详情（含状态/级别文案由 service 补）
     */
    @Select("SELECT f.* FROM biz_record_qc_flow f WHERE f.del_flag = 0 AND f.id = #{id}")
    RecordQcFlowVO selectFlowById(@Param("id") Long id);

    /**
     * 同一病历是否已有在途流转（未终审通过即在途）——防重复发起
     */
    @Select("SELECT COUNT(*) FROM biz_record_qc_flow WHERE del_flag = 0 AND record_id = #{recordId} AND flow_status != 4")
    int countActiveByRecordId(@Param("recordId") Long recordId);

    /**
     * 按主键取流转单并加行锁（流转动作的并发闸门）
     */
    @Select("SELECT * FROM biz_record_qc_flow WHERE id = #{id} AND del_flag = 0 FOR UPDATE")
    BizRecordQcFlow selectByIdForUpdate(@Param("id") Long id);
}

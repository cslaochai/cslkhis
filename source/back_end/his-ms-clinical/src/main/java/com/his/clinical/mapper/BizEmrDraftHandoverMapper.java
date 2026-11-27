package com.his.clinical.mapper;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.his.clinical.entity.BizEmrDraftHandover;
import com.his.clinical.vo.EmrHandoverVO;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 病历草稿移交代补 Mapper
 */
@Mapper
public interface BizEmrDraftHandoverMapper extends BaseMapper<BizEmrDraftHandover> {

    /**
     * 移交台账分页（管理侧）。
     * <p>ORDER BY 补唯一二级键 id：同秒创建顺序不稳定 → 翻页重复+丢行。
     */
    @Select("<script>" +
            "SELECT h.*, m.visit_date AS visitDate, m.regist_id AS registId FROM biz_emr_draft_handover h " +
            "LEFT JOIN biz_medical_record m ON m.id = h.record_id " +
            "WHERE h.del_flag = 0 " +
            "<if test='status != null'> AND h.status = #{status} </if> " +
            "<if test='receiverId != null'> AND h.receiver_id = #{receiverId} </if> " +
            "<if test='keyword != null and keyword != \"\"'> " +
            "  AND (h.handover_no LIKE CONCAT('%', #{keyword}, '%') " +
            "   OR h.record_no LIKE CONCAT('%', #{keyword}, '%') " +
            "   OR h.patient_name LIKE CONCAT('%', #{keyword}, '%')) </if> " +
            "ORDER BY h.create_time DESC, h.id DESC" +
            "</script>")
    Page<EmrHandoverVO> selectHandoverPage(Page<EmrHandoverVO> page,
                                           @Param("status") Integer status,
                                           @Param("receiverId") Long receiverId,
                                           @Param("keyword") String keyword);

    /**
     * 承接人待代补列表（关联病历取就诊日期/挂号单，供续写跳转）。
     */
    @Select("SELECT h.*, m.visit_date AS visitDate, m.regist_id AS registId FROM biz_emr_draft_handover h " +
            "LEFT JOIN biz_medical_record m ON m.id = h.record_id " +
            "WHERE h.del_flag = 0 AND h.receiver_id = #{receiverId} AND h.status = #{status} " +
            "ORDER BY h.create_time DESC, h.id DESC")
    List<EmrHandoverVO> selectMyReceiving(@Param("receiverId") Long receiverId, @Param("status") int status);

    /**
     * 按主键取移交记录（含就诊日期）
     */
    @Select("SELECT h.*, m.visit_date AS visitDate, m.regist_id AS registId FROM biz_emr_draft_handover h " +
            "LEFT JOIN biz_medical_record m ON m.id = h.record_id " +
            "WHERE h.id = #{id} AND h.del_flag = 0")
    EmrHandoverVO selectHandoverById(@Param("id") Long id);
}

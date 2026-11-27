package com.his.clinical.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.his.clinical.dto.EmrCorrectionPageQueryDTO;
import com.his.clinical.entity.BizEmrCorrectionRequest;
import com.his.clinical.vo.EmrCorrectionVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 病历更正申请 Mapper
 */
@Mapper
public interface BizEmrCorrectionRequestMapper extends BaseMapper<BizEmrCorrectionRequest> {

    /**
     * 更正申请台账分页（管理侧）。deptIds 为科室数据权限收口集合（null = 全院）。
     */
    @Select("<script>" + """
            SELECT c.*, m.visit_date AS visitDate, m.regist_id AS registId
            FROM biz_emr_correction_request c
            LEFT JOIN biz_medical_record m ON m.id = c.record_id
            WHERE c.del_flag = 0
              <if test="q.status != null"> AND c.status = #{q.status} </if>
              <if test="q.correctionType != null"> AND c.correction_type = #{q.correctionType} </if>
              <if test="q.deptId != null"> AND c.dept_id = #{q.deptId} </if>
              <if test="q.keyword != null and q.keyword != ''">
                AND (c.request_no LIKE CONCAT('%', #{q.keyword}, '%')
                     OR c.record_no LIKE CONCAT('%', #{q.keyword}, '%')
                     OR c.patient_name LIKE CONCAT('%', #{q.keyword}, '%'))
              </if>
              <if test="deptIds != null">
                AND c.dept_id IN
                <foreach collection="deptIds" item="d" open="(" separator="," close=")">#{d}</foreach>
              </if>
            ORDER BY c.create_time DESC, c.id DESC
            </script>
            """)
    IPage<EmrCorrectionVO> selectCorrectionPage(IPage<EmrCorrectionVO> page,
                                                @Param("q") EmrCorrectionPageQueryDTO query,
                                                @Param("deptIds") List<Long> deptIds);

    /**
     * 按主键取更正申请（含就诊日期/挂号单）
     */
    @Select("""
            SELECT c.*, m.visit_date AS visitDate, m.regist_id AS registId
            FROM biz_emr_correction_request c
            LEFT JOIN biz_medical_record m ON m.id = c.record_id
            WHERE c.id = #{id} AND c.del_flag = 0
            """)
    EmrCorrectionVO selectCorrectionById(@Param("id") Long id);

    /**
     * 病历在途更正申请（待科室审核 / 待医务审批 / 已批准待修改），同一病历只允许一条在途。
     */
    @Select("""
            SELECT * FROM biz_emr_correction_request
            WHERE record_id = #{recordId} AND del_flag = 0 AND status IN (1, 2, 3)
            ORDER BY id DESC LIMIT 1
            """)
    BizEmrCorrectionRequest selectActiveByRecordId(@Param("recordId") Long recordId);

    /**
     * 病历已批准待修改的更正申请（病历回写授权闸用）。
     */
    @Select("""
            SELECT * FROM biz_emr_correction_request
            WHERE record_id = #{recordId} AND del_flag = 0 AND status = 3
            ORDER BY id DESC LIMIT 1
            """)
    BizEmrCorrectionRequest selectApprovedByRecordId(@Param("recordId") Long recordId);
}

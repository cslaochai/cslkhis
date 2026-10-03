package com.his.emr.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.his.emr.entity.BizMedicalRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 病历Mapper
 */
@Mapper
public interface BizMedicalRecordMapper extends BaseMapper<BizMedicalRecord> {

    /**
     * 查询病历列表（LEFT JOIN处方和检查申请统计）
     */
    @Select("<script>" +
            "SELECT mr.*, " +
            "(SELECT COUNT(*) FROM biz_prescription p WHERE p.record_id = mr.id AND p.del_flag = 0) AS prescription_count, " +
            "(SELECT COUNT(*) FROM biz_inspection_apply ia WHERE ia.record_id = mr.id AND ia.del_flag = 0) AS inspection_count " +
            "FROM biz_medical_record mr " +
            "WHERE mr.del_flag = 0 " +
            "<if test='doctorId != null'> AND mr.doctor_id = #{doctorId} </if> " +
            "<if test='visitDate != null'> AND mr.visit_date = #{visitDate} </if> " +
            "<if test='recordStatus != null'> AND mr.record_status = #{recordStatus} </if> " +
            "ORDER BY mr.create_time DESC" +
            "</script>")
    List<BizMedicalRecord> selectRecordListWithStats(@Param("doctorId") Long doctorId,
                                                     @Param("visitDate") String visitDate,
                                                     @Param("recordStatus") Integer recordStatus);
}

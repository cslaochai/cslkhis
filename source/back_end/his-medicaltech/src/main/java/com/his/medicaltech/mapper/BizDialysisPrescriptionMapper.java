package com.his.medicaltech.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.his.medicaltech.entity.BizDialysisPrescription;
import com.his.medicaltech.vo.DialysisVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 透析处方 Mapper。
 */
@Mapper
public interface BizDialysisPrescriptionMapper extends BaseMapper<BizDialysisPrescription> {

    /**
     * 某档案的处方台账（新→旧，排班下拉只取 status=1）
     */
    @Select("""
            SELECT * FROM biz_dialysis_prescription
             WHERE del_flag = 0 AND archive_id = #{archiveId}
             ORDER BY status ASC, start_date DESC, id DESC
            """)
    List<DialysisVO.PrescriptionVO> selectByArchive(@Param("archiveId") Long archiveId);

    @Select("SELECT COUNT(*) FROM biz_dialysis_prescription WHERE del_flag = 0 AND archive_id = #{archiveId} AND status = 1")
    int countActive(@Param("archiveId") Long archiveId);
}

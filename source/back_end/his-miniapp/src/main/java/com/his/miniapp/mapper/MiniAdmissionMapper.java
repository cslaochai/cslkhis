package com.his.miniapp.mapper;

import com.his.miniapp.vo.MiniAdmiSelectListVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 住院登记跨模块只读 Mapper（裸 SQL，铁律：跨模块读异模块表用裸 SQL Mapper，
 */
@Mapper
public interface MiniAdmissionMapper {

    /**
     * 患者的住院记录（最近 10 条，含已出院——押金流水要能回看历史住院）。
     * admission_id/patient_id 出参字符串化由 VO 上的 ToStringSerializer 负责
     * （BIGINT 直接序列化成数字会在 JS 端丢精度，9007199254740993 以上）。
     */
    @Select("""
            SELECT admission_id, admission_no,
                   patient_id, dept_id, bed_id,
                   admit_time, discharge_time, admit_status, diagnosis
            FROM biz_admission
            WHERE patient_id = #{patientId} AND del_flag = 0
            ORDER BY admit_time DESC
            LIMIT 10
            """)
    List<MiniAdmiSelectListVO> selectByPatientId(Long patientId);
}

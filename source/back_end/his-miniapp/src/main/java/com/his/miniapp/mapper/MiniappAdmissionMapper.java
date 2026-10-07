package com.his.miniapp.mapper;

import com.his.miniapp.vo.AdmissionRowVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 住院登记跨模块只读 Mapper（裸 SQL，铁律：跨模块读异模块表用裸 SQL Mapper，
 * 不直接注入 his-patient 的 BizAdmissionMapper）。
 *
 * <p>患者端押金页只读少量展示列；写入（预交金充值/退款）全部走
 * his-charge 的 {@code InpatientAccountService}，不在这里碰。
 */
@Mapper
public interface MiniappAdmissionMapper {

    /**
     * 患者的住院记录（最近 10 条，含已出院——押金流水要能回看历史住院）。
     * admission_id/patient_id CAST 成字符串：Mapper 行不经 ToStringSerializer，直接序列化
     * BIGINT 会在 JS 端丢精度（9007199254740993 以上）。
     */
    @Select("""
            SELECT CAST(admission_id AS CHAR) AS admission_id, admission_no,
                   CAST(patient_id AS CHAR) AS patient_id, dept_id, bed_id,
                   admit_time, discharge_time, admit_status, diagnosis
            FROM biz_admission
            WHERE patient_id = #{patientId} AND del_flag = 0
            ORDER BY admit_time DESC
            LIMIT 10
            """)
    List<AdmissionRowVO> selectByPatientId(Long patientId);
}

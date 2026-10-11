package com.his.system.api.service;

import com.his.system.api.vo.RecordBriefVO;

import java.util.List;

/**
 * 挂号侧引用"病历"能力的 SPI（批次E/E6 复诊关联原病历）。
 */
public interface MedicalRecordApi {

    /**
     * 按病历ID取简要信息
     *
     * @param recordId 病历ID
     * @return 简要信息；不存在返回 null
     */
    RecordBriefVO getRecord(Long recordId);

    /**
     * 按患者取最近的就诊病历，供「原病历」下拉选择（窗口挂号/医生站/小程序共用）。
     *
     * <p>只要真正发生过的一次就诊（{@code registId} 非空）——草稿病历没有对应的号，
     * 选它当基准会让收费策略拿不到就诊日而判错。按就诊日倒序，同一天取最新一份。
     *
     * @param patientId 患者ID
     * @param limit     最多返回条数（&lt;=0 时由实现侧取默认值）
     * @return 候选列表；无病历返回空集合
     */
    List<RecordBriefVO> listRecentByPatient(Long patientId, int limit);
}

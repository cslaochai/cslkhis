package com.his.emr.service;

import com.his.common.base.PageResult;
import com.his.emr.dto.ChronicCancelDTO;
import com.his.emr.dto.ChronicQueryPageDTO;
import com.his.emr.dto.ChronicUpsertDTO;
import com.his.emr.vo.ChronicMyRecordsVO;
import com.his.emr.vo.ChronicRecordListVO;

import java.util.List;

/**
 * 慢病建档/认定（医生工作站）。
 */
public interface ChronicRecordService {

    /**
     * 慢病建档（建档即认定）
     */
    ChronicRecordListVO upsert(ChronicUpsertDTO dto);

    /**
     * 慢病档案作废（单向：1→2）
     */
    void cancel(ChronicCancelDTO dto);

    /**
     * 慢病档案分页（医生站）
     */
    PageResult<ChronicRecordListVO> listPage(ChronicQueryPageDTO dto);

    /**
     * 患者的有效慢病档案（长处方资格判定用）
     */
    List<ChronicRecordListVO> activeList(Long patientId);

    /**
     * 按患者查慢病档案列表（不按认定状态过滤，作废/待认定都在内，按主键倒序）。
     */
    List<ChronicRecordListVO> listByPatient(Long patientId);

    /**
     * 患者端「我的慢病档案」：patientId 一律取自登录态，不接收前端参数 ——
     * 患者端只能看自己的建档，一旦接收参数就等于任何登录患者可换 ID 读别人的慢病。
     */
    ChronicMyRecordsVO myRecords();
}

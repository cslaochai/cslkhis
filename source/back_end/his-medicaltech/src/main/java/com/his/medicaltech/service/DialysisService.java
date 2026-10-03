package com.his.medicaltech.service;

import com.his.common.base.PageResult;
import com.his.medicaltech.dto.DialysisDTO;
import com.his.medicaltech.vo.DialysisVO;
import java.time.LocalDate;
import java.util.List;

public interface DialysisService {

    PageResult<DialysisVO.ArchiveVO> archiveListPage(DialysisDTO.ArchiveQuery query);

    DialysisVO.ArchiveVO archiveGetById(Long id);

    DialysisVO.ArchiveVO archiveUpsert(DialysisDTO.ArchiveUpsert dto);

    DialysisVO.ArchiveVO archiveChangeStatus(DialysisDTO.ArchiveStatus dto);

    List<DialysisVO.PrescriptionVO> prescriptionList(Long archiveId);

    DialysisVO.PrescriptionVO prescriptionUpsert(DialysisDTO.PrescriptionUpsert dto);

    DialysisVO.PrescriptionVO prescriptionStop(DialysisDTO.PrescriptionStop dto);

    PageResult<DialysisVO.MachineVO> machineListPage(DialysisDTO.MachineQuery query);

    List<DialysisVO.MachineVO> machineSelectList();

    DialysisVO.MachineVO machineUpsert(DialysisDTO.MachineUpsert dto);

    PageResult<DialysisVO.SessionVO> sessionListPage(DialysisDTO.SessionQuery query);

    DialysisVO.SessionVO sessionGetById(Long id);

    DialysisVO.BoardVO board(LocalDate date);

    DialysisVO.SessionVO schedule(DialysisDTO.Schedule dto);

    DialysisVO.SessionVO reschedule(DialysisDTO.Reschedule dto);

    DialysisVO.SessionVO startSession(DialysisDTO.SessionStart dto);

    DialysisVO.SessionVO finishSession(DialysisDTO.SessionFinish dto);

    DialysisVO.SessionVO recordAdverse(DialysisDTO.AdverseUpsert dto);

    DialysisVO.SessionVO cancelSession(DialysisDTO.SessionCancel dto);

    DialysisVO.StatsVO stats(DialysisDTO.StatsQuery dto);
}

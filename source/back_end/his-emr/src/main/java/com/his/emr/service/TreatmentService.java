package com.his.emr.service;

import com.his.common.base.PageResult;
import com.his.emr.dto.TreatmentDTO;
import com.his.emr.vo.TreatmentVO;
import java.util.List;

public interface TreatmentService {

    PageResult<TreatmentVO.ApplyVO> listPageApplies(TreatmentDTO.ApplyQuery q);

    TreatmentVO.ApplyDetailVO getDetail(Long applyId);

    PageResult<TreatmentVO.ExecVO> listPageExecs(TreatmentDTO.ExecQuery q);

    List<TreatmentVO.StatusCountVO> statusCount(TreatmentDTO.ExecQuery q);

    TreatmentVO.StatsVO stats();

    List<TreatmentVO.ItemSelectListVO> itemSelectList(String keyword, Integer limit);

    TreatmentVO.ApplyDetailVO upsertApply(TreatmentDTO.ApplyUpsert dto);

    TreatmentVO.ExecVO rescheduleExec(TreatmentDTO.ExecReschedule dto);

    TreatmentVO.ExecVO executeExec(TreatmentDTO.ExecExecute dto);

    TreatmentVO.ExecVO retryCharge(Long recordId);

    int cancelApply(TreatmentDTO.ApplyCancel dto);

    void deleteApply(Long applyId);
}

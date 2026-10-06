package com.his.medicaltech.service;

import com.his.common.base.PageResult;
import com.his.medicaltech.dto.ExamApptDTO;
import com.his.medicaltech.vo.ExamApptVO;

import java.util.List;

public interface ExamAppointmentService {

    PageResult<ExamApptVO.ApplyVO> pendingListPage(ExamApptDTO.ApplyQuery q);

    PageResult<ExamApptVO.ApptVO> listPage(ExamApptDTO.ApptQuery q);

    List<ExamApptVO.StatusCountVO> statusCount(ExamApptDTO.ApptQuery q);

    ExamApptVO.ApptDetailVO getDetail(Long apptId);

    ExamApptVO.StatsVO stats();

    ExamApptVO.ApptDetailVO book(ExamApptDTO.Book dto);

    ExamApptVO.ApptDetailVO reschedule(ExamApptDTO.Reschedule dto);

    void cancel(ExamApptDTO.Cancel dto);

    void arrive(ExamApptDTO.ApptIdOnly dto);

    void finish(ExamApptDTO.ApptIdOnly dto);

    int autoNoShow();

    ExamApptVO.RecommendVO recommend(ExamApptDTO.Recommend dto);
}

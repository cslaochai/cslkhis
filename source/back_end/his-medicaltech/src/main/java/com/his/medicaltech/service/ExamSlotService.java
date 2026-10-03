package com.his.medicaltech.service;

import com.his.medicaltech.dto.ExamApptDTO;
import com.his.medicaltech.entity.BizExamDevice;
import com.his.medicaltech.entity.BizExamSlot;
import com.his.medicaltech.vo.ExamApptVO;
import java.time.LocalDate;
import java.util.List;

public interface ExamSlotService {

    ExamApptVO.SlotEnsureVO ensureSlots(ExamApptDTO.SlotEnsure dto);

    ExamApptVO.SlotBoardVO board(ExamApptDTO.SlotQuery dto);

    void toggle(ExamApptDTO.SlotToggle dto);

    ExamApptVO.SlotRecalcVO recalc(ExamApptDTO.SlotRecalc dto);

    List<BizExamSlot> ensureLockedDay(BizExamDevice device, LocalDate date);

    void claim(BizExamDevice device, LocalDate date, List<BizExamSlot> cells, int[] span);

    void release(BizExamDevice device, LocalDate date, List<BizExamSlot> cells, int[] span);

    int[] spanOf(List<BizExamSlot> cells, int startMin, int endMin);

    BizExamDevice requireDevice(Long deviceId);
}

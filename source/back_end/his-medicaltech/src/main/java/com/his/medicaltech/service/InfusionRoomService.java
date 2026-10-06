package com.his.medicaltech.service;

import com.his.common.base.PageResult;
import com.his.medicaltech.dto.InfusionRoomDTO;
import com.his.medicaltech.vo.InfusionRoomVO;

import java.util.List;

public interface InfusionRoomService {

    List<InfusionRoomVO.Seat> seats();

    InfusionRoomVO.Seat seatUpsert(InfusionRoomDTO.SeatUpsert dto);

    InfusionRoomVO.Infusion admit(InfusionRoomDTO.Admit dto);

    InfusionRoomVO.Infusion skinTest(InfusionRoomDTO.SkinTestCreate dto);

    InfusionRoomVO.Infusion skinTestResult(InfusionRoomDTO.SkinTestResult dto);

    InfusionRoomVO.Infusion start(InfusionRoomDTO.Start dto);

    InfusionRoomVO.Round round(InfusionRoomDTO.Round dto);

    InfusionRoomVO.Infusion finish(InfusionRoomDTO.Finish dto);

    InfusionRoomVO.Infusion cancel(InfusionRoomDTO.Cancel dto);

    PageResult<InfusionRoomVO.Infusion> listPage(InfusionRoomDTO.InfusionQuery query);

    List<InfusionRoomVO.Round> rounds(Long infusionId);

    InfusionRoomVO.Board board();
}

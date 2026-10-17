package com.his.charge.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.his.charge.dto.ArrearsBoardQueryDTO;
import com.his.charge.dto.ArrearsPolicyUpsertDTO;
import com.his.charge.vo.ArrearsPatientVO;
import com.his.charge.vo.ArrearsPolicyVO;


public interface ArrearsControlService {

    ArrearsPolicyVO getPolicy();

    ArrearsPolicyVO upsertPolicy(ArrearsPolicyUpsertDTO dto);

    IPage<ArrearsPatientVO> arrearsBoard(ArrearsBoardQueryDTO query);
}

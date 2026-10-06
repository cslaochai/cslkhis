package com.his.charge.service;

import com.his.charge.dto.ArrearsPolicyUpsertDTO;
import com.his.charge.vo.ArrearsPatientVO;
import com.his.charge.vo.ArrearsPolicyVO;
import com.baomidou.mybatisplus.core.metadata.IPage;




public interface ArrearsControlService {

    ArrearsPolicyVO getPolicy();

    ArrearsPolicyVO upsertPolicy(ArrearsPolicyUpsertDTO dto);

    IPage<ArrearsPatientVO> arrearsBoard(String keyword, Integer pageNum, Integer pageSize);
}

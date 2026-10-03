package com.his.charge.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.his.charge.vo.ArrearsPatientVO;
import com.his.charge.vo.ArrearsPolicyVO;

public interface ArrearsControlService {

    ArrearsPolicyVO getPolicy();

    ArrearsPolicyVO upsertPolicy(ArrearsPolicyVO dto);

    IPage<ArrearsPatientVO> arrearsBoard(String keyword, Integer pageNum, Integer pageSize);
}

package com.his.patient.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.his.patient.dto.DischargeDrugDTO;
import com.his.patient.vo.DischargeDrugSelectListVO;
import com.his.patient.vo.DischargeDrugVO;

import java.util.List;

public interface DischargeDrugService {

    DischargeDrugVO upsert(DischargeDrugDTO.Upsert dto);

    IPage<DischargeDrugVO> listPage(DischargeDrugDTO.QueryPage q);

    List<DischargeDrugSelectListVO> listByAdmission(Long admissionId);

    DischargeDrugVO getDetailById(Long id);

    List<DischargeDrugVO> dispense(DischargeDrugDTO.Dispense dto);

    void deleteById(Long id);
}

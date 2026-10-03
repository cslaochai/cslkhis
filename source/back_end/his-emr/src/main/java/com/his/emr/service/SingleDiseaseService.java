package com.his.emr.service;

import com.his.common.base.PageResult;
import com.his.emr.dto.SingleDiseaseDTO;
import com.his.emr.vo.SingleDiseaseAutoEnrollStatVO;
import com.his.emr.vo.SingleDiseaseVO;
import java.util.List;

public interface SingleDiseaseService {

    List<SingleDiseaseVO.Disease> diseaseList();

    SingleDiseaseVO.Disease diseaseUpsert(SingleDiseaseDTO.DiseaseUpsert dto);

    void diseaseDelete(Long id);

    SingleDiseaseVO.Case enroll(SingleDiseaseDTO.Enroll dto);

    SingleDiseaseAutoEnrollStatVO autoEnroll(SingleDiseaseDTO.AutoEnroll dto);

    SingleDiseaseVO.Case qc(SingleDiseaseDTO.Qc dto);

    SingleDiseaseVO.Case report(Long id);

    PageResult<SingleDiseaseVO.Case> casePage(SingleDiseaseDTO.CaseQuery query);

    List<SingleDiseaseVO.Metric> metrics();
}

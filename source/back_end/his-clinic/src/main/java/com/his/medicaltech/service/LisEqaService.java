package com.his.medicaltech.service;

import com.his.common.base.PageResult;
import com.his.medicaltech.dto.LisEqaDTO;
import com.his.medicaltech.vo.LisEqaVO;

public interface LisEqaService {

    PageResult<LisEqaVO.PlanVO> planPage(LisEqaDTO.PlanQuery q);

    String planUpsert(LisEqaDTO.PlanUpsert dto);

    void archive(LisEqaDTO.PlanArchive dto);

    PageResult<LisEqaVO.SampleVO> samplePage(LisEqaDTO.SampleQuery q);

    String sampleUpsert(LisEqaDTO.SampleUpsert dto);

    int sampleGenerate(LisEqaDTO.SampleGenerate dto);

    void sampleDeleteById(Long id);

    LisEqaVO.SampleVO test(LisEqaDTO.TestInput dto);

    String report(LisEqaDTO.ReportInput dto);

    LisEqaVO.JudgeVO returnScore(LisEqaDTO.ScoreReturn dto);

    void rebuildCompare(Long planId);

    PageResult<LisEqaVO.CompareVO> comparePage(LisEqaDTO.CompareQuery q);

    void rectify(LisEqaDTO.Rectify dto);

    void rectifyReview(LisEqaDTO.RectifyReview dto);

    LisEqaVO.StatsVO stats();
}

package com.his.medicaltech.service;


import com.his.common.base.PageResult;
import com.his.medicaltech.dto.UltrasoundDTO;
import com.his.medicaltech.entity.BizUltrasoundRecord;
import com.his.medicaltech.vo.UltrasoundVO;

import java.util.List;

public interface UltrasoundService extends com.baomidou.mybatisplus.extension.service.IService<BizUltrasoundRecord> {

    PageResult<UltrasoundVO.ListVO> pageVO(UltrasoundDTO.Query q);

    UltrasoundVO.StatsVO stats();

    UltrasoundVO.DetailVO getDetail(Long recordId);

    List<UltrasoundVO.MeasureVO> listMeasures(Long recordId);

    UltrasoundVO.DetailVO upsertRecord(UltrasoundDTO.RecordUpsert dto);

    void checkIn(Long recordId);

    void execute(UltrasoundDTO.Execute dto);

    int saveMeasures(UltrasoundDTO.MeasureSave dto);

    void report(UltrasoundDTO.Report dto);

    void audit(UltrasoundDTO.Audit dto);

    void publish(Long recordId);

    void cancel(UltrasoundDTO.Cancel dto);
}

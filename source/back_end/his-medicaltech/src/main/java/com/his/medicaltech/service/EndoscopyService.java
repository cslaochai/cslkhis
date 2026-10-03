package com.his.medicaltech.service;


import com.his.medicaltech.entity.BizEndoscopyRecord;
import com.his.common.base.PageResult;
import com.his.medicaltech.dto.EndoscopyDTO;
import com.his.medicaltech.vo.EndoscopyVO;

public interface EndoscopyService extends com.baomidou.mybatisplus.extension.service.IService<BizEndoscopyRecord> {

    PageResult<EndoscopyVO.ListVO> pageVO(EndoscopyDTO.Query q);

    EndoscopyVO.StatsVO stats();

    EndoscopyVO.DetailVO getDetail(Long recordId);

    EndoscopyVO.DetailVO upsertRecord(EndoscopyDTO.RecordUpsert dto);

    void checkIn(Long recordId);

    void execute(EndoscopyDTO.Execute dto);

    String sendBiopsy(EndoscopyDTO.BiopsySend dto);

    void report(EndoscopyDTO.Report dto);

    void audit(EndoscopyDTO.Audit dto);

    void publish(Long recordId);

    void cancel(EndoscopyDTO.Cancel dto);
}

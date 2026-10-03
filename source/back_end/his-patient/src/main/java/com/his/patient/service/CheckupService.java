package com.his.patient.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.his.patient.dto.CheckupDTO;
import com.his.patient.vo.CheckupVO;

public interface CheckupService {

    CheckupVO.PackageVO savePackage(CheckupDTO.PackageSave dto);

    IPage<CheckupVO.PackageVO> packagePage(CheckupDTO.PackageQuery dto);

    CheckupVO.PackageVO getPackage(Long id);

    void disablePackage(Long id);

    CheckupVO.RecordVO createRecord(CheckupDTO.RecordCreate dto);

    IPage<CheckupVO.RecordVO> recordPage(CheckupDTO.RecordQuery dto);

    CheckupVO.RecordVO getRecord(Long id);

    CheckupVO.ResultVO saveResult(CheckupDTO.ResultSave dto);

    CheckupVO.RecordVO conclude(CheckupDTO.Conclusion dto);

    void startCheckup(Long recordId);

    void deleteRecord(Long recordId);

    CheckupVO.RecordVO refreshFinishStatus(Long recordId);
}

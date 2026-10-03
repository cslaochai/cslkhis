package com.his.emr.service;

import com.his.emr.dto.BizDrugPackageUpsertDTO;
import com.his.emr.dto.BizRxTemplateUpsertDTO;
import com.his.emr.dto.DiagTemplateUpsertDTO;
import com.his.emr.vo.BizDiagTemplateVO;
import com.his.emr.vo.BizDrugPackageVO;
import com.his.emr.vo.BizRxTemplateVO;

import java.util.List;

/**
 * 医生个人模板服务接口
 */
public interface OtherTemplateService {

    // 常用诊断
    List<BizDiagTemplateVO> listDiagTemplates();

    boolean saveDiagTemplates(DiagTemplateUpsertDTO saveDTO);

    boolean deleteDiagTemplate(Long id);

    // 处方模板
    List<BizRxTemplateVO> listRxTemplates();

    BizRxTemplateVO getRxTemplateDetail(Long templateId);

    boolean saveRxTemplate(BizRxTemplateUpsertDTO upsertDTO);

    boolean deleteRxTemplate(Long id);

    // 药品套餐
    List<BizDrugPackageVO> listDrugPackages();

    BizDrugPackageVO getDrugPackageDetail(Long packageId);

    boolean saveDrugPackage(BizDrugPackageUpsertDTO upsertDTO);

    boolean deleteDrugPackage(Long id);
}

package com.his.system.service;

import com.his.common.base.PageResult;
import com.his.system.dto.SysInspectionItemQueryPageDTO;
import com.his.system.dto.SysInspectionItemUpsertDTO;
import com.his.system.dto.SysLaboratoryItemDetailUpsertDTO;
import com.his.system.dto.SysLaboratoryItemQueryPageDTO;
import com.his.system.dto.SysLaboratoryItemUpsertDTO;
import com.his.system.vo.SysInspectionItemSelectListVO;
import com.his.system.vo.SysInspectionItemVO;
import com.his.system.vo.SysLaboratoryItemDetailVO;
import com.his.system.vo.SysLaboratoryItemSelectListVO;
import com.his.system.vo.SysLaboratoryItemVO;

import java.util.List;

/**
 * 检查检验项目目录服务接口
 */
public interface MedicalItemService {

    PageResult<SysInspectionItemVO> inspectionListPage(SysInspectionItemQueryPageDTO queryDTO);

    List<SysInspectionItemSelectListVO> inspectionSelectList(String keyword, Integer limit);

    SysInspectionItemVO getInspectionItem(Long id);

    void inspectionUpsert(SysInspectionItemUpsertDTO upsertDTO);

    void deleteInspectionItem(Long id);

    PageResult<SysLaboratoryItemVO> laboratoryListPage(SysLaboratoryItemQueryPageDTO queryDTO);

    List<SysLaboratoryItemSelectListVO> laboratorySelectList(String keyword, Integer limit);

    SysLaboratoryItemVO getLaboratoryItem(Long id);

    void laboratoryUpsert(SysLaboratoryItemUpsertDTO upsertDTO);

    void deleteLaboratoryItem(Long id);

    List<SysLaboratoryItemDetailVO> laboratoryDetailList(Long laboratoryItemId);

    void laboratoryDetailUpsert(SysLaboratoryItemDetailUpsertDTO upsertDTO);

    void deleteLaboratoryDetail(Long id);
}

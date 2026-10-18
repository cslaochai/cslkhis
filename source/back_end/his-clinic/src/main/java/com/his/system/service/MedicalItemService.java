package com.his.system.service;

import com.his.common.base.PageResult;
import com.his.system.dto.*;
import com.his.system.vo.*;

import java.util.List;

/**
 * 检查检验项目目录服务接口
 */
public interface MedicalItemService {

    PageResult<SysInspectionItemVO> inspectionListPage(SysInspectionItemPageQueryDTO queryDTO);

    List<SysInspectionItemSelectListVO> inspectionSelectList(String keyword, Integer limit);

    SysInspectionItemVO getInspectionItem(Long id);

    void inspectionUpsert(SysInspectionItemUpsertDTO upsertDTO);

    void deleteInspectionItem(Long id);

    PageResult<SysLaboratoryItemVO> laboratoryListPage(SysLaboratoryItemPageQueryDTO queryDTO);

    List<SysLaboratoryItemSelectListVO> laboratorySelectList(String keyword, Integer limit);

    SysLaboratoryItemVO getLaboratoryItem(Long id);

    void laboratoryUpsert(SysLaboratoryItemUpsertDTO upsertDTO);

    void deleteLaboratoryItem(Long id);

    List<SysLaboratoryItemDetailVO> laboratoryDetailList(Long laboratoryItemId);

    void laboratoryDetailUpsert(SysLaboratoryItemDetailUpsertDTO upsertDTO);

    void deleteLaboratoryDetail(Long id);
}

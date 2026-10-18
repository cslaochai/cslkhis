package com.his.system.service;

import com.his.common.base.PageResult;
import com.his.system.dto.*;
import com.his.system.vo.*;

import java.util.List;

/**
 * 检查检验项目目录服务接口
 */
public interface MedicalItemService {

    /**
     * 检查项目分页：按项目名/项目码等条件筛选字典维护中的检查项目。
     * 用于「检查项目」管理页的查询卡表格。
     */
    PageResult<SysInspectionItemVO> inspectionListPage(SysInspectionItemPageQueryDTO queryDTO);

    /**
     * 检查项目下拉：不传条件返回全部启用项目，传关键字则按项目名/项目码模糊并截断条数。
     * 用于门诊医生站与住院医嘱工作台开检查申请时选项目，也用于开单字典一次预载。
     */
    List<SysInspectionItemSelectListVO> inspectionSelectList(String keyword, Integer limit);

    /**
     * 单个检查项目详情：取维护页表单需要回填的全部字段。
     * 用于「检查项目」页点编辑后的弹框回显。
     */
    SysInspectionItemVO getInspectionItem(Long id);

    /**
     * 新增或修改检查项目：按主键有无分支，落库前收口项目码重复等字典规则。
     * 用于「检查项目」页新增与编辑弹框的保存。
     */
    void inspectionUpsert(SysInspectionItemUpsertDTO upsertDTO);

    /**
     * 删除检查项目：从字典中摘掉该项目，历史申请单不受影响。
     * 用于「检查项目」页表格行上的删除动作。
     */
    void deleteInspectionItem(Long id);

    /**
     * 检验项目分页：按项目名/项目码等条件筛选字典维护中的检验项目。
     * 用于「检验项目」管理页的查询卡表格。
     */
    PageResult<SysLaboratoryItemVO> laboratoryListPage(SysLaboratoryItemPageQueryDTO queryDTO);

    /**
     * 检验项目下拉：不传条件返回全部启用项目，传关键字则按项目名/项目码模糊并截断条数。
     * 用于门诊医生站与住院医嘱工作台开检验申请时选项目，也用于开单字典一次预载。
     */
    List<SysLaboratoryItemSelectListVO> laboratorySelectList(String keyword, Integer limit);

    /**
     * 单个检验项目详情：取维护页表单需要回填的全部字段。
     * 用于「检验项目」页点编辑后的弹框回显。
     */
    SysLaboratoryItemVO getLaboratoryItem(Long id);

    /**
     * 新增或修改检验项目：按主键有无分支，落库前收口项目码重复等字典规则。
     * 用于「检验项目」页新增与编辑弹框的保存。
     */
    void laboratoryUpsert(SysLaboratoryItemUpsertDTO upsertDTO);

    /**
     * 删除检验项目：从字典中摘掉该项目，历史申请与结果不受影响。
     * 用于「检验项目」页表格行上的删除动作。
     */
    void deleteLaboratoryItem(Long id);

    /**
     * 一个检验项目的明细项列表：即该项目下要出结果的小项目集合。
     * 用于「检验项目」页的明细配置弹框，以及检验结果录入时展开待填明细。
     */
    List<SysLaboratoryItemDetailVO> laboratoryDetailList(Long laboratoryItemId);

    /**
     * 新增或修改检验项目明细项：挂在指定检验项目下，按主键有无分支。
     * 用于检验项目明细配置弹框的保存。
     */
    void laboratoryDetailUpsert(SysLaboratoryItemDetailUpsertDTO upsertDTO);

    /**
     * 删除检验项目明细项：只摘字典行，已录入的历史结果不追删。
     * 用于检验项目明细配置表格行上的删除动作。
     */
    void deleteLaboratoryDetail(Long id);
}

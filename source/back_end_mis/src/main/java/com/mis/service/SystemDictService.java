package com.mis.service;

import com.mis.common.base.PageResult;
import com.mis.dto.DictDataUpsertDTO;
import com.mis.dto.DictTypeUpsertDTO;
import com.mis.entity.MssDictData;
import com.mis.entity.MssDictType;
import com.mis.vo.DictGroupVO;

import java.util.List;

/**
 * 字典管理
 */
public interface SystemDictService {

    PageResult<MssDictType> typePage(String keyword, long pageNum, long pageSize);

    List<MssDictType> typeSelectList();

    MssDictType typeGetById(Long id);

    MssDictType typeUpsert(DictTypeUpsertDTO dto);

    void typeDelete(Long id);

    PageResult<MssDictData> dataPage(String dictType, String keyword, long pageNum, long pageSize);

    MssDictData dataGetById(Long id);

    /** 单值/逗号分隔多类型 → 数据列表（HIS：data/selectList） */
    List<MssDictData> dataSelectList(String dictTypes);

    /** 单值/逗号分隔多类型 → 分组 [{dictType, dataList}]（HIS：data/selectGroup） */
    List<DictGroupVO> dataSelectGroup(String dictTypes);

    MssDictData dataUpsert(DictDataUpsertDTO dto);

    void dataDelete(Long id);

    List<MssDictData> dataList(String dictType);
}

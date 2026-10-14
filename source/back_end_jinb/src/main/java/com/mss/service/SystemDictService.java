package com.mss.service;

import com.mss.common.base.PageResult;
import com.mss.dto.DictDataUpsertDTO;
import com.mss.dto.DictTypeUpsertDTO;
import com.mss.entity.MssDictData;
import com.mss.entity.MssDictType;

import java.util.List;

/**
 * 字典管理
 */
public interface SystemDictService {

    PageResult<MssDictType> typePage(String keyword, long pageNum, long pageSize);

    MssDictType typeUpsert(DictTypeUpsertDTO dto);

    void typeDelete(Long id);

    PageResult<MssDictData> dataPage(String dictType, String keyword, long pageNum, long pageSize);

    MssDictData dataUpsert(DictDataUpsertDTO dto);

    void dataDelete(Long id);

    List<MssDictData> dataList(String dictType);
}

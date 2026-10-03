package com.his.system.service;

import com.his.system.dto.DictDataQueryDTO;
import com.his.system.dto.SysDictDataUpsertDTO;
import com.his.system.vo.DictTypeGroupVO;
import com.his.system.vo.SysDictDataVO;

import java.util.List;

public interface SysDictDataService {

    List<SysDictDataVO> selectList(DictDataQueryDTO queryDTO);

    List<DictTypeGroupVO> selectGroup(DictDataQueryDTO queryDTO);

    SysDictDataVO getInfo(Long dataId);

    void upsert(SysDictDataUpsertDTO upsertDTO);

    void delete(Long dataId);
}

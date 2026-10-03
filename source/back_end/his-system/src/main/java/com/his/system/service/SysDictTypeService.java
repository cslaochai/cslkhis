package com.his.system.service;

import com.his.system.dto.SysDictTypeUpsertDTO;
import com.his.system.vo.SysDictTypeVO;

import java.util.List;

public interface SysDictTypeService {

    List<SysDictTypeVO> selectList();

    SysDictTypeVO getInfo(Long typeId);

    void upsert(SysDictTypeUpsertDTO upsertDTO);

    void delete(Long typeId);
}

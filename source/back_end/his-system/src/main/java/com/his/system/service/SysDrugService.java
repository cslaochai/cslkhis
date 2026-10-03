package com.his.system.service;

import com.his.common.base.PageResult;
import com.his.system.dto.SysDrugQueryPageDTO;
import com.his.system.dto.SysDrugSelectDTO;
import com.his.system.dto.SysDrugUpsertDTO;
import com.his.system.vo.SysDrugSelectListVO;
import com.his.system.vo.SysDrugVO;

import java.util.List;

public interface SysDrugService {

    PageResult<SysDrugVO> listPage(SysDrugQueryPageDTO queryDTO);

    List<SysDrugSelectListVO> selectList(SysDrugSelectDTO queryDTO);

    SysDrugVO getInfo(Long drugId);

    void upsert(SysDrugUpsertDTO upsertDTO);

    void delete(Long drugId);
}

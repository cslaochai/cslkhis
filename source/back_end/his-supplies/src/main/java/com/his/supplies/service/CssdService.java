package com.his.supplies.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.his.supplies.dto.CssdDTO;
import com.his.supplies.vo.CssdPackVO;

public interface CssdService {

    CssdPackVO receive(CssdDTO.Receive dto);

    CssdPackVO advance(CssdDTO.Advance dto);

    IPage<CssdPackVO> listPage(CssdDTO.QueryPage q);

    CssdPackVO getDetailById(Long packId);
}

package com.his.system.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.his.system.dto.WasteDTO;
import com.his.system.vo.WasteVO;

public interface WasteService {

    WasteVO create(WasteDTO.Create dto);

    WasteVO handover(WasteDTO.Handover dto);

    WasteVO dispose(WasteDTO.Dispose dto);

    void deleteById(Long id);

    IPage<WasteVO> listPage(WasteDTO.QueryPage q);
}

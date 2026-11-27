package com.his.platform.spi;

import com.his.platform.service.SysPrintRecordService;
import com.his.spi.dto.PrintRecordSaveDTO;
import com.his.spi.service.PrintRecordFacadeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * 打印台账端口实现。
 * <p>叶子适配器：只依赖本模块的打印台账服务（该服务仅持有本表 Mapper，
 * 不牵编排服务），调用方不会因此被拖进整张服务图。</p>
 */
@Service
@RequiredArgsConstructor
public class PrintRecordPortServiceImpl implements PrintRecordFacadeService {

    private final SysPrintRecordService sysPrintRecordService;

    @Override
    public int countByBiz(Integer bizType, Long bizId) {
        return sysPrintRecordService.countByBiz(bizType, bizId);
    }

    @Override
    public int record(PrintRecordSaveDTO saveDTO) {
        return sysPrintRecordService.record(saveDTO);
    }
}

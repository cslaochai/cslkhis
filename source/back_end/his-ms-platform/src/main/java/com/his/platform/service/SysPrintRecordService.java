package com.his.platform.service;

import com.his.spi.dto.PrintRecordSaveDTO;

/**
 * 打印台账服务：标签打印留痕的读写。
 */
public interface SysPrintRecordService {

    /**
     * 该对象已打印次数。
     */
    int countByBiz(Integer bizType, Long bizId);

    /**
     * 记一次打印，返回本次打印序号（1 起，>=2 为补打）。
     */
    int record(PrintRecordSaveDTO saveDTO);
}

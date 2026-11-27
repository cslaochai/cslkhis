package com.his.spi.service;

import com.his.spi.dto.PrintRecordSaveDTO;

/**
 * 打印台账端口：标签打印留痕的唯一写入口。
 * <p>宿主在 his-ms-platform（表 sys_print_record），各业务模块只依赖本接口，
 * 不直接读写该表。</p>
 */
public interface PrintRecordFacadeService {

    /**
     * 该对象已打印次数（0 = 从未打印）。
     *
     * @param bizType PrintBizTypeEnum 码值
     * @param bizId   业务对象ID
     */
    int countByBiz(Integer bizType, Long bizId);

    /**
     * 记一次打印并返回本次的打印序号（第 1 次为 1，第 2 次起为补打）。
     * <p>补打（序号 >= 2）未填原因时抛 BusinessException；
     * 并发重复打印由唯一键兜住后转成同一类业务提示。</p>
     *
     * @return 本次打印序号
     */
    int record(PrintRecordSaveDTO saveDTO);
}

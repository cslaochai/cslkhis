package com.his.charge.service;

import com.his.common.base.PageResult;
import com.his.charge.dto.PriceChangeDTO;
import com.his.charge.dto.PriceHistoryPageQueryDTO;
import com.his.charge.dto.PricePageQueryDTO;
import com.his.charge.vo.PriceChangeHistoryVO;
import com.his.charge.vo.PriceItemVO;

/**
 * 价格管理服务
 */
public interface PriceService {

    /**
     * 按项目类型分页查询价格
     */
    PageResult<PriceItemVO> listPage(PricePageQueryDTO queryDTO);

    /**
     * 调价：更新对应价表 + 写调价留痕，返回调价后的项目
     */
    PriceItemVO changePrice(PriceChangeDTO changeDTO);

    /**
     * 分页查询调价历史
     */
    PageResult<PriceChangeHistoryVO> historyListPage(PriceHistoryPageQueryDTO queryDTO);

    /**
     * 5 张价表的总条数
     */
    Long countAll();
}

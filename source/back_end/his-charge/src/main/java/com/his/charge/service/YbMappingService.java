package com.his.charge.service;

import com.his.charge.dto.YbAutoMatchDTO;
import com.his.charge.dto.YbMapDTO;
import com.his.charge.dto.YbMappingQueryPageDTO;
import com.his.charge.vo.YbAutoMatchResultVO;
import com.his.charge.vo.YbMappingListVO;
import com.his.charge.vo.YbMappingStatsVO;
import com.his.common.base.PageResult;

import java.util.List;

/**
 * 医保目录对照服务（院内项目 ↔ 国家医保编码，一对一）。
 */
public interface YbMappingService {

    /**
     * 对照工作台分页（itemType 必填；未对照行 mapping 字段为 null）
     */
    PageResult<YbMappingListVO> listPage(YbMappingQueryPageDTO queryDTO);

    /**
     * 各类型对照率
     */
    List<YbMappingStatsVO> stats();

    /**
     * 人工对照（已存在旧对照 = 换对照覆盖；对照快照在服务端取真实值）
     */
    YbMappingListVO map(YbMapDTO dto);

    /**
     * 解对照（物理删，uk_item 不含 del_flag）
     */
    void unmap(Integer itemType, Long itemId);

    /**
     * 自动对照（名称精确匹配且唯一命中才落；itemType 空=全部类型）
     */
    YbAutoMatchResultVO autoMatch(YbAutoMatchDTO dto);
}

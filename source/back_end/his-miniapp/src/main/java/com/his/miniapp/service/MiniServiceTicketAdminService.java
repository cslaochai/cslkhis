package com.his.miniapp.service;

import com.his.common.base.PageResult;
import com.his.miniapp.dto.TicketHandleDTO;
import com.his.miniapp.dto.TicketSearchDTO;
import com.his.miniapp.vo.MiniServiceDetailVO;
import com.his.miniapp.vo.MiniServiceMessageListVO;
import com.his.miniapp.vo.MiniTicketStatsVO;

/**
 * 院内工单受理（客服工作台）。
 */
public interface MiniServiceTicketAdminService {

    /**
     * 工单列表（含全部状态）
     */
    PageResult<MiniServiceMessageListVO> adminPage(TicketSearchDTO dto);

    /**
     * 工作台统计
     */
    MiniTicketStatsVO stats();

    /**
     * 工单详情（含全量流转记录，含内部备注）
     */
    MiniServiceDetailVO detail(Long id);

    /**
     * 受理 / 回复 / 办结 / 关闭 / 内部备注
     */
    void handle(TicketHandleDTO dto);
}
